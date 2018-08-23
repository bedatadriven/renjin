/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
import static org.junit.Assert.assertThat;

public class DoublePtrTest {

  @Test
  public void byteTest() {

    DoublePtr ptr = new DoublePtr(1.0);
    long longValue = Double.doubleToRawLongBits(ptr.array[0]);

    // Big-endian representation
    // 0b111111_11110000_00000000_00000000_00000000_00000000_00000000_00000000

    assertThat(ptr.getByte(0), equalTo((byte)0));
    assertThat(ptr.getByte(1), equalTo((byte)0));
    assertThat(ptr.getByte(2), equalTo((byte)0));
    assertThat(ptr.getByte(3), equalTo((byte)0));
    assertThat(ptr.getByte(4), equalTo((byte)0));
    assertThat(ptr.getByte(5), equalTo((byte)0));
    assertThat(ptr.getByte(6), equalTo((byte)0b11110000));
    assertThat(ptr.getByte(7), equalTo((byte)0b00111111));

    long relongValue = ptr.getLong();

    System.out.println(Long.toBinaryString(longValue));
    System.out.println(Long.toBinaryString(relongValue));

    assertThat(ptr.getLong(), equalTo(longValue));
  }

}