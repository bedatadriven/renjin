/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
 * Interface to objects which convert between JVM objects and S-expressions
 *
 * @param <T> the Java-language class to be converted to/from an R SEXP
 */
public interface Converter<T>  {

  /**
   * Converts a JVM object instance to an R S-expression
   * @param value
   * @return
   */
  SEXP convertToR(T value);

  /**
   *
   * @param expression
   * @return true if this converter can handle the given R {@code expression}
   */
  boolean acceptsSEXP(SEXP expression);

  /**
   * Converts the provided R {@code expression} to a JVM class instance
   * @param expression
   * @return
   */
  Object convertToJava(SEXP expression);

  /**
   * @return  an number indicating the specificity of the
   */
  int getSpecificity();

}