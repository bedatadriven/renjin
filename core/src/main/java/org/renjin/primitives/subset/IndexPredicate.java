package org.renjin.primitives.subset;

/**
 * Predicate interface, specialized for int primitives
 */
public interface IndexPredicate {
  
  boolean apply(int index);
}
