package org.renjin.gcc.runtime;


public abstract class Pointer {

  /**
   * Returns a new pointer
   * @param count
   * @return
   */
  public abstract Pointer plus(int count);

  public abstract double asDouble();
}
