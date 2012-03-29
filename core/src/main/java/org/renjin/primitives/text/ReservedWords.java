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
