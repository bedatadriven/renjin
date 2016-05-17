package org.renjin.gcc.runtime;

public class Builtins {

  public static double __builtin_powi__(double base, int exponent) {
    return powi(base, exponent);
  }
  
  public static double powi(double base, int exponent) {
    if(exponent == 1) {
      return base;
    } else if(exponent == 2) {
      return base * base;
    } else {
      return Math.pow(base, (double)exponent);
    }
  }

  public static float  __builtin_logf__(float x) {
    return (float) Math.log(x);
  }
  
  public static double __builtin_copysign__(double magnitude, double sign) {
    return Math.copySign(magnitude, sign);
  }

  public static float __builtin_copysignf__(float magnitude, float sign) {
    return Math.copySign(magnitude, sign);
  }

  public static double __builtin_exp__(double x) {
    return Math.exp(x);
  }
  
  public static float  __builtin_sqrtf__(float f) {
    return (float) Math.sqrt(f);
  }

  public static float __builtin_cosf__(double f) {
    return (float) Math.cos(f);
  }
  
  public static float  __builtin_sinf__ (double x) {
    return (float) Math.sin(x);
  }
  
  public static double __builtin_sin__(double x) {
    return Math.sin(x);
  }
  
  public static double __builtin_log__(double x) {
    return Math.log(x);
  }
  
  public static double __builtin_cos__(double x) {
    return Math.cos(x);
  }
  
  public static double __builtin_sqrt__(double x) {
    return Math.sqrt(x);
  }
  
  public static double __builtin_atan__(double x) { return Math.atan(x); }

  public static double __builtin_atan2__(double x, double y) { return Math.atan2(x, y); }

  public static double __builtin_asin__(double x) { return Math.asin(x); }
  
  public static double __builtin_fmod__(double x, double y) { return Mathlib.fmod(x, y); }

  public static double __builtin_pow__(double x, double y) {
    return Math.pow(x, y);
  }
  
  public static void __builtin_puts(BytePtr string) {
    System.out.println(string.nullTerminatedString());
  }

  public static int __fpclassifyd(double x) {
    // TODO: lookup the exact behavior of this function
    return Double.isNaN(x) ? 0 : 1;
  }

  public static int _gfortran_pow_i4_i4__(int base, int exponent) {
    if(exponent < 0) {
      throw new IllegalArgumentException("exponent must be > 0: " + exponent);
    }
    int result = 1;
    for(int i=0;i<exponent;++i) {
      result *= base;
    }
    return result;
  }

  public static float __builtin_powif__(float base, int exponent) {
    return powif(base, exponent);
  }

  public static float powif(float base, int exponent) {
    if(exponent == 0) {
      return 1;
    } else if(exponent == 1) {
      return base;
    } else if(exponent == 2) {
      return base*base;
    } else {
      return (float) Math.pow(base, exponent);
    }
  }

  public static int __isnan(double x) {
    return Double.isNaN(x) ? 1: 0;
  }

  public static int __finite(double x) {
    return Double.isInfinite(x) ? 0 : 1;
  }

  public static boolean unordered(double x, double y) {
    return Double.isNaN(x) || Double.isNaN(y);
  }

  @Deprecated
  public static double fmax(double x, double y) {
    return Mathlib.fmax(x, y);
  }

  @Deprecated
  public static double hypot(double x, double y) {
    return Mathlib.hypot(x, y);
  }
  
  
//  public static void _gfortran_set_args__(int argc, ObjectPtr argv) {
//    // TODO
//  }

  public static void _gfortran_set_options__(int x, IntPtr y) {
    // TODO
  }

  public static void _gfortran_stop_string__(int x, int y) {
    // TODO
  }

  @Deprecated
  public static double[] realloc(double[] p, int offset, int newCount) {
    return Realloc.realloc(p, offset, newCount);
  }

  @Deprecated
  public static int[] realloc(int[] p, int offset, int newCount) {
    return Realloc.realloc(p, offset, newCount);
  }

  @Deprecated
  public static long[] realloc(long[] p, int offset, int newCount) {
    return Realloc.realloc(p, offset, newCount);
  }

  @Deprecated
  public static boolean[] realloc(boolean[] p, int offset, int newCount) {
    return Realloc.realloc(p, offset, newCount);
  }

  @Deprecated
  public static float[] realloc(float[] p, int offset, int newCount) {
    return Realloc.realloc(p, offset, newCount);
  }

  @Deprecated
  public static short[] realloc(short[] p, int offset, int newCount) {
    return Realloc.realloc(p, offset, newCount);
  }

  @Deprecated
  public static byte[] realloc(byte[] p, int offset, int newCount) {
    return Realloc.realloc(p, offset, newCount);
  }

  @Deprecated
  public static Object[] realloc(Object[] p, int offset, int newCount) {
    return Realloc.realloc(p, offset, newCount);
  }
}
