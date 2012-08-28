package org.renjin.primitives.vector;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * Decorates an existing vector with new attributes.
 *
 */
public class AttributeDecoratingVector extends DoubleVector implements DeferredComputation {

  private final Vector vector;

  public AttributeDecoratingVector(Vector vector, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new AttributeDecoratingVector(vector, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    return vector.getElementAsDouble(index);
  }

  @Override
  public int length() {
    return vector.length();
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { vector };
  }

  @Override
  public String getComputationName() {
    return "attr";
  }
}
