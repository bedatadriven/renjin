package org.renjin.primitives.ni;

import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;


public class NativeOutputIntVector extends IntVector implements NativeOutputVector {

  private DeferredNativeCall call;
  private final int outputIndex;
  private int length;
  
  private int[] array;

  public NativeOutputIntVector(DeferredNativeCall call, int outputIndex, int length, AttributeMap attributes) {
    super(attributes);
    this.call = call;
    this.outputIndex = outputIndex;
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }


  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new NativeOutputIntVector(call, outputIndex, length, attributes);
  }
  
  @Override
  public int getElementAsInt(int i) {
    if(array == null) {
      array = (int[])call.output(outputIndex);
    }
    return array[i];
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
