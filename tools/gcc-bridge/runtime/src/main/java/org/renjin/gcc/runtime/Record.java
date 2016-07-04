package org.renjin.gcc.runtime;

/**
 * Generic record structure that can be used when 
 * we need to be able to cast between different types / unions
 */
public class Record {

  /**
   * Storage for integer fields
   */
  public int[] i;

  /**
   * Storage for double fields
   */
  public double[] d;


  /**
   * Storage for pointer fields.
   */
  public Object[] p;
}
