package org.renjin.gcc.runtime;

import java.util.Arrays;

public class ObjectPtr<T> implements Ptr {
  public Object[] array;
  public int offset;

  /**
   * Constructs a new ObjectPtr to a single value.
   */
  public ObjectPtr(T... array) {
    this.array = array;
    offset = 0;
  }
  
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


  /**
   * Copies the character c (an unsigned char) to 
   * the first n characters of the string pointed to, by the argument str.
   *
   * @param str an array of doubles
   * @param strOffset the first element to set
   * @param c the byte value to set
   * @param n the number of bytes to set
   */
  public static void memset(Object[] str, int strOffset, int c, int n) {

    if(c != 0) {
      throw new IllegalArgumentException("Unsafe operation: memset(T**) can only be used when c = 0");
    }
    
    Arrays.fill(str, strOffset, strOffset + (c / 32), null);
  }
  
  public static int memcmp(Object x, Object y, int numBytes) {
    if(x instanceof DoublePtr && y instanceof DoublePtr) {
      return DoublePtr.memcmp(((DoublePtr) x), ((DoublePtr) y), numBytes);
    }
    if(x instanceof LongPtr && y instanceof LongPtr) {
      return LongPtr.memcmp(((LongPtr) x), ((LongPtr) y), numBytes);
    }
    if(x instanceof IntPtr && y instanceof IntPtr) {
      return IntPtr.memcmp(((IntPtr) x), ((IntPtr) y), numBytes);
    }
    throw new UnsupportedOperationException("Not implemented: memcmp(" + 
        x.getClass().getName() + ", " + y.getClass().getName() + ", n)");
  }
  
  public static void memcpy(Object x, Object y, int numBytes) {
    throw new UnsupportedOperationException("TODO: Implement ObjectPtr.memcpy");
  }
}
