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

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;

/**
 * Converts between {@code String[]} and R {@code character} vectors
 */
public class StringArrayConverter implements Converter<String[]>{

  public static final Converter INSTANCE = new StringArrayConverter();
  
  private StringArrayConverter() { }
  
  public static boolean accept(Class clazz) {
    return clazz.isArray() && clazz.getComponentType() == String.class;
  }
  
  @Override
  public SEXP convertToR(String[] value) {
    return new StringArrayVector(value);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof StringVector;
  }

  @Override
  public Object convertToJava(SEXP value) {
    AtomicVector vector = (AtomicVector)value;
    String[] array = new String[value.length()];
    for(int i=0;i!=value.length();++i) {
      array[i] = vector.getElementAsString(i);
    }
    return array;
  }

  @Override
  public int getSpecificity() {
    return Specificity.SPECIFIC_OBJECT;
  }
}
