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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

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
public class MallocThunk extends AbstractPtr {

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
  
  public <T> T recordUnitPtr(Class<T> recordType) {
    if(pointer == null) {
      if(this.bytes != sizeOf(recordType)) {
        throw new IllegalStateException(String.format(
            "Misclassified record pointer: %s (bug in gcc-bridge compilation)",
              recordType.getName()));
      }
      Constructor constructor = constructorFor(recordType);
      try {
        pointer = constructor.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Failed to malloc element of type " + recordType.getClass().getName(), e);
      }
    }
    return (T)pointer;
  }
  
  public ObjectPtr objectPtr(Class<?> componentType) {
    if(pointer == null) {
      int sizeInBytes = sizeOf(componentType);

      Constructor<?> constructor = constructorFor(componentType);

      int numElements = bytes / sizeInBytes;
      Object[] array = (Object[])Array.newInstance(componentType, numElements);
      for(int i=0;i<array.length;++i) {
        try {
          array[i] = constructor.newInstance();
        } catch (Exception e) {
          throw new IllegalStateException(String.format(
              "Exception malloc'ing array for class %s: %s", componentType.getName(), e.getMessage()), e);
        }
      }
      pointer = new ObjectPtr<>(array);
    }
    
    return (ObjectPtr)pointer;
  }

  private Constructor<?> constructorFor(Class<?> componentType) {
    Constructor<?> constructor;
    try {
      constructor = componentType.getConstructor();
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(String.format(
          "Cannot malloc array for class %s: no default constructor.", componentType.getName()), e);
    }
    return constructor;
  }

  private int sizeOf(Class<?> componentType) {
    
    if(componentType.equals(ObjectPtr.class)) {
      // Pointer size. (Hardcoded to 4-bytes)
      return 4;
    }
    
    GccSize size = componentType.getAnnotation(GccSize.class);
    if(size == null) {
      throw new IllegalStateException(String.format(
          "Cannot malloc array for class %s: @GccSize annotation is absent.", componentType.getName()));
    }
    int sizeInBytes = size.value();
    if(sizeInBytes <= 0) {
      throw new IllegalStateException(String.format(
          "Cannot malloc array for class %s: @GccSize = %d", componentType.getName(), sizeInBytes));
    }
    return sizeInBytes;
  }

  public void assign(Object[] array, int offset) {
    if(pointer == null) {
      pointer = allocElement(array);
    }
    array[offset] = pointer;
  }

  private Object allocElement(Object[] array) {
    if(array instanceof BooleanPtr[]) {
      return booleanPtr();
    } else if(array instanceof BytePtr[]) {
      return bytePtr();
    } else if(array instanceof CharPtr[]) {
      return charPtr();
    } else if(array instanceof DoublePtr[]) {
      return doublePtr();
    } else if(array instanceof FloatPtr[]) {
      return floatPtr();
    } else if(array instanceof IntPtr[]) {
      return intPtr();
    } else if(array instanceof LongPtr[]) {
      return longPtr();
    } else if(array instanceof ShortPtr[]) {
      return shortPtr();
    } else if(array instanceof ObjectPtr[]) {
      throw new UnsupportedOperationException("TODO");
    } else {
      // For arrays of records, we can only allocate one element
      // in any case, so no need to consult size.
      Class<?> componentType = array.getClass().getComponentType();
      try {
        return componentType.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Exception while triggering malloc thunk for " + componentType.getName(), e);
      }
    }
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

  public static Object calloc(int numElements, int elementSize) {
    return new MallocThunk(numElements * elementSize);
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


  @Override
  public Object getArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getOffset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Ptr realloc(int newSizeInBytes) {
    if(pointer != null) {
      return ((Ptr) pointer).realloc(newSizeInBytes);
    } else {
      return new MallocThunk(newSizeInBytes);
    }
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
}
