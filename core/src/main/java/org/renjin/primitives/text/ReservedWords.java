/**
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
package org.renjin.primitives.text;

/**
 * The reserved words in R's parser.
 *
 * Reserved words outside quotes are always parsed to be references to the objects
 * linked to in the ‘Description’, and hence they are not allowed as syntactic names
 * (see make.names). They are allowed as non-syntactic names, e.g. inside backtick quotes.
 */
public class ReservedWords {

  public static final String[] WORDS = {
      "if",
      "else",
      "repeat",
      "while",
      "function",
      "for",
      "in",
      "next",
      "break",
      "TRUE",
      "FALSE",
      "NULL",
      "Inf",
      "NaN",
      "NA",
      "NA_integer_",
      "NA_real_",
      "NA_complex_",
      "NA_character_"
  };

  public static boolean isReserved(String word) {
    for(int i=0;i!=WORDS.length;++i) {
      if(WORDS[i].equals(word)) {
        return true;
      }
    }
    return false;
  }
}
