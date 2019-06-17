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
package org.renjin.gcc.format;

import java.text.DecimalFormatSymbols;

/**
 * <p>
 * ConversionSpecification allows the formatting of
 * a single primitive or object embedded within a
 * string.  The formatting is controlled by a
 * format string.  Only one Java primitive or
 * object can be formatted at a time.
 * <p>
 * A format string is a Java string that contains
 * a control string.  The control string starts at
 * the first percent sign (%) in the string,
 * provided that this percent sign
 * <ol>
 * <li>is not escaped protected by a matching % or
 * is not an escape % character,
 * <li>is not at the end of the format string, and
 * <li>precedes a sequence of characters that parses
 * as a valid control string.
 * </ol>
 * <p>
 * A control string takes the form:
 * <pre> % ['-+ #0]* [0..9]* { . [0..9]* }+
 *                { [hlL] }+ [idfgGoxXeEcs]
 * </pre>
 * <p>
 * The behavior is like printf.  One (hopefully the
 * only) exception is that the minimum number of
 * exponent digits is 3 instead of 2 for e and E
 * formats when the optional L is used before the
 * e, E, g, or G conversion character.  The
 * optional L does not imply conversion to a long
 * long double.
 */
class FormatSpec {


  private static final int DOUBLE_SIGNIFICAND_WIDTH = 53;


  /**
   * Bit mask to isolate the significand field of a
   * <code>double</code>.
   */
  public static final long DOUBLE_SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL;


  /**
   * Bit mask to isolate the sign bit of a <code>double</code>.
   */
  public static final long DOUBLE_SIGN_BIT_MASK = 0x8000000000000000L;


  /**
   * Bit mask to isolate the exponent field of a
   * <code>double</code>.
   */
  public static final long DOUBLE_EXP_BIT_MASK = 0x7FF0000000000000L;


  private final DecimalFormatSymbols dfs;

  /**
   * The integer portion of the result of a decimal
   * conversion (i, d, u, f, g, or G) will be
   * formatted with thousands' grouping characters.
   * For other conversions the flag is ignored.
   */
  boolean thousands = false;
  /**
   * The result of the conversion will be
   * left-justified within the field.
   */
  boolean leftJustify = false;
  /**
   * The result of a signed conversion will always
   * begin with a sign (+ or -).
   */
  boolean leadingSign = false;
  /**
   * Flag indicating that left padding with spaces is
   * specified.
   */
  boolean leadingSpace = false;
  /**
   * For an o conversion, increase the precision to
   * force the first digit of the result to be a
   * zero.  For x (or X) conversions, a non-zero
   * result will have 0x (or 0X) prepended to it.
   * For e, E, f, g, or G conversions, the result
   * will always contain a radix character, even if
   * no digits follow the point.  For g and G
   * conversions, trailing zeros will not be removed
   * from the result.
   */
  boolean alternateForm = false;
  /**
   * Flag indicating that left padding with zeroes is
   * specified.
   */
  boolean leadingZeros = false;
  /**
   * Flag indicating that the field width is *.
   */
  boolean variableFieldWidth = false;
  /**
   * If the converted value has fewer bytes than the
   * field width, it will be padded with spaces or
   * zeroes.
   */
  int fieldWidth = 0;
  /**
   * Flag indicating whether or not the field width
   * has been set.
   */
  boolean fieldWidthSet = false;
  /**
   * The minimum number of digits to appear for the
   * d, i, o, u, x, or X conversions.  The number of
   * digits to appear after the radix character for
   * the e, E, and f conversions.  The maximum number
   * of significant digits for the g and G
   * conversions.  The maximum number of bytes to be
   * printed from a string in s and S conversions.
   */
  int precision = 0;

  /**
   * Default precision.
   */
  private final static int defaultDigits = 6;

  /**
   * Flag indicating that the precision is *.
   */
  boolean variablePrecision = false;

  /**
   * Flag indicating whether or not the precision has
   * been set.
   */
  boolean precisionSet;

  /*
   */
  boolean positionalSpecification;

  int argumentPosition;

  int argumentPositionForFieldWidth;

  int argumentPositionForPrecision;

  /**
   * Flag specifying that a following d, i, o, u, x,
   * or X conversion character applies to a type
   * short int.
   */
  boolean optionalh = false;
  /**
   * Flag specifying that a following d, i, o, u, x,
   * or X conversion character applies to a type long
   * int argument.
   */
  boolean optionall = false;

  /**
   * Flag specifying that a following e, E, f, g, or
   * G conversion character applies to a type double
   * argument.  This is a noop in Java.
   */
  boolean optionalL = false;

  String characterClass;

  boolean inverseCharacterClass;

  /**
   * Control string type.
   */
  char conversionCharacter = '\0';

  /**
   * Literal or control format string.
   */
  String literal;

  FormatSpec(DecimalFormatSymbols dfs) {
    this.dfs = dfs;
  }

  Formatter.ArgumentType parseArgumentType() {
    switch (conversionCharacter) {
      case 'd':
      case 'i':
      case 'x':
      case 'X':
      case 'u':
      case 'o':
      case 'b':
        if (optionall) {
          return Formatter.ArgumentType.LONG;
        } else {
          return Formatter.ArgumentType.INTEGER;
        }

      case 'c':
        return Formatter.ArgumentType.INTEGER;

      case 'a':
      case 'A':
      case 'f':
      case 'E':
      case 'e':
      case 'g':
      case 'G':
        return Formatter.ArgumentType.DOUBLE;

      case 'p':
        return Formatter.ArgumentType.POINTER;

      case 's':
        return Formatter.ArgumentType.STRING;

      default:
        throw new IllegalArgumentException("Invalid conversion character '" + conversionCharacter + "'");
    }
  }


  public String format(FormatInput input) {

    if (literal != null) {
      return literal;
    }

    if (input.isNA(argumentPosition)) {
      return "NA";
    }

    if (variableFieldWidth) {
      int providedWidth = input.getInt(argumentPositionForFieldWidth);
      leftJustify = (providedWidth < 0);
      fieldWidth = Math.abs(providedWidth);
    }
    if (variablePrecision) {
      precision = input.getInt(argumentPositionForPrecision);
      if (precision < 0) {
        precision = 0;
      }
    }

    switch (conversionCharacter) {
      case 'd':
      case 'i':
        if (optionall) {
          return printDFormat(input.getLong(argumentPosition));
        } else {
          return printDFormat(input.getInt(argumentPosition));
        }
      case 'u':
        if (optionall) {
          return printDFormat(Long.toUnsignedString(input.getUnsignedLong(argumentPosition)));
        } else {
          return printDFormat(Integer.toUnsignedString(input.getInt(argumentPosition)));
        }

      case 'o':
        if (optionall) {
          return printOFormat(input.getLong(argumentPosition));
        } else {
          return printOFormat(input.getInt(argumentPosition));
        }

      case 'p':
        return printXFormat(input.getInt(argumentPosition));

      case 'b':
        if (optionall) {
          return printBFormat(input.getLong(argumentPosition));
        } else {
          return printBFormat(input.getInt(argumentPosition));
        }

      case 'X':
      case 'x':
        if (optionall) {
          return printXFormat(input.getUnsignedLong(argumentPosition));
        } else {
          return printXFormat(input.getInt(argumentPosition));
        }

      case 'f':
        return printFFormat(input.getDouble(argumentPosition));

      case 'e':
      case 'E':
        return printEFormat(input.getDouble(argumentPosition));

      case 'g':
      case 'G':
        return printGFormat(input.getDouble(argumentPosition));

      case 'a':
      case 'A':
        return printAFormat(input.getDouble(argumentPosition));

      case 'c':
        return printCFormat((char) input.getInt(argumentPosition));

      case 's':
        return formatArgument(input.getString(argumentPosition));

      default:
        throw new IllegalArgumentException("Invalid conversion character '" + conversionCharacter + "'");
    }
  }

  /**
   * Format a String argument using this conversion
   * specification.
   *
   * @param s the String to format.
   * @return the formatted String.
   * @throws IllegalArgumentException if the
   *                                  conversion character is neither s nor S.
   */
  String formatArgument(String s) {
    String s2 = "";
    if (conversionCharacter == 's'
        || conversionCharacter == 'S') {
      s2 = printSFormat(s);
    } else {
      throw new IllegalArgumentException("Cannot " +
          "format a String with a format using a " +
          conversionCharacter + " conversion character.");
    }
    return s2;
  }


  /**
   * For f format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  '+' character means that the conversion
   * will always begin with a sign (+ or -).  The
   * blank flag character means that a non-negative
   * input will be preceded with a blank.  If both
   * a '+' and a ' ' are specified, the blank flag
   * is ignored.  The '0' flag character implies that
   * padding to the field width will be done with
   * zeros instead of blanks.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the number of digits
   * to appear after the radix character.  Padding is
   * with trailing 0s.
   */
  private char[] fFormatDigits(double x) {
    // int defaultDigits=6;
    String sx, sxOut;
    int i, j, k;
    int n1In, n2In;
    int expon = 0;
    boolean minusSign = false;
    if (x > 0.0) {
      sx = Double.toString(x);
    } else if (x < 0.0) {
      sx = Double.toString(-x);
      minusSign = true;
    } else {
      sx = Double.toString(x);
      if (sx.charAt(0) == '-') {
        minusSign = true;
        sx = sx.substring(1);
      }
    }
    int ePos = sx.indexOf('E');
    int rPos = sx.indexOf('.');
    if (rPos != -1) {
      n1In = rPos;
    } else if (ePos != -1) {
      n1In = ePos;
    } else {
      n1In = sx.length();
    }
    if (rPos != -1) {
      if (ePos != -1) {
        n2In = ePos - rPos - 1;
      } else {
        n2In = sx.length() - rPos - 1;
      }
    } else {
      n2In = 0;
    }
    if (ePos != -1) {
      int ie = ePos + 1;
      expon = 0;
      if (sx.charAt(ie) == '-') {
        for (++ie; ie < sx.length(); ie++) {
          if (sx.charAt(ie) != '0') {
            break;
          }
        }
        if (ie < sx.length()) {
          expon = -Integer.parseInt(sx.substring(ie));
        }
      } else {
        if (sx.charAt(ie) == '+') {
          ++ie;
        }
        for (; ie < sx.length(); ie++) {
          if (sx.charAt(ie) != '0') {
            break;
          }
        }
        if (ie < sx.length()) {
          expon = Integer.parseInt(sx.substring(ie));
        }
      }
    }
    int p;
    if (precisionSet) {
      p = precision;
    } else {
      p = defaultDigits;
    }
    char[] ca1 = sx.toCharArray();
    char[] ca2 = new char[n1In + n2In];
    char[] ca3, ca4, ca5;
    for (j = 0; j < n1In; j++) {
      ca2[j] = ca1[j];
    }
    i = j + 1;
    for (k = 0; k < n2In; j++, i++, k++) {
      ca2[j] = ca1[i];
    }
    if (n1In + expon <= 0) {
      ca3 = new char[-expon + n2In];
      for (j = 0, k = 0; k < (-n1In - expon); k++, j++) {
        ca3[j] = '0';
      }
      for (i = 0; i < (n1In + n2In); i++, j++) {
        ca3[j] = ca2[i];
      }
    } else {
      ca3 = ca2;
    }
    boolean carry = false;
    if (p < -expon + n2In) {
      if (expon < 0) {
        i = p;
      } else {
        i = p + n1In;
      }
      carry = checkForCarry(ca3, i);
      if (carry) {
        carry = startSymbolicCarry(ca3, i - 1, 0);
      }
    }
    if (n1In + expon <= 0) {
      ca4 = new char[2 + p];
      if (!carry) {
        ca4[0] = '0';
      } else {
        ca4[0] = '1';
      }
      if (alternateForm || !precisionSet || precision != 0) {
        ca4[1] = '.';
        for (i = 0, j = 2; i < Math.min(p, ca3.length); i++, j++) {
          ca4[j] = ca3[i];
        }
        for (; j < ca4.length; j++) {
          ca4[j] = '0';
        }
      }
    } else {
      if (!carry) {
        if (alternateForm || !precisionSet
            || precision != 0) {
          ca4 = new char[n1In + expon + p + 1];
        } else {
          ca4 = new char[n1In + expon];
        }
        j = 0;
      } else {
        if (alternateForm || !precisionSet
            || precision != 0) {
          ca4 = new char[n1In + expon + p + 2];
        } else {
          ca4 = new char[n1In + expon + 1];
        }
        ca4[0] = '1';
        j = 1;
      }
      for (i = 0; i < Math.min(n1In + expon, ca3.length); i++, j++) {
        ca4[j] = ca3[i];
      }
      for (; i < n1In + expon; i++, j++) {
        ca4[j] = '0';
      }
      if (alternateForm || !precisionSet || precision != 0) {
        ca4[j] = '.';
        j++;
        for (k = 0; i < ca3.length && k < p; i++, j++, k++) {
          ca4[j] = ca3[i];
        }
        for (; j < ca4.length; j++) {
          ca4[j] = '0';
        }
      }
    }
    int nZeros = 0;
    if (!leftJustify && leadingZeros) {
      int xThousands = 0;
      if (thousands) {
        int xlead = 0;
        if (ca4[0] == '+' || ca4[0] == '-' || ca4[0] == ' ') {
          xlead = 1;
        }
        int xdp = xlead;
        for (; xdp < ca4.length; xdp++) {
          if (ca4[xdp] == '.') {
            break;
          }
        }
        xThousands = (xdp - xlead) / 3;
      }
      if (fieldWidthSet) {
        nZeros = fieldWidth - ca4.length;
      }
      if ((!minusSign && (leadingSign || leadingSpace)) || minusSign) {
        nZeros--;
      }
      nZeros -= xThousands;
      if (nZeros < 0) {
        nZeros = 0;
      }
    }
    j = 0;
    if ((!minusSign && (leadingSign || leadingSpace)) || minusSign) {
      ca5 = new char[ca4.length + nZeros + 1];
      j++;
    } else {
      ca5 = new char[ca4.length + nZeros];
    }
    if (!minusSign) {
      if (leadingSign) {
        ca5[0] = '+';
      }
      if (leadingSpace) {
        ca5[0] = ' ';
      }
    } else {
      ca5[0] = '-';
    }
    for (i = 0; i < nZeros; i++, j++) {
      ca5[j] = '0';
    }
    for (i = 0; i < ca4.length; i++, j++) {
      ca5[j] = ca4[i];
    }

    int lead = 0;
    if (ca5[0] == '+' || ca5[0] == '-' || ca5[0] == ' ') {
      lead = 1;
    }
    int dp = lead;
    for (; dp < ca5.length; dp++) {
      if (ca5[dp] == '.') {
        break;
      }
    }
    int nThousands = (dp - lead) / 3;
    // Localize the decimal point.
    if (dp < ca5.length) {
      ca5[dp] = dfs.getDecimalSeparator();
    }
    char[] ca6 = ca5;
    if (thousands && nThousands > 0) {
      ca6 = new char[ca5.length + nThousands + lead];
      ca6[0] = ca5[0];
      for (i = lead, k = lead; i < dp; i++) {
        if (i > 0 && (dp - i) % 3 == 0) {
          // ca6[k]=',';
          ca6[k] = dfs.getGroupingSeparator();
          ca6[k + 1] = ca5[i];
          k += 2;
        } else {
          ca6[k] = ca5[i];
          k++;
        }
      }
      for (; i < ca5.length; i++, k++) {
        ca6[k] = ca5[i];
      }
    }
    return ca6;
  }

  /**
   * An intermediate routine on the way to creating
   * an f format String.  The method decides whether
   * the input double value is an infinity,
   * not-a-number, or a finite double and formats
   * each type of input appropriately.
   *
   * @param x the double value to be formatted.
   * @return the converted double value.
   */
  private String fFormatString(double x) {
    boolean noDigits = false;
    char[] ca6, ca7;
    if (Double.isInfinite(x)) {
      if (x == Double.POSITIVE_INFINITY) {
        if (leadingSign) {
          ca6 = "+Inf".toCharArray();
        } else if (leadingSpace) {
          ca6 = " Inf".toCharArray();
        } else {
          ca6 = "Inf".toCharArray();
        }
      } else {
        ca6 = "-Inf".toCharArray();
      }
      noDigits = true;
    } else if (Double.isNaN(x)) {
      if (leadingSign) {
        ca6 = "+NaN".toCharArray();
      } else if (leadingSpace) {
        ca6 = " NaN".toCharArray();
      } else {
        ca6 = "NaN".toCharArray();
      }
      noDigits = true;
    } else {
      ca6 = fFormatDigits(x);
    }
    ca7 = applyFloatPadding(ca6, false);
    return new String(ca7);
  }

  /**
   * For e format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  '+' character means that the conversion
   * will always begin with a sign (+ or -).  The
   * blank flag character means that a non-negative
   * input will be preceded with a blank.  If both a
   * '+' and a ' ' are specified, the blank flag is
   * ignored.  The '0' flag character implies that
   * padding to the field width will be done with
   * zeros instead of blanks.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear after the radix character.
   * Padding is with trailing 0s.
   * <p>
   * The behavior is like printf.  One (hopefully the
   * only) exception is that the minimum number of
   * exponent digits is 3 instead of 2 for e and E
   * formats when the optional L is used before the
   * e, E, g, or G conversion character. The optional
   * L does not imply conversion to a long long
   * double.
   */
  private char[] eFormatDigits(double x, char eChar) {
    char[] ca1, ca2, ca3;
    // int defaultDigits=6;
    String sx, sxOut;
    int i, j, k, p;
    int n1In, n2In;
    int expon = 0;
    int ePos, rPos, eSize;
    boolean minusSign = false;
    if (x > 0.0) {
      sx = Double.toString(x);
    } else if (x < 0.0) {
      sx = Double.toString(-x);
      minusSign = true;
    } else {
      sx = Double.toString(x);
      if (sx.charAt(0) == '-') {
        minusSign = true;
        sx = sx.substring(1);
      }
    }
    ePos = sx.indexOf('E');
    if (ePos == -1) {
      ePos = sx.indexOf('e');
    }
    rPos = sx.indexOf('.');
    if (rPos != -1) {
      n1In = rPos;
    } else if (ePos != -1) {
      n1In = ePos;
    } else {
      n1In = sx.length();
    }
    if (rPos != -1) {
      if (ePos != -1) {
        n2In = ePos - rPos - 1;
      } else {
        n2In = sx.length() - rPos - 1;
      }
    } else {
      n2In = 0;
    }
    if (ePos != -1) {
      int ie = ePos + 1;
      expon = 0;
      if (sx.charAt(ie) == '-') {
        for (++ie; ie < sx.length(); ie++) {
          if (sx.charAt(ie) != '0') {
            break;
          }
        }
        if (ie < sx.length()) {
          expon = -Integer.parseInt(sx.substring(ie));
        }
      } else {
        if (sx.charAt(ie) == '+') {
          ++ie;
        }
        for (; ie < sx.length(); ie++) {
          if (sx.charAt(ie) != '0') {
            break;
          }
        }
        if (ie < sx.length()) {
          expon = Integer.parseInt(sx.substring(ie));
        }
      }
    }
    if (rPos != -1) {
      expon += rPos - 1;
    }
    if (precisionSet) {
      p = precision;
    } else {
      p = defaultDigits;
    }
    if (rPos != -1 && ePos != -1) {
      ca1 = (sx.substring(0, rPos) +
          sx.substring(rPos + 1, ePos)).toCharArray();
    } else if (rPos != -1) {
      ca1 = (sx.substring(0, rPos) +
          sx.substring(rPos + 1)).toCharArray();
    } else if (ePos != -1) {
      ca1 = sx.substring(0, ePos).toCharArray();
    } else {
      ca1 = sx.toCharArray();
    }
    boolean carry = false;
    int i0 = 0;
    if (ca1[0] != '0') {
      i0 = 0;
    } else {
      for (i0 = 0; i0 < ca1.length; i0++) {
        if (ca1[i0] != '0') {
          break;
        }
      }
    }
    if (i0 + p < ca1.length - 1) {
      carry = checkForCarry(ca1, i0 + p + 1);
      if (carry) {
        carry = startSymbolicCarry(ca1, i0 + p, i0);
      }
      if (carry) {
        ca2 = new char[i0 + p + 1];
        ca2[i0] = '1';
        for (j = 0; j < i0; j++) {
          ca2[j] = '0';
        }
        for (i = i0, j = i0 + 1; j < p + 1; i++, j++) {
          ca2[j] = ca1[i];
        }
        expon++;
        ca1 = ca2;
      }
    }
    if (Math.abs(expon) < 100 && !optionalL) {
      eSize = 4;
    } else {
      eSize = 5;
    }
    if (alternateForm || !precisionSet || precision != 0) {
      ca2 = new char[2 + p + eSize];
    } else {
      ca2 = new char[1 + eSize];
    }
    if (ca1[0] != '0') {
      ca2[0] = ca1[0];
      j = 1;
    } else {
      for (j = 1; j < (ePos == -1 ? ca1.length : ePos); j++) {
        if (ca1[j] != '0') {
          break;
        }
      }
      if ((ePos != -1 && j < ePos) ||
          (ePos == -1 && j < ca1.length)) {
        ca2[0] = ca1[j];
        expon -= j;
        j++;
      } else {
        ca2[0] = '0';
        j = 2;
      }
    }
    if (alternateForm || !precisionSet || precision != 0) {
      ca2[1] = '.';
      i = 2;
    } else {
      i = 1;
    }
    for (k = 0; k < p && j < ca1.length; j++, i++, k++) {
      ca2[i] = ca1[j];
    }
    for (; i < ca2.length - eSize; i++) {
      ca2[i] = '0';
    }
    ca2[i++] = eChar;
    if (expon < 0) {
      ca2[i++] = '-';
    } else {
      ca2[i++] = '+';
    }
    expon = Math.abs(expon);
    if (expon >= 100) {
      switch (expon / 100) {
        case 1:
          ca2[i] = '1';
          break;
        case 2:
          ca2[i] = '2';
          break;
        case 3:
          ca2[i] = '3';
          break;
        case 4:
          ca2[i] = '4';
          break;
        case 5:
          ca2[i] = '5';
          break;
        case 6:
          ca2[i] = '6';
          break;
        case 7:
          ca2[i] = '7';
          break;
        case 8:
          ca2[i] = '8';
          break;
        case 9:
          ca2[i] = '9';
          break;
      }
      i++;
    }
    switch ((expon % 100) / 10) {
      case 0:
        ca2[i] = '0';
        break;
      case 1:
        ca2[i] = '1';
        break;
      case 2:
        ca2[i] = '2';
        break;
      case 3:
        ca2[i] = '3';
        break;
      case 4:
        ca2[i] = '4';
        break;
      case 5:
        ca2[i] = '5';
        break;
      case 6:
        ca2[i] = '6';
        break;
      case 7:
        ca2[i] = '7';
        break;
      case 8:
        ca2[i] = '8';
        break;
      case 9:
        ca2[i] = '9';
        break;
    }
    i++;
    switch (expon % 10) {
      case 0:
        ca2[i] = '0';
        break;
      case 1:
        ca2[i] = '1';
        break;
      case 2:
        ca2[i] = '2';
        break;
      case 3:
        ca2[i] = '3';
        break;
      case 4:
        ca2[i] = '4';
        break;
      case 5:
        ca2[i] = '5';
        break;
      case 6:
        ca2[i] = '6';
        break;
      case 7:
        ca2[i] = '7';
        break;
      case 8:
        ca2[i] = '8';
        break;
      case 9:
        ca2[i] = '9';
        break;
    }
    int nZeros = 0;
    if (!leftJustify && leadingZeros) {
      int xThousands = 0;
      if (thousands) {
        int xlead = 0;
        if (ca2[0] == '+' || ca2[0] == '-' || ca2[0] == ' ') {
          xlead = 1;
        }
        int xdp = xlead;
        for (; xdp < ca2.length; xdp++) {
          if (ca2[xdp] == '.') {
            break;
          }
        }
        xThousands = (xdp - xlead) / 3;
      }
      if (fieldWidthSet) {
        nZeros = fieldWidth - ca2.length;
      }
      if ((!minusSign && (leadingSign || leadingSpace)) || minusSign) {
        nZeros--;
      }
      nZeros -= xThousands;
      if (nZeros < 0) {
        nZeros = 0;
      }
    }
    j = 0;
    if ((!minusSign && (leadingSign || leadingSpace)) || minusSign) {
      ca3 = new char[ca2.length + nZeros + 1];
      j++;
    } else {
      ca3 = new char[ca2.length + nZeros];
    }
    if (!minusSign) {
      if (leadingSign) {
        ca3[0] = '+';
      }
      if (leadingSpace) {
        ca3[0] = ' ';
      }
    } else {
      ca3[0] = '-';
    }
    for (k = 0; k < nZeros; j++, k++) {
      ca3[j] = '0';
    }
    for (i = 0; i < ca2.length && j < ca3.length; i++, j++) {
      ca3[j] = ca2[i];
    }

    int lead = 0;
    if (ca3[0] == '+' || ca3[0] == '-' || ca3[0] == ' ') {
      lead = 1;
    }
    int dp = lead;
    for (; dp < ca3.length; dp++) {
      if (ca3[dp] == '.') {
        break;
      }
    }
    int nThousands = dp / 3;
    // Localize the decimal point.
    if (dp < ca3.length) {
      ca3[dp] = dfs.getDecimalSeparator();
    }
    char[] ca4 = ca3;
    if (thousands && nThousands > 0) {
      ca4 = new char[ca3.length + nThousands + lead];
      ca4[0] = ca3[0];
      for (i = lead, k = lead; i < dp; i++) {
        if (i > 0 && (dp - i) % 3 == 0) {
          // ca4[k]=',';
          ca4[k] = dfs.getGroupingSeparator();
          ca4[k + 1] = ca3[i];
          k += 2;
        } else {
          ca4[k] = ca3[i];
          k++;
        }
      }
      for (; i < ca3.length; i++, k++) {
        ca4[k] = ca3[i];
      }
    }
    return ca4;
  }

  /**
   * Check to see if the digits that are going to
   * be truncated because of the precision should
   * force a round in the preceding digits.
   *
   * @param ca1    the array of digits
   * @param icarry the index of the first digit that
   *               is to be truncated from the print
   * @return <code>true</code> if the truncation forces
   * a round that will change the print
   */
  private boolean checkForCarry(char[] ca1, int icarry) {
    boolean carry = false;
    if (icarry < ca1.length) {
      if (ca1[icarry] == '6' || ca1[icarry] == '7'
          || ca1[icarry] == '8' || ca1[icarry] == '9') {
        carry = true;
      } else if (ca1[icarry] == '5') {
        int ii = icarry + 1;
        for (; ii < ca1.length; ii++) {
          if (ca1[ii] != '0') {
            break;
          }
        }
        carry = ii < ca1.length;
        if (!carry && icarry > 0) {
          carry = (ca1[icarry - 1] == '1' || ca1[icarry - 1] == '3'
              || ca1[icarry - 1] == '5' || ca1[icarry - 1] == '7'
              || ca1[icarry - 1] == '9');
        }
      }
    }
    return carry;
  }

  /**
   * Start the symbolic carry process.  The process
   * is not quite finished because the symbolic
   * carry may change the length of the string and
   * change the exponent (in e format).
   *
   * @param cLast  index of the last digit changed
   *               by the round
   * @param cFirst index of the first digit allowed
   *               to be changed by this phase of the round
   * @return <code>true</code> if the carry forces
   * a round that will change the print still
   * more
   */
  private boolean startSymbolicCarry(
      char[] ca, int cLast, int cFirst) {
    boolean carry = true;
    for (int i = cLast; carry && i >= cFirst; i--) {
      carry = false;
      switch (ca[i]) {
        case '0':
          ca[i] = '1';
          break;
        case '1':
          ca[i] = '2';
          break;
        case '2':
          ca[i] = '3';
          break;
        case '3':
          ca[i] = '4';
          break;
        case '4':
          ca[i] = '5';
          break;
        case '5':
          ca[i] = '6';
          break;
        case '6':
          ca[i] = '7';
          break;
        case '7':
          ca[i] = '8';
          break;
        case '8':
          ca[i] = '9';
          break;
        case '9':
          ca[i] = '0';
          carry = true;
          break;
      }
    }
    return carry;
  }

  /**
   * An intermediate routine on the way to creating
   * an e format String.  The method decides whether
   * the input double value is an infinity,
   * not-a-number, or a finite double and formats
   * each type of input appropriately.
   *
   * @param x     the double value to be formatted.
   * @param eChar an 'e' or 'E' to use in the
   *              converted double value.
   * @return the converted double value.
   */
  private String eFormatString(double x, char eChar) {
    boolean noDigits = false;
    char[] ca4, ca5;
    if (Double.isInfinite(x)) {
      if (x == Double.POSITIVE_INFINITY) {
        if (leadingSign) {
          ca4 = "+Inf".toCharArray();
        } else if (leadingSpace) {
          ca4 = " Inf".toCharArray();
        } else {
          ca4 = "Inf".toCharArray();
        }
      } else {
        ca4 = "-Inf".toCharArray();
      }
      noDigits = true;
    } else if (Double.isNaN(x)) {
      if (leadingSign) {
        ca4 = "+NaN".toCharArray();
      } else if (leadingSpace) {
        ca4 = " NaN".toCharArray();
      } else {
        ca4 = "NaN".toCharArray();
      }
      noDigits = true;
    } else {
      ca4 = eFormatDigits(x, eChar);
    }
    ca5 = applyFloatPadding(ca4, false);
    return new String(ca5);
  }

  /**
   * Apply zero or blank, left or right padding.
   *
   * @param ca4      array of characters before padding is
   *                 finished
   * @param noDigits NaN or signed Inf
   * @return a padded array of characters
   */
  private char[] applyFloatPadding(
      char[] ca4, boolean noDigits) {
    char[] ca5 = ca4;
    if (fieldWidthSet) {
      int i, j, nBlanks;
      if (leftJustify) {
        nBlanks = fieldWidth - ca4.length;
        if (nBlanks > 0) {
          ca5 = new char[ca4.length + nBlanks];
          for (i = 0; i < ca4.length; i++) {
            ca5[i] = ca4[i];
          }
          for (j = 0; j < nBlanks; j++, i++) {
            ca5[i] = ' ';
          }
        }
      } else if (!leadingZeros || noDigits) {
        nBlanks = fieldWidth - ca4.length;
        if (nBlanks > 0) {
          ca5 = new char[ca4.length + nBlanks];
          for (i = 0; i < nBlanks; i++) {
            ca5[i] = ' ';
          }
          for (j = 0; j < ca4.length; i++, j++) {
            ca5[i] = ca4[j];
          }
        }
      } else if (leadingZeros) {
        nBlanks = fieldWidth - ca4.length;
        if (nBlanks > 0) {
          ca5 = new char[ca4.length + nBlanks];
          i = 0;
          j = 0;
          if (ca4[0] == '-') {
            ca5[0] = '-';
            i++;
            j++;
          }
          for (int k = 0; k < nBlanks; i++, k++) {
            ca5[i] = '0';
          }
          for (; j < ca4.length; i++, j++) {
            ca5[i] = ca4[j];
          }
        }
      }
    }
    return ca5;
  }

  /**
   * Format method for the f conversion character.
   *
   * @param x the double to format.
   * @return the formatted String.
   */
  private String printFFormat(double x) {
    return fFormatString(x);
  }

  /**
   * Format method for the e or E conversion
   * character.
   *
   * @param x the double to format.
   * @return the formatted String.
   */
  private String printEFormat(double x) {
    if (conversionCharacter == 'e') {
      return eFormatString(x, 'e');
    } else {
      return eFormatString(x, 'E');
    }
  }

  /**
   * Format method for the g conversion character.
   * <p>
   * For g format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  '+' character means that the conversion
   * will always begin with a sign (+ or -).  The
   * blank flag character means that a non-negative
   * input will be preceded with a blank.  If both a
   * '+' and a ' ' are specified, the blank flag is
   * ignored.  The '0' flag character implies that
   * padding to the field width will be done with
   * zeros instead of blanks.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear after the radix character.
   * Padding is with trailing 0s.
   *
   * @param x the double to format.
   * @return the formatted String.
   */
  private String printGFormat(double x) {
    String sx, sy, sz, ret;
    int savePrecision = precision;
    int i;
    char[] ca4, ca5;
    boolean noDigits = false;
    if (Double.isInfinite(x)) {
      if (x == Double.POSITIVE_INFINITY) {
        if (leadingSign) {
          ca4 = "+Inf".toCharArray();
        } else if (leadingSpace) {
          ca4 = " Inf".toCharArray();
        } else {
          ca4 = "Inf".toCharArray();
        }
      } else {
        ca4 = "-Inf".toCharArray();
      }
      noDigits = true;
    } else if (Double.isNaN(x)) {
      if (leadingSign) {
        ca4 = "+NaN".toCharArray();
      } else if (leadingSpace) {
        ca4 = " NaN".toCharArray();
      } else {
        ca4 = "NaN".toCharArray();
      }
      noDigits = true;
    } else {
      if (!precisionSet) {
        precision = defaultDigits;
      }
      if (precision == 0) {
        precision = 1;
      }
      int ePos = -1;
      if (conversionCharacter == 'g') {
        sx = eFormatString(x, 'e').trim();
        ePos = sx.indexOf('e');
      } else {
        sx = eFormatString(x, 'E').trim();
        ePos = sx.indexOf('E');
      }
      i = ePos + 1;
      int expon = 0;
      if (sx.charAt(i) == '-') {
        for (++i; i < sx.length(); i++) {
          if (sx.charAt(i) != '0') {
            break;
          }
        }
        if (i < sx.length()) {
          expon = -Integer.parseInt(sx.substring(i));
        }
      } else {
        if (sx.charAt(i) == '+') {
          ++i;
        }
        for (; i < sx.length(); i++) {
          if (sx.charAt(i) != '0') {
            break;
          }
        }
        if (i < sx.length()) {
          expon = Integer.parseInt(sx.substring(i));
        }
      }
      // Trim trailing zeros.
      // If the radix character is not followed by
      // a digit, trim it, too.
      if (!alternateForm) {
        if (expon >= -4 && expon < precision) {
          sy = fFormatString(x).trim();
        } else {
          sy = sx.substring(0, ePos);
        }
        i = sy.length() - 1;
        for (; i >= 0; i--) {
          if (sy.charAt(i) != '0') {
            break;
          }
        }
        if (i >= 0 && sy.charAt(i) == '.') {
          i--;
        }
        if (i == -1) {
          sz = "0";
        } else if (!Character.isDigit(sy.charAt(i))) {
          sz = sy.substring(0, i + 1) + "0";
        } else {
          sz = sy.substring(0, i + 1);
        }
        if (expon >= -4 && expon < precision) {
          ret = sz;
        } else {
          ret = sz + sx.substring(ePos);
        }
      } else {
        if (expon >= -4 && expon < precision) {
          ret = fFormatString(x).trim();
        } else {
          ret = sx;
        }
      }
      // leading space was trimmed off during
      // construction
      if (leadingSpace) {
        if (x >= 0) {
          ret = " " + ret;
        }
      }
      ca4 = ret.toCharArray();
    }
    // Pad with blanks or zeros.
    ca5 = applyFloatPadding(ca4, false);
    precision = savePrecision;
    return new String(ca5);
  }

  private String printAFormat(double x) {
    int savePrecision = precision;
    int i;
    char[] ca4, ca5;
    boolean noDigits = false;
    if (Double.isInfinite(x)) {
      if (x == Double.POSITIVE_INFINITY) {
        if (leadingSign) {
          ca4 = "+Inf".toCharArray();
        } else if (leadingSpace) {
          ca4 = " Inf".toCharArray();
        } else {
          ca4 = "Inf".toCharArray();
        }
      } else {
        ca4 = "-Inf".toCharArray();
      }
      noDigits = true;
    } else if (Double.isNaN(x)) {
      if (leadingSign) {
        ca4 = "+NaN".toCharArray();
      } else if (leadingSpace) {
        ca4 = " NaN".toCharArray();
      } else {
        ca4 = "NaN".toCharArray();
      }
      noDigits = true;
    } else {
      if (!precisionSet) {
        precision = 15;
      }
      if (precision == 0) {
        precision = 1;
      }

      String hexString = "0x" + hexDouble(x, precision);
      if (conversionCharacter == 'A') {
        hexString = hexString.toUpperCase();
      }
      ca4 = hexString.toCharArray();
    }
    // Pad with blanks or zeros.
    ca5 = applyFloatPadding(ca4, false);
    precision = savePrecision;
    return new String(ca5);
  }

  // Method assumes that d > 0.
  private String hexDouble(double d, int prec) {

    double scaleUp;

    // Let Double.toHexString handle simple cases
    if (!Double.isFinite(d) || d == 0.0 || prec == 0 || prec >= 13) {
      return toHexString(d).substring(2);

    } else {
      int exponent = Math.getExponent(d);
      boolean subnormal
          = (exponent == Double.MIN_EXPONENT - 1);

      // If this is subnormal input so normalize (could be faster to
      // do as integer operation).
      if (subnormal) {
        scaleUp = Math.scalb(1.0, 54);
        d *= scaleUp;
        // Calculate the exponent.  This is not just exponent + 54
        // since the former is not the normalized exponent.
        exponent = Math.getExponent(d);
        assert exponent >= Double.MIN_EXPONENT &&
            exponent <= Double.MAX_EXPONENT : exponent;
      }

      int precision = 1 + prec * 4;
      int shiftDistance
          = DOUBLE_SIGNIFICAND_WIDTH - precision;
      assert (shiftDistance >= 1 && shiftDistance < DOUBLE_SIGNIFICAND_WIDTH);

      long doppel = Double.doubleToLongBits(d);
      // Deterime the number of bits to keep.
      long newSignif
          = (doppel & (DOUBLE_EXP_BIT_MASK
          | DOUBLE_SIGNIF_BIT_MASK))
          >> shiftDistance;
      // Bits to round away.
      long roundingBits = doppel & ~(~0L << shiftDistance);

      // To decide how to round, look at the low-order bit of the
      // working significand, the highest order discarded bit (the
      // round bit) and whether any of the lower order discarded bits
      // are nonzero (the sticky bit).

      boolean leastZero = (newSignif & 0x1L) == 0L;
      boolean round
          = ((1L << (shiftDistance - 1)) & roundingBits) != 0L;
      boolean sticky = shiftDistance > 1 &&
          (~(1L << (shiftDistance - 1)) & roundingBits) != 0;
      if ((leastZero && round && sticky) || (!leastZero && round)) {
        newSignif++;
      }

      long signBit = doppel & DOUBLE_SIGN_BIT_MASK;
      newSignif = signBit | (newSignif << shiftDistance);
      double result = Double.longBitsToDouble(newSignif);

      if (Double.isInfinite(result)) {
        // Infinite result generated by rounding
        return "1.0p+1024";
      } else {
        String res = toHexString(result).substring(2);
        if (!subnormal) {
          return res;
        } else {
          // Create a normalized subnormal string.
          int idx = res.indexOf('p');
          if (idx == -1) {
            // No 'p' character in hex string.
            assert false;
            return null;
          } else {
            // Get exponent and append at the end.
            String exp = res.substring(idx + 1);
            int iexp = Integer.parseInt(exp) - 54;
            return res.substring(0, idx) + "p"
                + (iexp < 0 ? "" : "+")
                + Integer.toString(iexp);
          }
        }
      }
    }
  }

  /**
   * Returns a hexadecimal string representation of the
   * {@code double} argument. All characters mentioned below
   * are ASCII characters.
   * <p>
   * <ul>
   * <li>If the argument is NaN, the result is the string
   * "{@code NaN}".
   * <li>Otherwise, the result is a string that represents the sign
   * and magnitude of the argument. If the sign is negative, the
   * first character of the result is '{@code -}'
   * ({@code '\u005Cu002D'}); if the sign is positive, no sign
   * character appears in the result. As for the magnitude <i>m</i>:
   * <p>
   * <ul>
   * <li>If <i>m</i> is infinity, it is represented by the string
   * {@code "Infinity"}; thus, positive infinity produces the
   * result {@code "Infinity"} and negative infinity produces
   * the result {@code "-Infinity"}.
   * <p>
   * <li>If <i>m</i> is zero, it is represented by the string
   * {@code "0x0.0p0"}; thus, negative zero produces the result
   * {@code "-0x0.0p0"} and positive zero produces the result
   * {@code "0x0.0p0"}.
   * <p>
   * <li>If <i>m</i> is a {@code double} value with a
   * normalized representation, substrings are used to represent the
   * significand and exponent fields.  The significand is
   * represented by the characters {@code "0x1."}
   * followed by a lowercase hexadecimal representation of the rest
   * of the significand as a fraction.  Trailing zeros in the
   * hexadecimal representation are removed unless all the digits
   * are zero, in which case a single zero is used. Next, the
   * exponent is represented by {@code "p"} followed
   * by a decimal string of the unbiased exponent as if produced by
   * a call to {@link Integer#toString(int) Integer.toString} on the
   * exponent value.
   * <p>
   * <li>If <i>m</i> is a {@code double} value with a subnormal
   * representation, the significand is represented by the
   * characters {@code "0x0."} followed by a
   * hexadecimal representation of the rest of the significand as a
   * fraction.  Trailing zeros in the hexadecimal representation are
   * removed. Next, the exponent is represented by
   * {@code "p-1022"}.  Note that there must be at
   * least one nonzero digit in a subnormal significand.
   * <p>
   * </ul>
   * <p>
   * </ul>
   * <p>
   * <table border>
   * <caption>Examples</caption>
   * <tr><th>Floating-point Value</th><th>Hexadecimal String</th>
   * <tr><td>{@code 1.0}</td> <td>{@code 0x1.0p0}</td>
   * <tr><td>{@code -1.0}</td>        <td>{@code -0x1.0p0}</td>
   * <tr><td>{@code 2.0}</td> <td>{@code 0x1.0p1}</td>
   * <tr><td>{@code 3.0}</td> <td>{@code 0x1.8p1}</td>
   * <tr><td>{@code 0.5}</td> <td>{@code 0x1.0p-1}</td>
   * <tr><td>{@code 0.25}</td>        <td>{@code 0x1.0p-2}</td>
   * <tr><td>{@code Double.MAX_VALUE}</td>
   * <td>{@code 0x1.fffffffffffffp1023}</td>
   * <tr><td>{@code Minimum Normal Value}</td>
   * <td>{@code 0x1.0p-1022}</td>
   * <tr><td>{@code Maximum Subnormal Value}</td>
   * <td>{@code 0x0.fffffffffffffp-1022}</td>
   * <tr><td>{@code Double.MIN_VALUE}</td>
   * <td>{@code 0x0.0000000000001p-1022}</td>
   * </table>
   *
   * @param d the {@code double} to be converted.
   * @return a hex string representation of the argument.
   * @author Joseph D. Darcy
   * @since 1.5
   */
  public String toHexString(double d) {
    /*
     * Modeled after the "a" conversion specifier in C99, section
     * 7.19.6.1; however, the output of this method is more
     * tightly specified.
     */
    assert Double.isFinite(d);
    // Initialized to maximum size of output.
    StringBuilder answer = new StringBuilder(24);

    if (Math.copySign(1.0, d) == -1.0)    // value is negative,
    {
      answer.append("-");                  // so append sign info
    }

    answer.append("0x");

    d = Math.abs(d);

    if (d == 0.0) {
      answer.append("0.0p+0");
    } else {
      boolean subnormal = (d < Double.MIN_NORMAL);

      // Isolate significand bits and OR in a high-order bit
      // so that the string representation has a known
      // length.
      long signifBits = (Double.doubleToLongBits(d)
          & DOUBLE_SIGNIF_BIT_MASK) |
          0x1000000000000000L;

      // Subnormal values have a 0 implicit bit; normal
      // values have a 1 implicit bit.
      answer.append(subnormal ? "0." : "1.");

      // Isolate the low-order 13 digits of the hex
      // representation.  If all the digits are zero,
      // replace with a single 0; otherwise, remove all
      // trailing zeros.
      String signif = Long.toHexString(signifBits).substring(3, 16);
      answer.append(signif.equals("0000000000000") ? // 13 zeros
          "0" :
          signif.replaceFirst("0{1,12}$", ""));

      answer.append('p');
      // If the value is subnormal, use the E_min exponent
      // value for double; otherwise, extract and report d's
      // exponent (the representation of a subnormal uses
      // E_min -1).
      int exponent = subnormal ?
          Double.MIN_EXPONENT :
          Math.getExponent(d);

      if (exponent >= 0) {
        answer.append("+");
      }

      answer.append(exponent);
    }
    return answer.toString();
  }


  /**
   * Format method for the d conversion specifier and
   * short argument.
   * <p>
   * For d format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  A '+' character means that the conversion
   * will always begin with a sign (+ or -).  The
   * blank flag character means that a non-negative
   * input will be preceded with a blank.  If both a
   * '+' and a ' ' are specified, the blank flag is
   * ignored.  The '0' flag character implies that
   * padding to the field width will be done with
   * zeros instead of blanks.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear.  Padding is with leading 0s.
   *
   * @param x the short to format.
   * @return the formatted String.
   */
  private String printDFormat(short x) {
    return printDFormat(Short.toString(x));
  }

  /**
   * Format method for the d conversion character and
   * long argument.
   * <p>
   * For d format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  A '+' character means that the conversion
   * will always begin with a sign (+ or -).  The
   * blank flag character means that a non-negative
   * input will be preceded with a blank.  If both a
   * '+' and a ' ' are specified, the blank flag is
   * ignored.  The '0' flag character implies that
   * padding to the field width will be done with
   * zeros instead of blanks.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear.  Padding is with leading 0s.
   *
   * @param x the long to format.
   * @return the formatted String.
   */
  private String printDFormat(long x) {
    return printDFormat(Long.toString(x));
  }

  /**
   * Format an integer in binary
   */
  private String printBFormat(long x) {
    return Long.toBinaryString(x);
  }


  /**
   * Format method for the d conversion character and
   * int argument.
   * <p>
   * For d format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  A '+' character means that the conversion
   * will always begin with a sign (+ or -).  The
   * blank flag character means that a non-negative
   * input will be preceded with a blank.  If both a
   * '+' and a ' ' are specified, the blank flag is
   * ignored.  The '0' flag character implies that
   * padding to the field width will be done with
   * zeros instead of blanks.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear.  Padding is with leading 0s.
   *
   * @param x the int to format.
   * @return the formatted String.
   */
  private String printDFormat(int x) {
    return printDFormat(Integer.toString(x));
  }

  /**
   * Utility method for formatting using the d
   * conversion character.
   *
   * @param sx the String to format, the result of
   *           converting a short, int, or long to a
   *           String.
   * @return the formatted String.
   */
  private String printDFormat(String sx) {
    int nLeadingZeros = 0;
    int nBlanks = 0, n = 0;
    int i = 0, jFirst = 0;
    boolean neg = sx.charAt(0) == '-';
    if (sx.equals("0") && precisionSet && precision == 0) {
      sx = "";
    }
    if (!neg) {
      if (precisionSet && sx.length() < precision) {
        nLeadingZeros = precision - sx.length();
      }
    } else {
      if (precisionSet && (sx.length() - 1) < precision) {
        nLeadingZeros = precision - sx.length() + 1;
      }
    }
    if (nLeadingZeros < 0) {
      nLeadingZeros = 0;
    }
    if (fieldWidthSet) {
      nBlanks = fieldWidth - nLeadingZeros - sx.length();
      if (!neg && (leadingSign || leadingSpace)) {
        nBlanks--;
      }
    }
    if (nBlanks < 0) {
      nBlanks = 0;
    }
    if (leadingSign) {
      n++;
    } else if (leadingSpace) {
      n++;
    }
    n += nBlanks;
    n += nLeadingZeros;
    n += sx.length();
    char[] ca = new char[n];
    if (leftJustify) {
      if (neg) {
        ca[i++] = '-';
      } else if (leadingSign) {
        ca[i++] = '+';
      } else if (leadingSpace) {
        ca[i++] = ' ';
      }
      char[] csx = sx.toCharArray();
      jFirst = neg ? 1 : 0;
      for (int j = 0; j < nLeadingZeros; i++, j++) {
        ca[i] = '0';
      }
      for (int j = jFirst; j < csx.length; j++, i++) {
        ca[i] = csx[j];
      }
      for (int j = 0; j < nBlanks; i++, j++) {
        ca[i] = ' ';
      }
    } else {
      if (!leadingZeros) {
        for (i = 0; i < nBlanks; i++) {
          ca[i] = ' ';
        }
        if (neg) {
          ca[i++] = '-';
        } else if (leadingSign) {
          ca[i++] = '+';
        } else if (leadingSpace) {
          ca[i++] = ' ';
        }
      } else {
        if (neg) {
          ca[i++] = '-';
        } else if (leadingSign) {
          ca[i++] = '+';
        } else if (leadingSpace) {
          ca[i++] = ' ';
        }
        for (int j = 0; j < nBlanks; j++, i++) {
          ca[i] = '0';
        }
      }
      for (int j = 0; j < nLeadingZeros; j++, i++) {
        ca[i] = '0';
      }
      char[] csx = sx.toCharArray();
      jFirst = neg ? 1 : 0;
      for (int j = jFirst; j < csx.length; j++, i++) {
        ca[i] = csx[j];
      }
    }
    return new String(ca);
  }

  /**
   * Format method for the x conversion character and
   * short argument.
   * <p>
   * For x format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  The '#' flag character means to lead with
   * '0x'.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear.  Padding is with leading 0s.
   *
   * @param x the short to format.
   * @return the formatted String.
   */
  private String printXFormat(short x) {
    String sx = null;
    if (x == Short.MIN_VALUE) {
      sx = "8000";
    } else if (x < 0) {
      String t;
      if (x == Short.MIN_VALUE) {
        t = "0";
      } else {
        t = Integer.toString(
            (~(-x - 1)) ^ Short.MIN_VALUE, 16);
        if (t.charAt(0) == 'F' || t.charAt(0) == 'f') {
          t = t.substring(16, 32);
        }
      }
      switch (t.length()) {
        case 1:
          sx = "800" + t;
          break;
        case 2:
          sx = "80" + t;
          break;
        case 3:
          sx = "8" + t;
          break;
        case 4:
          switch (t.charAt(0)) {
            case '1':
              sx = "9" + t.substring(1, 4);
              break;
            case '2':
              sx = "a" + t.substring(1, 4);
              break;
            case '3':
              sx = "b" + t.substring(1, 4);
              break;
            case '4':
              sx = "c" + t.substring(1, 4);
              break;
            case '5':
              sx = "d" + t.substring(1, 4);
              break;
            case '6':
              sx = "e" + t.substring(1, 4);
              break;
            case '7':
              sx = "f" + t.substring(1, 4);
              break;
          }
          break;
      }
    } else {
      sx = Integer.toString((int) x, 16);
    }
    return printXFormat(sx);
  }

  /**
   * Format method for the x conversion character and
   * long argument.
   * <p>
   * For x format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  The '#' flag character means to lead with
   * '0x'.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear.  Padding is with leading 0s.
   *
   * @param x the long to format.
   * @return the formatted String.
   */
  private String printXFormat(long x) {
    String sx = null;
    if (x == Long.MIN_VALUE) {
      sx = "8000000000000000";
    } else if (x < 0) {
      String t = Long.toString(
          (~(-x - 1)) ^ Long.MIN_VALUE, 16);
      switch (t.length()) {
        case 1:
          sx = "800000000000000" + t;
          break;
        case 2:
          sx = "80000000000000" + t;
          break;
        case 3:
          sx = "8000000000000" + t;
          break;
        case 4:
          sx = "800000000000" + t;
          break;
        case 5:
          sx = "80000000000" + t;
          break;
        case 6:
          sx = "8000000000" + t;
          break;
        case 7:
          sx = "800000000" + t;
          break;
        case 8:
          sx = "80000000" + t;
          break;
        case 9:
          sx = "8000000" + t;
          break;
        case 10:
          sx = "800000" + t;
          break;
        case 11:
          sx = "80000" + t;
          break;
        case 12:
          sx = "8000" + t;
          break;
        case 13:
          sx = "800" + t;
          break;
        case 14:
          sx = "80" + t;
          break;
        case 15:
          sx = "8" + t;
          break;
        case 16:
          switch (t.charAt(0)) {
            case '1':
              sx = "9" + t.substring(1, 16);
              break;
            case '2':
              sx = "a" + t.substring(1, 16);
              break;
            case '3':
              sx = "b" + t.substring(1, 16);
              break;
            case '4':
              sx = "c" + t.substring(1, 16);
              break;
            case '5':
              sx = "d" + t.substring(1, 16);
              break;
            case '6':
              sx = "e" + t.substring(1, 16);
              break;
            case '7':
              sx = "f" + t.substring(1, 16);
              break;
          }
          break;
      }
    } else {
      sx = Long.toString(x, 16);
    }
    return printXFormat(sx);
  }

  /**
   * Format method for the x conversion character and
   * int argument.
   * <p>
   * For x format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  The '#' flag character means to lead with
   * '0x'.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear.  Padding is with leading 0s.
   *
   * @param x the int to format.
   * @return the formatted String.
   */
  private String printXFormat(int x) {
    String sx = null;
    if (x == Integer.MIN_VALUE) {
      sx = "80000000";
    } else if (x < 0) {
      String t = Integer.toString(
          (~(-x - 1)) ^ Integer.MIN_VALUE, 16);
      switch (t.length()) {
        case 1:
          sx = "8000000" + t;
          break;
        case 2:
          sx = "800000" + t;
          break;
        case 3:
          sx = "80000" + t;
          break;
        case 4:
          sx = "8000" + t;
          break;
        case 5:
          sx = "800" + t;
          break;
        case 6:
          sx = "80" + t;
          break;
        case 7:
          sx = "8" + t;
          break;
        case 8:
          switch (t.charAt(0)) {
            case '1':
              sx = "9" + t.substring(1, 8);
              break;
            case '2':
              sx = "a" + t.substring(1, 8);
              break;
            case '3':
              sx = "b" + t.substring(1, 8);
              break;
            case '4':
              sx = "c" + t.substring(1, 8);
              break;
            case '5':
              sx = "d" + t.substring(1, 8);
              break;
            case '6':
              sx = "e" + t.substring(1, 8);
              break;
            case '7':
              sx = "f" + t.substring(1, 8);
              break;
          }
          break;
      }
    } else {
      sx = Integer.toString(x, 16);
    }
    return printXFormat(sx);
  }

  /**
   * Utility method for formatting using the x
   * conversion character.
   *
   * @param sx the String to format, the result of
   *           converting a short, int, or long to a
   *           String.
   * @return the formatted String.
   */
  private String printXFormat(String sx) {
    int nLeadingZeros = 0;
    int nBlanks = 0;
    if (sx.equals("0") && precisionSet && precision == 0) {
      sx = "";
    }
    if (precisionSet) {
      nLeadingZeros = precision - sx.length();
    }
    if (nLeadingZeros < 0) {
      nLeadingZeros = 0;
    }
    if (fieldWidthSet) {
      nBlanks = fieldWidth - nLeadingZeros - sx.length();
      if (alternateForm) {
        nBlanks = nBlanks - 2;
      }
    }
    if (nBlanks < 0) {
      nBlanks = 0;
    }
    int n = 0;
    if (alternateForm) {
      n += 2;
    }
    n += nLeadingZeros;
    n += sx.length();
    n += nBlanks;
    char[] ca = new char[n];
    int i = 0;
    if (leftJustify) {
      if (alternateForm) {
        ca[i++] = '0';
        ca[i++] = 'x';
      }
      for (int j = 0; j < nLeadingZeros; j++, i++) {
        ca[i] = '0';
      }
      char[] csx = sx.toCharArray();
      for (int j = 0; j < csx.length; j++, i++) {
        ca[i] = csx[j];
      }
      for (int j = 0; j < nBlanks; j++, i++) {
        ca[i] = ' ';
      }
    } else {
      if (!leadingZeros) {
        for (int j = 0; j < nBlanks; j++, i++) {
          ca[i] = ' ';
        }
      }
      if (alternateForm) {
        ca[i++] = '0';
        ca[i++] = 'x';
      }
      if (leadingZeros) {
        for (int j = 0; j < nBlanks; j++, i++) {
          ca[i] = '0';
        }
      }
      for (int j = 0; j < nLeadingZeros; j++, i++) {
        ca[i] = '0';
      }
      char[] csx = sx.toCharArray();
      for (int j = 0; j < csx.length; j++, i++) {
        ca[i] = csx[j];
      }
    }
    String caReturn = new String(ca);
    if (conversionCharacter == 'X' || conversionCharacter == 'p') {
      caReturn = caReturn.toUpperCase();
    }
    return caReturn;
  }

  /**
   * Format method for the o conversion character and
   * short argument.
   * <p>
   * For o format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  The '#' flag character means that the
   * output begins with a leading 0 and the precision
   * is increased by 1.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear.  Padding is with leading 0s.
   *
   * @param x the short to format.
   * @return the formatted String.
   */
  private String printOFormat(short x) {
    String sx = null;
    if (x == Short.MIN_VALUE) {
      sx = "100000";
    } else if (x < 0) {
      String t = Integer.toString(
          (~(-x - 1)) ^ Short.MIN_VALUE, 8);
      switch (t.length()) {
        case 1:
          sx = "10000" + t;
          break;
        case 2:
          sx = "1000" + t;
          break;
        case 3:
          sx = "100" + t;
          break;
        case 4:
          sx = "10" + t;
          break;
        case 5:
          sx = "1" + t;
          break;
      }
    } else {
      sx = Integer.toString((int) x, 8);
    }
    return printOFormat(sx);
  }

  /**
   * Format method for the o conversion character and
   * long argument.
   * <p>
   * For o format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  The '#' flag character means that the
   * output begins with a leading 0 and the precision
   * is increased by 1.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear.  Padding is with leading 0s.
   *
   * @param x the long to format.
   * @return the formatted String.
   */
  private String printOFormat(long x) {
    String sx = null;
    if (x == Long.MIN_VALUE) {
      sx = "1000000000000000000000";
    } else if (x < 0) {
      String t = Long.toString(
          (~(-x - 1)) ^ Long.MIN_VALUE, 8);
      switch (t.length()) {
        case 1:
          sx = "100000000000000000000" + t;
          break;
        case 2:
          sx = "10000000000000000000" + t;
          break;
        case 3:
          sx = "1000000000000000000" + t;
          break;
        case 4:
          sx = "100000000000000000" + t;
          break;
        case 5:
          sx = "10000000000000000" + t;
          break;
        case 6:
          sx = "1000000000000000" + t;
          break;
        case 7:
          sx = "100000000000000" + t;
          break;
        case 8:
          sx = "10000000000000" + t;
          break;
        case 9:
          sx = "1000000000000" + t;
          break;
        case 10:
          sx = "100000000000" + t;
          break;
        case 11:
          sx = "10000000000" + t;
          break;
        case 12:
          sx = "1000000000" + t;
          break;
        case 13:
          sx = "100000000" + t;
          break;
        case 14:
          sx = "10000000" + t;
          break;
        case 15:
          sx = "1000000" + t;
          break;
        case 16:
          sx = "100000" + t;
          break;
        case 17:
          sx = "10000" + t;
          break;
        case 18:
          sx = "1000" + t;
          break;
        case 19:
          sx = "100" + t;
          break;
        case 20:
          sx = "10" + t;
          break;
        case 21:
          sx = "1" + t;
          break;
      }
    } else {
      sx = Long.toString(x, 8);
    }
    return printOFormat(sx);
  }

  /**
   * Format method for the o conversion character and
   * int argument.
   * <p>
   * For o format, the flag character '-', means that
   * the output should be left justified within the
   * field.  The default is to pad with blanks on the
   * left.  The '#' flag character means that the
   * output begins with a leading 0 and the precision
   * is increased by 1.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is to
   * add no padding.  Padding is with blanks by
   * default.
   * <p>
   * The precision, if set, is the minimum number of
   * digits to appear.  Padding is with leading 0s.
   *
   * @param x the int to format.
   * @return the formatted String.
   */
  private String printOFormat(int x) {
    String sx = null;
    if (x == Integer.MIN_VALUE) {
      sx = "20000000000";
    } else if (x < 0) {
      String t = Integer.toString(
          (~(-x - 1)) ^ Integer.MIN_VALUE, 8);
      switch (t.length()) {
        case 1:
          sx = "2000000000" + t;
          break;
        case 2:
          sx = "200000000" + t;
          break;
        case 3:
          sx = "20000000" + t;
          break;
        case 4:
          sx = "2000000" + t;
          break;
        case 5:
          sx = "200000" + t;
          break;
        case 6:
          sx = "20000" + t;
          break;
        case 7:
          sx = "2000" + t;
          break;
        case 8:
          sx = "200" + t;
          break;
        case 9:
          sx = "20" + t;
          break;
        case 10:
          sx = "2" + t;
          break;
        case 11:
          sx = "3" + t.substring(1);
          break;
      }
    } else {
      sx = Integer.toString(x, 8);
    }
    return printOFormat(sx);
  }

  /**
   * Utility method for formatting using the o
   * conversion character.
   *
   * @param sx the String to format, the result of
   *           converting a short, int, or long to a
   *           String.
   * @return the formatted String.
   */
  private String printOFormat(String sx) {
    int nLeadingZeros = 0;
    int nBlanks = 0;
    if (sx.equals("0") && precisionSet && precision == 0) {
      sx = "";
    }
    if (precisionSet) {
      nLeadingZeros = precision - sx.length();
    }
    if (alternateForm) {
      nLeadingZeros++;
    }
    if (nLeadingZeros < 0) {
      nLeadingZeros = 0;
    }
    if (fieldWidthSet) {
      nBlanks = fieldWidth - nLeadingZeros - sx.length();
    }
    if (nBlanks < 0) {
      nBlanks = 0;
    }
    int n = nLeadingZeros + sx.length() + nBlanks;
    char[] ca = new char[n];
    int i;
    if (leftJustify) {
      for (i = 0; i < nLeadingZeros; i++) {
        ca[i] = '0';
      }
      char[] csx = sx.toCharArray();
      for (int j = 0; j < csx.length; j++, i++) {
        ca[i] = csx[j];
      }
      for (int j = 0; j < nBlanks; j++, i++) {
        ca[i] = ' ';
      }
    } else {
      if (leadingZeros) {
        for (i = 0; i < nBlanks; i++) {
          ca[i] = '0';
        }
      } else {
        for (i = 0; i < nBlanks; i++) {
          ca[i] = ' ';
        }
      }
      for (int j = 0; j < nLeadingZeros; j++, i++) {
        ca[i] = '0';
      }
      char[] csx = sx.toCharArray();
      for (int j = 0; j < csx.length; j++, i++) {
        ca[i] = csx[j];
      }
    }
    return new String(ca);
  }

  /**
   * Format method for the c conversion character and
   * char argument.
   * <p>
   * The only flag character that affects c format is
   * the '-', meaning that the output should be left
   * justified within the field.  The default is to
   * pad with blanks on the left.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  Padding is with
   * blanks by default.  The default width is 1.
   * <p>
   * The precision, if set, is ignored.
   *
   * @param x the char to format.
   * @return the formatted String.
   */
  private String printCFormat(char x) {
    int nPrint = 1;
    int width = fieldWidth;
    if (!fieldWidthSet) {
      width = nPrint;
    }
    char[] ca = new char[width];
    int i = 0;
    if (leftJustify) {
      ca[0] = x;
      for (i = 1; i <= width - nPrint; i++) {
        ca[i] = ' ';
      }
    } else {
      for (i = 0; i < width - nPrint; i++) {
        ca[i] = ' ';
      }
      ca[i] = x;
    }
    return new String(ca);
  }

  /**
   * Format method for the s conversion character and
   * String argument.
   * <p>
   * The only flag character that affects s format is
   * the '-', meaning that the output should be left
   * justified within the field.  The default is to
   * pad with blanks on the left.
   * <p>
   * The field width is treated as the minimum number
   * of characters to be printed.  The default is the
   * smaller of the number of characters in the the
   * input and the precision.  Padding is with blanks
   * by default.
   * <p>
   * The precision, if set, specifies the maximum
   * number of characters to be printed from the
   * string.  A null digit string is treated
   * as a 0.  The default is not to set a maximum
   * number of characters to be printed.
   *
   * @param x the String to format.
   * @return the formatted String.
   */
  private String printSFormat(String x) {
    int nPrint = x.length();
    int width = fieldWidth;
    if (precisionSet && nPrint > precision) {
      nPrint = precision;
    }
    if (!fieldWidthSet) {
      width = nPrint;
    }
    int n = 0;
    if (width > nPrint) {
      n += width - nPrint;
    }
    if (nPrint >= x.length()) {
      n += x.length();
    } else {
      n += nPrint;
    }
    char[] ca = new char[n];
    int i = 0;
    if (leftJustify) {
      if (nPrint >= x.length()) {
        char[] csx = x.toCharArray();
        for (i = 0; i < x.length(); i++) {
          ca[i] = csx[i];
        }
      } else {
        char[] csx =
            x.substring(0, nPrint).toCharArray();
        for (i = 0; i < nPrint; i++) {
          ca[i] = csx[i];
        }
      }
      for (int j = 0; j < width - nPrint; j++, i++) {
        ca[i] = ' ';
      }
    } else {
      for (i = 0; i < width - nPrint; i++) {
        ca[i] = ' ';
      }
      if (nPrint >= x.length()) {
        char[] csx = x.toCharArray();
        for (int j = 0; j < x.length(); i++, j++) {
          ca[i] = csx[j];
        }
      } else {
        char[] csx =
            x.substring(0, nPrint).toCharArray();
        for (int j = 0; j < nPrint; i++, j++) {
          ca[i] = csx[j];
        }
      }
    }
    return new String(ca);
  }

}
