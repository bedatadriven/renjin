package org.renjin.primitives.subset;

import org.renjin.sexp.*;


public interface Selection2 {

  SEXP get(Vector source, boolean drop);

  Vector replaceListElements(ListVector list, Vector replacement);

  Vector replaceAtomicVectorElements(AtomicVector source, Vector replacements);
  
  ListVector replaceSingleListElement(ListVector list, SEXP replacement);
  
  SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement);

  Vector replaceSingleElement(AtomicVector source, Vector replacement);

}
