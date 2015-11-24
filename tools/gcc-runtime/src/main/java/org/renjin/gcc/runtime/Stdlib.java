package org.renjin.gcc.runtime;

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
  
  public static int sprintf(BytePtr string, BytePtr format, Object... arguments) {
    return snprintf(string, Integer.MAX_VALUE, format, arguments);
  }
  
  public static int snprintf(BytePtr string, int limit, BytePtr format, Object... arguments) {

    Object[] convertedArgs = new Object[arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      convertedArgs[i] = convertFormatArg(arguments[i]);
    }
    
    byte[] outputBytes;
    try {
      outputBytes = String.format(format.nullTerminatedString(), convertedArgs).getBytes();
    } catch (Exception e) {
      return -1;
    }
        
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

  private static Object convertFormatArg(Object argument) {
    if(argument instanceof BytePtr) {
      return ((BytePtr) argument).nullTerminatedString();
    } else {
      return argument;
    }
  }
}
