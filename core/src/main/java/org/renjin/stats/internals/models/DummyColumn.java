/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
