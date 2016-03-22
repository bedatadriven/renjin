package org.renjin.primitives.subset;


public interface Subscript2 {
  int computeUniqueIndex();
  
  IndexIterator2 indexIterator();
  
  IndexPredicate computeIndexPredicate();
}
