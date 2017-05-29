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

import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.lang.reflect.Array;


/**
 * Converts between {@code double[]} and {@link DoubleArrayVector}s
 */
public class DoubleArrayConverter implements Converter<Object> {

  public static final DoubleArrayConverter DOUBLE_ARRAY = new DoubleArrayConverter(double.class);

  private final Class componentClass;

  protected DoubleArrayConverter(Class clazz) {
    componentClass = clazz;
  }

  public boolean accept(Class clazz) {
    return clazz.isArray() && clazz.getComponentType().equals(componentClass);
  }

  @Override
  public final DoubleVector convertToR(Object value) {
    if (value == null) {
      return new DoubleArrayVector(DoubleVector.NA);
    } else {
      double dArray[] = new double[Array.getLength(value)];
      for (int i = 0; i < Array.getLength(value); i++) {
        dArray[i] = ((Number)Array.get(value, i)).doubleValue();
      }
      return new DoubleArrayVector(dArray);
    }
  }

  @Override
  public final boolean acceptsSEXP(SEXP exp) {
    return  exp instanceof DoubleVector ||
            exp instanceof IntVector ||
            exp instanceof LogicalVector;
  }

  @Override
  public final int getSpecificity() {
    return Specificity.DOUBLE;
  }

  @Override
  public final Object convertToJava(SEXP value) {
    if(!(value instanceof AtomicVector)) {
      throw new EvalException("It's not an AtomicVector", value.getTypeName());
    }
    return convertToJavaArray((AtomicVector) value);
  }

  protected Object convertToJavaArray(AtomicVector vector) {
    return vector.toDoubleArray();
  }
}
