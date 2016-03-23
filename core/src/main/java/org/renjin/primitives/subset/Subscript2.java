package org.renjin.primitives.subset;


public interface Subscript2 {


  /**
   * Computes the single index selected by this subscript.
   * 
   * @throws org.renjin.eval.EvalException if this subscript selects less or more than one element.
   * @return zero-based index
   */
  int computeUniqueIndex();


  /**
   * Computes the sequence of indices to replace. The sequence will not
   * contain {@code NA}s
   * 
   * @return an iterator over the sequence.
   */
  IndexIterator2 computeIndexes();

  
  IndexPredicate computeIndexPredicate();

}
