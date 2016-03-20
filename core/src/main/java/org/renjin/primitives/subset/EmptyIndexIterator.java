package org.renjin.primitives.subset;

import java.util.NoSuchElementException;

public class EmptyIndexIterator implements IndexIterator {
  
  public static final EmptyIndexIterator INSTANCE = new EmptyIndexIterator();
  
  private EmptyIndexIterator() {}
  
  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public int next() {
    throw new NoSuchElementException();
  }
}
