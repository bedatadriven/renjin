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
