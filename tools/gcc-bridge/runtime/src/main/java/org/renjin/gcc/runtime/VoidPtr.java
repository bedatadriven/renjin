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

import java.lang.invoke.MethodHandle;

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

  public static Object pointerPlus(Object p, int bytes) {
    if(p instanceof Ptr) {
      return ((Ptr) p).pointerPlus(bytes);
    }
    throw new UnsupportedOperationException("TODO");
  }

  public static void memcpy(Object x, Object y, int numBytes) {

    if(x instanceof DoublePtr && y instanceof DoublePtr) {
      DoublePtr.memcpy(((DoublePtr) x), ((DoublePtr) y), numBytes);
    } else if(x instanceof LongPtr && y instanceof LongPtr) {
      LongPtr.memcpy(((LongPtr) x), ((LongPtr) y), numBytes);
    } else if(x instanceof IntPtr && y instanceof IntPtr) {
      IntPtr.memcpy(((IntPtr) x), ((IntPtr) y), numBytes);
    } else if(x instanceof FloatPtr && y instanceof FloatPtr) {
      FloatPtr.memcpy(((FloatPtr) x), ((FloatPtr) y), numBytes);
    } else if(x instanceof CharPtr && y instanceof CharPtr) {
      CharPtr.memcpy(((CharPtr) x), ((CharPtr) y), numBytes);
    } else if(x instanceof BytePtr && y instanceof BytePtr) {
      BytePtr.memcpy(((BytePtr) x), ((BytePtr) y), numBytes);
    } else {
      throw new UnsupportedOperationException("Not implemented: memcpy(" +
              x.getClass().getName() + ", " + y.getClass().getName() + ", n)");
    }
  }
  
  public static void memset(Object p, int value, int length) {
    
    if(p instanceof DoublePtr) {
      DoublePtr pd = (DoublePtr) p;
      DoublePtr.memset(pd.array, pd.offset, value, length);
    } else if(p instanceof BytePtr) {
      BytePtr pb = (BytePtr) p;
      BytePtr.memset(pb.array, pb.offset, value, length);
    } else if(p instanceof CharPtr) {
      CharPtr pc = (CharPtr) p;
      CharPtr.memset(pc.array, pc.offset, value, length);
    } else if(p instanceof ObjectPtr) {
      ObjectPtr po = (ObjectPtr) p;
      ObjectPtr.memset(po.array, po.offset, value, length);
    } else {
      throw new UnsupportedOperationException("TODO: p instanceof " + p.getClass().getName());
    }
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

  public static Ptr toPtr(Object voidPtr) {
    if(voidPtr == null) {
      return BytePtr.NULL;
    }
    if(voidPtr instanceof Ptr) {
      return ((Ptr) voidPtr);
    } else if(voidPtr instanceof MethodHandle) {
      return FunctionPtr1.malloc(((MethodHandle) voidPtr));
    } else {
      throw new UnsupportedOperationException("TODO: " + voidPtr.getClass().getName());
    }
  }

  public static void assign(Object[] array, int offset, Object value) throws NoSuchMethodException {

    // if this is a specialized array, then we need to trigger allocation now
    if(value instanceof MallocThunk && !array.getClass().equals(Object[].class)) {
      ((MallocThunk) value).assign(array, offset);

    } else {
      try {
        array[offset] = value;
      } catch (ArrayStoreException e) {
        throw new IllegalStateException("Exception storing value of class " +
            value.getClass().getName() + " to array of class " +
            array.getClass().getName());

      }
    }
  }
}
