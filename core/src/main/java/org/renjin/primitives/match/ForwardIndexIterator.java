package org.renjin.primitives.match;

import org.renjin.sexp.Vector;

import com.google.common.collect.UnmodifiableIterator;

/**
 * Iterates over the indexes of a vector, from the beginning of the 
 * vector (0) to the last element (length()-1)
 */
class ForwardIndexIterator extends UnmodifiableIterator<Integer> {
  private int index;
  private int size;
  
  public ForwardIndexIterator(Vector vector) {
    super();
    this.index = 0;
    this.size = vector.length();
  }

  @Override
  public boolean hasNext() {
    return index < size;
  }

  @Override
  public Integer next() {
    return index++;
  }
}