package org.renjin.primitives.subset;

/**
 * Created by alex on 22-3-16.
 */
public interface IndexIterator2 {
  
  int EOF = -1;
  
  int next();
  
  void restart();
}
