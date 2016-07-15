package org.renjin.primitives.vector;

import org.renjin.sexp.*;

/**
 * Lazily-evaluated is.na()
 */
public class IsNaVector extends LogicalVector implements DeferredComputation {
  private final Vector vector;

  public IsNaVector(Vector vector) {
    super(buildAttributes(vector));
    this.vector = vector;
  }

  private static AttributeMap buildAttributes(Vector vector) {
    AttributeMap sourceAttributes = vector.getAttributes();
    return AttributeMap.builder()
        .addIfNotNull(sourceAttributes, Symbols.DIM)
        .addIfNotNull(sourceAttributes, Symbols.NAMES)
        .addIfNotNull(sourceAttributes, Symbols.DIMNAMES)
        .validateAndBuildFor(vector);
  }

  private IsNaVector(AttributeMap attributes, Vector vector) {
    super(attributes);
    this.vector = vector;
  }

  @Override
  public int length() {
    return vector.length();
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return vector.isElementNaN(index) ? 1 : 0;
  }

  @Override
  public boolean isConstantAccessTime() {
    return vector.isConstantAccessTime();
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new IsNaVector(attributes, vector);
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { vector };
  }

  @Override
  public String getComputationName() {
    return "is.na";
  }
}
