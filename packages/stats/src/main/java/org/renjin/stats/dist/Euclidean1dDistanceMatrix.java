package org.renjin.stats.dist;

import org.renjin.invoke.annotations.Operand;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class Euclidean1dDistanceMatrix extends DoubleVector implements DeferredComputation {

  @Operand
  private Vector vector;
  private int size; // size of matrix
  private int length;

  public Euclidean1dDistanceMatrix(Vector vector, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
    this.size = vector.length();
    this.length = size * size;
  }

  public Euclidean1dDistanceMatrix(Vector vector) {
    this(vector, AttributeMap.dim(vector.length(), vector.length()));
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new Euclidean1dDistanceMatrix(vector, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    int size = vector.length();
    int row = index % size;
    int col = index / size;
    if(row == col) {
      return 0;
    } else {
      double x = getOperandElementAsDouble(row);
      double y = getOperandElementAsDouble(col);
      return Math.abs(x - y);
    }
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public boolean isDeferred() {
    return true;
  }

  public static double test(int index, int size) {
    return Math.sqrt(index % size);
  }

  private double getOperandElementAsDouble(int row) {
    return vector.getElementAsDouble(row);
  }

  @Override
  public int length() {
    return getOperandLength() * getOperandLength();
  }

  private int getOperandLength() {
    return vector.length();
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { vector };
  }

  @Override
  public String getComputationName() {
    return "dist";
  }
}
