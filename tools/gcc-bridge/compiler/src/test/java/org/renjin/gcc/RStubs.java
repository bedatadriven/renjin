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
