package org.renjin.primitives.summary;

import org.renjin.compiler.pipeline.DeferredGraph;
import org.renjin.compiler.pipeline.VectorPipeliner;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.primitives.vector.MemoizedComputation;
import org.renjin.sexp.*;

public class DeferredMean extends DoubleVector implements MemoizedComputation {
  private final Vector vector;
  private double result;
  private boolean calculated = false;

  public DeferredMean(Vector vector, AttributeMap attributes) {
    super(attributes);
    this.vector = vector;
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[]  { vector };
  }

  @Override
  public String getComputationName() {
    return "mean";
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getElementAsDouble(int index) {
    if(index != 0) {
      throw new IllegalArgumentException("index: " + index);
    }
    if(!calculated) {
        calculate();
      }
    return result;
  }

  @Override
  public boolean isConstantAccessTime() {
    return false;
  }

  @Override
  public boolean isCalculated() {
    return calculated;
  }

  private void calculate() {
    System.err.println("EEK! mean.calculate() called directly");
    double sum = 0;
    for(int i=0;i!=vector.length();++i) {
      sum += vector.getElementAsDouble(i);
    }
    result = sum / vector.length();
    calculated = true;
  }

  @Override
  public int length() {
    return 1;
  }

  @Override
  public Vector forceResult() {
    if(!calculated) {
      calculate();
    }
    return new DoubleArrayVector(result);
  }

  @Override
  public void setResult(Vector result) {
    this.result = result.getElementAsDouble(0);
    this.calculated = true;
  }
}
