/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
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

  public static final Ptr stdout = new RecordUnitPtr<>(new StdOutHandle(System.out));

  public static final Ptr stderr = new RecordUnitPtr<>(new StdOutHandle(System.err));

  public static final Ptr stdin = new RecordUnitPtr<>(new StdInHandle());

  @Deprecated
  public static int strncmp(BytePtr x, BytePtr y, int n) {
    return strncmp((Ptr)x, (Ptr)y, n);
  }

  public static int strncmp(Ptr x, Ptr y, int n) {
    for(int i=0;i<n;++i) {
      byte bx = x.getByte(i);
      byte by = y.getByte(i);

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


  @Deprecated
  public static int strcmp(BytePtr x, BytePtr y) {
    return strncmp((Ptr)x, (Ptr)y, Integer.MAX_VALUE);
  }

  public static int strcmp(Ptr x, Ptr y) {
    return strncmp(x, y, Integer.MAX_VALUE);
  }

  /**
   * The <code>strcasecmp()</code> function compares <code>x</code> and <code>y</code> without sensitivity to case.
   * 
   * All alphabetic characters in <code>x</code> and <code>y</code> are converted to lowercase before comparison.
   * 
   * @param x
   * @param y
   * @return a value indicating the relationship between the two strings, as follows:
   *         <table>
   *         <th>
   *         <td>Value</td>
   *         <td>Meaning</td></th>
   *         <tr>
   *         <td>Less than 0</td>
   *         <td><code>x</code> less than <code>y</code></td>
   *         </tr>
   *         <tr>
   *         <td>0</td>
   *         <td><code>x</code> equivalent to <code>y</code></td>
   *         </tr>
   *         <tr>
   *         <td>Greater than 0</td>
   *         <td><code>x</code> greater than <code>y</code></td>
   *         </tr>
   *         </table>
   */
  public static int strcasecmp(Ptr x, Ptr y) {
    for (int i = 0; i < Integer.MAX_VALUE; ++i) {
      int bx = Character.toLowerCase(x.getByte(i));
      int by = Character.toLowerCase(y.getByte(i));

      if (bx < by) {
        return -1;
      } else if (bx > by) {
        return 1;
      }
      if (bx == 0) {
        break;
      }
    }
    return 0;
  }

  /**
   * Returns a pointer to the first occurrence of character in the C string str.
   *
   * The terminating null-character is considered part of the C string. Therefore,
   * it can also be located in order to retrieve a pointer to the end of a string.
   *
   * @param string C string.
   * @param ch Character to be located. It is passed as its int promotion, but it is internally
   *          converted back to char for the comparison.
   * @return A pointer to the first occurrence of character in str.
   *    If the character is not found, the function returns a null pointer.
   */
  public static Ptr strchr(Ptr string, int ch) {
    int pos = 0;
    while(true) {
      int pc = string.getByte(pos);
      if(pc == ch) {
        return string.pointerPlus(pos);
      }
      if(pc == 0) {
        break;
      }
      pos++;
    }
    return BytePtr.NULL;
  }

  public static Ptr strrchr(Ptr string, int ch) {
    int len = 0;
    while(string.getByte(len) != 0) {
      len++;
    }
    int pos = len - 1;
    while(pos > 0) {
      int pc = string.getByte(pos);
      if(pc == ch) {
        return string.pointerPlus(pos);
      }
      pos--;
    }
    return BytePtr.NULL;
  }

  public static Ptr strstr(Ptr string, Ptr searched) {
    final int offset = nullTerminatedString(string).indexOf(nullTerminatedString(searched));
    return new OffsetPtr(string, offset);
  }

  @Deprecated
  public static long strtol(Ptr string) {
    return strtol(string, BytePtr.NULL, 10);
  }

  public static long strtol(Ptr str, Ptr endptr, int radix) {
    return strtol(str, endptr, radix, true);
  }

  /**
   * Parses the C-string str, interpreting its content as an integral number of the specified base, which is
   * returned as an value of type unsigned long int.
   *
   * @param str C-string containing the representation of an integral number.
   * @param endptr Reference to an object of type char*, whose value is set by the function to the next character
   *              in str after the numerical value. This parameter can also be a null pointer,
   *               in which case it is not used.
   * @param radix Numerical base (radix) that determines the valid characters and their interpretation.
   *        If this is 0, the base used is determined by the format in the sequence (see strtol for details).
   * @return On success, the function returns the converted integral number as an unsigned long int value.
   * If no valid conversion could be performed, a zero value is returned.
   * If the value read is out of the range of representable values by an unsigned long int, the function returns ULONG_MAX
   * (defined in <climits>), and errno is set to ERANGE.
   */
  public static long strtoul(Ptr str, Ptr endptr, int radix) {
    return strtol(str, endptr, radix, false);
  }

  public static double strtold(Ptr string) {
    return strtod(string);
  }

  public static double strtod(Ptr string) {
    return Double.parseDouble(nullTerminatedString(string));
  }

  static long strtol(Ptr str, Ptr endptr, int radix, boolean signed) {

    String s = nullTerminatedString(str);

    // Find the start of the number
    int start = 0;

    // Skip beginning whitespace
    while (start < s.length() && Character.isWhitespace(s.charAt(start))) {
      start++;
    }

    int pos = start;

    // Check for +/- prefix
    if(pos < s.length() && (s.charAt(pos) == '-' || s.charAt(pos) == '+')) {
      pos++;
    }

    // Check for hex prefix 0x/0X if the radix is 16 or unspecified
    else if( (radix == 0 || radix == 16) &&
        pos + 1 < s.length() && s.charAt(pos) == '0' &&
        (s.charAt(pos+1) == 'x' || s.charAt(pos+1) == 'X')) {
      start+=2;
      pos = start;
      radix = 16;

    }

    // If radix is not specified, then check for octal prefix
    else if(radix == 0 &&  pos < s.length() && s.charAt(pos) == '0') {
      radix = 8;
    }

    // Otherwise if radix is not specified, and there is no prefix,
    // assume decimal
    if(radix == 0) {
      radix = 10;
    }

    // Advance until we run out of digits
    while(pos < s.length() && Character.digit(s.charAt(pos), radix) != -1) {
      pos++;
    }

    // If requested, update the endptr
    if(!endptr.isNull()) {
      endptr.setPointer(str.pointerPlus(pos));
    }

    // If empty, return 0 and exit
    if(start == pos) {
      return 0;
    }

    s = s.substring(start, pos);

    if(signed) {
      try {
        return Long.parseLong(s, radix);
      } catch (NumberFormatException e) {
        return Long.MAX_VALUE;
      }
    } else {
      try {
        return Long.parseUnsignedLong(s, radix);
      } catch (NumberFormatException e) {
        return -1;
      }
    }
  }

  public static Ptr strdup(Ptr s) {
    int strlen = strlen(s);
    BytePtr dup = BytePtr.malloc(strlen + 1);
    strcpy(dup, s);
    return dup;
  }

  /**
   * Copies the C string pointed by source into the array pointed by destination, including the terminating
   * null character (and stopping at that point).
   * @return destination is returned.
   */
  @Deprecated
  public static BytePtr strcpy(BytePtr destination, BytePtr source) {
    return (BytePtr) strcpy((Ptr)destination, (Ptr)source);
  }

  /**
   * Copies the C string pointed by source into the array pointed by destination, including the terminating
   * null character (and stopping at that point).
   * @return destination is returned.
   */
  public static Ptr strcpy(Ptr destination, Ptr source) {
    int length = strlen(source);
    for (int i = 0; i < length + 1; i++) {
      destination.setByte(i, source.getByte(i));
    }
    return destination;
  }

  @Deprecated
  public static BytePtr strncpy(BytePtr destination, BytePtr source, int num) {
    return (BytePtr) strncpy((Ptr)destination, (Ptr)source, num);
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
  public static Ptr strncpy(Ptr destination, Ptr source, int num) {

    for (int i = 0; i < num; i++) {
      byte srcChar = source.getByte(i);
      destination.setByte(i, srcChar);
      if(srcChar == 0) {
        break;
      }
    }
    return destination;
  }

  @Deprecated
  public static int atoi(BytePtr str) {
    return atoi((Ptr)str);
  }

  /**
   * Converts the string argument str to an integer (type int).
   * @param str This is the string representation of an integral number.
   * @return the converted integral number as an int value. If no valid conversion could be performed, it returns zero.
   */
  public static int atoi(Ptr str) {
    try {
      return Integer.parseInt(nullTerminatedString(str));
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  public static double asinh(double x) {
    if(Double.isInfinite(x)) {
      return x;
    }
    return Math.log(x + Math.sqrt(x * x + 1));
  }

  public static double atanh(double x) {
    return 0.5 * Math.log((1d + x) / (1d - x));
  }

  public static String nullTerminatedString(Ptr x) {
    StringBuilder str = new StringBuilder();
    int i = 0;
    while(true) {
      byte b = x.getByte(i);
      if(b == 0) {
        break;
      }
      str.append((char)b); // Implicit US ASCII encoding...
      i++;
    }
    return str.toString();
  }

  @Deprecated
  public static int strlen(BytePtr x) {
    return strlen((Ptr)x);
  }

  public static int strlen(Ptr x) {
    int len = 0;
    while(true) {
      if(x.getByte(len) == 0) {
        return len;
      }
      len ++;
    }
  }

  @Deprecated
  public static BytePtr strcat(BytePtr dest, BytePtr src) {
    return (BytePtr)strcat((Ptr)dest, (Ptr)src);
  }

  /**
   * Appends the string pointed to by src to the end of the string pointed to by dest.
   *
   * @return pointer to the resulting string dest.
   */
  public static Ptr strcat(Ptr dest, Ptr src) {
    // Find the end of the dest null-terminated string
    int destPos = 0;
    while(dest.getByte(destPos) != 0) {
      destPos++;
    }
    // Copy into the dest buffer
    int srcPos = 0;
    for(;;) {
      byte srcByte = src.getByte(srcPos++);
      dest.setByte(destPos++, srcByte);
      if(srcByte == 0) {
        break;
      }
    }

    return dest;
  }

  public static int printf(BytePtr format, Object... arguments) {
    String outputString;

    try {
      outputString = format(format, arguments);
    } catch (Exception e) {
      return -1;
    }

    System.out.print(outputString);

    return outputString.length();
  }

  public static int puts(BytePtr string) {
    System.out.println(string.nullTerminatedString());
    return 0;
  }

  public static int putchar(int character) {
    System.out.println((char)character);
    return character;
  }

  public static int sprintf(BytePtr string, BytePtr format, Object... arguments) {
    return snprintf(string, Integer.MAX_VALUE, format, arguments);
  }

  public static int snprintf(BytePtr string, int limit, BytePtr format, Object... arguments) {

    String outputString;

    try {
      outputString = format(format, arguments);
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

  public static int tolower(int c) {
    return Character.toLowerCase(c);
  }

  public static int toupper(int c) {
    return Character.toUpperCase(c);
  }

  public static String format(Ptr format, Object[] arguments) {
    Object[] convertedArgs = new Object[arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      convertedArgs[i] = convertFormatArg(arguments[i]);
    }

    String formatString = nullTerminatedString(format);
    if(formatString.equals("%2.2x")) {
      return String.format("%02x", convertedArgs);
    } else if(formatString.equals("%016llx")) {
      return String.format("%016x", convertedArgs);
    } else {
      return String.format(formatString, convertedArgs);
    }
  }

  private static Object convertFormatArg(Object argument) {
    if(argument instanceof Ptr && ((Ptr) argument).isNull()) {
      return null;
    }
    if(argument instanceof BytePtr || argument instanceof MixedPtr) {
      return Stdlib.nullTerminatedString((Ptr) argument);
    } else {
      return argument;
    }
  }

  public static void qsort(Ptr base, int nitems, int size, MethodHandle comparator) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public static void qsort(Object base, int nitems, int size, MethodHandle comparator) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public static ObjectPtr<CharPtr> __ctype_b_loc() {
    return CharTypes.TABLE_OBJECT_PTR;
  }


  @Struct
  public static int[] div(int numer, int denom) {
    int quot = numer / denom;
    int rem = numer % denom;

    return new int[] { quot, rem };
  }

  public static double nearbyint(double arg) {
    return Math.round(arg);
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

  private static final DateFormat CTIME_FORMAT = new SimpleDateFormat("E MMM d HH:mm:ss YYYY");

  /**
   * Interprets the value pointed by timer as a calendar time and converts it to a C-string containing a human-readable
   * version of the corresponding time and date, in terms of local time.
   *
   * <p>The returned string has the following format:</p>
   * <blockquote>Www Mmm dd hh:mm:ss yyyy</blockquote>
   *
   * <p>The returned value points to an internal array whose validity or value may be altered by any
   * subsequent call to asctime or ctime.</p>
   *
   * @param timePtr Pointer to an object of type time_t that contains a time value.
   * @return A C-string containing the date and time information in a human-readable format.
   */
  public static BytePtr ctime(IntPtr timePtr) {
    Date date = new Date(timePtr.get() * 1000L);
    return BytePtr.nullTerminatedString(CTIME_FORMAT.format(date) + "\n", StandardCharsets.US_ASCII);
  }

  @Deprecated
  public static tm localtime(IntPtr time) {
    return new tm(time.unwrap());
  }

  public static Ptr localtime(Ptr time) {
    int instant = time.getInt();
    Calendar instance = Calendar.getInstance();
    instance.setTimeInMillis(instant);

    int[] tm = new int[9];
    tm[0] = instance.get(Calendar.SECOND);
    tm[1] = instance.get(Calendar.MINUTE);
    tm[2] = instance.get(Calendar.HOUR);
    tm[3] = instance.get(Calendar.DAY_OF_MONTH);
    tm[4] = instance.get(Calendar.MONTH);
    tm[5] = instance.get(Calendar.YEAR);
    tm[6] = instance.get(Calendar.DAY_OF_WEEK);
    tm[7] = instance.get(Calendar.DAY_OF_YEAR);
    tm[8] = instance.getTimeZone().inDaylightTime(new Date(instant)) ? 1 : 0;

    return new IntPtr(tm);
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

  @Deprecated
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

  @Deprecated
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

  public static int clock_gettime(int clockId, Ptr tp) {

    long duration;
    TimeUnit timeUnit;

    switch (clockId) {
      case CLOCK_REALTIME:
      case CLOCK_REALTIME_COARSE:
        // Return the current time since 1970-01-01
        duration = System.currentTimeMillis();
        timeUnit = TimeUnit.MILLISECONDS;
        break;

      case CLOCK_MONOTONIC:
        // Return a high precision time from some arbitrary offset
        duration = System.nanoTime();
        timeUnit = TimeUnit.NANOSECONDS;
        break;

      default:
        // ClockId not supported
        return -1;
    }

    // the timespec struct has two int members:
    // 0: number of seconds
    // 4: number of nanoseconds
    int seconds = (int) timeUnit.toSeconds(duration);
    int nanoseconds = (int) (timeUnit.toNanos(duration) - TimeUnit.SECONDS.toNanos(seconds));

    tp.setAlignedInt(0, seconds);
    tp.setAlignedInt(1, nanoseconds);

    return 0;
  }

  @Deprecated
  public static Object fopen() {
    throw new UnsupportedOperationException("Please recompile with the latest version of Renjin.");
  }

  public static Ptr fopen(Ptr filename, Ptr mode) {
    String filenameString = nullTerminatedString(filename);
    String modeString = nullTerminatedString(mode);

    switch (modeString) {
      case "r":
      case "rb":
        try {
          return new RecordUnitPtr<>(new FileHandleImpl(new RandomAccessFile(filenameString, "r")));
        } catch (FileNotFoundException e) {
          return BytePtr.NULL;
        }
      case "w":
      case "wb":
        try {
          return new RecordUnitPtr<>(new FileHandleImpl(new RandomAccessFile(filenameString, "rw")));
        } catch (FileNotFoundException e) {
          return BytePtr.NULL;
        }
      default:
        throw new UnsupportedOperationException("Not implemented. Mode = " + modeString);
    }
  }

  public static int fflush(Ptr stream) throws IOException {
    FileHandle handle = (FileHandle) stream.getArray();
    handle.flush();
    return 0;
  }

  public static int fprintf(Ptr stream, BytePtr format, Object... arguments) {
    try {
      String outputString = format(format, arguments);
      BytePtr outputBytes = BytePtr.nullTerminatedString(outputString, StandardCharsets.UTF_8);
      int bytesWritten = fwrite(outputBytes, 1, outputBytes.getArray().length, stream);

      return bytesWritten;
    } catch (Exception e) {
      return -1;
    }
  }


  @Deprecated
  public static int fwrite(BytePtr ptr, int size, int count, Ptr stream) throws IOException {
    return fwrite((Ptr)ptr, size, count, stream);
  }

  public static int fwrite(Ptr ptr, int size, int count, Ptr stream) throws IOException {
    FileHandle handle = (FileHandle) stream.getArray();
    int bytesWritten = 0;

    // Super naive implementation.
    // Performance to be improved...
    for (int i = 0; i < (count * size); ++i) {
      try {
        handle.write(ptr.getByte(i));
        bytesWritten++;
      } catch (ArrayIndexOutOfBoundsException aioobe) {
        i = count * size;
      }
    }

    return bytesWritten;
  }

  public static int ferror(Ptr stream) {
    FileHandle handle = (FileHandle) stream.getArray();

    return handle.getError();
  }

  public static void clearerr(Ptr stream) {
    FileHandle handle = (FileHandle) stream.getArray();

    handle.clearError();
  }


  /***
   *
   * @param ptr A pointer to memory that will receive the data. Must be at least (size * count) bytes.
   * @param size The size of the elements to read
   * @param count the number of elements to read
   * @param stream a pointer to a file handle created with fopen()
   * @return The total number of elements successfully read are returned as a size_t object, which is an integral
   * data type. If this number differs from the nmemb parameter,
   * then either an error had occurred or the End Of File was reached.
   */
  public static int fread(Ptr ptr, int size, int count, Ptr stream) throws IOException {

    FileHandle handle = (FileHandle) stream.getArray();

    int bytesRead = 0;

    // Super naive implementation.
    // Performance to be improved...
    for(int i=0;i<(count*size);++i) {
      int b = handle.read();
      if(b == -1) {
        break;
      }
      ptr.setByte(i, (byte)b);
      bytesRead++;
    }

    // Return the number of elements read, _not_ bytes read
    return bytesRead / size;
  }

  public static void rewind(Ptr stream) throws IOException {
    FileHandle handle = (FileHandle) stream.getArray();
    handle.rewind();
  }

  public static int fseek(Ptr stream, long offset, int whence) {
    FileHandle fileHandle = (FileHandle) stream.getArray();
    try {
      switch (whence) {
        case FileHandle.SEEK_SET:
          fileHandle.seekSet(offset);
          break;
        case FileHandle.SEEK_CURRENT:
          fileHandle.seekCurrent(offset);
          break;
        case FileHandle.SEEK_END:
          fileHandle.seekEnd(offset);
          break;
      }
      return 0;
    } catch (IOException e) {
      return -1;
    }
  }

  public static int fclose(Ptr stream) {
    try {
      ((FileHandle) stream.getArray()).close();
      return 0;
    } catch (IOException e) {
      return -1;
    }
  }

  public static int fgetc(Ptr stream) {
    try {
      return ((FileHandle) stream.getArray()).read();
    } catch (IOException e) {
      return -1;
    }
  }

  /**
   *
   * Writes a character (an unsigned char) specified by the argument char to the specified stream
   * and advances the position indicator for the stream.
   *

   *
   * @param character This is the character to be written. This is passed as its int promotion.
   * @param stream This is the pointer to a FILE object that identifies the stream where the
   *               character is to be written.
   *
   * @return If there are no errors, the same character that has been written is returned.
   * If an error occurs, EOF is returned and the error indicator is set.   \
   */
  public static int fputc(int character, Ptr stream) {
    FileHandle handle = (FileHandle) stream.getArray();
    try {
      handle.write(character);
      return character;
    } catch (IOException e) {
      return -1;
    }
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

  public static int isinf(double x) {
    return __isinf(x);
  }

  public static float logf(float x) {
    return (float)Math.log(x);
  }

  public static long lroundf(float x) {

    if(Float.isInfinite(x)) {
      if(x < 0) {
        return Long.MIN_VALUE;
      } else {
        return Long.MAX_VALUE;
      }
    }

    // Math.round() rounds ties towards positive infinity,
    // while lroundf is meant to round ties away from zero.
    long sign = (long)Math.signum(x);
    long closest = Math.round((double)Math.abs(x));

    return closest * sign;
  }

  /**
   * Effects: This function is called before initialization takes place. If this function returns 1, either
   * __cxa_guard_release or __cxa_guard_abort must be called with the same argument.
   *
   * The first byte of the guard_object is not modified by this function.
   */
  public static int __cxa_guard_acquire(LongPtr guard_object) {
    return 1;
  }

  /**
   * Effects: Sets the first byte of the guard object to a non-zero value. This function is called after initialization
   * is complete. A thread-safe implementation will release the mutex acquired by __cxa_guard_acquire after setting
   * the first byte of the guard object.
   */
  public static void __cxa_guard_release(LongPtr guard_object) {

  }

  /**
   * Effects: This function is called if the initialization terminates by throwing an exception.
   */
  public static void __cxa_guard_abort(LongPtr p) {

  }

  /**
   *  Frees memory allocated by __cxa_allocate_exception
   */
  public static void __cxa_free_exception(Ptr p) {
    // NOOP : We have a garbage collector :-P
  }

  public static void __cxa_call_unexpected(Ptr p) {
    // TODO
  }

  /**
   * Register a function to be called by exit or when a shared library is unloaded
   */
  public static int __cxa_atexit(MethodHandle fn, Ptr arg, Ptr dso_handle) {
    // TODO: This needs to be implemented properly
    return 0;
  }

  public static int posix_memalign(Ptr memPtr, int aligment, int size) {
    memPtr.setPointer(MixedPtr.malloc(size));
    return 0;
  }

  public static void inlineAssembly() {
    throw new UnsupportedOperationException("Compilation of inline assembly not supported");
  }

  /**
   * The random number generator used for srand() and rand().
   *
   * <p>Note that we don't use ThreadLocalRandom as it does not allow
   * re-initializing the seed, which srand() requires.</p>
   */
  private static final ThreadLocal<Random> RANDOM = new ThreadLocal<Random>() {
    @Override
    protected Random initialValue() {
      return new Random(1L);
    }
  };

  /**
   * The value of RAND_MAX when compiling with GCC on Linux 32 bit system.
   */
  public static final int RAND_MAX = 2147483647;

  /**
   * Initialize random number generator
   *
   * <p>The pseudo-random number generator is initialized using the argument passed as seed.
   *
   * <p>For every different seed value used in a call to srand, the pseudo-random number generator can be
   * expected to generate a different succession of results in the subsequent calls to rand.
   *
   * <p>Two different initializations with the same seed will generate the same succession of results in
   * subsequent calls to rand.
   *
   * <p>If seed is set to 1, the generator is reinitialized to its initial value and produces the same
   * values as before any call to rand or srand.
   *
   * <p>Note: Renjin's maintains a copy of the internal state on a per-thread basis.</p>
   *
   */
  public static void srand(int seed) {
    RANDOM.get().setSeed(seed);
  }

  public static int rand() {
    return RANDOM.get().nextInt(RAND_MAX);
  }

  public static int _setjmp(Ptr buf) {
    // this is a placeholder. It will actually work perfectly
    // if longjmp is never called...
    return 0;
  }

  public static void longjmp(Ptr buf, int value) {
    throw new LongJumpException(buf, value);
  }
}
