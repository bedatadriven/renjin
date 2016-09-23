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
package org.renjin.invoke.reflection.converters;

import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * Converter between a JVM {@code int} scalar value and the
 */
public class IntegerConverter extends PrimitiveScalarConverter<Number> {

  public static final IntegerConverter INSTANCE = new IntegerConverter();
  
  private IntegerConverter() {
  }

  @Override
  public SEXP convertToR(Number value) {
    if(value == null) {
      return new IntArrayVector(IntVector.NA);
    } else {
      return new IntArrayVector(value.intValue());
    }
  }

  public static boolean accept(Class clazz) {
    return 
        clazz == Integer.TYPE || clazz == Integer.class ||
        clazz == Short.TYPE || clazz == Short.class;
  }

  @Override
  public Vector.Type getVectorType() {
    return IntVector.VECTOR_TYPE;
  }

  @Override
  protected Object getFirstElement(Vector value) {
    return value.getElementAsInt(0);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof IntVector;// ||exp instanceof DoubleVector ;
//           exp instanceof LogicalVector;
  }

  @Override
  public int getSpecificity() {
    return Specificity.INTEGER;
  }
}
