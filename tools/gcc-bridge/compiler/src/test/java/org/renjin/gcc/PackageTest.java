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
package org.renjin.gcc;

import org.junit.Test;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

import java.lang.reflect.Method;
import java.util.Arrays;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

/**
 * Tests of various sources files from CRAN packages
 */
public class PackageTest extends AbstractGccTest {
  
  @Test
  public void ash() throws Exception {
    Class clazz = compile("ash.f");

    // This is a test case extracted from the R call
    // described in the ash package help:
    // x <- rnorm(20)
    // ash1(bin1(x,nbin=10),5)
    
    IntPtr m = new IntPtr(5);
    IntPtr nc = new IntPtr( 1, 1, 2, 1, 3, 2, 4, 2, 3, 1);
    IntPtr nbin = new IntPtr(10);
    DoublePtr ab = new DoublePtr( -2.347650,  1.912399);
    IntPtr kopt = new IntPtr(2, 2);
    DoublePtr t = new DoublePtr(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    DoublePtr f = new DoublePtr(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    DoublePtr w = new DoublePtr(0, 0, 0, 0, 0);
    IntPtr ier = new IntPtr(0);
    
    
    Method ash1 = findMethod(clazz, "ash1_");
    
    ash1.invoke(null, m, nc, nbin, ab, kopt, t, f, w, ier);

    // Result from call above
    // from R version 3.2.0 (2015-04-16)

    System.out.println("t = " + Arrays.toString(t.array));
    System.out.println("f = " + Arrays.toString(f.array));

    checkArray("t", t.array, 
        -2.13464784622192, -1.70864295959473, -1.28263807296753, -0.856633186340332, 
        -0.430628299713135, -0.0046234130859375, 0.42138147354126, 0.847386360168457, 
        1.27339124679565, 1.69939613342285);

    checkArray("f", f.array,
        0.0909235381235159, 0.13113836562282, 0.176142346248676, 0.22079418638534,
        0.25850879002991, 0.283370147256633, 0.286081654097309, 0.262488017942826,
        0.215582457028981, 0.155682790100541);
    
  }

  private void checkArray(String name, double[] array, double... expected) {
    assertThat(name, array.length, equalTo(expected.length));
    for(int i=0;i<array.length;++i) {
      assertThat(format("%s[%d]", name, i), array[i], closeTo(expected[i], 0.000001));
    }
  }


}
