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

import org.renjin.gcc.annotations.Struct;
import org.renjin.gcc.format.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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

  public static int strcoll(Ptr x, Ptr y) {
    throw new UnsupportedOperationException("TODO: strcoll");
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
    return strncasecmp(x, y, Integer.MAX_VALUE);
  }

  public static int strncasecmp(Ptr x, Ptr y, int len) {
    for (int i = 0; i < len; ++i) {
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

  /**
   * Scans str1 for the first occurrence of any of the characters that are part of str2,
   * returning the number of characters of str1 read before this first occurrence.
   *
   * The search includes the terminating null-characters. Therefore, the function will return the
   * length of str1 if none of the characters of str2 are found in str1.
   */
  public static int strcspn(Ptr str1, Ptr str2) {
    int i = 0;
    byte c, d;
    while(true) {
      c = str1.getByte(i);
      int j = 0;
      do {
        d = str2.getByte(j);
        if(c == d) {
          return i;
        }
        j++;
      } while(d != 0);
      i++;
    }
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

  /**
   * Get span of character set in string
   * Returns the length of the initial portion of str1 which consists only of characters that are part of str2.
   *
   * The search does not include the terminating null-characters of either strings, but ends there.
   */
  public static int strspn ( Ptr str1, Ptr str2 ) {
    int len = 0;
    while(true) {
      byte c1 = str1.getByte(len);
      byte c2 = str2.getByte(len);
      if(c1 != c2) {
        break;
      }
      if(c1 == 0) {
        break;
      }
      len++;
    }
    return len;
  }


  public static int printf(BytePtr format, Object... arguments) {
    String outputString;

    try {
      outputString = format(format, f -> new FormatArrayInput(arguments));
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

  public static int sprintf(Ptr string, Ptr format, Object... arguments) {
    return snprintf(string, Integer.MAX_VALUE, format, arguments);
  }

  @Deprecated
  public static int sprintf(BytePtr string, BytePtr format, Object... arguments) {
    return snprintf(string, Integer.MAX_VALUE, format, arguments);
  }

  @Deprecated
  public static int snprintf(BytePtr string, int limit, BytePtr format, Object... arguments) {
    return sprintf(string, limit, format, f -> new FormatArrayInput(arguments));
  }

  public static int snprintf(Ptr string, int limit, Ptr format, Object... arguments) {
    return sprintf(string, limit, format, f -> new FormatArrayInput(arguments));
  }

  @Deprecated
  public static int vsnprintf(BytePtr string, int n, BytePtr format, Ptr argumentList) {
    return sprintf(string, n, format, f -> new VarArgsInput(f, argumentList));
  }

  public static int vsnprintf(Ptr string, int n, Ptr format, Ptr argumentList) {
    return sprintf(string, n, format, f -> new VarArgsInput(f, argumentList));
  }

  private static int sprintf(Ptr string, int limit, Ptr format, Function<Formatter, FormatInput> arguments) {
    String outputString;

    try {
      outputString = format(format, arguments);
    } catch (Exception e) {
      return -1;
    }

    byte[] outputBytes = outputString.getBytes();

    // the NULL-termination character is countered towards the limit
    int bytesToCopy = Math.min(outputBytes.length, limit - 1);

    if(string instanceof BytePtr) {
      copyToString((BytePtr) string, outputBytes, bytesToCopy);
    } else {
      copyToString(string, outputBytes, bytesToCopy);
    }

    // terminate string with null byte
    if(limit > 0) {
      string.setByte(bytesToCopy, (byte)0);
    }

    return outputBytes.length;
  }
  private static void copyToString(Ptr string, byte[] outputBytes, int n) {
    for (int i = 0; i < n; i++) {
      string.setByte(i, outputBytes[i]);
    }
  }

  private static void copyToString(BytePtr string, byte[] outputBytes, int n) {
    if(n > 0) {
      // copy the formatted string to the output
      System.arraycopy(outputBytes, 0, string.array, string.offset, n);
    }
  }

  public static int __isoc99_sscanf(Ptr str, Ptr format, Object... arguments) {
    return sscanf(str, format, arguments);
  }

  public static int sscanf(Ptr str, Ptr format, Object... arguments) {
    Formatter formatter = new Formatter(nullTerminatedString(format), Formatter.Mode.SCAN);
    return formatter.scan(new ByteCharIterator(str), arguments);
  }

  public static int __isoc99_fscanf(Ptr fileHandle, Ptr format, Object... args) {
    return fscanf(fileHandle, format, args);
  }

  public static int fscanf(Ptr fileHandle, Ptr format, Object... args) {
    FileHandle h = (FileHandle) fileHandle.getArray();
    Formatter formatter = new Formatter(nullTerminatedString(format), Formatter.Mode.SCAN);
    try(FileHandleCharIterator it = new FileHandleCharIterator(h)) {
      return formatter.scan(it, args);
    }
  }

  public static int tolower(int c) {
    return Character.toLowerCase(c);
  }

  public static int toupper(int c) {
    return Character.toUpperCase(c);
  }

  public static String format(Ptr format, Object... arguments) {
    return format(format, f -> new FormatArrayInput(arguments));
  }

  public static String format(Ptr format, Function<Formatter, FormatInput> input) {
    String formatString = nullTerminatedString(format);
    Formatter formatter = new Formatter(formatString);
    return formatter.format(input.apply(formatter));
  }

  public static void qsort(Ptr base, int nitems, int size, MethodHandle comparator) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public static void qsort(Object base, int nitems, int size, MethodHandle comparator) {
    throw new UnsupportedOperationException();
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
  @Deprecated
  public static int time(IntPtr seconds) {
    return time((Ptr)seconds);
  }

  /**
   * Returns the time since the Epoch (00:00:00 UTC, January 1, 1970), measured in seconds.
   * If seconds is not NULL, the return value is also stored in variable seconds.
   */
  public static int time(Ptr seconds) {
    int time = (int) (System.currentTimeMillis() / 1000L);
    if(!seconds.isNull()) {
      seconds.setInt(time);
    }
    return time;
  }

  private static final DateFormat CTIME_FORMAT = new SimpleDateFormat("E MMM d HH:mm:ss yyyy");

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

  /**
   *  Identifier for system-wide realtime clock.
   */
  private static final int CLOCK_REALTIME = 0;

  /**
   * High-resolution timer from the CPU.
   */
  private static final int CLOCK_MONOTONIC = 1;

  /**
   * Monotonic system-wide clock, not adjusted for frequency scaling.
   */
  private static final int CLOCK_MONOTONIC_RAW = 4;

  /**
   * Identifier for system-wide realtime clock, updated only on ticks.
   */
  private static final int CLOCK_REALTIME_COARSE = 5;

  @Deprecated
  public static int clock_gettime(int clockId, timespec tp) {
    switch (clockId) {
      case CLOCK_REALTIME:
      case CLOCK_REALTIME_COARSE:
        // Return the current time since 1970-01-01
        tp.set(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        return 0;

      case CLOCK_MONOTONIC_RAW:
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
      case CLOCK_MONOTONIC_RAW:
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

  /**
   * Get the time as well as a timezone.  The tv argument is a struct timeval (as
   * specified in <sys/time.h>):
   *
   * <pre>
   * struct timeval {
   *    time_t      tv_sec;     // seconds
   *    suseconds_t tv_usec;    // microseconds
   * }
   * </pre>
   *
   * and gives the number of seconds and microseconds since the Epoch.
   * The tz argument is a struct timezone:
   *
   * <pre>
   * struct timezone {
   *    int tz_minuteswest;     // minutes west of Greenwich
   *    int tz_dsttime;         // type of DST correction
   * };
   * </pre>
   *
   * Apparently the tz field is no longer used. This implementation sets both field
   * to zero f {@code tz} is not null.
   *
   * @param tv timeval structure to update
   * @param tz timezone structure to update
   * @return always zero for success
   */
  public static int gettimeofday(Ptr tv, Ptr tz) {
    Instant now = Instant.now();
    if(!tv.isNull()) {
      tv.setInt(0, (int) now.getEpochSecond());
      tv.setInt(4, now.getNano() / 1000);
    }
    if(!tz.isNull()) {
      // Modern systems appear to set these fields to zero...
      tv.setInt(0, 0);
      tv.setInt(4, 0);
    }
    return 0;
  }


  @Deprecated
  public static Object fopen() {
    throw new UnsupportedOperationException("Please recompile with the latest version of Renjin.");
  }

  public static Ptr fopen(Ptr filename, Ptr mode) {
    String filenameString = nullTerminatedString(filename);
    String modeString = nullTerminatedString(mode);

    try {
      return new RecordUnitPtr<>(openFile(filenameString, modeString));
    } catch (IOException e) {
      return BytePtr.NULL;
    }
  }

  public static FileHandleImpl openFile(String filenameString, String modeString) throws IOException {
    switch (modeString) {
      case "r":
      case "rb":
        return new FileHandleImpl(new RandomAccessFile(filenameString, "r"));
      case "w":
      case "wb":
        return new FileHandleImpl(new RandomAccessFile(filenameString, "rw"));

      case "w+b":
        RandomAccessFile raf = new RandomAccessFile(filenameString, "rw");
        raf.seek(raf.length());
        return new FileHandleImpl(raf);

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
      String outputString = format(format, f -> new FormatArrayInput(arguments));
      byte[] outputBytes = outputString.getBytes(StandardCharsets.UTF_8);
      Ptr outputPtr = new BytePtr(outputBytes);
      int bytesWritten = fwrite(outputPtr, 1, outputBytes.length, stream);

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

  /**
   * sets the file position to the beginning of the file of the given stream.
   */
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

  private static FileHandle fileHandle(Ptr ptr) {
    return (FileHandle)ptr.getArray();
  }

  public static int feof(Ptr handlePtr) {
    return fileHandle(handlePtr).isEof() ? 1 : 0;
  }

  /**
   * Returns the current value of the position indicator of the stream.
   *
   * For binary streams, this is the number of bytes from the beginning of the file.
   *
   * For text streams, the numerical value may not be meaningful but can still be used to
   * restore the position to the same position later using fseek (if there are characters put
   * back using ungetc still pending of being read, the behavior is undefined).
   *
   * @param handle
   * @return
   */
  public static long ftell(Ptr stream) {
    FileHandle handle = (FileHandle) stream.getArray();
    try {
      return handle.position();
    } catch (IOException e) {
      handle.setError(e);
      return -1L;
    }
  }

  public static int fputs(Ptr str, Ptr stream) {
    throw new UnsupportedOperationException("fputs");
  }


  /**
   * Reads characters from stream and stores them as a C string into str until (num-1) characters have been
   * read or either a newline or the end-of-file is reached, whichever happens first.
   *
   */
  public static Ptr fgets(Ptr str, int num, Ptr stream) {

    FileHandle handle = fileHandle(stream);

    int charsRead = 0;
    while (charsRead < (num - 1)) {
      int c;
      try {
        c = handle.read();
      } catch (IOException e) {
        // If a read error occurs, the error indicator (ferror) is set and a null pointer is also returned
        // (but the contents pointed by str may have changed).
        handle.setError(e);
        return BytePtr.NULL;
      }
      if (c == -1) {
        // If the end-of-file is encountered while attempting to read a character, the eof indicator is
        // set (feof). If this happens before any characters could be read, the pointer returned is a null
        // pointer (and the contents of str remain unchanged).
        if (charsRead == 0) {
          handle.setError(1);
          return BytePtr.NULL;
        } else {
          break;
        }
      } else if (c == 0) {
        break;
      } else {
        str.setByte(charsRead, (byte) c);
        charsRead++;

        // A newline character makes fgets stop reading, but it is considered a valid
        // character by the function and included in the string copied to str.
        if (c == '\n') {
          break;
        }
      }
    }
    str.setByte(charsRead, (byte) 0);
    return str;
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
  @Deprecated
  public static int __cxa_guard_acquire(LongPtr guard_object) {
    return __cxa_guard_acquire((Ptr)guard_object);
  }

  public static int __cxa_guard_acquire(Ptr guard_object) {
    return 1;
  }

  /**
   * Effects: Sets the first byte of the guard object to a non-zero value. This function is called after initialization
   * is complete. A thread-safe implementation will release the mutex acquired by __cxa_guard_acquire after setting
   * the first byte of the guard object.
   */
  @Deprecated
  public static void __cxa_guard_release(LongPtr guard_object) {
    __cxa_guard_release((Ptr)guard_object);
  }

  public static void __cxa_guard_release(Ptr guard_object) {
  }

  /**
   * Effects: This function is called if the initialization terminates by throwing an exception.
   */
  @Deprecated
  public static void __cxa_guard_abort(LongPtr p) {
    __cxa_guard_abort((Ptr)p);
  }

  public static void __cxa_guard_abort(Ptr p) {

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

  /**
   * Returns maximum length of a multibyte character in the current locale.
   *
   */
  public static int __ctype_get_mb_cur_max() {
    // Based on the output of the function when compiled with gcc -m32.
    return 1;
  }


  public static int mbrtowc(Ptr pwc, Ptr s, int n, Ptr ps) {
    throw new UnsupportedOperationException("TODO: mbrtowc");
  }

  public static int mbstowcs(Ptr dst, Ptr src, int len) {
    throw new UnsupportedOperationException("TODO: mbstowcs");
  }

  public static int wcrtomb(Ptr s, int wc, Ptr ps) {
    throw new UnsupportedOperationException("TODO: wcrtomb");
  }

  public static Ptr strerror(int errnum) {
    throw new UnsupportedOperationException("strerror");
  }

  public static Ptr dngettext (Ptr domainname, Ptr __msgid1, Ptr __msgid2, long n) {
    if(n > 1) {
      return __msgid2;
    } else {
      return __msgid1;
    }
  }

  public static boolean signbit(double x) {
    return x < 0;
  }

  public static boolean __signbitf(float x) {
    return x < 0;
  }
  public static boolean __signbitf128(double x) {
    return x < 0;
  }

  public static boolean __signbit(double x) {
    return x < 0;
  }

  /**
   * Returns the value of an environment variable.
   */
  public static Ptr getenv(Ptr name) {
    String value = System.getenv(Stdlib.nullTerminatedString(name));
    if(value == null || value.isEmpty()) {
      return BytePtr.NULL;
    } else {
      return BytePtr.nullTerminatedString(value, StandardCharsets.UTF_8);
    }
  }

  /**
   * This is a (deprecated) C standard library function. If a TTY is not available, the expected
   * response is NULL, so we always return null.
   */
  public static Ptr getpass(Ptr prompt) {
    return BytePtr.NULL;
  }

  /**
   * Checks if the given character is an alphanumeric character as classified by the current C locale.
   */
  public static int isalnum( int ch ) {
    boolean alnum = (ch >= 'A' && ch <= 'Z') ||
        (ch >= 'a' && ch <= 'z') ||
        (ch >= '0' && ch <= '9');

    return alnum ? 1 : 0;
  }

  /**
   * Checks if the given character is whitespace character as classified by the currently installed C locale.
   */
  public static int isspace( int ch ) {
    switch (ch) {
      case ' ':
      case '\f':
      case '\n':
      case '\r':
      case '\t':
      case 0x0b: // vertical tab
        return 1;
      default:
        return 0;
    }
  }

  /**
   *  abort the program after false assertion
   */
  public static void __assert_fail(Ptr assertion, Ptr file, int line, Ptr function) {
    throw new IllegalStateException(String.format("%s:%d: %s: Assertion %s failed",
        nullTerminatedString(file), line,
        nullTerminatedString(function),
        nullTerminatedString(assertion)));
  }

  public static void abort() {
    throw new RuntimeException("abort() invoked");
  }

  public static Ptr wmemcpy( Ptr dest, Ptr src, int count) {
    throw new UnsupportedOperationException("TODO");
  }

}
