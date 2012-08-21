/*
 *  Copyright (C) Martin Maechler, 1994, 1998
 *  Copyright (C) 2001-2011 the R Development Core Team
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  http://www.r-project.org/Licenses/
 *
 *  I want you to preserve the copyright of the original author(s),
 *  and encourage you to send me any improvements by e-mail. (MM).
 *
 *  Originally from Bill Dunlap
 *  bill@stat.washington.edu
 *  Wed Feb 21, 1990
 *
 *  Much improved by Martin Maechler, including the "fg" format.
 *
 *  Patched by Friedrich.Leisch@ci.tuwien.ac.at
 *  Fri Nov 22, 1996
 *
 *  Some fixes by Ross Ihaka
 *  ihaka@stat.auckland.ac.nz
 *  Sat Dec 21, 1996
 *  Integer arguments changed from "long" to "int"
 *  Bus error due to non-writable strings fixed
 *
 *  BDR 2001-10-30 use R_alloc not Calloc as memory was not
 *  reclaimed on error (and there are many error exits).
 *
 *  type  "double" or "integer" (R - numeric 'mode').
 *

 */

package org.renjin.base;


import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StrSignIf {

  /**
   *  
   * @param x
   * @param n
   * @param type
   * @param width  The total field width; width < 0 means to left justify
   *    the number in this field (equivalent to flag = "-").
   *    It is possible that the result will be longer than this,
   *    but that should only happen in reasonable cases.
   * @param digits The desired number of digits after the decimal point.
   *    digits < 0 uses the default for C, namely 6 digits.
   * @param format   "d" (for integers) or "f", "e","E", "g", "G" (for 'real')
   *    "f" gives numbers in the usual "xxx.xxx" format;
   *    "e" and "E" give n.ddde<nn> or n.dddE<nn> (scientific format);
   *    "g" and "G" puts them into scientific format if it saves
   *    space to do so.
   *      NEW: "fg" gives numbers in "xxx.xxx" format as "f",
   *      ~~  however, digits are *significant* digits and,
   *           if digits > 0, no trailing zeros are produced, as in "g".
   * @param flag Format modifier as in K&R "C", 2nd ed., p.243;
   *    e.g., "0" pads leading zeros; "-" does left adjustment
   *    the other possible flags are  "+", " ", and "#".
   *    New (Feb.98): if flag has more than one character, all are passed..
   * @return
   */
  public static StringVector str_signif(Vector x, int width, int digits,
      String format, String flag)
  {
      NumberFormat formatter = buildFormat(digits, format, flag);
      
      StringArrayVector.Builder result = new StringArrayVector.Builder();
      for(int i=0;i!=x.length();++i) {
        result.add(formatter.format(x.getElementAsDouble(i)));
      }
      return result.build();
  
  }

  private static NumberFormat buildFormat(int digits, String format, String flag) {
    if(format.equals("d")) {
      return NumberFormat.getIntegerInstance();
    } else {
      
      DecimalFormat formatter;
      if(format.equals("e")) {
        formatter = new DecimalFormat("0.00e0");
      } else if(format.equals("E")) {
        formatter = new DecimalFormat("0.00E0");
      } else {
        formatter = new DecimalFormat();
      }
      if(digits < 0) {
        digits = 6;
      }
      formatter.setMaximumFractionDigits(digits);
      formatter.setMinimumFractionDigits(digits);
      return formatter;
    }
  }
}
