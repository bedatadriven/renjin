/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  public static Ptr of(int value) {
    return NULL.pointerPlus(value);
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

  public static BytePtr fromString(String string) {
    return new BytePtr(string.getBytes(), 0);
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
    Arrays.fill(str, strOffset, strOffset + n, (byte)c);
  }

  public static byte memset(int c) {
    return (byte) c;
  }
  
  @Override
  public byte[] getArray() {
    return array;
  }

  @SuppressWarnings("deprecation")
  private int getOffset() {
    return offset;
  }

  @Override
  public int getOffsetInBytes() {
    return offset;
  }

  @Override
  public BytePtr realloc(int newSizeInBytes) {
    return new BytePtr(Realloc.realloc(array, offset, newSizeInBytes));
  }

  @Override
  public BytePtr copyOf(int numBytes) {
    return new BytePtr(Arrays.copyOf(array, numBytes));
  }

  @Override
  public void memcpy(Ptr source, int numBytes) {
    if(source instanceof BytePtr) {
      BytePtr sourceBytePtr = (BytePtr) source;
      System.arraycopy(sourceBytePtr.array, sourceBytePtr.offset, this.array, this.offset, numBytes);
    } else {
      super.memcpy(source, numBytes);
    }
  }

  @Override
  public Ptr pointerPlus(int bytes) {
    if(bytes == 0) {
      return this;
    }
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

  @Override
  public boolean isNull() {
    return array == null && offset == 0;
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

  public static void memcpy(BytePtr x, BytePtr y, int numBytes) {
    byte[] arrayS = y.getArray();
    int offsetS = y.getOffset();
    int restY = arrayS.length - offsetS;
    if(restY > 0) {
      byte[] carray = new byte[numBytes];
      for(int i = 0, j = offsetS; j < arrayS.length && i < numBytes; j++, i++) {
        carray[i] = arrayS[j];
      }
      x = new BytePtr(carray);
    }
  }

  /**
   * Creates a pointer to an array of pointers to strings from a list of null-terminated strings.
   */
  public static Ptr stringArray(String string) {
    return stringArray(string.getBytes(StandardCharsets.US_ASCII));
  }

  public static Ptr stringArrayFromResource(Class clazz, String resourceName) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[0x1000];

    try(InputStream in = clazz.getResourceAsStream(resourceName)) {
      while (true) {
        int r = in.read(buffer);
        if (r == -1) {
          break;
        }
        baos.write(buffer, 0, r);
      }
    }
    return stringArray(baos.toByteArray());
  }

  private static Ptr stringArray(byte[] bytes) {
    List<Ptr> pointers = new ArrayList<>();
    int start = 0;
    for (int i = 0; i < bytes.length; i++) {
      if(bytes[i] == 0) {
        pointers.add(new BytePtr(bytes, start));
        start = i + 1;
      }
    }

    return new PointerPtr(pointers.toArray(new Ptr[0]));
  }
}
