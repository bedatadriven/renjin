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

import org.junit.Assert;
import org.renjin.gcc.runtime.BooleanPtr;
import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

public class RStubs {

  public static double R_NaN = 0;

  public static double my_global = 42;

  public static boolean R_finite(double x) {
    return true;
  }

  public static boolean R_IsNA(double x) {
    return false;
  }

  public static void error(BytePtr message) {
    throw new RuntimeException(message.nullTerminatedString());
  }
  
  public static void Rf_error(String msg) {
    throw new RuntimeException(msg);
  }

  private static double[] array(DoublePtr x) {
    if(x.offset != 0) {
      throw new RuntimeException();
    }
    return x.array;
  }

  public static void dumpx_(DoublePtr x) {
    System.out.println("---x---");
    for(int i=0;i!=x.array.length;++i) {
      System.out.println(x.array[i]);
    }
  }
  
  public static void dumpt_(DoublePtr t) {
    System.out.println("t = " + t.unwrap());
  }

  public static void dumpxll_(DoublePtr x, IntPtr l) {
    System.out.println("x(l,l) = " + x.unwrap());
  }

  public static void dump_nrmxl_(DoublePtr t) {
    System.out.println("nrmxl = " + t.unwrap());
  }
  public static void prenrmxl_(IntPtr n, DoublePtr x, IntPtr incx) {
    System.out.println(String.format("n=%s, x=%s, incx=%s", n, x, incx));
  }
  
  public static double magicnumber_(BytePtr x, int ldx) {
    if(x.array[x.offset] == 'Z') {
      return 42;
    } else {
      return -1;
    }
  }

  public static void asserttrue_(BooleanPtr x) {
    Assert.assertTrue(x.unwrap());
  }

  public static void assertfalse_(BooleanPtr x) {
    Assert.assertFalse(x.unwrap());
  }

  public static void xerbla_(BytePtr functionName, IntPtr code, int functionNameLength) {
    throw new RuntimeException( "** On entry to " + 
        functionName.toString(functionNameLength) +
        " parameter number " + code.unwrap() + " had an illegal value");
  }
}
