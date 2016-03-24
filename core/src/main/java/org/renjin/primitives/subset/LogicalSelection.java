package org.renjin.primitives.subset;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

/**
 * Selection based on a boolean mask, for example {@code x[TRUE] or x[c(TRUE,FALSE)]}
 */
public class LogicalSelection implements Selection2 {

  /**
   * The boolean mask of elements to replace
   */
  private final LogicalVector mask;

  public LogicalSelection(LogicalVector mask) {
    this.mask = mask;
  }

  @Override
  public SEXP getVectorSubset(Vector source, boolean drop) {
    return IndexSelection.buildSelection(source, new LogicalSubscript2(this.mask, source.length()));
  }

  @Override
  public SEXP getFunctionCallSubset(FunctionCall call) {
    return IndexSelection.buildCallSelection(call, new LogicalSubscript2(this.mask, call.length()));
  }


  @Override
  public Vector replaceAtomicVectorElements(AtomicVector source, Vector replacements) {
    
    if(source instanceof DeferredComputation ||
       replacements instanceof DeferredComputation ||
       source.length() > 1000) {
      
      // Compute the replacement type 
      Vector.Type resultType = Vector.Type.widest(source, replacements);
      if(resultType == DoubleVector.VECTOR_TYPE) {
        return new MaskedDoubleReplacement(source.getAttributes(), source, mask, (AtomicVector)replacements);
      }
    }
    
    return buildReplacement(source, replacements);
  }

  @Override
  public Vector replaceListElements(ListVector source, Vector replacement) {
    return buildReplacement(source, replacement);
  }
  
  private Vector buildReplacement(Vector source, Vector replacements) {
    
    Vector.Builder builder = source.newCopyBuilder(replacements.getVectorType());
    int maskIndex = 0;
    int sourceIndex = 0;
    int replacementIndex = 0;

    if(mask.length() > 0) {
      while (sourceIndex < source.length()) {
        int maskValue = mask.getElementAsRawLogical(maskIndex++);
        if (maskValue == 1) {
          builder.setFrom(sourceIndex, replacements, replacementIndex++);
        } else if (IntVector.isNA(maskValue)) {
          builder.setNA(sourceIndex);
        }
        sourceIndex++;
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
