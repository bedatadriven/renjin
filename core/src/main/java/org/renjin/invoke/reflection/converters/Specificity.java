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

public class Specificity {
  public static final int BOOLEAN = 1;
  public static final int INTEGER = 2;
  public static final int DOUBLE = 3;
  public static final int ENUM = 4;
  public static final int STRING = 4;

  public static final int SEXP = 9;

  public static final int COLLECTION = 10;

  public static final int SPECIFIC_OBJECT = 10;

  public static final int OBJECT = 100;
}
