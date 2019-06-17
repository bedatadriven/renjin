package org.renjin.gcc.format;

import org.renjin.gcc.runtime.Ptr;

public class ByteCharIterator implements CharIterator {

  private final Ptr input;
  private int index;

  public ByteCharIterator(Ptr input) {
    this.input = input;
  }

  @Override
  public boolean hasMore() {
    return input.getByte(index) != 0;
  }

  @Override
  public char peek() {
    return (char)input.getByte(index);
  }

  @Override
  public char next() {
    return (char)input.getByte(index++);
  }
}
