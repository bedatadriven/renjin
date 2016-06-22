package org.renjin.gcc.peephole;

/**
 * Matches and replaces a sequence of instructions 
 */
public interface PeepholeOptimization {

  /**
   * Attempts to match and replace a sequence of instructions at the current
   * position.
   * 
   * @param it node iterator
   * @return true if the optimization was applied.
   */
  boolean apply(NodeIt it);

}
