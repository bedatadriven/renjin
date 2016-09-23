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
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * The contrast matrix specifies how a factor is to be coded into the 
 * model matrix. A single factor with multiple levels will generally 
 * end up coded as several "dummy" variables according to one of several
 * schemes. 
 * 
 * <p>In the simplest example, consider a factor with three levels, "Good", 
 * "Bad", and "Ugly". The simplest contrast matrix will look like:</p>
 * 
 * <pre>
 *       Bad  Ugly
 * Good    0     0  
 * Bad     1     0
 * Ugly    0     1
 * </pre>
 * 
 * <p>This matrix basically says that the factor should be encoded into two 
 * dummy variables (num columns = 2). Furthermore, if the value of the factor is Bad, 
 * then the value of both dummy variables should be zero, if it's bad, then the first
 * variable is coded as one and the second as zero, etc.</p>
 * 
 */
public class ContrastMatrix {

  private Matrix matrix;
  
  public ContrastMatrix(SEXP matrix) {
    this.matrix = new Matrix((Vector) matrix);
  }

  public int getEncoding(int value, int dummyVariableIndex) {
    return matrix.getElementAsInt(value, dummyVariableIndex);
  }

  public int getNumDummyVariables() {
    return matrix.getNumCols();
  }

  public String getDummyVariableName(int i) {
    return matrix.getColName(i);
  }
}
