/**
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
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;
import org.renjin.gcc.runtime.Ptr;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class Dqrdc2Test  extends AbstractGccTest {

  @Test
  public void dqrdc2() throws Exception {
    compile(Arrays.asList("dqrdc2.f", "ddot.f", "daxpy.f", "dscal.f", "dnrm2.f"));

    Method dqrdc2 = Class.forName("org.renjin.gcc.dqrdc2").getMethod("dqrdc2_",
        Ptr.class,  // x (in/out)
        Ptr.class,     // ldx - number of rows
        Ptr.class,     // n - number of rows
        Ptr.class,     // p - number of columns
        Ptr.class,  // tol - tolerance
        Ptr.class,     // k - rank  (out)
        Ptr.class,  // qraux - out
        Ptr.class,     // jpvt - out
        Ptr.class); // work - out

    Method dnrm2 = Class.forName("org.renjin.gcc.dnrm2").getMethod("dnrm2_",
        Ptr.class,     // n
        Ptr.class,  // x
        Ptr.class);    // incx


    // 3 x 4 matrix (in column-major order)
    DoublePtr x = new DoublePtr(
        0.906247654231265,0.0680010921787471,0.397506368579343,
        0.836464448831975,0.460515640443191,0.927493635797873,
        0.219096470857039,0.300599258393049,0.561568872537464,
        0.20270675746724,0.303500573383644,0.967535280855373);

    IntPtr ldx = new IntPtr(3);
    IntPtr n = new IntPtr(3);
    IntPtr p = new IntPtr(4);
    DoublePtr tol = new DoublePtr(1e-07);
    IntPtr k = new IntPtr(new int[1]);
    DoublePtr qraux = new DoublePtr(new double[p.unwrap()]);
    IntPtr jpvt = new IntPtr(new int[] { 1, 2, 3, 4 });
    DoublePtr work = new DoublePtr(new double[2*p.unwrap()]);


    // check some sub-calcs first
    double nrmxl = (Double)dnrm2.invoke(null, n, x, new IntPtr(1));

    assertThat(nrmxl, closeTo(0.99192755400699972, 1e-5));

    dqrdc2.invoke(null, x, ldx, n, p, tol, k, qraux, jpvt, work);

    System.out.println(x);

    // expected results
    // 3 x 4 matrix (in column-major order)
    double [] expected_x = new double[] {
        -0.99192755,0.06855449,0.40074133,
        -1.16746846,-0.63953832, 0.79407285,
        -0.44582282, -0.50358912,  0.03691595,
        -0.59373523, -0.80298460 , 0.26836795
    };

    double [] expected_qraux = new double[] { 1.91362283, 1.60782259, 0.03691595, 0.26836795 };

    assertArraysEqual(x, expected_x);
    assertArraysEqual(qraux, expected_qraux);

    assertThat(k.unwrap(), equalTo(3));
  }

  private void assertArraysEqual(DoublePtr x, double[] expected_x) {
    for(int i=0;i!=expected_x.length;++i) {
      assertThat("element " + i, x.array[i], closeTo(expected_x[i], 1e-5));
    }
  }
}
