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
  
  @SuppressWarnings("unchecked")
  public <T> T get() {
    return get(0);
  }
  
  public void set(Object value) {
    this.array[offset] = value;
  }
  
  @SuppressWarnings("unchecked")
  public <T> T get(int index) {
    return (T)array[offset+index];
  }
  
  
}
