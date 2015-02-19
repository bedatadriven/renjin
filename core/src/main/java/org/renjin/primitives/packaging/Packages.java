package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Invisible;
import org.renjin.invoke.annotations.Unevaluated;
import org.renjin.sexp.*;

import java.io.IOException;

public class Packages {

  @Builtin
  public static void library(
      @Current Context context,
      @Current NamespaceRegistry namespaceRegistry, 
      @Unevaluated SEXP packageNameExp) throws IOException {

    String packageName = parsePackageName(packageNameExp);

    Namespace namespace = namespaceRegistry.getNamespace(packageName);
    
    // Add "Depends" packages to the global search path
    // (But not "Imports" !)
    for(String dependencyName : namespace.getPackage().getPackageDependencies()) {
      context.getSession().getStdOut().println("Loading required package: " + dependencyName);
      library(context, namespaceRegistry, Symbol.get(dependencyName));
    }
    
    // Create the package environment
    Environment packageEnv = context.getGlobalEnvironment().insertAbove(new HashFrame());
    packageEnv.setAttribute(Symbols.NAME, StringVector.valueOf("package:" + packageName));
    
    // Copy in the namespace's exports
    namespace.copyExportsTo(packageEnv);
    
    // Load dataset objects as promises
    for(Dataset dataset : namespace.getPackage().getDatasets()) {
      for(String objectName : dataset.getObjectNames()) {
        packageEnv.setVariable(objectName, new DatasetObjectPromise(dataset, objectName));
      }
    }
    
    context.setInvisibleFlag();
  }

  @Builtin
  @Invisible
  public static boolean require(@Current Context context,
                                @Current NamespaceRegistry registry,
                                @Unevaluated SEXP packageNameExp) {
    try {
      library(context, registry, packageNameExp);
      return true;
    } catch(Exception e) {
      return false;
    }
  }


  private static String parsePackageName(SEXP packageNameExp) {
    String packageName;
    if(packageNameExp instanceof Symbol) {
      packageName = ((Symbol) packageNameExp).getPrintName();
    } else if(packageNameExp instanceof StringVector && packageNameExp.length()==1) {
      packageName = ((StringVector) packageNameExp).getElementAsString(0);
    } else {
      throw new UnsupportedOperationException("Unexpected package name argument: " + packageNameExp);
    }
    return packageName;
  }
  
}
