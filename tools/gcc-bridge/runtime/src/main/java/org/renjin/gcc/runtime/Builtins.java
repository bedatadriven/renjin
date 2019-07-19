/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

import org.renjin.gcc.format.FormatArrayInput;

public class Builtins {

  private static final ThreadLocal<IntPtr> ERRNO = new ThreadLocal<>();

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

  /**
   * The __errno_location() function shall return the address of the errno variable for the current thread.
   */
  public static Ptr __errno_location() {
    IntPtr intPtr = ERRNO.get();
    if(intPtr == null) {
      intPtr = new IntPtr(0);
      ERRNO.set(intPtr);
    }
    return intPtr;
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

  /**
   * Compares two fortran strings.
   *
   * <p>Ported from {@code compare_strings} in libfortran/intrinsics/string_intrinsics_inc.c</p>
   *
   * @param len1 length of the first string
   * @param s1 the first string
   * @param len2 length of the second string
   * @param s2 the second string
   * @return 0 if the strings are equal, -1 if the first is less than the second, or +1 if the
   * first is greater than the second.
   */
  public static int _gfortran_compare_string(int len1, BytePtr s1, int len2, BytePtr s2) {

    int res = BytePtr.memcmp(s1, s2, ((len1 < len2) ? len1 : len2));
    if (res != 0) {
      return res;
    }

    if (len1 == len2) {
      return 0;
    }

    int len;
    byte[] s;
    int si;

    if (len1 < len2) {
      len = len2 - len1;
      s = s2.array;
      si = s2.offset + len1;
      res = -1;

    }  else  {
      len = len1 - len2;
      s = s1.array;
      si = s1.offset + len2;
      res = 1;
    }

    while (len-- != 0) {
      if (s[si] != ' ') {
        if (s[si] > ' ') {
          return res;
        } else {
          return -res;
        }
      }
      si++;
    }

    return 0;
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
    return Double.isInfinite(x) || Double.isNaN(x) ? 0 : 1;
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


  public static void __cxa_pure_virtual() {
    throw new RuntimeException("Pure virtual function invoked");
  }

  /**
   * A handle for __cxa_finalize to manage c++ local destructors.
   */
  public static Ptr[] __dso_handle = new Ptr[] { BytePtr.NULL };

  
  public static void undefined_std() {
    throw new RuntimeException("Invocation of std:: method");
  }

  public static void _gfortran_runtime_error_at(Ptr position, Ptr format, Object... arguments) {
    throw new RuntimeException(Stdlib.format(format, new FormatArrayInput(arguments)));
  }

  public static int _gfortran_pow_i4_i4(int base, int power) {
    int result = 1;
    for (int i = 1; i <= power; i++) {
      result *= base;
    }
    return result;
  }
  private static volatile int __sync_synchronize_value = 0;

  public static void __sync_synchronize() {
    // The following volatile field access should convince the JVM to emit a memory fence instruction.
    // https://www.infoq.com/articles/memory_barriers_jvm_concurrency
    __sync_synchronize_value++;
  }

  public static void _gfortran_concat_string(int resultLength, Ptr result, int arg1Length, Ptr arg1, int arg2Length, Ptr arg2) {
    int resultPos = 0;
    for(int i=0;i<arg1Length;++i) {
      result.setByte(resultPos++, arg1.getByte(i));
    }
    for (int i=0;i<arg2Length;++i) {
      result.setByte(resultPos++, arg2.getByte(i));
    }
  }

  public static int __atomic_fetch_add_4(Ptr result, int value) {
    int previous = result.getInt();
    result.setInt(previous + value);
    return previous;
  }

  public static char __builtin_bswap16(char x) {
    throw new UnsupportedOperationException("__builtin_bswap16");
  }

  public static int __builtin_bswap32(int x) {
    throw new UnsupportedOperationException("__builtin_bswap32");
  }

  public static long __builtin_bswap64(long x) {
    throw new UnsupportedOperationException("__builtin_bswap64");
  }

}
