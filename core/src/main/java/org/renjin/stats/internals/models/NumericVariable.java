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

import org.renjin.primitives.matrix.Matrix;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

import java.util.Collections;
import java.util.List;

public class NumericVariable extends Variable {

  private final String name;
  private final Vector vector;
  private final int numColumns;
  
  public NumericVariable(String name, SEXP vector) {
    this.name = name;
    this.vector = (Vector)vector;
   
    Vector dim = vector.getAttributes().getDim();
    if(dim.length() < 2) {
      numColumns = 1;
    } else if(dim.length() == 2) {
      numColumns = dim.getElementAsInt(1);
    } else {
      throw new UnsupportedOperationException("variable " + name + " has " + dim.length() + 
          " dimensions, don't know what to do");
    }
  }

  @Override
  public List<? extends ModelMatrixColumn> getModelMatrixColumns() {
    if(numColumns == 1) {
      return Collections.singletonList(new UnivariateColumn(name, vector));
    } else {
      List<ModelMatrixColumn> columns = Lists.newArrayList();
      for(int i=0;i!=numColumns;++i) {
        columns.add(new MultivariateColumn(name + "." + (i+1), new Matrix(vector, numColumns), i));
      }
      return columns;
    }
  }
}
