package org.renjin.gcc.runtime;

/**
 * Runtime methods for operations on void pointers.
 */
public final class VoidPtr {
  
  private VoidPtr() {}
  
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
    throw new UnsupportedOperationException("TODO: Implement VoidPtr.memcpy");
  }

  /**
   * Compares the address of two pointers.
   * 
   * @return 0 if the two pointers are equal
   */
  public static int compare(Object x, Object y) {
    
    if(x instanceof Ptr && y instanceof Ptr) {
      // Two different fat pointers may actually point to the same value
      Ptr px = (Ptr) x;
      Ptr py = (Ptr) y;
      
      if(px.getArray() == py.getArray()) {
        return Integer.compare(px.getOffset(), py.getOffset());
      }
    }
    
    return Integer.compare(System.identityHashCode(x), System.identityHashCode(y));
  }
}
