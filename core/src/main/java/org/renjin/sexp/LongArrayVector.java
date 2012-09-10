package org.renjin.sexp;

/**
 * Wrapper for 64-bit integer values. 
 */
public class LongArrayVector extends DoubleVector {

  private long[] values;
  
  public LongArrayVector(long value) {
    super();
    this.values = new long[] { value };
  }
  
  private LongArrayVector(long[] values, AttributeMap attributes) {
    super(attributes);
    this.values = values;
  }

  @Override
  public int length() {
    return values.length;
  }

  @Override
  public int getElementAsInt(int i) {
    long value = values[i];
    if(value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
      return IntVector.NA;
    } 
    return (int)value;
  }
  
  @Override
  public double getElementAsDouble(int index) {
    return (double)values[index];
  }

  @Override
  public String getElementAsString(int index) {
    return Long.toString(values[index]);
  }
 
  public long getElementAsLong(int index) {
    return values[index];
  }
  
  @Override
  public boolean isElementNA(int index) {
    return false;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new LongArrayVector(this.values, attributes);
  }

}
