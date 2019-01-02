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
package org.renjin.primitives.special;

/**
 * 
 * Implements the `=` function. 
 * 
 * <p>Note(ab): As far as I can tell, there is no functional difference
 * between the `<-` and `=` operators: the difference comes into play
 * at the level of the parser because the two have different associativity,
 * and `=` in the context of a function definition/call is actually not a 
 * syntatical structure. 
 * 
 * <p>For example:
 * <pre>
 * f <- function(x, na.rm = TRUE) {} 
 * </pre>
 * 
 * <p>is not an invocation of this function, but syntax.
 *
 */
public class AssignFunction extends AssignLeftFunction {

  public AssignFunction() {
    super("=");
  }
  
}
