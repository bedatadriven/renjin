package org.renjin.primitives.subset;

/**
 * Created by alex on 22-3-16.
 */
public enum  EmptyIndexIterator2 implements IndexIterator2 {
  
  INSTANCE;
  
  @Override
  public int next() {
    return EOF;
  }
}
