package org.renjin.stats.internals.models;

import org.renjin.primitives.matrix.Matrix;

/**
 * Column containing the values of 
 *
 */
public class MultivariateColumn implements ModelMatrixColumn {

  private final String name;
  private final Matrix matrix;
  private final int columnIndex;
  
  public MultivariateColumn(String name, Matrix matrix, int columnIndex) {
    super();
    this.name = name;
    this.matrix = matrix;
    this.columnIndex = columnIndex;
  }

  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public double getValue(int observationIndex) {
    return matrix.getElementAsDouble(observationIndex, columnIndex);
  }
}
