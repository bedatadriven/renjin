package org.renjin.primitives.subset;

/**
 * Iterator over an empty sequence of indices.
 */
enum EmptyIndexIterator implements IndexIterator {
  
  INSTANCE;
  
  @Override
  public int next() {
    return EOF;
  }

  @Override
  public void restart() {
    // NOOP
  }
}
