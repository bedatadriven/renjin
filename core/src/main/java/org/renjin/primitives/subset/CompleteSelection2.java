package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.primitives.Vectors;
import org.renjin.primitives.sequence.RepFunction;
import org.renjin.sexp.*;

/**
 * Selection of the entire vector, for example {@code x[], or y[] <- 3}
 */
public class CompleteSelection2 implements Selection2 {


  @Override
  public SEXP getVectorSubset(Vector source, boolean drop) {
    // As far as I can tell, this never changes the input in any way
    return source;
  }

  @Override
  public SEXP getFunctionCallSubset(FunctionCall call) {
    return call;
  }

  @Override
  public SEXP getSingleListElement(ListVector source, boolean exact) {
    // Cannot be used with [[ operator
    throw new EvalException("[[ operator requires a subscript");
  }

  @Override
  public AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact) {
    // Cannot be used with [[ operator
    throw new EvalException("[[ operator requires a subscript");
  }

  @Override
  public Vector replaceAtomicVectorElements(AtomicVector source, Vector replacements) {

    checkReplacementLength(source, replacements);

    if(replacements instanceof ListVector) {
      throw new UnsupportedOperationException("TODO");
    }

    // Increase the length, if necessary, of the replacements vector so that
    // it matches the source vector
    AtomicVector result;
    if(source.length() == replacements.length()) {
      result = (AtomicVector) replacements;
    } else {
      result = (AtomicVector) RepFunction.rep(replacements, Null.INSTANCE, source.length(), 1);
    }
    
    // If the source vector is wider than the replacement vector, then we need to change its
    // type. For example, 
    // x <- sqrt(1:10)   # double type
    // y <- 1:10         # integer type
    // x[] <- y          # convert y to double
    
    if(source.getVectorType().isWiderThan(replacements.getVectorType())) {
      result = Vectors.toType(result, source.getVectorType());
    }
    
    // Finally, copy all attributes from the source to the transformed replacement
    result = (AtomicVector) result.setAttributes(source.getAttributes());
    
    return result;
  }

  @Override
  public Vector replaceListElements(ListVector source, Vector replacement) {

    if (replacement == Null.INSTANCE) {
      return clearList(source);
    }

    if (replacement.length() == 0) {
      throw new EvalException("replacement has length zero");
    }
    
    checkReplacementLength(source, replacement);
    
    ListVector.Builder result = new ListVector.Builder();
    result.copyAttributesFrom(source);
    
    int sourceIndex = 0;
    int replacementIndex = 0;
    while(sourceIndex < source.length()) {
      result.setFrom(replacementIndex, replacement, replacementIndex++);
      if(replacementIndex > replacement.length()) {
        replacementIndex = 0;
      }
    }
    
    return result.build();
  }

  private Vector clearList(ListVector list) {
    // Create an empty list, preserving only non-structural attributes
    AttributeMap.Builder builder = new AttributeMap.Builder();
    for (Symbol attribute : list.getAttributes().names()) {
      if(attribute != Symbols.NAMES &&
         attribute != Symbols.DIM && 
         attribute != Symbols.DIMNAMES) {
        
        builder.set(attribute, list.getAttribute(attribute));
      }
    }
    
    return new ListVector(new SEXP[0], builder.build());
  }

  private void checkReplacementLength(Vector source, SEXP replacements) {
    if( (source.length() % replacements.length()) != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }
  }

  @Override
  public Vector replaceSingleElement(AtomicVector source, Vector replacement) {
    throw new EvalException("[[ ]] with missing subscript");
  }


  @Override
  public ListVector replaceSingleListElement(ListVector list, SEXP replacement) {
    throw new EvalException("[[ ]] with missing subscript");
  }

  @Override
  public SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement) {
    throw new EvalException("[[ ]] with missing subscript");
  }
}
