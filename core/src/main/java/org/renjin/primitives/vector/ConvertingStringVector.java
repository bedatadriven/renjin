package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

/**
 * A vector which converts its operand to Strings on the fly, so
 * that extra memory to does not need to be allocated.
 */
public class ConvertingStringVector extends StringVector implements DeferredComputation {

  private final Vector operand;

  public ConvertingStringVector(Vector operand, AttributeMap attributes) {
    super(attributes);
    this.operand = operand;
  }

  public ConvertingStringVector(Vector operand) {
    this(operand, AttributeMap.EMPTY);
  }

  @Override
  public String getElementAsString(int index) {
    return operand.getElementAsString(index);
  }

  @Override
  public int length() {
    return operand.length();
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new ConvertingStringVector(operand, attributes);
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] {operand};
  }

  @Override
  public String getComputationName() {
    return "as.character";
  }
}
