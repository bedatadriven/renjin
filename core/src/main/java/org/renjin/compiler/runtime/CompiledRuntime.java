package org.renjin.compiler.runtime;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Logical;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;


public class CompiledRuntime {

  public static boolean evaluateCondition(SEXP s) {

    if (s.length() == 0) {
      throw new EvalException("argument is of length zero");
    }
//    if (s.length() > 1) {
//      Warning.invokeWarning(context, call, "the condition has length > 1 and only the first element will be used");
//    }

    Logical logical = s.asLogical();
    if (logical == Logical.NA) {
      throw new EvalException("missing value where TRUE/FALSE needed");
    }

    return logical == Logical.TRUE;
  }
  
  /**
   * Splices an elipses into the argument name list
   * @param staticArgName
   * @param elipses
   * @param insertPos
   * @return
   */
  public static String[] spliceArgNames(String staticArgName[], PairList elipses, int insertPos) {
    String [] spliced = new String[staticArgName.length + elipses.length() - 1];
    int i=0;
    for(;i<insertPos;++i) {
      spliced[i] = staticArgName[i];
    }
    int j = insertPos;
    for(PairList.Node node : elipses.nodes()) {
      if(node.hasTag()) {
        spliced[j++] = node.getTag().getPrintName();
      }
    }
    for(i=insertPos+1;i<staticArgName.length;++i) {
      spliced[j++] = staticArgName[i];
    }
    return spliced;
  }
  
  public static SEXP[] spliceArgValues(Context context, SEXP staticArgValues[], PairList elipses, int insertPos) {
    SEXP [] spliced = new SEXP[staticArgValues.length + elipses.length() - 1];
    int i=0;
    for(;i<insertPos;++i) {
      spliced[i] = staticArgValues[i];
    }
    int j = insertPos;
    for(PairList.Node node : elipses.nodes()) {
      spliced[j++] = node.getValue().force(context);
    }
    for(i=insertPos+1;i<staticArgValues.length;++i) {
      spliced[j++] = staticArgValues[i];
    }
    return spliced;
  }
}
