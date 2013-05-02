package org.renjin.stats.internals.models;

/**
 * Interface to the values in a single column of model matrix
 *
 */
public interface ModelMatrixColumn {
  
  String getName();

  double getValue(int observationIndex);
  
}
