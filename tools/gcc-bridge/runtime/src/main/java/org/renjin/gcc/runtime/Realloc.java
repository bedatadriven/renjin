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
package org.renjin.gcc.runtime;

/**
 * Implementation of type-specific Realloc calls
 */
public class Realloc {


  public static double[] realloc(double[] p, int offset, int newCount) {
    double[] np = new double[newCount];
    if(p != null) {
      System.arraycopy(p, offset, np, 0, Math.min(p.length - offset, newCount));
    }
    return np;
  }


  public static char[] realloc(char[] p, int offset, int newCount) {
    char[] np = new char[newCount];
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
  
  public static Object realloc(Object p, int newSize) {
    throw new UnsupportedOperationException("TODO");
  }
}
