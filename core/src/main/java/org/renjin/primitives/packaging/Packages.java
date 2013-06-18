package org.renjin.primitives.packaging;

import java.io.IOException;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Unevaluated;
import org.renjin.invoke.annotations.Invisible;
import org.renjin.sexp.Environment;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

public class Packages {

  @Builtin
  @Invisible
  public static void library(
      @Current Context context,
      @Current NamespaceRegistry namespaceRegistry, 
      @Unevaluated Symbol packageName) throws IOException {
    
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
                                @Unevaluated Symbol name) {
    try {
      library(context, registry, name);
      return true;
    } catch(Exception e) {
      return false;
    }
  }
  
}
