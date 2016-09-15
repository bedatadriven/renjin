package org.renjin.primitives.ni;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;


public class NativeOutputBoolVector extends LogicalVector implements NativeOutputVector {

  private final DeferredNativeCall call;
  private final int outputIndex;
  private final int length;

  private boolean[] array;

  public NativeOutputBoolVector(DeferredNativeCall call, int outputIndex, int length, AttributeMap attributes) {
    super(attributes);
    this.call = call;
    this.outputIndex = outputIndex;
    this.length = length;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new NativeOutputBoolVector(call, outputIndex, length, attributes);
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    if(array == null) {
      array = (boolean[]) call.output(outputIndex);
    }
    return array[index] ? 1: 0;
  }

  @Override
  public boolean isConstantAccessTime() {
    return call.isEvaluated();
  }

  @Override
  public boolean isDeferred() {
    return !call.isEvaluated();
  }

  @Override
  public DeferredNativeCall getCall() {
    return call;
  }

  @Override
  public int getOutputIndex() {
    return outputIndex;
  }
}
