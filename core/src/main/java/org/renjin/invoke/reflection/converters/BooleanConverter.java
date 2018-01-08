/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.invoke.reflection.converters;

import org.renjin.sexp.*;

/**
 * Converts between scalar {@code boolean} values and {@code logical} vectors
 */
public class BooleanConverter extends PrimitiveScalarConverter<Boolean> {

  public static final BooleanConverter INSTANCE = new BooleanConverter();

  public static boolean accept(Class clazz) {
    return clazz == Boolean.TYPE || clazz == Boolean.class;
  }
  
  @Override
  public LogicalVector convertToR(Boolean value) {
    if(value == null) {
      return new LogicalArrayVector(LogicalVector.NA);
    } else {
      return new LogicalArrayVector(value);
    }
  }

  @Override
  public Vector.Type getVectorType() {
    return LogicalVector.VECTOR_TYPE;
  }

  @Override
  protected Object getFirstElement(Vector vector) {
    return vector.getElementAsLogical(0) != Logical.FALSE;
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof LogicalVector;
  }

  @Override
  public int getSpecificity() {
    return Specificity.BOOLEAN;
  }
}
