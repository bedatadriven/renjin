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
package org.renjin.parser;

import org.apache.commons.math.complex.Complex;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleVector;

import java.text.NumberFormat;

/**
 * Parses and formats numbers to/from strings 
 */
public class NumericLiterals {

  public static final NumberFormat REAL_FORMAT = createRealFormat();

  //
  // Constants of the implementation;
  // most are IEEE-754 related.
  // (There are more really boring constants at the end.)
  //

  /**
   * The number of logical bits in the significand of a
   * <code>double</code> number, including the implicit bit.
   */
  private static final int    SIGNIFICAND_WIDTH   = 53;
  private static final int    EXP_SHIFT = SIGNIFICAND_WIDTH - 1;
  private static final long   FRACT_HOB = ( 1L<<EXP_SHIFT ); // assumed High-Order bit

  /**
   * Bias used in representing a <code>double</code> exponent.
   */
  public static final int     EXP_BIAS        = 1023;
  private static final long   EXP_ONE   = ((long)EXP_BIAS)<<EXP_SHIFT; // exponent of 1.0
  private static final int    MAX_SMALL_BIN_EXP = 62;
  private static final int    MIN_SMALL_BIN_EXP = -( 63 / 3 );
  private static final int    MAX_DECIMAL_DIGITS = 15;
  private static final int    MAX_DECIMAL_EXPONENT = 308;
  private static final int    MIN_DECIMAL_EXPONENT = -324;
  private static final int    BIG_DECIMAL_EXPONENT = 324; // i.e. abs(MIN_DECIMAL_EXPONENT)
  private static final int    MAX_NDIGITS = 1100;

  private static final int    INT_DECIMAL_DIGITS = 9;


  /**
   * Bit mask to isolate the exponent field of a
   * <code>double</code>.
   */
  public static final long    EXP_BIT_MASK    = 0x7FF0000000000000L;

  /**
   * Bit mask to isolate the significand field of a
   * <code>double</code>.
   */
  public static final long    SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL;

  /**
   * Bit mask to isolate the sign bit of a <code>double</code>.
   */
  private static final long   SIGN_BIT_MASK   = 0x8000000000000000L;

  /**
   * All the positive powers of 10 that can be
   * represented exactly in double/float.
   */
  private static final double[] SMALL_10_POW = {
      1.0e0,
      1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5,
      1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10,
      1.0e11, 1.0e12, 1.0e13, 1.0e14, 1.0e15,
      1.0e16, 1.0e17, 1.0e18, 1.0e19, 1.0e20,
      1.0e21, 1.0e22
  };

  private static final float[] SINGLE_SMALL_10_POW = {
      1.0e0f,
      1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
      1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
  };

  private static final double[] BIG_10_POW = {
      1e16, 1e32, 1e64, 1e128, 1e256 };
  private static final double[] TINY_10_POW = {
      1e-16, 1e-32, 1e-64, 1e-128, 1e-256 };

  private static final int MAX_SMALL_TEN = SMALL_10_POW.length-1;
  private static final int SINGLE_MAX_SMALL_TEN = SINGLE_SMALL_10_POW.length-1;


  /**
   * Formats a {@code double} as a literal
   * @param value the value to be formatted
   * @param naString the string to use when {@code RealExp.isNA(value) } is {@code true}
   * @return
   */
  public static String format(double value, String naString) {
    if(DoubleVector.isNA(value)) {
      return naString;
    } else {
      return toString(value);
    }
  }

  public static String format(int value) {
    return Integer.toString(value);
  }

  public static String toString(double value) {
    if(Double.isNaN(value)) {
      return "NaN";
    }
    if(Double.isInfinite(value)) {
      if (value < 0) {
        return "-Inf";
      } else {
        return "Inf";
      }
    }
    // TODO: the original R implementation formats numbers based on options,
    // this is just a quick hack
    if(value > 100000) {
      return Double.toString(value);
    }
    return REAL_FORMAT.format(value);
  }

  public static String toString(Complex complex) {
    StringBuilder sb = new StringBuilder();
    sb.append(toString(complex.getReal()));
    if(complex.getImaginary() >= 0) {
      sb.append('+');
    }
    sb.append(toString(complex.getImaginary()));
    sb.append('i');
    return sb.toString();
  }

  public static NumberFormat createRealFormat() {
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(14);
    format.setGroupingUsed(false);
    return format;
  }

  /**
   * Parses a String to a double using the decimal point '.'
   */
  public static double parseDouble(CharSequence text) {
    return parseDouble(text, 0, text.length(), '.', false);
  }

  public static Complex parseComplex(CharSequence s) {
    int lastCharIndex = s.length()-1;
    if(s.charAt(lastCharIndex) == 'i') {
      // parse as number with imaginary component
      int imaginaryStart = findImaginaryStart(s);
      if(imaginaryStart <= 0) {
        // malformed
        return ComplexVector.NA;
      }
      double real = parseDouble(s, 0, imaginaryStart, '.', true);
      if(DoubleVector.isNA(real)) {
        return ComplexVector.NA;
      }
      double imaginary = parseDouble(s, imaginaryStart, lastCharIndex, '.', true);
      return ComplexVector.complex(real, imaginary);

    } else {
      // parse as number with only real component
      double real = parseDouble(s, 0, s.length(), '.', true);
      return ComplexVector.complex(real);
    }
  }

  private static int findImaginaryStart(CharSequence s) {
    // cannot be the last character
    int index = s.length()-2;
    while(index >= 0) {
      char c = s.charAt(index);
      if(c == '+' || c == '-') {
        return index;
      }
      index --;
    }
    return -1;
  }

  public static int parseInt(CharSequence line) {
    return (int)parseDouble(line);
  }

  /**
   * Parses a string to a double.
   *
   * @param s the string to parse
   * @param dec the decimal point character to use. Generally '.' or ','
   * @param startIndex the index, inclusive at which to start parsing
   * @param endIndex the index, exculsive, at which to stop parsing
   * @return the number as a {@code double} value, or {@code DoubleVector.NA} if {@code s} is not a number or
   * malformatted.
   */
  public static double parseDouble(CharSequence s, int startIndex, int endIndex, char dec, boolean NA) {
    int sign = 1;
    int p = startIndex;
  
    /* Trim optional whitespace */
    while ( p < endIndex && Character.isWhitespace(s.charAt(p))) {
      p++;
    }
    while ( endIndex > p && Character.isWhitespace(s.charAt(endIndex-1))) {
      endIndex--;
    }

    /* Check for the input 'NA' */
    if (NA && (p+2 < endIndex) && s.charAt(p) == 'N' && s.charAt(p+1) == 'A') {
      return DoubleVector.NA;
    }

    /* Empty input? Return NA */
    if( p == endIndex) {
      return DoubleVector.NA;
    }
  
    /* Sign is optional */
    switch (s.charAt(p)) {
      case '-':
        sign = -1;
        p++;
        break;
      case '+':
        p++;
        break;
    }

    if (equalsIgnoringCase(s, p, endIndex, "NAN")) {
      return Double.NaN;
    }

    if (equalsIgnoringCase(s, p, endIndex, "INF") ||
        equalsIgnoringCase(s, p, endIndex, "INFINITY")) {

      return sign * Double.POSITIVE_INFINITY;
    }

    if(( (endIndex-p) > 2) && s.charAt(p) == '0' && (s.charAt(p+1) == 'x' || s.charAt(p+2) == 'X')) {
      return parseDoubleHex(s, sign, p, endIndex, dec);
    } else {
      return parseDoubleDecimal(s, sign, p, endIndex, dec);
    }
  }

  /**
   * Parses a real-valued number in decimal format.
   *
   * <p>This implementation is based on OpenJDK's {@code com.sun.misc.FloatingDecimal.readJavaFormatString}, but
   * adapted to allow for different decimal points, and to match R's allowed numeric formats. The original code
   * is copyright 1996, 2013, Oracle and/or its affiliates and licensed under the GPL v2.</p>
   *
   * @param in the input string
   * @param sign the sign, -1 or +1, parsed above in {@link #parseDouble(CharSequence, int, int, char, boolean)}
   * @param startIndex the index at which to start parsing
   * @param endIndex the index, exclusive, at which to stop parsing
   * @param decimalPoint the decimal point character to use. Generally either '.' or ','
   * @return the number as a {@code double}, or {@code NA} if the string is malformatted.
   */
  private static double parseDoubleDecimal(CharSequence in, int sign, int startIndex, int endIndex, char decimalPoint) {

    char[] digits = new char[ endIndex - startIndex ];
    int    nDigits= 0;
    boolean decSeen = false;
    int decPt = 0;
    int nLeadZero = 0;
    int nTrailZero= 0;
    int i = startIndex;

    char c;

  skipLeadingZerosLoop:
    while (i < endIndex) {
      c = in.charAt(i);
      if (c == '0') {
        nLeadZero++;
      } else if (c == decimalPoint) {
        if (decSeen) {
          // already saw one ., this is the 2nd.
          return DoubleVector.NA;
        }
        decPt = i - startIndex;
        decSeen = true;
      } else {
        break skipLeadingZerosLoop;
      }
      i++;
    }

  digitLoop:
    while (i < endIndex) {
      c = in.charAt(i);
      if (c >= '1' && c <= '9') {
        digits[nDigits++] = c;
        nTrailZero = 0;
      } else if (c == '0') {
        digits[nDigits++] = c;
        nTrailZero++;
      } else if (c == decimalPoint) {
        if (decSeen) {
          // already saw one ., this is the 2nd.
          return DoubleVector.NA;
        }
        decPt = i - startIndex;
        decSeen = true;
      } else {
        break digitLoop;
      }
      i++;
    }
    nDigits -=nTrailZero;
    //
    // At this point, we've scanned all the digits and decimal
    // point we're going to see. Trim off leading and trailing
    // zeros, which will just confuse us later, and adjust
    // our initial decimal exponent accordingly.
    // To review:
    // we have seen i total characters.
    // nLeadZero of them were zeros before any other digits.
    // nTrailZero of them were zeros after any other digits.
    // if ( decSeen ), then a . was seen after decPt characters
    // ( including leading zeros which have been discarded )
    // nDigits characters were neither lead nor trailing
    // zeros, nor point
    //
    //
    // special hack: if we saw no non-zero digits, then the
    // answer is zero!
    // Unfortunately, we feel honor-bound to keep parsing!
    //
    boolean isZero = (nDigits == 0);
    if ( isZero &&  nLeadZero == 0 ){
      // we saw NO DIGITS AT ALL,
      // not even a crummy 0!
      // this is not allowed.
      return DoubleVector.NA;
    }
    //
    // Our initial exponent is decPt, adjusted by the number of
    // discarded zeros. Or, if there was no decPt,
    // then its just nDigits adjusted by discarded trailing zeros.
    //
    int decExp;
    if ( decSeen ){
      decExp = decPt - nLeadZero;
    } else {
      decExp = nDigits + nTrailZero;
    }

    //
    // Look for 'e' or 'E' and an optionally signed integer.
    //
    if ( (i < endIndex) &&  (((c = in.charAt(i) )=='e') || (c == 'E') ) ){
      int expSign = 1;
      int expVal  = 0;
      int reallyBig = Integer.MAX_VALUE / 10;
      boolean expOverflow = false;
      switch( in.charAt(++i) ){
        case '-':
          expSign = -1;
          //FALLTHROUGH
        case '+':
          i++;
      }
      int expAt = i;
      expLoop:
      while ( i < endIndex  ){
        if ( expVal >= reallyBig ){
          // the next character will cause integer
          // overflow.
          expOverflow = true;
        }
        c = in.charAt(i++);
        if(c>='0' && c<='9') {
          expVal = expVal*10 + ( (int)c - (int)'0' );
        } else {
          i--;           // back up.
          break expLoop; // stop parsing exponent.
        }
      }
      int expLimit = BIG_DECIMAL_EXPONENT+nDigits+nTrailZero;
      if ( expOverflow || ( expVal > expLimit ) ){
        //
        // The intent here is to end up with
        // infinity or zero, as appropriate.
        // The reason for yielding such a small decExponent,
        // rather than something intuitive such as
        // expSign*Integer.MAX_VALUE, is that this value
        // is subject to further manipulation in
        // doubleValue() and floatValue(), and I don't want
        // it to be able to cause overflow there!
        // (The only way we can get into trouble here is for
        // really outrageous nDigits+nTrailZero, such as 2 billion. )
        //
        decExp = expSign*expLimit;
      } else {
        // this should not overflow, since we tested
        // for expVal > (MAX+N), where N >= abs(decExp)
        decExp = decExp + expSign*expVal;
      }

      // if we saw something not a digit ( or end of string )
      // after the [Ee][+-], without seeing any digits at all
      // this is certainly an error. If we saw some digits,
      // but then some trailing garbage, that might be ok.
      // so we just fall through in that case.
      // HUMBUG
      if ( i == expAt ) {
        return DoubleVector.NA; // certainly bad
      }
    }
    //
    // We parsed everything we could.
    // If there are leftovers, then this is not good input!
    //
    if ( i < endIndex) {
      // Not a number...
      return DoubleVector.NA;
    }
    if(isZero) {
      return sign * 0d;
    }
    return doubleValue((sign < 0), decExp, digits, nDigits);
  }

  private static double parseDoubleHex(CharSequence s, int sign, int startIndex, int endIndex, char dec) {
    int n;
    double fac;
    double ans = 0.0;
    int expn = 0;
    int exph = -1;
    int p = startIndex;

      /* This will overflow to Inf if appropriate */
    for(p += 2; p<s.length(); p++) {
      char c = s.charAt(p);
      if('0' <= c && c <= '9') {
        ans = 16 * ans + (s.charAt(p) - '0');
      } else if('a' <= c && c <= 'f') {
        ans = 16 * ans + (s.charAt(p) - 'a' + 10);
      } else if('A' <= c && c <= 'F') {
        ans = 16 * ans + (s.charAt(p) - 'A' + 10);
      } else if(c == dec) {
        exph = 0;
        continue;
      } else {
        break;
      }
      if (exph >= 0) {
        exph += 4;
      }
    }
    if ( p < endIndex && (s.charAt(p) == 'p' || s.charAt(p) == 'P')) {
      int expsign = 1;
      double p2 = 2.0;
      switch(s.charAt(++p)) {
        case '-':
          expsign = -1;
          p++;
          break;
        case '+':
          p++;
          break;
      }
      for (n = 0; p < endIndex && s.charAt(p) >= '0' && s.charAt(p) <= '9'; p++) {
        n = n * 10 + (s.charAt(p) - '0');
      }
      expn += expsign * n;
      if(exph > 0) {
        expn -= exph;
      }
      if (expn < 0) {
        for (n = -expn, fac = 1.0; n!=0; n >>= 1, p2 *= p2) {
          if ((n & 1) != 0) {
            fac *= p2;
          }
        }
        ans /= fac;
      } else {
        for (n = expn, fac = 1.0; n!=0; n >>= 1, p2 *= p2) {
          if ((n & 1) != 0) fac *= p2;
        }
        ans *= fac;
      }
    }

    // Safeguard against malformed input
    if(p < endIndex){
      ans = DoubleVector.NA;
      p = 0; /* back out */
      return (sign * ans);
    }

    return sign * ans;
  }


  /**
   * Finds the closest double-precision floating point number to the given decimal string, parsed by
   * {@link #parseDoubleDecimal(CharSequence, int, int, int, char)} above.
   *
   * <p>This implementation is based on OpenJDK's {@code com.sun.misc.FloatingDecimal.ASCIIToBinaryBuffer.doubleValue()},
   * but included here nearly verbatim to avoid a dependency on an internal SDK class. The original code
   * is copyright 1996, 2013, Oracle and/or its affiliates and licensed under the GPL v2.</p></p>
   *
   * @param in the input string
   * @param sign the sign, -1 or +1, parsed above in {@link #parseDouble(CharSequence, int, int, char, boolean)}
   * @param startIndex the index at which to start parsing
   * @param endIndex the index, exclusive, at which to stop parsing
   * @param decimalPoint the decimal point character to use. Generally either '.' or ','
   * @return the number as a {@code double}, or {@code NA} if the string is malformatted.
   */
  public static double doubleValue(boolean isNegative, int decExponent, char[] digits, int nDigits) {
    int kDigits = Math.min(nDigits, MAX_DECIMAL_DIGITS + 1);
    //
    // convert the lead kDigits to a long integer.
    //
    // (special performance hack: start to do it using int)
    int iValue = (int) digits[0] - (int) '0';
    int iDigits = Math.min(kDigits, INT_DECIMAL_DIGITS);
    for (int i = 1; i < iDigits; i++) {
      iValue = iValue * 10 + (int) digits[i] - (int) '0';
    }
    long lValue = (long) iValue;
    for (int i = iDigits; i < kDigits; i++) {
      lValue = lValue * 10L + (long) ((int) digits[i] - (int) '0');
    }
    double dValue = (double) lValue;
    int exp = decExponent - kDigits;
    //
    // lValue now contains a long integer with the value of
    // the first kDigits digits of the number.
    // dValue contains the (double) of the same.
    //

    if (nDigits <= MAX_DECIMAL_DIGITS) {
      //
      // possibly an easy case.
      // We know that the digits can be represented
      // exactly. And if the exponent isn't too outrageous,
      // the whole thing can be done with one operation,
      // thus one rounding error.
      // Note that all our constructors trim all leading and
      // trailing zeros, so simple values (including zero)
      // will always end up here
      //
      if (exp == 0 || dValue == 0.0) {
        return (isNegative) ? -dValue : dValue; // small floating integer
      }
      else if (exp >= 0) {
        if (exp <= MAX_SMALL_TEN) {
          //
          // Can get the answer with one operation,
          // thus one roundoff.
          //
          double rValue = dValue * SMALL_10_POW[exp];
          return (isNegative) ? -rValue : rValue;
        }
        int slop = MAX_DECIMAL_DIGITS - kDigits;
        if (exp <= MAX_SMALL_TEN + slop) {
          //
          // We can multiply dValue by 10^(slop)
          // and it is still "small" and exact.
          // Then we can multiply by 10^(exp-slop)
          // with one rounding.
          //
          dValue *= SMALL_10_POW[slop];
          double rValue = dValue * SMALL_10_POW[exp - slop];
          return (isNegative) ? -rValue : rValue;
        }
        //
        // Else we have a hard case with a positive exp.
        //
      } else {
        if (exp >= -MAX_SMALL_TEN) {
          //
          // Can get the answer in one division.
          //
          double rValue = dValue / SMALL_10_POW[-exp];
          return (isNegative) ? -rValue : rValue;
        }
        //
        // Else we have a hard case with a negative exp.
        //
      }
    }

    //
    // Harder cases:
    // The sum of digits plus exponent is greater than
    // what we think we can do with one error.
    //
    // Start by approximating the right answer by,
    // naively, scaling by powers of 10.
    //
    if (exp > 0) {
      if (decExponent > MAX_DECIMAL_EXPONENT + 1) {
        //
        // Lets face it. This is going to be
        // Infinity. Cut to the chase.
        //
        return (isNegative) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
      }
      if ((exp & 15) != 0) {
        dValue *= SMALL_10_POW[exp & 15];
      }
      if ((exp >>= 4) != 0) {
        int j;
        for (j = 0; exp > 1; j++, exp >>= 1) {
          if ((exp & 1) != 0) {
            dValue *= BIG_10_POW[j];
          }
        }
        //
        // The reason for the weird exp > 1 condition
        // in the above loop was so that the last multiply
        // would get unrolled. We handle it here.
        // It could overflow.
        //
        double t = dValue * BIG_10_POW[j];
        if (Double.isInfinite(t)) {
          //
          // It did overflow.
          // Look more closely at the result.
          // If the exponent is just one too large,
          // then use the maximum finite as our estimate
          // value. Else call the result infinity
          // and punt it.
          // ( I presume this could happen because
          // rounding forces the result here to be
          // an ULP or two larger than
          // Double.MAX_VALUE ).
          //
          t = dValue / 2.0;
          t *= BIG_10_POW[j];
          if (Double.isInfinite(t)) {
            return (isNegative) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
          }
          t = Double.MAX_VALUE;
        }
        dValue = t;
      }
    } else if (exp < 0) {
      exp = -exp;
      if (decExponent < MIN_DECIMAL_EXPONENT - 1) {
        //
        // Lets face it. This is going to be
        // zero. Cut to the chase.
        //
        return (isNegative) ? -0.0 : 0.0;
      }
      if ((exp & 15) != 0) {
        dValue /= SMALL_10_POW[exp & 15];
      }
      if ((exp >>= 4) != 0) {
        int j;
        for (j = 0; exp > 1; j++, exp >>= 1) {
          if ((exp & 1) != 0) {
            dValue *= TINY_10_POW[j];
          }
        }
        //
        // The reason for the weird exp > 1 condition
        // in the above loop was so that the last multiply
        // would get unrolled. We handle it here.
        // It could underflow.
        //
        double t = dValue * TINY_10_POW[j];
        if (t == 0.0) {
          //
          // It did underflow.
          // Look more closely at the result.
          // If the exponent is just one too small,
          // then use the minimum finite as our estimate
          // value. Else call the result 0.0
          // and punt it.
          // ( I presume this could happen because
          // rounding forces the result here to be
          // an ULP or two less than
          // Double.MIN_VALUE ).
          //
          t = dValue * 2.0;
          t *= TINY_10_POW[j];
          if (t == 0.0) {
            return (isNegative) ? -0.0 : 0.0;
          }
          t = Double.MIN_VALUE;
        }
        dValue = t;
      }
    }

    //
    // dValue is now approximately the result.
    // The hard part is adjusting it, by comparison
    // with FDBigInteger arithmetic.
    // Formulate the EXACT big-number result as
    // bigD0 * 10^exp
    //
    if (nDigits > MAX_NDIGITS) {
      nDigits = MAX_NDIGITS + 1;
      digits[MAX_NDIGITS] = '1';
    }
    FDBigInteger bigD0 = new FDBigInteger(lValue, digits, kDigits, nDigits);
    exp = decExponent - nDigits;

    long ieeeBits = Double.doubleToRawLongBits(dValue); // IEEE-754 bits of double candidate
    final int B5 = Math.max(0, -exp); // powers of 5 in bigB, value is not modified inside correctionLoop
    final int D5 = Math.max(0, exp); // powers of 5 in bigD, value is not modified inside correctionLoop
    bigD0 = bigD0.multByPow52(D5, 0);
    bigD0.makeImmutable();   // prevent bigD0 modification inside correctionLoop
    FDBigInteger bigD = null;
    int prevD2 = 0;

    correctionLoop:
    while (true) {
      // here ieeeBits can't be NaN, Infinity or zero
      int binexp = (int) (ieeeBits >>> EXP_SHIFT);
      long bigBbits = ieeeBits & SIGNIF_BIT_MASK;
      if (binexp > 0) {
        bigBbits |= FRACT_HOB;
      } else { // Normalize denormalized numbers.
        assert bigBbits != 0L : bigBbits; // doubleToBigInt(0.0)
        int leadingZeros = Long.numberOfLeadingZeros(bigBbits);
        int shift = leadingZeros - (63 - EXP_SHIFT);
        bigBbits <<= shift;
        binexp = 1 - shift;
      }
      binexp -= EXP_BIAS;
      int lowOrderZeros = Long.numberOfTrailingZeros(bigBbits);
      bigBbits >>>= lowOrderZeros;
      final int bigIntExp = binexp - EXP_SHIFT + lowOrderZeros;
      final int bigIntNBits = EXP_SHIFT + 1 - lowOrderZeros;

      //
      // Scale bigD, bigB appropriately for
      // big-integer operations.
      // Naively, we multiply by powers of ten
      // and powers of two. What we actually do
      // is keep track of the powers of 5 and
      // powers of 2 we would use, then factor out
      // common divisors before doing the work.
      //
      int B2 = B5; // powers of 2 in bigB
      int D2 = D5; // powers of 2 in bigD
      int Ulp2;   // powers of 2 in halfUlp.
      if (bigIntExp >= 0) {
        B2 += bigIntExp;
      } else {
        D2 -= bigIntExp;
      }
      Ulp2 = B2;
      // shift bigB and bigD left by a number s. t.
      // halfUlp is still an integer.
      int hulpbias;
      if (binexp <= -EXP_BIAS) {
        // This is going to be a denormalized number
        // (if not actually zero).
        // half an ULP is at 2^-(DoubleConsts.EXP_BIAS+EXP_SHIFT+1)
        hulpbias = binexp + lowOrderZeros + EXP_BIAS;
      } else {
        hulpbias = 1 + lowOrderZeros;
      }
      B2 += hulpbias;
      D2 += hulpbias;
      // if there are common factors of 2, we might just as well
      // factor them out, as they add nothing useful.
      int common2 = Math.min(B2, Math.min(D2, Ulp2));
      B2 -= common2;
      D2 -= common2;
      Ulp2 -= common2;
      // do multiplications by powers of 5 and 2
      FDBigInteger bigB = FDBigInteger.valueOfMulPow52(bigBbits, B5, B2);
      if (bigD == null || prevD2 != D2) {
        bigD = bigD0.leftShift(D2);
        prevD2 = D2;
      }
      //
      // to recap:
      // bigB is the scaled-big-int version of our floating-point
      // candidate.
      // bigD is the scaled-big-int version of the exact value
      // as we understand it.
      // halfUlp is 1/2 an ulp of bigB, except for special cases
      // of exact powers of 2
      //
      // the plan is to compare bigB with bigD, and if the difference
      // is less than halfUlp, then we're satisfied. Otherwise,
      // use the ratio of difference to halfUlp to calculate a fudge
      // factor to add to the floating value, then go 'round again.
      //
      FDBigInteger diff;
      int cmpResult;
      boolean overvalue;
      if ((cmpResult = bigB.cmp(bigD)) > 0) {
        overvalue = true; // our candidate is too big.
        diff = bigB.leftInplaceSub(bigD); // bigB is not user further - reuse
        if ((bigIntNBits == 1) && (bigIntExp > -EXP_BIAS + 1)) {
          // candidate is a normalized exact power of 2 and
          // is too big (larger than Double.MIN_NORMAL). We will be subtracting.
          // For our purposes, ulp is the ulp of the
          // next smaller range.
          Ulp2 -= 1;
          if (Ulp2 < 0) {
            // rats. Cannot de-scale ulp this far.
            // must scale diff in other direction.
            Ulp2 = 0;
            diff = diff.leftShift(1);
          }
        }
      } else if (cmpResult < 0) {
        overvalue = false; // our candidate is too small.
        diff = bigD.rightInplaceSub(bigB); // bigB is not user further - reuse
      } else {
        // the candidate is exactly right!
        // this happens with surprising frequency
        break correctionLoop;
      }
      cmpResult = diff.cmpPow52(B5, Ulp2);
      if ((cmpResult) < 0) {
        // difference is small.
        // this is close enough
        break correctionLoop;
      } else if (cmpResult == 0) {
        // difference is exactly half an ULP
        // round to some other value maybe, then finish
        if ((ieeeBits & 1) != 0) { // half ties to even
          ieeeBits += overvalue ? -1 : 1; // nextDown or nextUp
        }
        break correctionLoop;
      } else {
        // difference is non-trivial.
        // could scale addend by ratio of difference to
        // halfUlp here, if we bothered to compute that difference.
        // Most of the time ( I hope ) it is about 1 anyway.
        ieeeBits += overvalue ? -1 : 1; // nextDown or nextUp
        if (ieeeBits == 0 || ieeeBits == EXP_BIT_MASK) { // 0.0 or Double.POSITIVE_INFINITY
          break correctionLoop; // oops. Fell off end of range.
        }
        continue; // try again.
      }

    }
    if (isNegative) {
      ieeeBits |= SIGN_BIT_MASK;
    }
    return Double.longBitsToDouble(ieeeBits);
  }


  private static boolean equalsIgnoringCase(CharSequence s, int start, int endIndex, String word) {
    int lenRemaining = endIndex-start;
    int wordLen = word.length();
    if(lenRemaining != wordLen) {
      return false;
    }
    for(int i=0;i<wordLen;++i) {
      if(Character.toUpperCase(s.charAt(start+i)) != word.charAt(i)) {
        return false;
      }
    }
    return true;
  }
}
