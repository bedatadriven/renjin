package org.renjin.gcc.runtime;

import java.nio.charset.StandardCharsets;

public class BytePtr implements Ptr {
  
  public final byte[] array;
  public final int offset;

  public BytePtr(byte[] array, int offset) {
    this.array = array;
    this.offset = offset;
  }
  
  public static byte[] toArray(String constant) {
    // The string literals are technically not in UTF-8 encoding:
    // the literals that GCC emits are really just a string of bytes, 
    // but during compilation we encode those byte streams as a UTF-8 string
    return constant.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * 
   * @return the length of the null-terminated string referenced by this pointer
   */
  public int stringLength() {
    int i = offset;
    while(i < array.length) {
      if(array[i] == 0) {
        return i-offset;
      }
      i++;
    }
    throw new IllegalStateException("String is not null-terminated.");
  }
  
  public String getString() {
    return new String(array, offset, stringLength(), StandardCharsets.UTF_8);
  }
}
