package org.renjin.primitives.packaging;

import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.reflection.ClassBindingImpl;
import org.renjin.methods.S4;
import org.renjin.primitives.S3;
import org.renjin.primitives.text.regex.ExtendedRE;
import org.renjin.sexp.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Namespace {

  private final Environment namespaceEnvironment;
  private final Environment importsEnvironment;
  private final Environment baseNamespaceEnvironment;

  private final List<Symbol> exports = Lists.newArrayList();

  private Package pkg;

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

  public SEXP getEntry(Symbol entry) {
    SEXP value = namespaceEnvironment.getVariable(entry);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Namespace " + pkg.getName() + " has no symbol named '" + entry + "'");
    }
    return value;
  }

  public SEXP getExport(Symbol entry) {
    // the base package's namespace is treated specially for historical reasons:
    // all symbols are considered to be exported.
    if(FqPackageName.BASE.equals(pkg.getName())) {
      return getEntry(entry);
    }
    if(exports.contains(entry)) {
      return this.namespaceEnvironment.getVariable(entry);
    }
    throw new EvalException("Namespace " + pkg.getName() + " has no exported symbol named '" + entry.getPrintName() + "'");
  }

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
   * @param packageEnv
   */
  public void copyExportsTo(Environment packageEnv) {
    for(Symbol name : exports) {
      packageEnv.setVariable(name, namespaceEnvironment.getVariable(name));
    }
  }

  public void addExport(Symbol export) {
    exports.add(export);
  }

  public Package getPackage() {
    return pkg;
  }


  public void initImports(NamespaceRegistry registry, NamespaceFile file) {

    // Import symbols from other package namespaces
    for (NamespaceFile.PackageImportEntry entry : file.getPackageImports()) {
      Namespace importedNamespace = registry.getNamespace(entry.getPackageName());
      if(entry.isAllSymbols()) {
        importedNamespace.copyExportsTo(importsEnvironment);
      } else {
        for (Symbol symbol : entry.getSymbols()) {
          importsEnvironment.setVariable(symbol, importedNamespace.getExport(symbol));
        }

        for (String className : entry.getClasses()) {
          Symbol symbol = S4.classNameMetadata(className);
          importsEnvironment.setVariable(symbol, importedNamespace.getExport(symbol));
        }
      }
    }

    // Import from JVM classes
    for (NamespaceFile.JvmClassImportEntry entry : file.getClassImports()) {
      Class importedClass = pkg.loadClass(entry.getClassName());

      if(entry.isClassImported()) {
        importsEnvironment.setVariable(entry.getClassName(), new ExternalPtr(importedClass));
      }
      if(!entry.getMethods().isEmpty()) {
        ClassBindingImpl importedClassBinding = ClassBindingImpl.get(importedClass);
        for (String method : entry.getMethods()) {
          importsEnvironment.setVariable(method, importedClassBinding.getStaticMember(method).getValue());
        }
      }
    }

    // Import from transpiled classes
//    try {
//      FqPackageName packageName = pkg.getName();
//      String className = packageName.getGroupId() + "." + packageName.getPackageName() + "." + libraryName;
//      Class clazz = pkg.loadClass(className);
//
//      if(entries.isEmpty()) {
//        // add all methods from class file
//        for(Method method : clazz.getMethods()) {
//          if(isPublicStatic(method)) {
//            addGnurMethod(fixes + method.getName(), method);
//          }
//        }
//      } else {
//        for(NamespaceFile.DynlibEntry entry : entries) {
//          Method method = findGnurMethod(clazz, entry.getSymbolName());
//          if(entry.getAlias() != null) {
//            addGnurMethod(fixes + entry.getAlias(), method);
//          } else {
//            addGnurMethod(fixes + entry.getSymbolName(), method);
//          }
//        }
//      }
//    } catch(Exception e) {
//      // Allow the program to continue, there may be some packages whose gnur
//      // compilation failed but can still partially function.
//      System.err.println("WARNING: Failed to import dynLib entries for " + namespace.getName() + ", expect subsequent failures");
//    }

  }


  private void addGnurMethod(String name, Method method) {
    getImportsEnvironment().setVariable(name, new ExternalPtr<Method>(method));
  }

  private boolean isPublicStatic(Method method) {
    return Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers());
  }

  private Method findGnurMethod(Class clazz, String symbolName) {
    for(Method method : clazz.getMethods()) {
      if(method.getName().equals(symbolName) && isPublicStatic(method)) {
        return method;
      }
    }
    throw new RuntimeException("Couldn't find method '" + symbolName + "'");
  }

  public void initExports(NamespaceFile file) {

    // First add all symbols that match the patterns
    for (String pattern : file.getExportedPatterns()) {
      ExtendedRE re = new ExtendedRE(pattern);
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
    Environment definitionEnv = resolveGenericFunctionNamespace(context, entry.getGenericMethod());

    // Add an entry in a special .__S3MethodsTable__. so that UseMethod() can find this specialization
    if (!definitionEnv.hasVariable(S3.METHODS_TABLE)) {
      definitionEnv.setVariable(S3.METHODS_TABLE, Environment.createChildEnvironment(context.getBaseEnvironment()));
    }
    Environment methodsTable = (Environment) definitionEnv.getVariable(S3.METHODS_TABLE);
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
   * @return the namespace environment in which the function was defined
   */
  private Environment resolveGenericFunctionNamespace(Context context, String genericName) {

    if (S3.GROUPS.contains(genericName)) {
      return baseNamespaceEnvironment;

    } else {
      SEXP genericFunction = namespaceEnvironment.findFunction(context, Symbol.get(genericName));
      if (genericFunction == null) {
        throw new EvalException("Cannot find S3 method definition '" + genericName + "'");
      }
      if (genericFunction instanceof Closure) {
        return ((Closure) genericFunction).getEnclosingEnvironment();

      } else if (genericFunction instanceof PrimitiveFunction) {
        return baseNamespaceEnvironment;

      } else {
        throw new EvalException("Cannot resolve namespace environment from generic function '%s' of type '%s'",
            genericName, genericFunction.getTypeName());

      }
    }
  }
}
