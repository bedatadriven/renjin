/*
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
 * Converts between JVM {@code double} scalar values and R {@code double} vectors
 */
public class DoubleConverter extends PrimitiveScalarConverter<Number> {

  public static final Converter INSTANCE = new DoubleConverter();
  
  private DoubleConverter() {
  }

  @Override
  public SEXP convertToR(Number value) {
    if(value == null) {  
      return new DoubleArrayVector(DoubleVector.NA);
    } else {
      return new DoubleArrayVector(value.doubleValue());
    }
  }

  public static boolean accept(Class clazz) {
    return 
        clazz == Double.TYPE || clazz == Double.class ||
        clazz == Float.TYPE || clazz == Float.class ||
        clazz == Long.TYPE || clazz == Long.class;
  }

  @Override
  public Vector.Type getVectorType() {
    return DoubleVector.VECTOR_TYPE;
  }

  @Override
  protected Object getFirstElement(Vector value) {
    return value.getElementAsDouble(0);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof DoubleVector || exp instanceof IntVector ||
           exp instanceof LogicalVector;
  }

  @Override
  public int getSpecificity() {
    return Specificity.DOUBLE;
  }
}
