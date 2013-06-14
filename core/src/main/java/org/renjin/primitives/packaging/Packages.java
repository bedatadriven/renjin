package org.renjin.primitives.packaging;

import java.io.IOException;

import org.renjin.eval.Context;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Evaluate;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.annotations.Visible;
import org.renjin.sexp.Environment;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

public class Packages {

  @Primitive
  @Visible(false)
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
    
    // Load datasets
    for(String dataset : namespace.getPackage().getDatasets()) {
      SEXP data = namespace.getPackage().loadDataset(dataset);
      if(data instanceof PairList) {
        PairList list = (PairList)data;
        for(PairList.Node node : list.nodes()) {
          packageEnv.setVariable(node.getTag(), node.getValue());
        }
      } else {
        System.out.println(dataset + " is not a pairlist, ignoring");
      }
    }
  }

  @Primitive
  @Visible(false)
  public static boolean require(@Current Context context,
                                @Current NamespaceRegistry registry, @Evaluate(false) Symbol name) {
    try {
      library(context, registry, name);
      return true;
    } catch(Exception e) {
      return false;
    }
  }
  
}
