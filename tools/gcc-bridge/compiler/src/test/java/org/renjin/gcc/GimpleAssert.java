package org.renjin.gcc;

import org.renjin.gcc.runtime.BytePtr;

/**
 * Methods to be used in tests
 */
public class GimpleAssert {
  
  public static void assertTrue(BytePtr message, int x) {
    if(x == 0) {
      throw new AssertionError(message.nullTerminatedString());
    }
  }
}
