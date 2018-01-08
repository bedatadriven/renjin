/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Unevaluated;
import org.renjin.sexp.*;

import java.io.IOException;

public class Namespaces {

  @Internal
  public static SEXP getRegisteredNamespace(@Current Context context, @Current NamespaceRegistry registry, SEXP nameSexp) {
    Symbol name;
    if(nameSexp instanceof Symbol) {
      name = (Symbol) nameSexp;
      
      // Some GNU R functions use the name in package-attribute to load the necessary namespace. However, the
      // package-attribute is also used to store information about where a class is created which can be in
      // global environment (.GlobalEnv). In those cases no namespace need to be loaded. GNU R, therefor, returns
      // NULL when getNamespace is called on ".GlobalEnv".
      if (".GlobalEnv".equals(name.getPrintName())) {
        return Null.INSTANCE;
      }
      
    } else if(nameSexp instanceof StringVector) {
      name = Symbol.get(nameSexp.asString());
    } else {
      throw new EvalException("Illegal type of argument name: '%s'", nameSexp.getTypeName());
    }
    
    if(registry.isRegistered(name)) {
      return registry.getNamespace(context, name).getNamespaceEnvironment();
    } else {
      return Null.INSTANCE;
    }
  }
  
  @Internal
  public static Environment getNamespaceRegistry(@Current NamespaceRegistry registry) {
    return Environment.createChildEnvironment(Environment.EMPTY, new NamespaceFrame(registry)).build();
  }

  @Builtin
  public static SEXP getNamespace(@Current Context context, @Current NamespaceRegistry registry, Symbol name) {
    Namespace namespace = registry.getNamespace(context, name);
    Environment namespaceEnv = namespace.getNamespaceEnvironment();
    return namespaceEnv;
  }

  @Builtin
  public static SEXP getNamespace(@Current Context context, @Current NamespaceRegistry registry, String name) {
    Namespace namespace = registry.getNamespace(context, name);
    SEXP namespaceEnv = namespace.getNamespaceEnvironment();
    return namespaceEnv;
  }
  

  @Builtin
  public static boolean isNamespace(@Current NamespaceRegistry registry, SEXP envExp) {
    if(envExp instanceof Environment) {
      return registry.isNamespaceEnv((Environment)envExp);
    } else {
      return false;
    }
  }

  @Builtin
  public static StringVector loadedNamespaces(@Current NamespaceRegistry registry) {
    StringVector.Builder result = new StringVector.Builder();
    for(Symbol name : registry.getLoadedNamespaces()) {
      result.add(name.getPrintName());
    }
    return result.build();
  }

  @Builtin(":::")
  public static SEXP getNamespaceValue(@Current Context context,
                                       @Current NamespaceRegistry registry,
                                       @Unevaluated Symbol namespace,
                                       @Unevaluated Symbol entry) {

    return registry.getNamespace(context, namespace).getEntry(entry).force(context);
  }

  @Builtin("::")
  public static SEXP getExportedNamespaceValue(@Current Context context,
                                               @Current NamespaceRegistry registry,
                                               @Unevaluated Symbol namespace,
                                               @Unevaluated Symbol entry) {

    return registry.getNamespace(context, namespace).getExport(entry).force(context);
  }

  @Internal
  public static SEXP getDataset(@Current Context context, 
                                @Current NamespaceRegistry registry,
                                String namespaceName,
                                String datasetName) throws IOException {
    return registry.getNamespace(context, namespaceName).getPackage().getDataset(datasetName);
  }

  private static Namespace resolveNamespace(Context context, NamespaceRegistry registry, SEXP sexp) {

    if (sexp instanceof Environment) {
      Environment environment = (Environment) sexp;
      if (registry.isNamespaceEnv(environment)) {
        return registry.getNamespace(environment);
      }
    } else if(sexp instanceof StringVector && sexp.length() == 1) {
      return registry.getNamespace(context, ((StringVector) sexp).getElementAsString(0));
    }
    throw new EvalException("Error in argument " + sexp + " : not a namespace");
  }

  @Builtin
  public static StringVector getNamespaceName(@Current Context context,
                                              @Current NamespaceRegistry registry,
                                              final SEXP envExp) {

    Namespace namespace = resolveNamespace(context, registry, envExp);

    if(namespace == registry.getBaseNamespace()) {
      // For whatever reason R3.2.0 returns a simple character vector without attributes
      return new StringArrayVector("base");

    } else {
      // All other package names result in a named vector
      StringVector.Builder builder = StringArrayVector.newBuilder();
      builder.add(namespace.getCompatibleName());
      builder.setAttribute(Symbols.NAMES, StringArrayVector.valueOf("name"));
      return builder.build();
    }
  }

  @Builtin
  public static StringVector getNamespaceExports(@Current Context context, @Current NamespaceRegistry registry, final SEXP sexp) {
    final Namespace ns = resolveNamespace(context, registry, sexp);

    StringVector.Builder result = new StringVector.Builder();
    for (Symbol name : ns.getExports()) {
      result.add(name.getPrintName());
    }
    return result.build();
  }
  

  @Builtin
  public static StringVector getNamespaceImports(@Current Context context, @Current NamespaceRegistry registry, final SEXP sexp) {
    Namespace ns = resolveNamespace(context, registry, sexp);
    throw new UnsupportedOperationException("TODO: implement getNamespaceImports!");
  }
  
  @Internal("find.package")
  public static StringVector findPackage(@Current Context context, final AtomicVector packageNames) throws FileSystemException {
    StringArrayVector.Builder result = new StringArrayVector.Builder();
    for (int i = 0; i < packageNames.length(); i++) {
      String packageName = packageNames.getElementAsString(i);
      Namespace namespace = context.getNamespaceRegistry().getNamespace(context, packageName);
      FileObject fileObject = namespace.getPackage().resolvePackageRoot(context.getFileSystemManager());
      result.add(fileObject.getURL().toString());
    }
    return result.build();
  }

  @Internal("library.dynam")
  public static SEXP libraryDynam(@Current Context context, String libraryName, String packageName) {
    Namespace namespace = context.getNamespaceRegistry().getNamespace(context, packageName);
    DllInfo dllInfo;
    try {
      dllInfo = namespace.loadDynamicLibrary(context, libraryName);
    } catch (ClassNotFoundException e) {
      // Allow the package to continue loading...
      context.warn("Could not load the dynamic library: " + e.getMessage());
      return Null.INSTANCE;
    }

    return dllInfo.buildDllInfoSexp();
  }

  @Internal("library.dynam.unload")
  public static SEXP libraryDynamUnload(@Current Context context, String name) {
    return Null.INSTANCE;
  }
}
