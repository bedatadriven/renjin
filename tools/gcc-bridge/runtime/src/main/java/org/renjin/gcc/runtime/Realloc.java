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
