package org.renjin.primitives.subset;

/**
 * Iterator interface, specialized for primitivate, unboxed integers.
 */
public interface IndexIterator {
  
  boolean hasNext();
  
  int next();

}
