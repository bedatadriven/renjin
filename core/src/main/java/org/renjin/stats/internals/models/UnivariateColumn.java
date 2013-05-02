package org.renjin.stats.internals.models;

import org.renjin.sexp.Vector;

/** 
 * Model matrix column for a single univariate variable.
 */
public class UnivariateColumn implements ModelMatrixColumn {

  private final String name;
  private final Vector vector;
  
  public UnivariateColumn(String name, Vector vector) {
    super();
    this.name = name;
    this.vector = vector;
  }

  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public double getValue(int observationIndex) {
    return vector.getElementAsDouble(observationIndex);
  }


}
