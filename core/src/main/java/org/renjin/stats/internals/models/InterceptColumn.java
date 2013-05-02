package org.renjin.stats.internals.models;

public class InterceptColumn implements ModelMatrixColumn {

  @Override
  public String getName() {
    return "(Intercept)";
  }

  @Override
  public double getValue(int observationIndex) {
    return 1;
  }

}
