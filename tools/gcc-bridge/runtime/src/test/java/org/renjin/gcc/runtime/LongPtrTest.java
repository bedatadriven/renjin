package org.renjin.gcc.runtime;

import org.junit.Test;


public class LongPtrTest {

  @Test
  public void testCmp() {
    test(0, 15, -1);
    test(0xFFFFFFFFFFFFFFFFL,  0xFFFL, 1);
  }
  
  private void test(long x, long y, int expectedSign) {
    int cmp = Long.signum(LongPtr.memcmp(x, y, 8));
    if(cmp != expectedSign) {
      throw new AssertionError(Long.toHexString(x) + " <> " + Long.toHexString(y) + ": expected " + expectedSign +
          ", got cmp");
    }
  }
}