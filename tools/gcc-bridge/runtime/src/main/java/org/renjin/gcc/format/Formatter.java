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
// Source: http://java.sun.com/developer/technicalArticles/Programming/sprintf/
// Adapted to match the R Language

package org.renjin.gcc.format;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Implementation of the C standard string formatting function.
 *
 * @author Allan Jacobs
 * @author Alex Bertram
 * 
 */
public class Formatter {


  public enum ArgumentType {
    INTEGER,
    DOUBLE,
    POINTER,
    UNUSED, LONG, STRING
  }

  /**
   * The input format string
   */
  private final String formatString;

  /**
   * List of control strings and format literals.
   */
  private final List<FormatSpec> conversions = new ArrayList<>();

  /**
   * List of expected argument types
   */
  private List<ArgumentType> argumentTypes = new ArrayList<>();

  private int currentPos = 0;

  private int currentArgumentIndex = 0;

  private FormatSpec currentSpec;

  private DecimalFormatSymbols dfs;

  /**
   * Constructs an array of control specifications
   * possibly preceded, separated, or followed by
   * ordinary strings.  Control strings begin with
   * unpaired percent signs.  A pair of successive
   * percent signs designates a single percent sign in
   * the format.
   * @param formatString  Control string.
   * @exception IllegalArgumentException if the control
   * string is null, zero length, or otherwise
   * malformed.
   */
  public Formatter(String formatString) throws IllegalArgumentException {
    this(Locale.getDefault(), formatString);
  }
  
  
  /**
   * Constructs an array of control specifications
   * possibly preceded, separated, or followed by
   * ordinary strings.  Control strings begin with
   * unpaired percent signs.  A pair of successive
   * percent signs designates a single percent sign in
   * the format.
   * @param formatString  Control string.
   * @exception IllegalArgumentException if the control
   * string is null, zero length, or otherwise
   * malformed.
   */
  public Formatter(Locale locale, String formatString) throws IllegalArgumentException {
    this.formatString = formatString;
    dfs = new DecimalFormatSymbols(locale);

    consumeLiteral();

    while(currentPos < formatString.length()) {
      consumeNextSpec();
      consumeLiteral();
    }
  }


  /**
   * Return a substring starting at
   * <code>start</code> and ending at either the end
   * of the String <code>s</code>, the next unpaired
   * percent sign, or at the end of the String if the
   * last character is a percent sign.
   */
  private void consumeLiteral() {
    StringBuilder sb = new StringBuilder();
    while (hasMoreChars()) {
      char c = next();
      if (c == '%') {
        pushBack();
        break;
      }
      if (c == '\\') {
        if (hasMoreChars()) {
          c = next();
          switch (c) {
            case 'a':
              sb.append((char) 0x07);
              break;
            case 'b':
              sb.append('\b');
              break;
            case 'f':
              sb.append('\f');
              break;
            case 'n':
              sb.append(System.getProperty("line.separator"));
              break;
            case 'r':
              sb.append('\r');
              break;
            case 't':
              sb.append('\t');
              break;
            case 'v':
              sb.append((char) 0x0b);
              break;
            case '\\':
              sb.append('\\');
              break;
          }
        }
      } else {
        sb.append(c);
      }
    }
    if(sb.length() != 0) {
      FormatSpec spec = new FormatSpec(dfs);
      spec.conversionCharacter = '\0';
      spec.literal = sb.toString();
      conversions.add(spec);
    }
  }

  private void consumeNextSpec() {
    if(consumeIf('%')) {

      currentSpec = new FormatSpec(dfs);

      //  A control string takes the form:
      //  % ['-+ #0]* [0..9]* { . [0..9]* }+  { [hlL] }+ [idfgGoxXeEcs]

      consumePosition();
      consumeFlags();
      consumeFieldWidth();
      consumePrecision();
      consumeSizeFlags();
      consumeConversionCharacter();

      if(currentSpec.conversionCharacter == '%') {
        FormatSpec spec = new FormatSpec(dfs);
        spec.conversionCharacter = '\0';
        spec.literal = "%";
        conversions.add(spec);
        return;
      }

      if(currentSpec.leadingZeros && currentSpec.leftJustify) {
        currentSpec.leadingZeros = false;
      }
      if(currentSpec.precisionSet && currentSpec.leadingZeros) {
        switch (currentSpec.conversionCharacter) {
          case 'd':
          case 'i':
          case 'o':
          case 'x':
            currentSpec.leadingZeros = false;
            break;
        }
      }

      if(currentSpec.conversionCharacter == 'u') {
        currentSpec.leadingSign = false;
        currentSpec.leadingSpace = false;
      }

      if(currentSpec.conversionCharacter == 'p') {
        currentSpec.fieldWidth = 8;
        currentSpec.fieldWidthSet = true;
        currentSpec.leadingZeros = true;
      }

      if(!currentSpec.positionalSpecification) {
        currentSpec.argumentPosition = currentArgumentIndex++;
      }

      setArgumentType(currentSpec.argumentPosition, currentSpec.getArgumentType());
      conversions.add(currentSpec);
    }
  }

  private void setArgumentType(int argumentIndex, ArgumentType argumentType) {
    while(argumentTypes.size() < argumentIndex) {
      argumentTypes.add(ArgumentType.UNUSED);
    }
    if(argumentIndex == argumentTypes.size()) {
      argumentTypes.add(argumentType);
    } else {
      argumentTypes.set(argumentIndex, argumentType);
    }
  }

  private void consumePosition() {
    int argumentIndex = maybeConsumeArgumentIndex();
    if(argumentIndex != -1) {
      currentSpec.positionalSpecification = true;
      currentSpec.argumentPosition = argumentIndex;
    }
  }

  private void consumeFlags() {
    while(hasMoreChars()) {
      char c = peek();
      if (c == '\'') {
        currentSpec.thousands = true;

      } else if (c == '-') {
        currentSpec.leftJustify = true;
        currentSpec.leadingZeros = false;

      } else if (c == '+') {
        currentSpec.leadingSign = true;
        currentSpec.leadingSpace = false;

      } else if (c == ' ') {
        if (!currentSpec.leadingSign) {
          currentSpec.leadingSpace = true;
        }
      } else if (c == '#') {
        currentSpec.alternateForm = true;

      } else if (c == '0') {
        if (!currentSpec.leftJustify) {
          currentSpec.leadingZeros = true;
        }
      } else {
        break;
      }

      currentPos++;
    }
  }

  /**
   * Set the field width.
   */
  private void consumeFieldWidth() {
    currentSpec.fieldWidth = 0;
    currentSpec.fieldWidthSet = false;

    if (consumeIf('*')) {
      currentSpec.fieldWidthSet = true;
      currentSpec.variableFieldWidth = true;

      int fieldWidthArgumentIndex = maybeConsumeArgumentIndex();
      if (fieldWidthArgumentIndex != -1) {
        currentSpec.argumentPositionForFieldWidth = fieldWidthArgumentIndex;
      } else {
        currentSpec.argumentPositionForFieldWidth = currentArgumentIndex++;
      }

      setArgumentType(currentSpec.argumentPositionForFieldWidth, ArgumentType.INTEGER);

    } else {
      int digitCount = peekDigitCount();
      if (digitCount > 0) {
        currentSpec.fieldWidth = consumeDigits(digitCount);
        currentSpec.fieldWidthSet = true;
      }
    }
  }

  /**
   * Set the precision.
   */
  private void consumePrecision() {
    if(consumeIf('.')) {
      currentSpec.precisionSet = true;

      if (consumeIf('*')) {
        currentSpec.variablePrecision = true;

        int precisionArgumentIndex = maybeConsumeArgumentIndex();
        if(precisionArgumentIndex != -1) {
          currentSpec.argumentPositionForPrecision = precisionArgumentIndex;
        } else {
          currentSpec.argumentPositionForPrecision = currentArgumentIndex++;
        }

        setArgumentType(currentSpec.argumentPositionForPrecision, ArgumentType.INTEGER);

      } else {
        int digitCount = peekDigitCount();
        if (digitCount == 0) {
          currentSpec.precision = 0;
        } else {
          currentSpec.precision = consumeDigits(digitCount);
        }
      }
    }
  }

  /**
   * Check for an h, l, or L in a format.  An L is
   * used to control the minimum number of digits
   * in an exponent when using floating point
   * formats.  An l or h is used to control
   * conversion of the input to a long or short,
   * respectively, before formatting.  If any of
   * these is present, store them.
   */
  private void consumeSizeFlags() {
    if(consumeIf('z')) {
      // Indicates that we should expect an size_t-sized integer argument.
      // This is the same as our default, which is 32-bit integers, so ignore

    } else if(consumeIf('h')) {
      currentSpec.optionalh = true;
      // ignore additonal h
      consumeIf('h');

    } else if(consumeIf('l')) {
      currentSpec.optionall = true;
      // ignore additional l
      consumeIf('l');

    } else if(consumeIf('L')) {
      currentSpec.optionalL = true;
      // ignore additional L
      consumeIf('L');
    }
  }
  /**
   * Check for a conversion character.
   */
  private void consumeConversionCharacter() {
    currentSpec.conversionCharacter = next();
  }

  /**
   * Checks to see if the streams consists of a number followed by a $,
   * indicating a positional argument
   */
  private int maybeConsumeArgumentIndex() {

    int digitCount = peekDigitCount();

    if(digitCount > 0 && formatString.charAt(currentPos + digitCount) == '$') {
      int argumentIndex = consumeDigits(digitCount) - 1;
      currentPos ++;
      return argumentIndex;
    } else {
      return -1;
    }
  }

  /**
   * Counts the nubmer of digits occurring in the stream, without advancing the stream.
   */
  private int peekDigitCount() {
    int digitCount = 0;
    while(currentPos + digitCount < formatString.length() &&
         Character.isDigit(formatString.charAt(currentPos + digitCount))) {
      digitCount++;
    }
    return digitCount;
  }

  private int consumeDigits(int digitCount) {
    int number = Integer.parseInt(formatString.substring(currentPos, currentPos + digitCount));
    currentPos += digitCount;
    return number;
  }


  private boolean hasMoreChars() {
    return currentPos < formatString.length();
  }

  private char peek() {
    return formatString.charAt(currentPos);
  }

  private void pushBack() {
    currentPos--;
  }

  private char next() {
    return formatString.charAt(currentPos++);
  }

  private boolean consumeIf(char c) {
    if(formatString.charAt(currentPos) == c) {
      currentPos++;
      return true;
    } else {
      return false;
    }
  }

  public List<ArgumentType> getArgumentTypes() {
    return argumentTypes;
  }

  public ArgumentType getArgumentType(int i) {
    if(i < argumentTypes.size()) {
      return argumentTypes.get(i);
    }
    return ArgumentType.UNUSED;
  }


  public String format(FormatInput input) {
    StringBuilder result = new StringBuilder();
    for (FormatSpec conversion : conversions) {
      result.append(conversion.format(input));
    }
    return result.toString();
  }

  public static String format(String formatString, Object... arguments) {
    return new Formatter(formatString).format(new FormatArrayInput(arguments));
  }

}