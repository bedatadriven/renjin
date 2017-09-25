package org.renjin.stats.dist;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * View of an underlying vector as a lower triangle of the distance matrix between
 * it's elements, using euclidean distance as a measure.
 */
public class Euclidean1dDistanceTriangle extends DoubleVector implements DeferredComputation {


  private Vector vector;
  private int length;
  private int size;
  
  public Euclidean1dDistanceTriangle(Vector vector, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
    this.size = vector.length();
    this.length = (size * (size - 1)) / 2;
  }
  
  public Vector getVector() {
    return vector;
  }
  
  @Override
  public Vector[] getOperands() {
    return new Vector[] { vector };
  }

  @Override
  public String getComputationName() {
    return "dist";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new Euclidean1dDistanceTriangle(vector, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {

    // In GNU R, the dist() function returns a vector containing the
    // lower triangle, without diagonal, of the distance matrix, laid out
    // in column major order. We need to unpack the row/column index
    // from this index in order to calculate the value.

    int colStart = 0;
    int col = 0;
    
    do {
      int colLength = (size-col-1);
      if(index < colStart+colLength) {
        int row = col + 1 + (index - colStart);
        return Math.abs(vector.getElementAsDouble(row) - vector.getElementAsDouble(col) );
      }
      colStart += colLength;
      col++;
    } while(true);

  }


  @Override
  public boolean isConstantAccessTime() {
    return vector.isConstantAccessTime();
  }
  
  @Override
  public boolean isDeferred() {
    return true;
  }

  @Override
  public int length() {
    return length;
  }
}
