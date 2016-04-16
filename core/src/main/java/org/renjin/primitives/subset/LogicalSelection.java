package org.renjin.primitives.subset;

import org.renjin.eval.Context;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;

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

    // For ATOMIC VECTORS, logical subscripts behave quite differently if they are longer
    // than the source vector.

    // For example, if x = c(1,2)
    // Then x[TRUE] <- 99 will assign c(99, 99) to x
    // But x[TRUE,FALSE,TRUE,FALSE] will extend the vector to x(99, 2, 99, NA)

    if(mask.length() <= source.length()) {
      return buildMaskedReplacement(source, replacements);
    } else {
      return buildExtendedReplacement(source, replacements);
    }
  }

  @Override
  public Vector replaceListElements(Context context, ListVector source, Vector replacement) {
    
    // Behavior for lists is to remove selected elements
    if(replacement == Null.INSTANCE) {
      if(mask.length() == 0) {
        return source;
      } else {
        return ListSubsetting.removeListElements(source, new LogicalPredicate(mask));
      }
    }
    
    if(mask.length() <= source.length()) {
      return buildMaskedReplacement(source, replacement);
    } else {
      return buildExtendedReplacement(source, replacement);
    }
  }

  private Vector buildExtendedReplacement(Vector source, Vector replacements) {
    assert source.length() < mask.length();

    int resultLength = mask.length();
    int sourceLength = source.length();

    Vector.Builder result = Vector.Type.widest(source, replacements).newBuilderWithInitialCapacity(resultLength);
    NamesBuilder resultNames = NamesBuilder.withInitialCapacity(resultLength);

    int replacementIndex = 0;

    for(int i=0;i<resultLength;++i) {
      int maskValue = mask.getElementAsRawLogical(i);
      if (maskValue == 0) {
        // FALSE: use source value IF still in range
        if(i < sourceLength) {
          result.addFrom(source, i);
          resultNames.add(source.getName(i));
        } else {
          result.addNA();
          resultNames.addNA();
        }
      } else if (IntVector.isNA(maskValue)) {
        // NA: set value to NA
        result.setNA(i);
        resultNames.addNA();

      } else {
        // TRUE: use next replacement element
        result.setFrom(i, replacements, replacementIndex);

        replacementIndex++;
        if (replacementIndex >= replacements.length()) {
          replacementIndex = 0;
        }
      }
    }
    result.setAttribute(Symbols.NAMES, resultNames.build());
    return result.build();
  }

  private Vector buildMaskedReplacement(Vector source, Vector replacements) {

    Vector.Builder builder = source.newCopyBuilder(replacements.getVectorType());
    int maskIndex = 0;
    int resultIndex = 0;
    int replacementIndex = 0;

    int sourceLength = source.length();

    if (mask.length() > 0) {
      while (resultIndex < sourceLength) {
        int maskValue = mask.getElementAsRawLogical(maskIndex++);
        
        if (maskValue == 1) {
          builder.setFrom(resultIndex, replacements, replacementIndex++);
          if (replacementIndex >= replacements.length()) {
            replacementIndex = 0;
          }
        } 
        resultIndex++;
        
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
