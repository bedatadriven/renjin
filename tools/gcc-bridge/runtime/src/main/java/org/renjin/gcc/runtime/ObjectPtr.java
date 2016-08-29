package org.renjin.gcc.runtime;

import java.util.Arrays;

public class ObjectPtr<T> implements Ptr {
  
  public static final ObjectPtr NULL = new ObjectPtr();
  
  public final Object[] array;
  public final int offset;
  public Class baseType;

  private ObjectPtr() {
    this.array = null;
    this.offset = 0;
  }

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

  public ObjectPtr(Class baseType, Object[] array, int offset) {
    this.array = array;
    this.offset = offset;
    this.baseType = baseType;
  }
  
  @Override
  public Object[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public ObjectPtr<T> realloc(int newSizeInBytes) {
    return new ObjectPtr(Realloc.realloc(array, offset, newSizeInBytes / 4));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    throw new UnsupportedOperationException("TODO");
  }
  
  public T get() {
    return get(0);
  }
  
  public void set(Object value) {
    if(value instanceof MallocThunk) {
      MallocThunk thunk = (MallocThunk) value;
      if(array instanceof BooleanPtr[] || BooleanPtr.class.equals(baseType)) {
        array[offset] = thunk.booleanPtr();
        
      } else if(array instanceof BytePtr[] || BytePtr.class.equals(baseType)) {
        array[offset] = thunk.bytePtr();
      
      } else if(array instanceof CharPtr[] || CharPtr.class.equals(baseType)) {
        array[offset] = thunk.charPtr();
        
      } else if(array instanceof DoublePtr[] || DoublePtr.class.equals(baseType)) {
        array[offset] = thunk.doublePtr();
        
      } else if(array instanceof FloatPtr[] || FloatPtr.class.equals(baseType)) {
        array[offset] = thunk.floatPtr();
        
      } else if(array instanceof IntPtr[] || IntPtr.class.equals(baseType)) {
        array[offset] = thunk.intPtr();
        
      } else if(array instanceof LongPtr[] || LongPtr.class.equals(baseType)) {
        array[offset] = thunk.longPtr();

      } else if(array instanceof ShortPtr[] || ShortPtr.class.equals(baseType)) {
        array[offset] = thunk.shortPtr();

      } else if(array instanceof ObjectPtr[]) {
        this.array[offset] = thunk.objectPtr(baseType);
        
      } else {
        this.array[offset] = thunk.recordUnitPtr(this.array.getClass().getComponentType());
      }
    } else {
      this.array[offset] = value;
    }
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

    Class<?> wrapperType = str.getClass().getComponentType();
    Object nullInstance;
    try {
      nullInstance = wrapperType.getField("NULL").get(null);
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new IllegalStateException("Cannot access NULL instance for " + wrapperType.getName());
    }

    Arrays.fill(str, strOffset, strOffset + (n / 4), nullInstance);
  }

  public static ObjectPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      throw new UnsupportedOperationException("TODO");
    }
    return (ObjectPtr) voidPointer;
  }
}
