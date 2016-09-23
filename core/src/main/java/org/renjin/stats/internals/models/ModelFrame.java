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
import org.renjin.primitives.Types;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import java.util.List;

/**
 * Encapsulates a model frame object. 
 * 
 * <p>A model frame is an R object of class "data.frame" that is created by
 * the {@code model.frame} function and contains a column for every unique
 * variable that is referenced by the model formula.
 */
public class ModelFrame {
  
  private final ListVector frame;
  private final int numRows;
  private final List<Variable> variables;
  
  public ModelFrame(ListVector frame) {
    this.frame = frame;
    if(frame.length() == 0) {
      throw new EvalException("do not know how many cases");
    }
    this.numRows = Models.nrows(frame.getElementAsSEXP(0));
    
    variables = Lists.newArrayList();
    for(int i=0; i!=frame.length(); ++i) {
      SEXP vector = frame.getElementAsSEXP(i);
      if(Models.nrows(vector) != numRows) {
        throw new EvalException("variable lengths differ");
      }
      variables.add(createVariable(frame.getName(i), vector));
    }
  }
  
  private Variable createVariable(String name, SEXP vector) {
    if(Types.isFactor(vector)) {
      return new FactorVariable(name, vector);
    } else {
      return new NumericVariable(name, vector);
    }
  }

  public int getNumRows() {
    return numRows;
  }
  
  public Variable getVariable(int index) {
    return variables.get(index);
  }
  
  public Vector getRowNames() {
    return (Vector)frame.getAttribute(Symbols.ROW_NAMES);
  }

  public static int ncols(SEXP s) {
    SEXP t;
    if (s instanceof Vector) {
      Vector dim = (Vector) s.getAttribute(Symbols.DIM);
      if(dim.length() >= 2) {
        return dim.getElementAsInt(1);
      } else {
        return 1;
      }
    } else if (s.inherits("data.frame")) {
      return s.length();
    } else {
      throw new EvalException("object is not a matrix");
    }
  }

  public static int nlevels(SEXP exp) {
    return exp.getAttribute(Symbols.LEVELS).length();
  }
}
