package org.renjin.sexp;

import java.util.BitSet;

/**
 * Implementation of the LogicalVector that uses
 * a BitSet as a backing storage. 
 *
 */
public class LogicalBitSetVector extends LogicalVector {

  private final BitSet bitSet;
  private final int length;
  
  public LogicalBitSetVector(BitSet bitSet, int length, AttributeMap attributes) {
    super(attributes);
    this.length = length;
    this.bitSet = (BitSet) bitSet.clone();
  }

  public LogicalBitSetVector(BitSet bitSet, int length) {
    this.bitSet = (BitSet) bitSet.clone();
    this.length = length;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public int getElementAsRawLogical(int index) {
    return bitSet.get(index) ? 1 : 0;
  }
  
  @Override
  public Logical getElementAsLogical(int index) {
    return bitSet.get(index) ? Logical.TRUE : Logical.FALSE;
  }

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    return new LogicalBitSetVector(this.bitSet, length, attributes);
  }
  
}
