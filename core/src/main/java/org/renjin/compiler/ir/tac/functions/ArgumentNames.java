package org.renjin.compiler.ir.tac.functions;

import org.renjin.sexp.PairList;

public class ArgumentNames {

  public static String[] toArray(PairList arguments) {
    String[] names = new String[arguments.length()];
    int i=0;
    for(PairList.Node node : arguments.nodes()) {
      if(node.hasTag()) {
        names[i] = node.getTag().getPrintName();
      }
    }
    return names;
  }
}
