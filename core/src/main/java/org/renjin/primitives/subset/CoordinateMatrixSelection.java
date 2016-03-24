package org.renjin.primitives.subset;

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

/**
 * Selects elements from an array or matrix using a matrix of coordinates. 
 * 
 * <p>In this case, if you have a matrix {@code x}:</p>
 * <pre>
 *       [,1] [,2] [,3] [,4]
 *  [1,]    1    4    7   10
 *  [2,]    2    5    8   11
 *  [3,]    3    6    9   12
 * </pre>
 * 
 * and another matrix {@code i}:
 * <pre>
 *       [,1] [,2]
 *  [1,]    3    2
 *  [2,]    3    4
 *  [3,]    1    1
 * </pre>
 * 
 * <p>When {@code x[i]} is evaluated, each row in {@code i} is treated as the coordinates
 * of an element to select in {@code x}, and we return the elements at (3,2), (3,4), and (1,1) or
 * [6, 12, 1].</p>
 * 
 * <p>The resulting vector has no dimensions or names.</p>
 * 
 * <p>Coordinate matrix selection can <em>only</em> be used with the {@code [} and {@code [<-} operator.
 * In the context of the {@code [[} or {@code [[<-} operators, it is treated like any other numeric subscript.</p>
 * 
 */
class CoordinateMatrixSelection implements SelectionStrategy {


  private final int numCoordinates;

  public static boolean isCoordinateMatrix(SEXP source, SEXP subscript) {

    if(!(subscript instanceof IntVector) &&
        !(subscript instanceof DoubleVector)) {
      return false;
    }

    Vector subscriptDim = subscript.getAttributes().getDim();
    if(subscriptDim.length() != 2) {
      return false;
    }

    // now check that the columns in the subscript match the number of
    // dimensions in the source.

    SEXP sourceDim = source.getAttribute(Symbols.DIM);
    return sourceDim.length() == subscriptDim.getElementAsInt(1);
  }
  
  private AtomicVector matrix;
  private int[] matrixDims;
  
  
  public CoordinateMatrixSelection(AtomicVector matrix) {
    this.matrix = matrix;
    this.matrixDims = matrix.getAttributes().getDimArray();

    numCoordinates = matrixDims[0];
  }

  @Override
  public SEXP getVectorSubset(Vector source, boolean drop) {

    CoordinateMatrixIterator it = new CoordinateMatrixIterator(source, matrix);

    Vector.Builder result = source.getVectorType().newBuilderWithInitialCapacity(numCoordinates);
    
    int index;
    while((index=it.next())!= IndexIterator.EOF) {
      
      if(IntVector.isNA(index)) {
        result.addNA();
      
      } else if(index >= source.length()) {
        throw new EvalException("subscript out of bounds");
      
      } else {
        result.addFrom(source, index);
      }
    }
    return result.build();
  }

  @Override
  public SEXP getFunctionCallSubset(FunctionCall call) {
    throw new IllegalStateException("lang objects should NOT have dim attributes.");
  }

  @Override
  public Vector replaceListElements(ListVector source, Vector replacement) {
    return replaceElements(source, replacement);
  }

  @Override
  public Vector replaceAtomicVectorElements(AtomicVector source, Vector replacements) {
    return replaceElements(source, replacements);
  }

  private Vector replaceElements(Vector source, Vector replacements) {
    CoordinateMatrixIterator it = new CoordinateMatrixIterator(source, matrix);

    Vector.Builder result = source.newCopyBuilder(replacements.getVectorType());

    int replacementIndex = 0;

    int index;
    while((index=it.next())!= IndexIterator.EOF) {

      if(IntVector.isNA(index)) {
        throw new EvalException("NAs are not allowed in subscripted assignments");

      } else if(index >= source.length()) {
        throw new EvalException("subscript out of bounds");

      } else {
        result.setFrom(index, source, replacementIndex++);
        if(replacementIndex >= replacements.length()) {
          replacementIndex = 0;
        }
      }
    }

    if(replacementIndex != 0) {
      throw new EvalException("number of items to replace is not a multiple of replacement length");
    }

    return result.build();
  }
  
  /*
   * Subscripts are NEVER interpreted as coordinate matrixes in the context
   * of the [[ operator. The getSingleXX methods should never be called because
   * Selections.parseSingleSelection() should never return an instance of CoordinateMatrixSelection
   */

  @Override
  public SEXP getSingleListElement(ListVector source, boolean exact) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AtomicVector getSingleAtomicVectorElement(AtomicVector source, boolean exact) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ListVector replaceSingleListElement(ListVector list, SEXP replacement) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SEXP replaceSinglePairListElement(PairList.Node list, SEXP replacement) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Vector replaceSingleElement(AtomicVector source, Vector replacement) {
    throw new UnsupportedOperationException();
  }
}
