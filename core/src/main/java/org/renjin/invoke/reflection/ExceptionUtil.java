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
package org.renjin.invoke.reflection;

import org.renjin.sexp.SEXP;

class ExceptionUtil {

  static String toString(Iterable<SEXP> args) {
    StringBuilder list = new StringBuilder();
    for(SEXP arg : args) {
      if(arg == null) {
        break;
      }
      if(list.length() > 0) {
        list.append(", ");
      }
      list.append(arg.getTypeName());
    }
    return list.toString();
  }

  static String overloadListToString(Iterable<?> overloads) {
    StringBuilder sb = new StringBuilder();
    for(Object overload : overloads) {
      sb.append("\n\t").append(overload.toString());
    }
    return sb.toString();
  }

  
}
