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

/**
 * A null converter for JVM arguments
 */
public class SexpConverter implements Converter<SEXP> {

  private Class clazz;
  
  public SexpConverter(Class clazz) {
    super();
    this.clazz = clazz;
  }

  @Override
  public SEXP convertToR(SEXP value) {
    return value;
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return clazz.isAssignableFrom(exp.getClass());
  }

  public static boolean acceptsJava(Class clazz) {
    return SEXP.class.isAssignableFrom(clazz);
  }
  
  @Override
  public Object convertToJava(SEXP value) {
    return value;
  }

  @Override
  public int getSpecificity() {
    return Specificity.SEXP;
  }
}
