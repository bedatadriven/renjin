package org.renjin.gcc.runtime;

public class ObjectPtr<T> implements Ptr {
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
  
  public T get() {
    return get(0);
  }
  
  public void set(T value) {
    this.array[offset] = value;
  }
  
  @SuppressWarnings("unchecked")
  public T get(int index) {
    return (T)array[offset+index];
  }
  
  
}
