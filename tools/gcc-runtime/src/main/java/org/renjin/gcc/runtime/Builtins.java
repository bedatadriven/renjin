package org.renjin.gcc.runtime;

public class Builtins {
  
	public static double __builtin_powi__(double base, int exponent) {
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
  
  public static double __builtin_pow__(double x, double y) {
    return Math.pow(x, y);
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

  public static double[] realloc(double[] p, int offset, int newCount) {
    double[] np = new double[newCount];
    if(p != null) {
      System.arraycopy(p, offset, np, 0, Math.min(p.length - offset, newCount));
    }
    return np;
  }

  public static int[] realloc(int[] p, int offset, int newCount) {
    int[] np = new int[newCount];
    if(p != null) {
      System.arraycopy(p, offset, np, 0, Math.min(p.length - offset, newCount));
    }
    return np;
  }

  public static long[] realloc(long[] p, int offset, int newCount) {
    long[] np = new long[newCount];
    if(p != null) {
      System.arraycopy(p, offset, np, 0, Math.min(p.length - offset, newCount));
    }
    return np;
  }
  
  public static boolean[] realloc(boolean[] p, int offset, int newCount) {
    boolean[] np = new boolean[newCount];
    if(p != null) {
      System.arraycopy(p, offset, np, 0, Math.min(p.length - offset, newCount));
    }
    return np;
  }
  
  public static float[] realloc(float[] p, int offset, int newCount) {
    float[] np = new float[newCount];
    if(p != null) {
      System.arraycopy(p, offset, np, 0, Math.min(p.length - offset, newCount));
    }
    return np;
  }

  public static short[] realloc(short[] p, int offset, int newCount) {
    short[] np = new short[newCount];
    if(p != null) {
      System.arraycopy(p, offset, np, 0, Math.min(p.length - offset, newCount));
    }
    return np;
  }

  public static byte[] realloc(byte[] p, int offset, int newCount) {
    byte[] np = new byte[newCount];
    if(p != null) {
      System.arraycopy(p, offset, np, 0, Math.min(p.length - offset, newCount));
    }
    return np;
  }

  public static Object[] realloc(Object[] p, int offset, int newCount) {
    Object[] np = new Object[newCount];
    if(p != null) {
      System.arraycopy(p, offset, np, 0, Math.min(p.length - offset, newCount));
    }
    return np;
  }

}
