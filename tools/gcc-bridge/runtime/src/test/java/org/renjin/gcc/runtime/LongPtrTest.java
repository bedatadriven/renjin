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
package org.renjin.gcc.runtime;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


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


  @Test
  public void setFirstByte() {
    LongPtr ptr = new LongPtr(0L);
    ptr.setByte(0, (byte)0xFF);

    assertThat(ptr.array[0], equalTo(0xFFL));
  }

  @Test
  public void setSecondByte() {
    LongPtr ptr = new LongPtr(0L);
    ptr.setByte(1, (byte)0x34);

    assertThat(ptr.getByte(1), equalTo((byte)0x34));
  }
}