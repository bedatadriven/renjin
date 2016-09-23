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

import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

/**
 * Converts between a JVM {@code String} instance and R {@code character} vectors
 */
public class StringConverter extends BoxedScalarConverter<String> {

  public static final StringConverter INSTANCE = new StringConverter();
  
  private StringConverter() { }
 
  public static boolean accept(Class clazz) {
    if(clazz.isArray()) {
      return false;
    }
    return clazz == String.class;
  }
  
  @Override
  public SEXP convertToR(String value) {
    return StringVector.valueOf(value);
  }

  @Override
  protected Object getFirstElement(Vector value) {
    return value.getElementAsString(0);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return (exp instanceof Vector)&&(((Vector)exp).length()==1);
  }

  @Override
  public int getSpecificity() {
    return Specificity.STRING;
  }

  @Override
  public Vector.Type getVectorType() {
    return StringVector.VECTOR_TYPE;
  }
}
