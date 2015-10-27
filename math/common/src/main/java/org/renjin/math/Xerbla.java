package org.renjin.math;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.IntPtr;

/**
 * Error handling routine for netlib libraries
 */
public class Xerbla {
  
  public static void xerbla_(BytePtr functionName, IntPtr code, int functionNameLength) {
    throw new RuntimeException( "** On entry to " +
        functionName.toString(functionNameLength) +
        " parameter number " + code.unwrap() + " had an illegal value");
  }
}
