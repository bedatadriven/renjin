package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
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
  public Vector replaceElements(AtomicVector source, Vector replacements) {
    return buildReplacement(source, replacements);
  }

  @Override
  public Vector replaceListElements(ListVector source, Vector replacement) {
    
    if(!(replacement instanceof Vector)) {
      throw new EvalException("object of type '%s' cannot be coerced to type 'list'", replacement.getTypeName());
    }

    return buildReplacement(source, (Vector)replacement);
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
