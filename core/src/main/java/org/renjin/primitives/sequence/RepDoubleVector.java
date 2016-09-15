package org.renjin.primitives.sequence;


import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

import java.util.Arrays;

public class RepDoubleVector extends DoubleVector implements DeferredComputation {

  public static final int LENGTH_THRESHOLD = 100;

  private final Vector source;
  private int length;
  private int each;

  public RepDoubleVector(Vector source, int length, int each, AttributeMap attributes) {
    super(attributes);
    this.source = source;
    this.length = length;
    this.each = each;
    if(this.length <= 0) {
      throw new IllegalArgumentException("length: " + length);
    }
  }
  
  private RepDoubleVector(double constant, int length) {
    super(AttributeMap.EMPTY);
    this.source = DoubleVector.valueOf(constant);
    this.length = length;
    this.each = 1;
  }

  public static DoubleVector createConstantVector(double constant, int length) {
    if (length <= 0) {
      return DoubleVector.EMPTY;
    }
    if (length < LENGTH_THRESHOLD) {
      double array[] = new double[length];
      Arrays.fill(array, constant);
      return new DoubleArrayVector(array);
    
    } else {
      return new RepDoubleVector(constant, length);
    }
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new RepDoubleVector(source, length, each, attributes);
  }

  @Override
  public double getElementAsDouble(int index) {
    return source.getElementAsDouble((index / each) % source.length());
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
  
  @Override
  public boolean isDeferred() {
    return true;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public Vector[] getOperands() {
    return new Vector[] { source, new IntArrayVector(length/each/source.length()), new IntArrayVector(each) };
  }

  @Override
  public String getComputationName() {
    return "rep";
  }
}
