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
package org.renjin.math;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.Stdlib;

/**
 * Error handling routine for netlib libraries
 */
public class Errors {
  
  /** 
   * Fortran error handler
   */
  public static void xerbla_(BytePtr functionName, IntPtr code, int functionNameLength) {
    throw new IllegalArgumentException( "** On entry to " +
        functionName.toString(functionNameLength) +
        " parameter number " + code.unwrap() + " had an illegal value");
  }

  /**
   * C error handler
   */
  public static void arith_error(BytePtr messageFormat, double x) {
    BytePtr message = new BytePtr(new byte[512]);
    Stdlib.sprintf(message, messageFormat, x);
    
    throw new ArithmeticException(message.nullTerminatedString());
  }
}
