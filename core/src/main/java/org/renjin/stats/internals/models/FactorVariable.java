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

import org.renjin.eval.EvalException;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;

import java.util.List;


public class FactorVariable extends Variable {

  private final String name;
  private final Vector vector;
  private ContrastMatrix contrastMatrix;
  
  public FactorVariable(String name, SEXP vector) {
    this.name = name;
    this.vector = (Vector)vector;
    
    SEXP contrasts = vector.getAttribute(Symbol.get("contrasts"));
    if(contrasts != Null.INSTANCE) {
      contrastMatrix = new ContrastMatrix(contrasts);
    } else {
      throw new EvalException("Invalid contrast matrix for " + name);
    }
  }

  @Override
  public List<ModelMatrixColumn> getModelMatrixColumns() {
    List<ModelMatrixColumn> columns = Lists.newArrayList();
    for(int i=0;i!=contrastMatrix.getNumDummyVariables();++i) {
      columns.add(new DummyColumn(
          name + contrastMatrix.getDummyVariableName(i), 
          vector, contrastMatrix, i));
    }
    return columns;
  }
}
