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

package r.lang;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DoubleNaNTest {

  private double x = DoubleVector.NA();

  @Test
  public void test() {

    System.out.println("DoubleVector.NA = " + bits(DoubleVector.NA()));

    assertFalse("isNA(NaN) #1", DoubleVector.isNA(DoubleVector.NaN));
    assertTrue("isNaN(NaN)", Double.isNaN(DoubleVector.NaN));
    assertTrue("isNaN(NA)", Double.isNaN(DoubleVector.NA()));
    assertTrue("isNA(NA) #2", DoubleVector.isNA(DoubleVector.NA()));
    assertFalse("isNA(NaN)", DoubleVector.isNA(DoubleVector.NaN));
  }
  
  @Test
  public void test2() {
    int j=0;
    for(int i = 0; i < 1000000; ++i) {
      assertTrue("isNA(x = NA) #1", DoubleVector.isNA(x));
      assertTrue("isNaN(x = NA)", Double.isNaN(x));
      assertTrue("isNA(x = NA) #2", DoubleVector.isNA(x));
      j++;
    }
    System.out.println(j);
  }
    
  private String bits(double d) {
    return Long.toHexString(Double.doubleToRawLongBits(d));
  }

}
