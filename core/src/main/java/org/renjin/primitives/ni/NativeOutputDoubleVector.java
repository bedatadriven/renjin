package org.renjin.primitives.ni;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.SEXP;

public class NativeOutputDoubleVector extends DoubleVector implements NativeOutputVector {

  private final DeferredNativeCall call;
  private final int outputIndex;
  private final int length;

  private double[] array;
  
  public NativeOutputDoubleVector(DeferredNativeCall call, int outputIndex, int length, AttributeMap attributes) {
    super(attributes);
    this.call = call;
    this.outputIndex = outputIndex;
    this.length = length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new NativeOutputDoubleVector(call, outputIndex, length, attributes);
  }

  @Override
  public int length() {
    return length;
  }
  
  @Override
  public double getElementAsDouble(int index) {
    if(array == null) {
      array = (double[])call.output(outputIndex);
    }
    return array[index];
  }

  @Override
  public boolean isConstantAccessTime() {
    return call.isEvaluated();
  }

  @Override
  public boolean isDeferred() {
    return !call.isEvaluated();
  }
}
