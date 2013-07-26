package org.renjin.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class PrimitiveIterators {

  private static final EmptyIterator EMPTY = new EmptyIterator();

  public static IntIterator emptyIterator() {
    return EMPTY;
  }


  private static class EmptyIterator implements IntIterator {

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public int nextInt() {
      throw new NoSuchElementException();
    }
  }
}
