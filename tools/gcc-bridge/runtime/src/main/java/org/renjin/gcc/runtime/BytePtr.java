package org.renjin.gcc.runtime;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BytePtr implements Ptr {
  
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
    return new BytePtr(array, offset + 1);
  }

  public static BytePtr cast(Object voidPointer) {
    if(voidPointer instanceof MallocThunk) {
      return ((MallocThunk) voidPointer).bytePtr();
    }
    return (BytePtr) voidPointer;
  }
}
