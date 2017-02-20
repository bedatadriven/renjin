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

import org.renjin.gcc.annotations.Struct;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * C standard library functions
 */
public class Stdlib {
  
  public static final int CLOCKS_PER_SEC = 4;

  private static long PROGRAM_START = System.currentTimeMillis();

  public static BytePtr tzname;
  public static int timezone;
  public static int daylight;

  public static int strncmp(BytePtr x, BytePtr y, int n) {
    for(int i=0;i<n;++i) {
      byte bx = x.array[x.offset+i];
      byte by = y.array[y.offset+i];

      if(bx < by) {
        return -1;
      } else if(bx > by) {
        return 1;
      }
      if(bx == 0) {
        break;
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
    System.arraycopy(source.array, source.offset, destination.array, destination.offset, length+1);
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
   * Locate first occurrence of character in string.
   *
   * Returns a pointer to the first occurrence of character in the C string str.
   *
   * The terminating null-character is considered part of the C string. Therefore,
   * it can also be located in order to retrieve a pointer to the end of a string.
   *
   * @param str C string
   * @param character Character to be located. It is passed as its int promotion, but it is internally converted
   *                  back to char for the comparison.
   * @return Returns a pointer to the first occurrence of character in the C string str.
   */
  public static BytePtr strchr(BytePtr str, int character) {
    byte search = (byte) character;
    byte[] array = str.array;
    int i = str.offset;
    while(true) {
      byte c = array[i];
      if(c == search) {
        return new BytePtr(array, i);
      }
      if(c == 0) {
        return BytePtr.NULL;
      }
      i++;
      assert i < array.length : "str is not null-terminated.";
    }
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

  /**
   * Write character to stream
   *
   * Writes a character to the stream and advances the position indicator.
   *
   * The character is written at the position indicated by the internal position indicator of the stream,
   * which is then automatically advanced by one.
   *
   * @param character The int promotion of the character to be written.
   * @param stream Pointer to a FILE object that identifies an output stream.
   * @return On success, the character written is returned.
   *  If a writing error occurs, EOF is returned and the error indicator (ferror) is set.
   */
  public static int fputc( int character, Object stream) {
    System.out.print((char)character);
    return character;
  }

  /**
   * Write character to stdout
   *
   * Writes a character to the standard output (stdout).
   *
   * It is equivalent to calling putc with stdout as second argument.
   *
   * @param character The int promotion of the character to be written.
   *                  The value is internally converted to an unsigned char when written.
   * @return On success, the character written is returned.
   * If a writing error occurs, EOF is returned and the error indicator (ferror) is set.
   */
  public static int putchar( int character ) {
    System.out.print((char)character);
    return character;
  }

  public static int fprintf(Object file, BytePtr format, Object... arguments) {
    return printf(format, arguments);
  }

  public static int printf(BytePtr format, Object... arguments) {
    String outputString;

    try {
      outputString = doFormat(format, arguments);
    } catch (Exception e) {
      return -1;
    }

    System.out.print(outputString);

    return outputString.length();
  }

  /**
   * Writes a string to stdout up to but not including the null character.
   * A newline character is appended to the output.
   *
   * @param string  This is the C string to be written.
   * @return If successful, non-negative value is returned. On error, the function returns {@code EOF}.
   */
  public static int puts(BytePtr string) {
    System.out.println(string.nullTerminatedString());
    return 0;
  }

  /**
   * Writes data from the array pointed to, by ptr to the given stream.
   *
   * This function returns the total number of elements successfully returned as a size_t object, which
   * is an integral data type. If this number differs from the nmemb parameter, it will show an error.
   *
   * @param ptr  This is the pointer to the array of elements to be written.
   * @param size This is the size in bytes of each element to be written.
   * @param nmemb This is the number of elements, each one with a size of size bytes.
   * @param stream This is the pointer to a FILE object that specifies an output stream.
   */
  public static int fwrite(Object ptr, int size, int nmemb, Object stream) {
    System.out.print(((BytePtr) ptr).nullTerminatedString());
    System.out.flush();
    return nmemb;
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

  @Deprecated
  public static void qsort(Ptr base, int nitems, int size, MethodHandle comparator) {
    throw new UnsupportedOperationException();
  }

  public static void qsort(Object base, int nitems, int size, final MethodHandle comparatorMethod) {
    if(base instanceof ObjectPtr) {

      // We need to pass the comparatorMethod a pointer, so
      // allocate a fly-weight instance now to avoid a hit on each comparison
      ObjectPtr ptr = (ObjectPtr) base;
      final Object[] array = Arrays.copyOf(ptr.array, 2);
      final ObjectPtr p1 = new ObjectPtr(array, 0);
      final ObjectPtr p2 = new ObjectPtr(array, 1);

      Comparator<Object> comparator = new Comparator<Object>() {
        @Override
        public int compare(Object x, Object y) {
          array[0] = x;
          array[1] = y;
          try {
            return (int)comparatorMethod.invoke(p1, p2);
          } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
          }
        }
      };
      ObjectPtr objectPtr = (ObjectPtr) base;
      Arrays.sort(objectPtr.array, objectPtr.offset, objectPtr.offset + nitems, comparator);

    } else {
      throw new UnsupportedOperationException("base: " + base.getClass().getName());
    }
  }


  public static ObjectPtr<CharPtr> __ctype_b_loc() {
    return CharTypes.TABLE_PTR;
  }


  @Struct
  public static int[] div(int numer, int denom) {
    int quot = numer / denom;
    int rem = numer % denom;

    return new int[] { quot, rem };
  }

  /**
   * Returns the time since the Epoch (00:00:00 UTC, January 1, 1970), measured in seconds.
   * If seconds is not NULL, the return value is also stored in variable seconds.
   */
  public static int time(IntPtr seconds) {
    int time = (int) (System.currentTimeMillis() / 1000L);
    if(seconds.array != null) {
      seconds.array[seconds.offset] = time;
    }
    return time;
  }

  public static tm localtime(IntPtr time) {
    return new tm(time.unwrap());
  }


  /**
   * The tzset function initializes the tzname variable from the value of the TZ environment variable.
   * It is not usually necessary for your program to call this function, because it is called automatically
   * when you use the other time conversion functions that depend on the time zone.
   */
  public static void tzset() {
    TimeZone currentTimezone = TimeZone.getDefault();
    tzname = BytePtr.nullTerminatedString(currentTimezone.getDisplayName(), StandardCharsets.US_ASCII);
    timezone = currentTimezone.getOffset(System.currentTimeMillis());
    daylight = currentTimezone.inDaylightTime(new Date()) ? 1 : 0;
  }

  public static void fflush(Object file) {
    // TODO: implement properly
  }

  /**
   * This function returns the number of clock ticks elapsed since the start of the program.
   * On failure, the function returns a value of -1.
   */
  public static int clock() {
    long millisSinceProgramStart = System.currentTimeMillis() - PROGRAM_START;
    int secondsSinceProgramStart = (int)TimeUnit.MILLISECONDS.toSeconds(millisSinceProgramStart);
    return secondsSinceProgramStart * CLOCKS_PER_SEC;
  }

  private static final int CLOCK_REALTIME = 0;
  private static final int CLOCK_MONOTONIC = 1;
  private static final int CLOCK_REALTIME_COARSE = 5;

  public static int clock_gettime(int clockId, timespec tp) {

    switch (clockId) {
      case CLOCK_REALTIME:
      case CLOCK_REALTIME_COARSE:
        // Return the current time since 1970-01-01
        tp.set(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        return 0;

      case CLOCK_MONOTONIC:
        // Return a high precision time from some arbitrary offset
        tp.set(System.nanoTime(), TimeUnit.NANOSECONDS);
        return 0;

      default:
        // ClockId not supported
        return -1;
    }
  }

  public static Object fopen() {
    throw new UnsupportedOperationException("fopen() not implemented");
  }

  /**
   * test for infinity.
   *
   * <p>__isinf() has the same specification as isinf() in ISO POSIX (2003), except that the
   * argument type for __isinf() is known to be double.
   *
   * <p>__isinf() is not in the source standard; it is only in the binary standard.
   * <p>See <a href="https://refspecs.linuxbase.org/LSB_3.0.0/LSB-PDA/LSB-PDA/baselib---isinf.html">
   *   Linux Standard Base PDA Specification 3.0RC1</a></p>
   */
  public static int __isinf(double x) {
    return Double.isInfinite(x) ? 1 : 0;
  }

}
