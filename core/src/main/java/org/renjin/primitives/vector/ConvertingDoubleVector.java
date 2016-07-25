package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;

public class ConvertingDoubleVector extends DoubleVector implements DeferredComputation {

  private final Vector operand;

  public ConvertingDoubleVector(Vector operand, AttributeMap attributes) {
    super(attributes);
    this.operand = operand;
  }

  public ConvertingDoubleVector(Vector operand) {
    this(operand, AttributeMap.EMPTY);
  }

  @Override
  public int length() {
    return operand.length();
  }

  @Override
  protected DoubleVector cloneWithNewAttributes(AttributeMap attributes) {
    return new ConvertingDoubleVector(operand, attributes);
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] {operand};
  }

  @Override
  public String getComputationName() {
    return "as.double";
  }

  @Override
  public double getElementAsDouble(int index) {
    return operand.getElementAsDouble(index);
  }
  
  @Override
  public boolean isConstantAccessTime() {
    return operand.isConstantAccessTime();
  }

  @Override
  public boolean isDeferred() {
    return true;
  }

  public static double compute(double x) {
    return x;
  }
}
