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
 * Converts between a JVM enumeration value and an R {@code character} vector.
 */
public class EnumConverter extends PrimitiveScalarConverter<Enum> {

  private Class enumType;
  
  public EnumConverter(Class enumType) {
    super();
    this.enumType = enumType;
  }

  @Override
  public SEXP convertToR(Enum value) {
    return StringVector.valueOf(value.name());
  }

  @Override
  public Vector.Type getVectorType() {
    return StringVector.VECTOR_TYPE;
  }

  @Override
  protected Object getFirstElement(Vector value) {
    return Enum.valueOf(enumType, value.getElementAsString(0));
  }

  
  public static boolean accept(Class clazz) {
    // not sure what is preferable here:
    // do we convert enums with extra methods to objects or to strings?
    // Enum.class.isAssignableFrom will catch all enums, while clazz.isEnum
    // will only apply to those with no special methods 
    return Enum.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof StringVector;
  }

  @Override
  public int getSpecificity() {
    return 300;
  }
}
