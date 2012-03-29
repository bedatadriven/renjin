package org.renjin.primitives.match;

import org.renjin.sexp.Vector;

import com.google.common.collect.UnmodifiableIterator;

/**
 * Iterates over the indexes of a vector, from the end of the 
 * vector (length()-1) to the beginning (0)
 */
class ReverseIndexIterator extends UnmodifiableIterator<Integer> {
  private int index;
  
  public ReverseIndexIterator(Vector vector) {
    this.index = vector.length()-1;
  }

  @Override
  public boolean hasNext() {
    return index >= 0;
  }

  @Override
  public Integer next() {
    return index--;
  }
}