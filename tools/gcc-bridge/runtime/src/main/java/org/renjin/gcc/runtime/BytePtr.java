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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BytePtr extends AbstractPtr {
  
  public static final BytePtr NULL = new BytePtr();
  
  public final byte[] array;
  public final int offset;

  private BytePtr() {
    this.array = null;
    this.offset = 0;
  }
  
  public BytePtr(byte... array) {
    this(array, 0);
  }
  
  public BytePtr(byte[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }
  
  public byte get() {
    return array[offset];
  }
  
  public void set(byte value) {
    array[offset] = value;
  }
  
  public static byte[] toArray(String constant) {
    // The string literals are technically not in UTF-8 encoding:
    // the literals that GCC emits are really just a string of bytes, 
    // but during compilation we encode those byte streams as a UTF-8 string
    return constant.getBytes(StandardCharsets.UTF_8);
  }

  public static BytePtr asciiString(String string) {
    return new BytePtr(string.getBytes(StandardCharsets.US_ASCII), 0);
  }
  
  public static BytePtr nullTerminatedString(String string, Charset charset) {
    byte[] bytes = string.getBytes(charset);
    byte[] nullTerminatedBytes = Arrays.copyOf(bytes, bytes.length+1);
    return new BytePtr(nullTerminatedBytes, 0);
  }
  
  /**
   * 
   * @return the length of the null-terminated string referenced by this pointer
   */
  public int nullTerminatedStringLength() {
    int i = offset;
    while(i < array.length) {
      if(array[i] == 0) {
        return i-offset;
      }
      i++;
    }
    throw new IllegalStateException("String is not null-terminated.");
  }

  /**
   * @return the null-terminated string pointed to by this byte array as a Java String. 
   * Asumes UTF-8 encoding. 
   */
  public String nullTerminatedString() {
    return new String(array, offset, nullTerminatedStringLength(), StandardCharsets.UTF_8);
  }
  
  public String toString(int length) {
    return new String(array, offset, length, StandardCharsets.UTF_8);
  }


  public static BytePtr malloc(int bytes) {
    return new BytePtr(new byte[bytes]);
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
  public static void memset(byte[] str, int strOffset, int c, int n) {
    Arrays.fill(str, strOffset, strOffset + (c / Double.SIZE), (byte)c);
  }

  public static byte memset(int c) {
    return (byte) c;
  }
  
  @Override
  public byte[] getArray() {
    return array;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public BytePtr realloc(int newSizeInBytes) {
    return new BytePtr(Realloc.realloc(array, offset, newSizeInBytes));
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    return new BytePtr(array, offset + bytes);
  }

  @Override
  public byte getByte(int offset) {
    return this.array[this.offset + offset];
  }

  @Override
  public void setByte(int offset, byte value) {
    this.array[this.offset + offset] = value;
  }

  @Override
  public int toInt() {
    return offset;
  }

  public static BytePtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).bytePtr();
    }
    if(voidPointer == null) {
      return NULL;
    }
    return (BytePtr) voidPointer;
  }

  public static int memcmp(BytePtr s1, BytePtr s2, int len) {
    for (int i = 0; i < len; i++) {
      byte b1 = s1.array[s1.offset+i];
      byte b2 = s2.array[s2.offset+i];
      if(b1 != b2) {
        int i1 = b1 & 0xFF;
        int i2 = b2 & 0xFF;
        return i1 - i2;
      }
    }
    return 0;
  }
}
