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
 * Converts between JVM {@code long} scalars and R {@code double} vectors
 *
 * <p>The {@code long} values are wrapped in {@link LongArrayVector}, a special subclass
 * of {@code DoubleArrayVector} that exposes values as 64-bit floating point values, which
 * still retaining the original long values. This way they can be appropriately unwrapped if
 * passed back into the JVM.</p>
 */
public class LongConverter extends PrimitiveScalarConverter<Number> {

  public static final Converter INSTANCE = new LongConverter();
  
  private LongConverter() {
  }

  @Override
  public SEXP convertToR(Number value) {
    if(value == null) {  
      return new DoubleArrayVector(DoubleVector.NA);
    } else {
      return new LongArrayVector(value.longValue());
    }
  }

  public static boolean accept(Class clazz) {
    return 
        clazz == Long.TYPE || clazz == Long.class;
  }

  @Override
  public Vector.Type getVectorType() {
    return DoubleVector.VECTOR_TYPE;
  }

  @Override
  protected Object getFirstElement(Vector value) {
    if(value instanceof LongArrayVector) {
      return ((LongArrayVector) value).getElementAsLong(0);
    } else {
      return (long)value.getElementAsDouble(0);
    }
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
