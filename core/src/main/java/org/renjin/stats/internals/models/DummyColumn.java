package org.renjin.stats.internals.models;

import org.renjin.sexp.Vector;

/**
 * Dummy model matrix column encoding a factor.
 *
 */
public class DummyColumn implements ModelMatrixColumn {

  private String name;
  private Vector variable;
  private ContrastMatrix contrastMatrix;
  private int dummyVariableIndex;

  public DummyColumn(String name, Vector variable,
      ContrastMatrix contrastMatrix, int dummyVariableIndex) {
    super();
    this.name = name;
    this.variable = variable;
    this.contrastMatrix = contrastMatrix;
    this.dummyVariableIndex = dummyVariableIndex;
  }

  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public double getValue(int observationIndex) {
    int value = variable.getElementAsInt(observationIndex);
    int coding = contrastMatrix.getEncoding(value-1, dummyVariableIndex);
    return coding;
  }
}
