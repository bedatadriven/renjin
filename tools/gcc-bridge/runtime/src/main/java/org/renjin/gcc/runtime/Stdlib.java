package org.renjin.gcc.runtime;

import java.lang.invoke.MethodHandle;

/**
 * C standard library functions
 */
public class Stdlib {

  public static int strncmp(BytePtr x, BytePtr y, int n) {
    for(int i=0;i<n;++i) {
      byte bx = x.array[x.offset+i];
      byte by = y.array[y.offset+i];
      if(bx < by) {
        return -1;
      } else if(bx > by) {
        return 1;
      }
    }
    return 0;
  }
  
  public static int strcmp(BytePtr x, BytePtr y) {
    return strncmp(x, y, Integer.MAX_VALUE);
  }

  /**
   * Copies the C string pointed by source into the array pointed by destination, including the terminating 
   * null character (and stopping at that point).
   * @return destination is returned.
   */
  public static BytePtr strcpy(BytePtr destination, BytePtr source) {
    int length = source.nullTerminatedStringLength();
    System.arraycopy(source, source.offset, destination, destination.offset, length+1);
    return destination;
  }

  /**
   * Copies the first num characters of source to destination. 
   * If the end of the source C string (which is signaled by a null-character) is 
   * found before num characters have been copied, destination is padded with zeros until a
   * total of num characters have been written to it.
   * 
   * <p>No null-character is implicitly appended at the end of destination if source is longer than num. 
   * Thus, in this case, destination shall not be considered a null terminated C string (reading it as 
   * such would overflow).</p>
   * 
   * <p>destination and source shall not overlap</p>
   * 
   * @return destination pointer
   */
  public static BytePtr strncpy(BytePtr destination, BytePtr source, int num) {
    int di = destination.offset;
    int si = source.offset;
    
    while(num > 0) {
      byte srcChar = source.array[si++];
      destination.array[di++] = srcChar;
      num--;
      if(srcChar == 0) {
        break;
      }
    }
    while(num > 0) {
      destination.array[di++] = 0;
      num--;
    }
   
    return destination;
  }

  /**
   * Converts the string argument str to an integer (type int).
   * @param str This is the string representation of an integral number.
   * @return the converted integral number as an int value. If no valid conversion could be performed, it returns zero.
   */
  public static int atoi(BytePtr  str) {
    try {
      return Integer.parseInt(str.nullTerminatedString());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  public static int strlen(BytePtr x) {
    return x.nullTerminatedStringLength();
  }

  /**
   * Appends the string pointed to by src to the end of the string pointed to by dest.
   *
   * @return pointer to the resulting string dest.
   */
  public static BytePtr strcat(BytePtr dest, BytePtr src) {
    // Find the end of the dest null-terminated string
    int start = dest.offset;
    while(dest.array[start] != 0) {
      start++;
    }
    // Find the length of the src string
    int srcLen = strlen(src);
    
    // Copy into the dest buffer
    System.arraycopy(src.array, src.offset, dest.array, start, srcLen);
    
    // Null terminate the concatenated string
    dest.array[start+srcLen] = 0;
    
    return dest;
  }
  
  public static int printf(BytePtr format, Object... arguments) {
    String outputString;

    try {
      outputString = doFormat(format, arguments);
    } catch (Exception e) {
      return -1;
    }
    
    System.out.println(outputString);
    
    return outputString.length();
  }
  
  public static int sprintf(BytePtr string, BytePtr format, Object... arguments) {
    return snprintf(string, Integer.MAX_VALUE, format, arguments);
  }

  public static int snprintf(BytePtr string, int limit, BytePtr format, Object... arguments) {

    String outputString;

    try {
      outputString = doFormat(format, arguments);
    } catch (Exception e) {
      return -1;
    }

    byte[] outputBytes = outputString.getBytes();

    // the NULL-termination character is countered towards the limit
    int bytesToCopy = Math.min(outputBytes.length, limit-1);

    if(bytesToCopy > 0) {
      // copy the formatted string to the output
      System.arraycopy(outputBytes, 0, string.array, string.offset, bytesToCopy);

      // terminate string with null byte
      string.array[string.offset + bytesToCopy] = 0;
    }

    return outputBytes.length;
  }

  public static int sscanf(BytePtr format, Object... arguments) { 
    throw new UnsupportedOperationException("TODO: implement " + Stdlib.class.getName() + ".sscanf");
  }

  private static String doFormat(BytePtr format, Object[] arguments) {
    Object[] convertedArgs = new Object[arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      convertedArgs[i] = convertFormatArg(arguments[i]);
    }

    return String.format(format.nullTerminatedString(), convertedArgs);
  }

  private static Object convertFormatArg(Object argument) {
    if(argument instanceof BytePtr) {
      return ((BytePtr) argument).nullTerminatedString();
    } else {
      return argument;
    }
  }

  public static void qsort(Ptr base, int nitems, int size, MethodHandle comparator) {
    throw new UnsupportedOperationException();
  }
  
  public static ObjectPtr<CharPtr> __ctype_b_loc() {
    return CharTypes.TABLE_PTR;
  }

}
