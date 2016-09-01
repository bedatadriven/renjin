package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.reflection.ClassBindingImpl;
import org.renjin.methods.S4;
import org.renjin.primitives.Native;
import org.renjin.primitives.S3;
import org.renjin.primitives.text.regex.ExtendedRE;
import org.renjin.primitives.text.regex.RESyntaxException;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Package namespace.
 *
 */
public class Namespace {

  private final Package pkg;

  private final Environment namespaceEnvironment;
  private final Environment importsEnvironment;
  private final Environment baseNamespaceEnvironment;

  private final List<Symbol> exports = Lists.newArrayList();
  private final Map<String, Class> nativeSymbolMap = new HashMap<>();

  public Namespace(Package pkg, Environment namespaceEnvironment) {
    this.pkg = pkg;
    this.namespaceEnvironment = namespaceEnvironment;
    this.importsEnvironment = namespaceEnvironment.getParent();
    this.baseNamespaceEnvironment = importsEnvironment.getParent();
  }

  public String getName() {
    return pkg.getName().getPackageName();
  }

  public FqPackageName getFullyQualifiedName() {
    return pkg.getName();
  }

  public String getCompatibleName() {
    if (pkg.getName().getGroupId().equals(FqPackageName.CORE_GROUP_ID)) {
      return pkg.getName().getPackageName();
    } else {
      return pkg.getName().toString(':');
    }
  }

  /**
   *
   * @return a collection of symbols exported by the package
   */
  public Collection<Symbol> getExports() {
    if(FqPackageName.BASE.equals(pkg.getName())) {
      // For historical reasons, all symbols are exported from the base package
      return this.namespaceEnvironment.getSymbolNames();

    } else {
      return Collections.unmodifiableCollection(exports);
    }
  }

  public Map<String, Class> getNativeSymbolMap() {
    return nativeSymbolMap;
  }

  public SEXP getEntry(Symbol entry) {
    SEXP value = namespaceEnvironment.getVariable(entry);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Namespace " + pkg.getName() + " has no symbol named '" + entry + "'");
    }
    return value;
  }

  public SEXP getExport(Symbol entry) {
    SEXP value = getExportIfExists(entry);
    if (value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Namespace " + pkg.getName() + " has no exported symbol named '" + entry.getPrintName() + "'");
    }
    return value;
  }
  
  public SEXP getExportIfExists(Symbol entry) {
    // the base package's namespace is treated specially for historical reasons:
    // all symbols are considered to be exported.
    if(FqPackageName.BASE.equals(pkg.getName())) {
      return namespaceEnvironment.getVariable(entry);

    }
    if(exports.contains(entry)) {
      return this.namespaceEnvironment.findVariable(entry);
    }
    return Symbol.UNBOUND_VALUE;
  }

  /**
   *
   * @return the imports environemnt 
   */
  public Environment getImportsEnvironment() {
    return importsEnvironment;
  }

  /**
   *
   * @return the private environment for this namespace
   */
  public Environment getNamespaceEnvironment() {
    return this.namespaceEnvironment;
  }

  /**
   * Copies the exported (public) symbols from our namespace environment
   * to the given package environment
   *
   */
  public void copyExportsTo(Context context, Environment packageEnv) {
    for(Symbol name : exports) {
      SEXP exportValue = namespaceEnvironment.findVariable(name);
      if(exportValue == Symbol.UNBOUND_VALUE) {
        context.warn(String.format("Symbol '%s' is not defined in package '%s'", name.getPrintName(), pkg.getName()));
      } else {
        packageEnv.setVariable(name, exportValue);
      }
    }
  }

  public void addExport(Symbol export) {
    exports.add(export);
  }

  public Package getPackage() {
    return pkg;
  }


  public void initImports(Context context, NamespaceRegistry registry, NamespaceFile file) {

    // Import symbols from other package namespaces
    for (NamespaceFile.PackageImportEntry entry : file.getPackageImports()) {
      Namespace importedNamespace = registry.getNamespace(context, entry.getPackageName());
      if(entry.isAllSymbols()) {
        importedNamespace.copyExportsTo(context, importsEnvironment);
      } else {
        for (Symbol symbol : entry.getSymbols()) {
          SEXP export = importedNamespace.getExportIfExists(symbol);
          if(export == Symbol.UNBOUND_VALUE) {
            context.warn(String.format("Symbol '%s' not exported from namespace '%s'", 
                symbol.getPrintName(), 
                importedNamespace.getName()));
          } else {
            importsEnvironment.setVariable(symbol, export);
          }
        }

        for (String className : entry.getClasses()) {
          Symbol symbol = S4.classNameMetadata(className);
          SEXP export = importedNamespace.getExportIfExists(symbol);
          if(export == Symbol.UNBOUND_VALUE) {
            context.warn(String.format("Class '%s' is not exported from namespace '%s'", 
                className, 
                importedNamespace.getName()));
          } else {
            importsEnvironment.setVariable(symbol, export);
          }
        }
      }
    }

    // Import from JVM classes
    for (NamespaceFile.JvmClassImportEntry entry : file.getJvmImports()) {
      Class importedClass = null;
      try {
        importedClass = pkg.loadClass(entry.getClassName());
      } catch (ClassNotFoundException e) {
        throw new EvalException("Could not load class '%s' from package '%s'", entry.getClassName(), pkg.getName());
      }

      if(entry.isClassImported()) {
        importsEnvironment.setVariable(importedClass.getSimpleName(), new ExternalPtr(importedClass));
      }
      if(!entry.getMethods().isEmpty()) {
        ClassBindingImpl importedClassBinding = ClassBindingImpl.get(importedClass);
        for (String method : entry.getMethods()) {
          importsEnvironment.setVariable(method, importedClassBinding.getStaticMember(method).getValue());
        }
      }
    }

    // Import from transpiled classes
    for (NamespaceFile.DynLibEntry library : file.getDynLibEntries()) {
      importDynamicLibrary(context, library);
    }

  }

  private void importDynamicLibrary(Context context, NamespaceFile.DynLibEntry entry) {
    DllInfo info = new DllInfo(entry.getLibraryName());
    Class clazz;

    try {

      FqPackageName packageName = pkg.getName();
      String className = packageName.getGroupId() + "." + packageName.getPackageName() + "." + entry.getLibraryName();
      clazz = pkg.loadClass(className);

    } catch (ClassNotFoundException e) {
      context.warn("Could not load compiled Fortran/C/C++ sources class for package " + pkg.getName() + ".\n" +
          "This is most likely because Renjin's compiler is not yet able to handle the sources for this\n" + 
          "particular package. As a result, some functions may not work.\n");
      return;
    }
    try {
      // Call the initialization routine
      Optional<Method> initMethod = findInitRoutine(entry.getLibraryName(), clazz);
      if(initMethod.isPresent()) {
        Context previousContext = Native.CURRENT_CONTEXT.get();
        Native.CURRENT_CONTEXT.set(context);
        try {
          initMethod.get().invoke(null, info);
        } catch (InvocationTargetException e) {
          throw new EvalException("Exception initializing compiled GNU R library " + entry.getLibraryName(), e.getCause());
        } finally {
          Native.CURRENT_CONTEXT.set(previousContext);
        }
      }

      // Create NativeSymbol objects in the namespace environment for registered 
      // methods. 
      if(entry.isRegistration()) {
        if (!initMethod.isPresent()) {
          throw new EvalException("useDynLib(.registration = TRUE) but no init method found!");
        }
        // Use the symbols registered by the R_init_xxx() function
        for (DllSymbol symbol : info.getSymbols()) {
          namespaceEnvironment.setVariable(entry.getPrefix() + symbol.getName(), symbol.createObject());
        }

      } else if(!entry.getSymbols().isEmpty()) {

        // Add the explicitly imported symbols as objects to the namespace
        for (NamespaceFile.DynLibSymbol declaredSymbol : entry.getSymbols()) {
          DllSymbol symbol = new DllSymbol(info);
          symbol.setName(declaredSymbol.getSymbolName());
          symbol.setMethodHandle(findGnurMethod(clazz, declaredSymbol.getSymbolName()));
          namespaceEnvironment.setVariable(entry.getPrefix() + declaredSymbol.getAlias(), symbol.createObject());
        }
      
      } else {

        // Make a list of all exported methods, we'll need this to resolve .Call() calls without
        // a PACKAGE argument
        for (Method method : clazz.getMethods()) {
          if(Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
            nativeSymbolMap.put(method.getName(), clazz);
          }
        }
        
      }

      
    } catch(Exception e) {
      // Allow the program to continue, there may be some packages whose gnur
      // compilation failed but can still partially function.
      e.printStackTrace();
      System.err.println("WARNING: Failed to import dynLib entries for " + getName() + ", expect subsequent failures");
    }
  }

  /**
   * GNU R provides a way of executing some code automatically when a object/DLL is either loaded or unloaded. 
   * This can be used, for example, to register native routines with R's dynamic symbol mechanism, initialize some data 
   * in the native code, or initialize a third party library. On loading a DLL, R will look for a routine within that
   * DLL named R_init_lib where lib is the name of the DLL file with the extension removed.
   *
   * @param libraryName the name of the library to load
   * @param clazz the JVM class containing the compiled routines
   * @return the method handle if it exists
   */
  private Optional<Method> findInitRoutine(String libraryName, Class clazz) {
    String initName = "R_init_" + libraryName;
    Class[] expectedParameterTypes = new Class[] { DllInfo.class };

    for (Method method : clazz.getMethods()) {
      if(method.getName().equals(initName)) {
        if(!Arrays.equals(method.getParameterTypes(), expectedParameterTypes)) {
          throw new EvalException(String.format("%s.%s has invalid signature: %s. Expected %s(DllInfo info)",
              clazz.getName(),
              initName,
              method.toString(),
              initName));
        }
        return Optional.of(method);
      }
    }
    return Optional.absent();
  }

  private boolean isPublicStatic(Method method) {
    return Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers());
  }

  private MethodHandle findGnurMethod(Class clazz, String symbolName) {
    for(Method method : clazz.getMethods()) {
      if(method.getName().equals(symbolName) && isPublicStatic(method)) {
        try {
          return MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
    throw new RuntimeException("Couldn't find method '" + symbolName + "'");
  }

  public void initExports(NamespaceFile file) {

    // First add all symbols that match the patterns
    for (String pattern : file.getExportedPatterns()) {
      ExtendedRE re = null;
      try {
        re = new ExtendedRE(pattern);
      } catch (RESyntaxException e) {
        throw new EvalException("Invalid export pattern '%s': %s", pattern, e.getMessage());
      }
      for(Symbol symbol : namespaceEnvironment.getSymbolNames()) {
        if(re.match(symbol.getPrintName())) {
          exports.add(symbol);
        }
      }
    }

    // ...And then symbols specified by name...
    for(Symbol symbol : file.getExportedSymbols()) {
      exports.add(symbol);
    }

    // .. And finally the metadata of exported classes
    for (String className : file.getExportedClasses()) {
      exports.add(S4.classNameMetadata(className));
    }

    // .. And the S4 methods and their classes
    for (String methodName : file.getExportedS4Methods()) {
      exports.add(Symbol.get(methodName));
    }
  }

  /**
   * Exports S3 methods defined in this package to the S3 Methods Table in the namespace
   * where the generic function was originally defined.
   *
   */
  public void registerS3Methods(Context context, NamespaceFile file) {

    for (NamespaceFile.S3MethodEntry entry : file.getExportedS3Methods()) {
      registerS3Method(context, entry);
    }
  }

  private void registerS3Method(Context context, NamespaceFile.S3MethodEntry entry) {
    // Find the S3 method implemented in this namespace
    Function method = resolveFunction(context, entry.getFunctionName());

    // Find the environment in which the original generic function was defined
    Optional<Environment> definitionEnv = resolveGenericFunctionNamespace(context, entry.getGenericMethod());
    if(!definitionEnv.isPresent()) {
      context.warn("Cannot resolve namespace environment from generic function '%s'");
      return;
    } 
    
    // Add an entry in a special .__S3MethodsTable__. so that UseMethod() can find this specialization
    if (!definitionEnv.get().hasVariable(S3.METHODS_TABLE)) {
      definitionEnv.get().setVariable(S3.METHODS_TABLE, Environment.createChildEnvironment(context.getBaseEnvironment()));
    }
    Environment methodsTable = (Environment) definitionEnv.get().getVariable(S3.METHODS_TABLE);
    methodsTable.setVariable(entry.getGenericMethod() + "." + entry.getClassName(), method);
  }

  private Function resolveFunction(Context context, String functionName) {
    SEXP methodExp = namespaceEnvironment.getVariable(functionName).force(context);
    if (methodExp == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Missing export: " + functionName + " not found in " + getName());
    }
    if (!(methodExp instanceof Function)) {
      throw new EvalException(functionName + ": expected function but was " + methodExp.getTypeName());
    }
    return (Function) methodExp;
  }

  /**
   * Resolves the namespace environment in which the original S3 generic function is defined.
   *
   * @param context the current evaluation context
   * @param genericName the name of the generic function (for example, "print" or "summary")
   * @return the namespace environment in which the function was defined, or {@code Optional.absent()} if
   * the function could not be resolved.
   */
  private Optional<Environment> resolveGenericFunctionNamespace(Context context, String genericName) {

    if (S3.GROUPS.contains(genericName)) {
      return Optional.of(baseNamespaceEnvironment);

    } else {
      SEXP genericFunction = namespaceEnvironment.findFunction(context, Symbol.get(genericName));
      if (genericFunction == null) {
        return Optional.absent();
      }
      if (genericFunction instanceof Closure) {
        return Optional.of(((Closure) genericFunction).getEnclosingEnvironment());

      } else if (genericFunction instanceof PrimitiveFunction) {
        return Optional.of(baseNamespaceEnvironment);

      } else {
        throw new EvalException("Cannot resolve namespace environment from generic function '%s' of type '%s'",
            genericName, genericFunction.getTypeName());
      }
    }
  }
}
