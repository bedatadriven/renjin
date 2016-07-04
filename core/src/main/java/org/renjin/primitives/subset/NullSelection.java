package org.renjin.primitives.subset;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

/**
 * Selection using the NULL subscript, for example {@code x[NULL] or x[[NULL]]}
 */
public enum NullSelection implements SelectionStrategy {
  
  INSTANCE;
  
  @Override
  public SEXP getVectorSubset(Context context, Vector source, boolean drop) {
    
    // Return an empty vector of the same type
    Vector.Builder result = source.getVectorType().newBuilderWithInitialCapacity(0);
    
    // If the source is named, then attach an empty names vector
    if(source.getNames() != Null.INSTANCE) {
      result.setAttribute(Symbols.NAMES, StringArrayVector.EMPTY);
    }

    return result.build();
  }

  @Override
  public ListVector replaceListElements(Context context, ListVector source, Vector replacement) {
    // No changes to the source
    return source;
  }

  @Override
  public Vector replaceAtomicVectorElements(Context context, AtomicVector source, Vector replacements) {
    // No changes to the source
    return source;
  }
  
  @Override
  public SEXP getSingleListElement(ListVector source, boolean exact) {
    throw new EvalException("attempt to select less than one element");
  }

  @Override
  public AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact) {
    throw new EvalException("attempt to select less than one element");
  }

  @Override
  public ListVector replaceSingleListElement(ListVector list, SEXP replacement) {
    throw new EvalException("attempt to select less than one element");
  }

  @Override
  public SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement) {
    throw new EvalException("attempt to select less than one element");
  }

  @Override
  public Vector replaceSingleElement(AtomicVector source, Vector replacement) {
    throw new EvalException("attempt to select less than one element");
  }
}
