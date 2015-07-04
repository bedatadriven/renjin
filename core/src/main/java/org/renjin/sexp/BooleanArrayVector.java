package org.renjin.sexp;

public class BooleanArrayVector extends LogicalVector {
  
  private boolean array[];

  private BooleanArrayVector(AttributeMap attributes) {
    super(attributes);
  }

  public BooleanArrayVector(boolean[] array, AttributeMap attributes) {
    super(attributes);
    this.array = array;
  }

  @Override
  public int length() {
    return array.length;
  }

  @Override
  public boolean isElementNA(int index) {
    return false;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return array[index] ? 1 : 0;
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new BooleanArrayVector(array, attributes);
  }
  
  public static BooleanArrayVector unsafe(boolean[] array) {
    BooleanArrayVector vector = new BooleanArrayVector(AttributeMap.EMPTY);
    vector.array = array;
    return vector;
  }
}
