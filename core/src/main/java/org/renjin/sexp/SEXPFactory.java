/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.sexp;

/**
 * Creates a {@code SEXP} from a Java object
 */
public class SEXPFactory {

  public static SEXP fromJava(Object result) {

    if(result instanceof SEXP) {
      return (SEXP) result;

    } else if(result instanceof Long) {
      return new DoubleArrayVector(((Long)result).doubleValue());

    } else if(result instanceof Double) {
      return new DoubleArrayVector( (Double) result );

    } else if(result instanceof Boolean) {
      return new LogicalArrayVector( (Boolean) result );

    } else if(result instanceof Logical) {
      return new LogicalArrayVector( (Logical) result );

    } else if(result instanceof Integer) {
      return new IntArrayVector( (Integer) result);

    } else if(result instanceof String) {
      return new StringVector( (String) result );

    } else if(result instanceof int[]) {
      return new IntArrayVector((int[]) result);

    } else if(result instanceof double[]) {
      return new DoubleArrayVector((double[]) result);

    } else if(result instanceof boolean[]) {
      return new LogicalArrayVector((boolean[]) result);

    } else {
      return new ExternalExp(result, Null.INSTANCE);
    }
  }

}
