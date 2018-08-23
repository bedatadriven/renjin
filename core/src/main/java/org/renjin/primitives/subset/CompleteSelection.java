/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.subset;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Vectors;
import org.renjin.primitives.sequence.RepDoubleVector;
import org.renjin.primitives.sequence.RepIntVector;
import org.renjin.primitives.sequence.RepLogicalVector;
import org.renjin.primitives.sequence.RepStringVector;
import org.renjin.sexp.*;

/**
 * Selection of the entire vector, for example {@code x[], or y[] <- 3}
 */
class CompleteSelection implements SelectionStrategy {


  @Override
  public SEXP getVectorSubset(Context context, Vector source, boolean drop) {
    // As far as I can tell, this never changes the input in any way
    return source;
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
  public Vector replaceAtomicVectorElements(Context context, AtomicVector source, Vector replacements) {

    // Change the length, if necessary, of the replacements vector so that
    // it matches the source vector
    Vector result = recycle(replacements, source.length());

    // If the source vector is wider than the replacement vector, then we need to change its
    // type. For example, 
    // x <- sqrt(1:10)   # double type
    // y <- 1:10         # integer type
    // x[] <- y          # convert y to double

    if (source.getVectorType().isWiderThan(replacements.getVectorType())) {
      result = Vectors.toType((AtomicVector)result, source.getVectorType());
    }

    // Finally, copy all attributes from the source to the transformed replacement
    return (Vector) result.setAttributes(source.getAttributes());
  
  }

  private Vector recycle(Vector x, int length) {
    
    if(x.length() == length) {
      return x;
    }
    
    // Try to avoid making a copy if possible or neccessary
    if(x.isDeferred()|| length > RepDoubleVector.LENGTH_THRESHOLD) {

      if (x instanceof DoubleVector) {
        return new RepDoubleVector(x, length, 1, AttributeMap.EMPTY);
      } else if (x instanceof IntVector) {
        return new RepIntVector(x, length, 1, AttributeMap.EMPTY);
      } else if (x instanceof StringVector) {
        return new RepStringVector(x, length, 1, AttributeMap.EMPTY);
      } else if (x instanceof LogicalVector) {
        return new RepLogicalVector(x, length, 1, AttributeMap.EMPTY);
      }
    }

    // Otherwise allocate the memory...
    Vector.Builder builder = x.newBuilderWithInitialCapacity(length);
    for(int i=0;i<length;++i) {
      builder.setFrom(i, x, i % x.length());
    }
    
    return builder.build();
  }

  @Override
  public ListVector replaceListElements(Context context, ListVector source, Vector replacement) {

    if (replacement == Null.INSTANCE) {
      return clearList(source);
    }

    if (replacement.length() == 0) {
      throw new EvalException("replacement has length zero");
    }
    
    ListVector.Builder result = new ListVector.Builder();
    result.copyAttributesFrom(source);
    
    int replacementIndex = 0;
    for (int i = 0; i < source.length(); i++) {
      result.setFrom(i, replacement, replacementIndex++);
      if(replacementIndex >= replacement.length()) {
        replacementIndex = 0;
      }
    }
    
    return result.build();
  }


  private ListVector clearList(ListVector list) {
    // Create an empty list, preserving only non-structural attributes
    AttributeMap.Builder builder = new AttributeMap.Builder();
    for (Symbol attribute : list.getAttributes().names()) {
      if (attribute != Symbols.NAMES &&
          attribute != Symbols.DIM && 
          attribute != Symbols.DIMNAMES) {
        
        builder.set(attribute, list.getAttribute(attribute));
      }
    }
    
    return new ListVector(new SEXP[0], builder.validateAndBuildForVectorOfLength(0));
  }

  private void checkReplacementLength(Vector source, SEXP replacements) {
    if( (source.length() % replacements.length()) != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }
  }

  @Override
  public Vector replaceSingleElement(Context context, AtomicVector source, Vector replacement) {
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
