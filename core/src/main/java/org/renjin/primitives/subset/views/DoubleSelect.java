package org.renjin.primitives.subset.views;

import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class DoubleSelect extends DoubleVector implements DeferredComputation {
  
  private Vector source;
  private Vector sourceIndexVector;

  public DoubleSelect(Vector source, Vector sourceIndexVector, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.sourceIndexVector = sourceIndexVector;
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] {source, sourceIndexVector};
  }

  @Override
  public String getComputationName() {
    return "subset";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new DoubleSelect(source, sourceIndexVector, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    int sourceIndex = sourceIndexVector.getElementAsInt(index);
    return source.getElementAsDouble(sourceIndex);
  }

  @Override
  public boolean isConstantAccessTime() {
    return source.isConstantAccessTime() && sourceIndexVector.isConstantAccessTime();
  }

  @Override
  public int length() {
    return sourceIndexVector.length();
  }
}
