/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
  public Vector replaceSingleElement(Context context, AtomicVector source, Vector replacement) {
    throw new EvalException("attempt to select less than one element");
  }
}
