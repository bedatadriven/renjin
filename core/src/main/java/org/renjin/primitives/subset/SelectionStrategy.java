/**
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
import org.renjin.sexp.*;

/**
 * Provides an interface for different strategies for selecting and replacing elements. 
 * 
 * <p>R has a very rich set of ways to select and replace elements, and each can have quite 
 * different behavior and potential for optimization. For example, the expression {@code x[]}
 * evaluates simply to {@code x} and requires no computation, while {@code x[3,4:5]} requires materializing
 * {@code x} if it is differed and has complex behavior related to the dimension of the result.</p>
 * 
 * <p>For this reason, the implementation of the operators is factored into several different strategy objects:</p>
 * <ul>
 *   <li>{@link CompleteSelection} handles selections with no subscripts such as {@code x[]}
 *   <li>{@link NullSelection} handles selections using a single {@code NULL} subscript, such as {@code x[NULL]}
 *   <li>{@link VectorIndexSelection} handles selections with a single numeric subscript vector such as {@code x[1:3]}, {@code x[0]} or {@code x[-1]}
 *   <li>{@link LogicalSelection} handles selections using logical patterns, such as {@code x[TRUE]} or {@code x[c(TRUE,FALSE)]}</li>
 *   <li>{@link MatrixSelection} handles selections such as {@code x[3,4]} and {@code x[TRUE,]}</li>
 *   <li>{@link CoordinateMatrixSelection} handles selections of elements from matrices or arrays using coordinate matrices.</li>
 * </ul>
 * 
 * <p>The behavior of these strategies also varies significantly when used with the {@code [[} or the {@code [} operator,
 * between selection and replacement, and between atomic vectors, lists, and pairlists.</p>
 * 
 * <p>For this reason, each strategy is factored into a seperate method for each case to simplify implementation and 
 * testing.</p>
 * 
 */
public interface SelectionStrategy {

  
  SEXP getVectorSubset(Context context, Vector source, boolean drop);

  SEXP getSingleListElement(ListVector source, boolean exact);

  AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact);
  
  ListVector replaceListElements(Context context, ListVector list, Vector replacement);

  SEXP replaceAtomicVectorElements(Context context, AtomicVector source, Vector replacements);
  
  ListVector replaceSingleListElement(ListVector list, SEXP replacement);
  
  SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement);

  Vector replaceSingleElement(Context context, AtomicVector source, Vector replacement);
  
  
}
