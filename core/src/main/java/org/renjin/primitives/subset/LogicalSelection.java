package org.renjin.primitives.subset;

import org.renjin.eval.Context;
import org.renjin.sexp.*;

/**
 * Selection based on a boolean mask, for example {@code x[TRUE] or x[c(TRUE,FALSE)]}
 */
class LogicalSelection implements SelectionStrategy {

  /**
   * The boolean mask of elements to replace
   */
  private final LogicalVector mask;

  public LogicalSelection(LogicalVector mask) {
    this.mask = mask;
  }

  @Override
  public SEXP getVectorSubset(Context context, Vector source, boolean drop) {
    return VectorIndexSelection.buildSelection(source, new LogicalSubscript(this.mask, source.length()));
  }

  @Override
  public SEXP getFunctionCallSubset(FunctionCall call) {
    return VectorIndexSelection.buildCallSelection(call, new LogicalSubscript(this.mask, call.length()));
  }


  @Override
  public Vector replaceAtomicVectorElements(Context context, AtomicVector source, Vector replacements) {
    
//    if( source.length() >= mask.length() &&
//        source instanceof DeferredComputation ||
//        replacements instanceof DeferredComputation ||
//        source.length() > 1000) {
//      
//      // Compute the replacement type 
//      Vector.Type resultType = Vector.Type.widest(source, replacements);
//      if(resultType == DoubleVector.VECTOR_TYPE) {
//        return new MaskedDoubleReplacement(source.getAttributes(), source, mask, (AtomicVector)replacements);
//      }
//    }
    
    return buildReplacement(source, replacements);
  }
  
  @Override
  public Vector replaceListElements(Context context, ListVector source, Vector replacement) {
    return buildReplacement(source, replacement);
  }

  private Vector buildReplacement(Vector source, Vector replacements) {
    
    Vector.Builder builder = source.newCopyBuilder(replacements.getVectorType());
    int maskIndex = 0;
    int resultIndex = 0;
    int replacementIndex = 0;
    
    // The length of the result vector is the longer of the 
    // source vector or the logical subscript
    int resultLength = Math.max(source.length(), mask.length());

    if(mask.length() > 0) {
      while (resultIndex < resultLength) {
        int maskValue = mask.getElementAsRawLogical(maskIndex++);
        if (maskValue == 1) {
          builder.setFrom(resultIndex, replacements, replacementIndex++);
        } else if (IntVector.isNA(maskValue)) {
          builder.setNA(resultIndex);
        }
        resultIndex++;
        if (replacementIndex >= replacements.length()) {
          replacementIndex = 0;
        }
        if (maskIndex >= mask.length()) {
          maskIndex = 0;
        }
      }
    }
    return builder.build();
  }

  @Override
  public SEXP getSingleListElement(ListVector source, boolean exact) {
    throw new UnsupportedOperationException("[[ operator never uses logical subscrpts");
  }

  @Override
  public AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact) {
    throw new UnsupportedOperationException("[[ operator never uses logical subscrpts");
  }

  @Override
  public Vector replaceSingleElement(AtomicVector source, Vector replacement) {
    throw new UnsupportedOperationException("[[ operator never uses logical subscrpts");
  }


  @Override
  public ListVector replaceSingleListElement(ListVector list, SEXP replacement) {
    throw new UnsupportedOperationException("[[ operator never uses logical subscrpts");
  }

  @Override
  public SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement) {
    throw new UnsupportedOperationException("[[ operator never uses logical subscrpts");
  }
}
