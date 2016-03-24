package org.renjin.primitives.subset;

import org.renjin.sexp.*;


public interface Selection2 {

  SEXP getVectorSubset(Vector source, boolean drop);
  
  SEXP getFunctionCallSubset(FunctionCall call);

  SEXP getSingleListElement(ListVector source, boolean exact);
  
  AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact);
  
  Vector replaceListElements(ListVector list, Vector replacement);

  Vector replaceAtomicVectorElements(AtomicVector source, Vector replacements);
  
  ListVector replaceSingleListElement(ListVector list, SEXP replacement);
  
  SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement);

  Vector replaceSingleElement(AtomicVector source, Vector replacement);

}
