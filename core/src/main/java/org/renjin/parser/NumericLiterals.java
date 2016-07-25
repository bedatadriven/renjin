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

  /**
   * Formats a {@code double} as a literal
   * @param value the value to be formatted
   * @param naString the string to use when {@code RealExp.isNA(value) } is {@code true}
   * @return
   */
  public static String format(double value, String naString) {
    if(DoubleVector.isNA(value)) {
      return naString;
    } else if(Double.isNaN(value)) {
      return "NaN";
    } else if(Double.isInfinite(value)) {
      return "Inf";
    } else {
      return toString(value);
    }
  }

  public static String format(int value) {
    return Integer.toString(value);
  }

  public static String toString(double value) {
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
   */
  public static double parseDouble(CharSequence s, int startIndex, int endIndex, char dec, boolean NA) {
    double ans = 0.0, p10 = 10.0, fac = 1.0;
    int n;
    int expn = 0;
    int sign = 1;
    int ndigits = 0;
    int exph = -1;
    int p = startIndex;
  
      /* optional whitespace */
    while ( p < endIndex && Character.isWhitespace(s.charAt(p))) {
      p++;
    }

    if (NA && (p+2 < endIndex) && s.charAt(p) == 'N' && s.charAt(p+1) == 'A') {
      ans = DoubleVector.NA;
      p += 2;
      return ans;
    }

    if( p == endIndex) {
      return DoubleVector.NA;
    }
  
      /* optional sign */
    switch (s.charAt(p)) {
      case '-':
        sign = -1;
        p++;
        break;
      case '+':
        p++;
        break;
    }

    if ( nextWordIgnoringCaseIs(s, p, endIndex, "NAN")) {
      ans = Double.NaN;
      p += 3;
      return (sign * ans);

    } else if ( nextWordIgnoringCaseIs(s, p, endIndex, "INF") ) {
      ans = Double.POSITIVE_INFINITY;
      p += 3;
      return (sign * ans);
  
        /* C99 specifies this */
    } else if ( nextWordIgnoringCaseIs(s, p, endIndex, "INFINITY") ) {
      ans = Double.POSITIVE_INFINITY;
      p += 8;
      return (sign * ans);
    }

    if(( (endIndex-p) > 2) && s.charAt(p) == '0' && (s.charAt(p+1) == 'x' || s.charAt(p+2) == 'X')) {
        /* This will overflow to Inf if appropriate */
      for(p += 2; p<s.length(); p++) {
        if('0' <= s.charAt(p) && s.charAt(p) <= '9') {
          ans = 16 * ans + (s.charAt(p) - '0');
        } else if('a' <= s.charAt(p) && s.charAt(p) <= 'f') {
          ans = 16 * ans + (s.charAt(p) - 'a' + 10);
        } else if('A' <= s.charAt(p) && s.charAt(p) <= 'F') {
          ans = 16 * ans + (s.charAt(p) - 'A' + 10);
        } else if(s.charAt(p) == dec) {
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
      return sign * ans;
    }

    for ( ; p < endIndex && s.charAt(p) >= '0' && s.charAt(p) <= '9'; p++, ndigits++) {
      ans = 10 * ans + (s.charAt(p) - '0');
    }
    if ( p < endIndex && s.charAt(p) == dec) {
      for (p++; p < endIndex && s.charAt(p) >= '0' && s.charAt(p) <= '9'; p++, ndigits++, expn--) {
        ans = 10 * ans + (s.charAt(p) - '0');
      }
    }
    if (ndigits == 0) {
      ans = DoubleVector.NA;
      p = 0; /* back out */
      return (sign * ans);
    }

    if ( p < endIndex && (s.charAt(p) == 'e' || s.charAt(p) == 'E')) {
      int expsign = 1;
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
    }
  
      /* avoid unnecessary underflow for large negative exponents */
    if (expn + ndigits < -300) {
      for (n = 0; n < ndigits; n++) {
        ans /= 10.0;
      }
      expn += ndigits;
    }
    if (expn < -307) { /* use underflow, not overflow */
      for (n = -expn, fac = 1.0; n!=0; n >>= 1, p10 *= p10) {
        if ((n & 1) != 0) {
          fac /= p10;
        }
      }
      ans *= fac;
    } else if (expn < 0) { /* positive powers are exact */
      for (n = -expn, fac = 1.0; n!=0; n >>= 1, p10 *= p10) {
        if ((n & 1) != 0) {
          fac *= p10;
        }
      }
      ans /= fac;
    } else {
      for (n = expn, fac = 1.0; n!=0; n >>= 1, p10 *= p10) {
        if ((n & 1) != 0) {
          fac *= p10;
        }
      }
      ans *= fac;
    }
    return (sign * ans);
  }

  private static boolean nextWordIgnoringCaseIs(CharSequence s, int start, int endIndex, String word) {
    int lenRemaining = endIndex-start;
    int wordLen = word.length();
    if(lenRemaining < wordLen) {
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
