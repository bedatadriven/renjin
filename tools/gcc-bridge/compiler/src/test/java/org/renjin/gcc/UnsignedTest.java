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
package org.renjin.gcc;

import org.junit.Test;
import org.renjin.repackaged.guava.primitives.UnsignedInts;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

/**
 * Validate approaches to unsigned integers
 */
public class UnsignedTest {
  
  
  @Test
  public void unsignedInt32ToInt64() {
    int unsignedInt32 = UnsignedInts.parseUnsignedInt("4294967295");
    assertThat("signed value < 0", unsignedInt32, lessThan(0));
    
    // Can we just cast up and preserve the value?
    // NOPE: need to preserve the value
    long unsignedInt64 = unsignedInt32 & 0xffffffffL;
    
    assertThat(unsignedInt64, equalTo(4294967295L));
    
  }
  
  @Test
  public void signedInt8ToUnsignedInt16() {
    
   // assertThat((int) Math.pow(2, 8), equalTo(2 << 8));
    
    // C Standard says:
    // If the destination type is unsigned, the resulting value is the least unsigned integer congruent 
    // to the source integer (modulo 2^n where n is the number of bits used to represent the unsigned type).
    

    int signed = -62;
    int unsigned = (-62) & (-1 >>> 16);
  
    System.out.println(-1 >>> 16);
    
    assertThat(unsigned, equalTo( 65474));
  }
  
}
