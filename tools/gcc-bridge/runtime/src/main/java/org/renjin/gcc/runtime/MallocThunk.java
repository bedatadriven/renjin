package org.renjin.gcc.runtime;

/**
 * Represents a block of requested memory that is allocated at the moment when it is casted to 
 * a concrete type.
 * 
 * <p>In the machine model underpinning C/C++/Fortran, memory is memory is memory, so it's no problem to 
 * allocate an arbitrary block of bytes and then later treat that as a double array, or a structure, or 
 * whatever.</p>
 * 
 * <p>The JVM, on the other hand, is very particular about what is pointed to. You can't allocate an array
 * of doubles and then subsequently treat it as an array of integers.</p>
 */
public class MallocThunk {

  
  /**
   * The number of bytes to be allocated
   */
  public int bytes;

  public Object pointer = null;

  public MallocThunk(int bytes) {
    this.bytes = bytes;
  }
  
  public BooleanPtr booleanPtr() {
    if(pointer == null) {
      pointer = new BooleanPtr(new boolean[bytes]);
    }
    return (BooleanPtr) pointer;
  }
  
  public BytePtr bytePtr() {
    if(pointer == null) {
      pointer = new BytePtr(new byte[bytes]);
    }
    return (BytePtr) pointer;
  }
  
  public ShortPtr shortPtr() {
    if(pointer == null) {
      pointer = new ShortPtr(new short[bytes / 2]);
    }
    return (ShortPtr) pointer;
  }
  
  public CharPtr charPtr() {
    if(pointer == null) {
      pointer = new CharPtr(new char[bytes / 2]);
    }
    return (CharPtr) pointer;
  }
  
  public IntPtr intPtr() {
    if(pointer == null) {
      pointer = new IntPtr(new int[bytes / 4]);
    }
    return (IntPtr) pointer;
  }
  
  public LongPtr longPtr() {
    if(pointer == null) {
      pointer = new LongPtr(new long[bytes / 8]);
    }
    return (LongPtr) pointer;
  }
  
  public FloatPtr floatPtr() {
    if(pointer == null) {
      pointer = new FloatPtr(new float[bytes / 4]);
    }
    return (FloatPtr) pointer;
  }
  
  public DoublePtr doublePtr() {
    if(pointer == null) {
      pointer = new DoublePtr(new double[bytes / 8], 0);
    }
    return (DoublePtr) pointer;
  }

  /**
   * The C library function malloc. 
   *
   * <p>Normally, malloc calls are compiled by inferring the result type and directly allocating the 
   * equivalent java type, whether that's an int array or a Java object.</p>
   *
   * <p>However, in some cases we cannot infer the type of object being allocated, and so we can only return a
   * new {@link MallocThunk}. This method is only used in order to provide a function pointer to malloc, one
   * such case where it's impossible to know the type of memory we're allocating.</p>
   */
  public static Object malloc(int size) {
    return new MallocThunk(size);
  }

  
  /**
   * The C library function void free(void *ptr) deallocates the memory previously allocated by a
   * call to calloc, malloc, or realloc. This is a NO-OP in GCC-Bridge compiled code.
   *
   * <p>This method is only used to provide a function pointer to the free function.</p>
   */
  public static void free(Object ptr) {
    // NO-OP    
  }



}
