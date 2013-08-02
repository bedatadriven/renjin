package org.renjin.primitives.packaging;

import java.io.IOException;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Unevaluated;
import org.renjin.invoke.annotations.Invisible;
import org.renjin.sexp.*;

public class Packages {

  @Builtin
  @Invisible
  public static void library(
      @Current Context context,
      @Current NamespaceRegistry namespaceRegistry, 
      @Unevaluated SEXP packageNameExp) throws IOException {

    String packageName = parsePackageName(packageNameExp);

    Namespace namespace = namespaceRegistry.getNamespace(packageName);
    
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
