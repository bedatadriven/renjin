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

import java.util.Arrays;

public class CharPtr implements Ptr {
  
  public static final CharPtr NULL = new CharPtr();
  
  public final char[] array;
  public final int offset;
  
  private CharPtr() {
    this.array = null;
    this.offset = 0;
  }

  public CharPtr(char[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }

  public CharPtr(char... array) {
    this.array = array;
    this.offset = 0;
  }

  @Override
  public char[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public CharPtr realloc(int newSizeInBytes) {
    return new CharPtr(Realloc.realloc(array, offset, newSizeInBytes / 2));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new CharPtr(array, offset + (bytes / 2));
  }

  public static CharPtr fromString(String string) {
    int nchars = string.length();
    char array[] = new char[nchars+1];
    System.arraycopy(string.toCharArray(), 0, array, 0, nchars);
    return new CharPtr(array);
  }

  @Override
  public String toString() {
    return offset + "+" + Arrays.toString(array);
  }

  public String asString() {
    // look for null terminator
    int length;
    for(length=offset;length<array.length;++length) {
      if(array[length] == 0) {
        break;
      }
    }
    return new String(array, offset, length-offset);
  }
  
  public static CharPtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).charPtr();
    }
    if(voidPointer == null) {
      return NULL;
    }
    return (CharPtr) voidPointer;
  }

  public static void memset(char[] array, int offset, int value, int length) {
    throw new UnsupportedOperationException("TODO");
  }
  
  public static char memset(int byteValue) {
    throw new UnsupportedOperationException("TODO");
  }
}