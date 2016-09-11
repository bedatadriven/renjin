package org.renjin.gcc.runtime;

import org.renjin.gcc.annotations.GccSize;

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
   * @param byteCount the number of bytes to set
   */
  public static void memset(Object[] str, int strOffset, int c, int byteCount) {

    if(byteCount == 0) {
      return;
    }


    Class<?> elementType = str.getClass().getComponentType();
    
    if(Ptr.class.isAssignableFrom(elementType)) {

      // This is an array of DoublePtr[] etc
      // Fill it with DoublePtr.NULL instance

      Object nullInstance;
      try {
        nullInstance = elementType.getField("NULL").get(null);
      } catch (IllegalAccessException | NoSuchFieldException e) {
        throw new IllegalStateException("Cannot access NULL instance for " + elementType.getName());
      }
      Arrays.fill(str, strOffset, strOffset + (byteCount / 4), nullInstance);

    } else {
      
      // Otherwise a record class: we need to memset each instance
      // Calculate the number of records based on the "size" of the record
      // as understood by GCC

      GccSize size = elementType.getAnnotation(GccSize.class);
      if(size == null) {
        throw new IllegalStateException(elementType.getClass().getName() + " is missing @GccSize annotation");
      }
      int numElements = byteCount / size.value();

      for (int i = 0; i < numElements; i++) {
      }
    }
  }

  public static ObjectPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      throw new UnsupportedOperationException("TODO");
    }
    return (ObjectPtr) voidPointer;
  }
}
