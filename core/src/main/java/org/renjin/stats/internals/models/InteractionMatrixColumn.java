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

import java.util.List;

/**
 * Column containing the interaction between two or more 
 * variables.
 *
 */
public class InteractionMatrixColumn implements ModelMatrixColumn {

  private final ModelMatrixColumn[] variables;
  private final String name;
  
  public InteractionMatrixColumn(ModelMatrixColumn[] variables) {
    super();
    this.variables = variables;

    // compose the name of this column in the
    // form x:y:z
    StringBuilder name = new StringBuilder();
    for(ModelMatrixColumn variable : variables) {
      if(name.length() > 0) {
        name.append(":");
      }
      name.append(variable.getName());
    }
    this.name = name.toString();
  }
  
  public InteractionMatrixColumn(List<ModelMatrixColumn> parts) {
    this(parts.toArray(new ModelMatrixColumn[parts.size()]));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public double getValue(int observationIndex) {
    double value = 1;
    for(int i=0;i!=variables.length;++i) {
      value *= variables[i].getValue(observationIndex);
    }
    return value;
  }
}
