package org.renjin.gcc.runtime;

public class ObjectPtr implements Ptr {
  public final Object[] array;
  public final int offset;

  public ObjectPtr(Object[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }
}
