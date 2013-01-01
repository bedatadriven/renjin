package org.renjin.primitives.packaging;

import java.io.IOException;

import org.renjin.eval.Context;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Evaluate;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.Environment;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

public class Packages {

  @Primitive
  public static void library(
      @Current Context context,
      @Current NamespaceRegistry namespaceRegistry, 
      @Evaluate(false) Symbol packageName) throws IOException {
    
    Namespace namespace = namespaceRegistry.getNamespace(packageName);
    
    // Create the package environment
    Environment packageEnv = context.getGlobalEnvironment().insertAbove(new HashFrame());
    packageEnv.setAttribute(Symbols.NAME, StringVector.valueOf("package:" + packageName));
    // Copy in the namespace's exports
    namespace.copyExportsTo(packageEnv);
   
  }
  
}
