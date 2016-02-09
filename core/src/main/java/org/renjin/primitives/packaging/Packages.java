package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Invisible;
import org.renjin.sexp.Environment;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;

import java.io.IOException;

public class Packages {

  @Internal
  public static void library(
      @Current Context context,
      @Current NamespaceRegistry namespaceRegistry, 
      String packageName) throws IOException {


    Namespace namespace = namespaceRegistry.getNamespace(context, packageName);
    
    // Add "Depends" packages to the global search path
    // (But not "Imports" !)
    for(String dependencyName : namespace.getPackage().getPackageDependencies()) {
      context.getSession().getStdOut().println("Loading required package: " + dependencyName);
      library(context, namespaceRegistry, dependencyName);
    }
    
    // Create the package environment
    Environment packageEnv = context.getGlobalEnvironment().insertAbove(new HashFrame());
    packageEnv.setAttribute(Symbols.NAME, StringVector.valueOf("package:" + packageName));
    
    // Copy in the namespace's exports
    namespace.copyExportsTo(context, packageEnv);
    
    // Load dataset objects as promises
    for(Dataset dataset : namespace.getPackage().getDatasets()) {
      for(String objectName : dataset.getObjectNames()) {
        packageEnv.setVariable(objectName, new DatasetObjectPromise(dataset, objectName));
      }
    }
    
    context.setInvisibleFlag();
  }

  @Internal
  @Invisible
  public static boolean require(@Current Context context,
                                @Current NamespaceRegistry registry, 
                                String packageName) {
    try {
      library(context, registry, packageName);
      return true;
    } catch(Exception e) {
      return false;
    }
  }
}
