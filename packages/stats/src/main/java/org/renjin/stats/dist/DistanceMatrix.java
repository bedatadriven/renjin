package org.renjin.stats.dist;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * This view wraps the result of the distance matrix, which is stored as the lower
 * triangle of the matrix, minus the diagonal. We normally try to avoid using this
 * view as it's another layer of indirection, but we do
 */
public class DistanceMatrix extends DoubleVector implements DeferredComputation {
  
  private Vector triangle;
  private int size;

  public DistanceMatrix(Vector triangle, AttributeMap attributes) {
    super(attributes);
    this.triangle = triangle;
    
    // the length L of the triangle vector of the distance matrix as a function
    // of the size of the distance matrix M is: 
    // L = M (M-1) / 2
    
    // so given L, we can find the size of the original distance matrix as:
    // 0 = 1/2 M^2 - 1/2 M + L
    
    // so we can solve for M using the quadratic formula:
    size = (int)(0.5 + Math.sqrt(0.25 - 2 * triangle.length()));
    
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { triangle };
  }

  @Override
  public String getComputationName() {
    return "dist";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DistanceMatrix(triangle, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    throw new UnsupportedOperationException();
  }
  
  private int triangleIndex(int row, int column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isConstantAccessTime() {
    return triangle.isConstantAccessTime();
  }

  @Override
  public int length() {
    return size*size;
  }
}
