package org.renjin.gcc.runtime;


public class DoubleArrayPointer extends Pointer {
  private final double[] array;
  private final int index;

  public DoubleArrayPointer(double... array) {
    this.array = array;
    this.index = 0;
  }

  public DoubleArrayPointer(int index, double[] array) {
    this.index = index;
    this.array = array;
  }

  @Override
  public Pointer plus(int count) {
    assert count % 8 == 0;
    return new DoubleArrayPointer(index+(count/8), array);
  }

  @Override
  public double asDouble() {
    return array[index];
  }
}
