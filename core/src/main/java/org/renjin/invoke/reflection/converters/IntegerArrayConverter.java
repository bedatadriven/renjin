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

import org.renjin.eval.EvalException;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

import java.lang.reflect.Array;


/**
 * Converts between JVM {@code int[]} arrays and R {@code integer} vectors
 */
public class IntegerArrayConverter implements Converter<Object> {

  public static final IntegerArrayConverter INSTANCE = new IntegerArrayConverter();

  private IntegerArrayConverter() {
  }

  @Override
  public SEXP convertToR(Object value) {
    if (value == null) {
      return new IntArrayVector(IntArrayVector.NA);
    } else {
      int iArray[] = new int[Array.getLength(value)];
      for (int i = 0; i < Array.getLength(value); i++) {
        iArray[i] = ((Number)Array.get(value, i)).intValue();
      }
      return new IntArrayVector(iArray);
    }
  }

  public static boolean accept(Class clazz) {
    Class iclazz = clazz.getComponentType();
    return clazz.isArray()
        && (iclazz == Integer.TYPE || iclazz == Integer.class
            || iclazz == Short.TYPE || iclazz == Short.class);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return  exp instanceof IntVector;
//        || exp instanceof LogicalVector;exp instanceof DoubleVector ||
  }

  @Override
  public int getSpecificity() {
    return Specificity.INTEGER;
  }

  @Override
  public Object convertToJava(SEXP value) {  
    if(!(value instanceof AtomicVector)) {
      throw new EvalException("It's not an AtomicVector", value.getTypeName());
    } else if(value.length() < 1) {
      //to keep its type info
      return new Integer[0];
    }
    IntVector lv= (IntVector)value;
    int length = lv.length();
    Integer[] values = new Integer[length];
    for(int i=0;i<length;i++){
      values[i]= lv.getElementAsObject(i);
    }
    return values;
  }
}
