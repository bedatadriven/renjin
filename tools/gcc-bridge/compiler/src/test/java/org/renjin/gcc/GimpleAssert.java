package org.renjin.gcc;

/**
 * Methods to be used in tests
 */
public class GimpleAssert {
  
  public static void assertTrue(String message, int x) {
    if(x == 0) {
      throw new AssertionError(message);
    }
  }
}
