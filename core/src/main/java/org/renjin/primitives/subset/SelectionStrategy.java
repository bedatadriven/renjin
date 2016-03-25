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
  
  SEXP getFunctionCallSubset(FunctionCall call);

  SEXP getSingleListElement(ListVector source, boolean exact);

  AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact);
  
  Vector replaceListElements(ListVector list, Vector replacement);

  SEXP replaceAtomicVectorElements(Context context, AtomicVector source, Vector replacements);
  
  ListVector replaceSingleListElement(ListVector list, SEXP replacement);
  
  SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement);

  Vector replaceSingleElement(AtomicVector source, Vector replacement);
  
}
