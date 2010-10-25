/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.parser;

import com.google.common.base.Function;
import r.lang.Logical;

import java.text.NumberFormat;

public class ParseUtil {
  public static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance();
  public static final NumberFormat REAL_FORMAT = createRealFormat();


  public static void appendEscaped(StringBuilder buf, String s) {
    for(int i=0;i!=s.length(); ++i) {

      int codePoint = s.codePointAt(i);
      if(codePoint == '\n') {
        buf.append("\\n");
      } else if(codePoint == '\r') {
        buf.append("\\r");
      } else if(codePoint == '\t') {
        buf.append("\\t");
      } else if(codePoint == 7) {
        buf.append("\\a");
      } else if(codePoint == '\b') {
        buf.append("\\b");
      } else if(codePoint == '\f') {
        buf.append("\\f");
      } else if(codePoint == 11) {
        buf.append("\\v");
      } else if(codePoint == '\"') {
        buf.append("\\\"");
      } else if(codePoint == '\\') {
        buf.append("\\\\");
      } else if(codePoint < 32 || codePoint > 126) {
        buf.append("\\u");
        buf.append(Integer.toHexString(codePoint));
      } else
        buf.appendCodePoint(codePoint);
    }
  }

  public static double parseDouble(String text) {
    if (text.startsWith("0x")) {
      return Integer.parseInt(text.substring(2), 16);
    } else {
      return Double.parseDouble(text);
    }
  }

  public static NumberFormat createRealFormat() {
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits(0);
    format.setMaximumFractionDigits(15);
    return format;
  }

  public static String toString(int value) {
    return INTEGER_FORMAT.format(value);
  }

  public static String toString(double value) {
    return REAL_FORMAT.format(value);
  }

  public static class RealPrinter implements Function<Double, String> {
    @Override
    public String apply(Double aDouble) {
      return ParseUtil.toString(aDouble);
    }
  }

  public static class RealDeparser extends RealPrinter {

  }

  public static class IntPrinter implements Function<Integer, String> {
    @Override
    public String apply(Integer integer) {
      return ParseUtil.toString(integer);
    }
  }

  public static class IntDeparser extends IntPrinter {

  }

  public static class LogicalPrinter implements Function<Logical, String> {
    @Override
    public String apply(Logical logical) {
      return logical.toString();
    }
  }

  public static class LogicalDeparser extends LogicalPrinter {

  }

  public static class StringPrinter implements Function<String, String> {
    @Override
    public String apply(String s) {
      StringBuilder sb = new StringBuilder("\"");
      appendEscaped(sb, s);
      sb.append('"');
      return sb.toString();
    }
  }

  public static class StringDeparser extends StringPrinter {

  }
}
