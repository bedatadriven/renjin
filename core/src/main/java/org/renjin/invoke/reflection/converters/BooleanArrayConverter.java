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
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;


/**
 * Converts between {@code boolean[]} and R {@code logical} vectors
 */
public class BooleanArrayConverter implements Converter<Boolean[]> {

  public static final BooleanArrayConverter INSTANCE = new BooleanArrayConverter();

  public static boolean accept(Class clazz) {
    return clazz.isArray() &&( clazz.getComponentType() == Boolean.class||clazz.getComponentType()== Boolean.TYPE);
  }
  
  @Override
  public LogicalVector convertToR(Boolean[] value) {
    if(value == null) {
      return new LogicalArrayVector(LogicalVector.NA);
    } else {
      return new LogicalArrayVector(value);
    }
  }
  
  @Override
  public Object convertToJava(SEXP value) {  
    if(!(value instanceof AtomicVector)) {
      throw new EvalException("It's not an AtomicVector", value.getTypeName());
    } else if(value.length() < 1) {
      //to keep its type info
      return new Boolean[0];
    }
    LogicalVector lv= (LogicalVector)value;
    int length = lv.length();
    Boolean[] values = new Boolean[length];
    for(int i=0;i<length;i++){
      values[i]= lv.getElementAsObject(i);
    }
    return values;
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof LogicalVector && exp.length() >= 1;
  }

  @Override
  public int getSpecificity() {
    return Specificity.SPECIFIC_OBJECT;
  }
}
