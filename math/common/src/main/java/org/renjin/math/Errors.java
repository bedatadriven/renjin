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
