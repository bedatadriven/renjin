package org.renjin.primitives.subset;

/**
 * Iterator over a sequence of integer indices.
 */
interface IndexIterator {
  
  int EOF = -1;

  /**
   * 
   * Returns the next index in the sequence, and advances the iterator to next element. 
   * 
   * @return the next zero-based index in this sequence or {@code EOF} if there are no more elements.
   */
  int next();

  /**
   * Resets the position in the sequence to the beginning. Any subsequent call to {@link #next()} will return 
   * the first index in the sequence.
   */
  void restart();
}
