package org.renjin.gcc.runtime;

public class ObjectPtr implements Ptr {
  public Object[] array;
  public int offset;

  public ObjectPtr(Object[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }
  
  public void update(Object[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }
}
