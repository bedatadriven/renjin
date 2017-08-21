/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.runtime;

import org.renjin.gcc.annotations.GccSize;

import java.util.Arrays;

public class ObjectPtr<T> extends AbstractPtr {

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

  @Override
  public byte getByte(int offset) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public void setByte(int offset, byte value) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public int toInt() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public boolean isNull() {
    return array == null && offset == 0;
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
        
      } else if(baseType.equals(Object.class)) {
        // Don't force allocation yet...
        this.array[offset] = thunk;
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

//      for (int i = 0; i < numElements; i++) {
//      }
      throw new UnsupportedOperationException("TODO");
    }
  }

  public static ObjectPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      throw new UnsupportedOperationException("Casting of void* to record type without type information. " +
          "Please recompile against the latest version of gcc-bridge to resolve.");
    }
    return (ObjectPtr) voidPointer;
  }


  public static ObjectPtr cast(Object voidPointer, Class<?> recordType) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).objectPtr(recordType);
    }

    return (ObjectPtr)voidPointer;
  }

  public static <T> T castUnit(Object voidPointer, Class<T> recordType) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).recordUnitPtr(recordType);
    }
    return (T)recordType;
  }

  @Override
  public Ptr getPointer() {
    return (Ptr) array[this.offset];
  }

  @Override
  public Ptr getPointer(int offset) {
    int byteOffset = (this.offset * 4) + offset;
    if(byteOffset % 4 == 0) {
      return (Ptr)array[byteOffset / 4];
    } else {
      throw new UnsupportedOperationException("Unaligned access");
    }
  }
}
