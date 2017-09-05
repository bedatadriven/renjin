/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.reflection.ClassBindingImpl;
import org.renjin.methods.S4;
import org.renjin.primitives.S3;
import org.renjin.primitives.text.regex.RE;
import org.renjin.primitives.text.regex.REFactory;
import org.renjin.repackaged.guava.base.Optional;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.*;

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

  protected final List<DllInfo> libraries = new ArrayList<>(0);

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
    SEXP value = namespaceEnvironment.getVariableUnsafe(entry);
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
      return namespaceEnvironment.getVariableUnsafe(entry);

    }
    if(exports.contains(entry)) {
      return this.namespaceEnvironment.findVariableUnsafe(entry);
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


  public List<DllInfo> getLibraries() {
    return libraries;
  }

  /**
   * Copies the exported (public) symbols from our namespace environment
   * to the given package environment
   *
   */
  public void copyExportsTo(Context context, Environment packageEnv) {
    for(Symbol name : exports) {
      SEXP exportValue = namespaceEnvironment.findVariableUnsafe(name);
      if(exportValue == Symbol.UNBOUND_VALUE) {
        context.warn(String.format("Symbol '%s' is not defined in package '%s'", name.getPrintName(), pkg.getName()));
      } else {
        packageEnv.setVariableUnsafe(name, exportValue);
      }
    }
  }

  public void addExport(Symbol export) {
    exports.add(export);
  }

  public Package getPackage() {
    return pkg;
  }

  /**
   * It is legal for package names to contain dots, and we can load resources with dots in their names, such
   * as environment, serialized symbols, and data files, but we CANNOT load a class through reflection with a dot
   * in its name.
   *
   * @param packageName the original package name
   * @return a sanitized name of a package with '.', replaced with '$'
   */
  public static String sanitizePackageNameForClassFiles(String packageName) {
    return packageName.replace('.', '$');
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
            importsEnvironment.setVariableUnsafe(symbol, export);
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
            importsEnvironment.setVariableUnsafe(symbol, export);
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
        throw new EvalException("Could not load JVM class '%s' from package '%s'", entry.getClassName(), pkg.getName());
      }

      if(entry.isClassImported()) {
        importsEnvironment.setVariableUnsafe(importedClass.getSimpleName(), new ExternalPtr(importedClass));
      }
      if(!entry.getMethods().isEmpty()) {
        ClassBindingImpl importedClassBinding = ClassBindingImpl.get(importedClass);
        for (String method : entry.getMethods()) {
          importsEnvironment.setVariableUnsafe(method, importedClassBinding.getStaticMember(method).getValue());
        }
      }
    }

    // Import from compiled classes
    for (NamespaceFile.DynLibEntry library : file.getDynLibEntries()) {
      importDynamicLibrary(context, library);
    }

  }

  private void importDynamicLibrary(Context context, NamespaceFile.DynLibEntry entry) {

    // The process of loading a "native" library is slightly complicated.
    // Comments below are excerpts from the Writing R Extensions Guide:
    // https://cran.r-project.org/doc/manuals/r-release/R-exts.html#useDynLib

    // A NAMESPACE file can contain one or more useDynLib directives which allows shared objects that need to be loaded.
    // The directive
    //
    //    useDynLib(foo)
    //
    // registers the shared object foo for loading with library.dynam.
    // Loading of registered object(s) occurs after the package code has been loaded
    // and before running the load hook function.

    DllInfo library;
    try {
      library = loadDynamicLibrary(context, entry.getLibraryName());
    } catch (Exception e) {
      context.warn("Could not load compiled Fortran/C/C++ sources class for package " + pkg.getName() + ".\n" +
          "This is most likely because Renjin's compiler is not yet able to handle the sources for this\n" +
          "particular package. As a result, some functions may not work.\n");
      e.printStackTrace();
      return;
    }

    // The useDynLib directive also accepts the names of the native routines that are to be used in R via the .C,
    // .Call, .Fortran and .External interface functions. These are given as additional arguments to the directive,
    // for example,
    //
    //    useDynLib(foo, myRoutine, myOtherRoutine)
    //
    // By specifying these names in the useDynLib directive, the native symbols are resolved when the package is
    // loaded and R variables identifying these symbols are added to the package’s namespace with these names.
    // These can be used in the .C, .Call, .Fortran and .External calls in place of the name of the routine and the
    // PACKAGE argument.

    for (NamespaceFile.DynLibSymbol declaredSymbol : entry.getSymbols()) {
      Optional<DllSymbol> symbol = library.lookup(DllSymbol.Convention.C, declaredSymbol.getSymbolName());
      if(symbol.isPresent()) {
        namespaceEnvironment.setVariableUnsafe(entry.getPrefix() + declaredSymbol.getAlias(), symbol.get().buildNativeSymbolInfoSexp());
      }
    }

    // If the package has registration information (via the library's R_init_mylib method), then we can use that
    // directly rather than specifying the list of symbols again in the useDynLib directive in the NAMESPACE file.
    //
    // Using the .registration argument of useDynLib, we can instruct the namespace mechanism to create R
    // variables for these symbols.

    if(entry.isRegistration()) {
      // Use the symbols registered by the R_init_xxx() function
      for (DllSymbol symbol : library.getRegisteredSymbols()) {
        namespaceEnvironment.setVariableUnsafe(entry.getPrefix() + symbol.getName(), symbol.buildNativeSymbolInfoSexp());
      }
    }
  }

  /**
   * Loads a compiled library from this namespace's package.
   *
   * <p>Many packages written for GNU R contain C, C++, Fortran sources, which are
   * compiled into a dynamically-linked library (DLL) at build time, and then loaded
   * dynamically when the package is loaded.</p>
   *
   * <p>Renjin does its best to map this system to the JVM with a few steps:</p>
   *
   * <p>At compile time, native sources are compiled to JVM byte code with gcc-bridge.
   *
   * <p>GCC Bridge generates a single "trampoline" class that contains a public static method
   * for each of the functions that would normally be exported from a DLL. This trampoline class
   * is named using the groupId and package name, for example "org.renjin.cran.Matrix".
   *
   * <p>At runtime, we load this trampoline class dynamically and locate "native" symbols
   * with Java Reflection.</p>
   *
   */
  public DllInfo loadDynamicLibrary(Context context, String libraryName) throws ClassNotFoundException {
    Class libraryClass;

    FqPackageName packageName = pkg.getName();
    String className = packageName.getGroupId() + "." +
        Namespace.sanitizePackageNameForClassFiles(packageName.getPackageName()) + "." +
        Namespace.sanitizePackageNameForClassFiles(libraryName);
    libraryClass = pkg.loadClass(className);


    DllInfo library = new DllInfo(libraryName, libraryClass);
    library.initialize(context);

    // The "library" is registered both at the session-level...
    context.getSession().loadLibrary(library);

    // ... within this namespace
    libraries.add(library);

    return library;
  }

  public Optional<DllSymbol> lookupSymbol(DllSymbol.Convention convention, String name) {
    for (DllInfo library : libraries) {
      Optional<DllSymbol> symbol = library.lookup(convention, name);
      if(symbol.isPresent()) {
        return symbol;
      }
    }
    return Optional.absent();
  }

  public void initExports(NamespaceFile file) {

    // First add all symbols that match the patterns
    for (String pattern : file.getExportedPatterns()) {
      RE re = REFactory.compile(pattern,
          /* ignore.case = */ false,
          /* perl = */ false,
          /* fixed = */ false,
          /* useBytes = */ false);

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
      context.warn(String.format("Cannot resolve namespace environment from generic function '%s'", 
          entry.getGenericMethod()));
      return;
    } 
    
    // Add an entry in a special .__S3MethodsTable__. so that UseMethod() can find this specialization
    if (!definitionEnv.get().hasVariable(S3.METHODS_TABLE)) {
      definitionEnv.get().setVariableUnsafe(S3.METHODS_TABLE, Environment.createChildEnvironment(context.getBaseEnvironment()).build());
    }
    Environment methodsTable = (Environment) definitionEnv.get().getVariableUnsafe(S3.METHODS_TABLE);
    methodsTable.setVariableUnsafe(entry.getGenericMethod() + "." + entry.getClassName(), method);
  }

  private Function resolveFunction(Context context, String functionName) {
    SEXP methodExp = namespaceEnvironment.getVariableUnsafe(functionName).force(context);
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
