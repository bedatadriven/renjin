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

package org.renjin.primitives.text.regex;

/**
 * Compiled regular expression.
 */
public interface RE {
  /**
   * Flag bit that indicates that subst should replace all occurrences of this
   * regular expression.
   */
  int REPLACE_ALL            = 0x0000;
  /**
   * Flag bit that indicates that subst should only replace the first occurrence
   * of this regular expression.
   */
  int REPLACE_FIRSTONLY      = 0x0001;

  /**
   * Flag bit that indicates that subst should replace backreferences
   */
  int REPLACE_BACKREFERENCES = 0x0002;


  /**
   * Matches the current regular expression program against a character array.
   *
   * @param search String to match against
   * @return True if string matched
   */
  boolean match(String search);

  /**
   * Substitutes a string for this regular expression in another string.
   * This method works like the Perl function of the same name.
   * Given a regular expression of "a*b", a String to substituteIn of
   * "aaaabfooaaabgarplyaaabwackyb" and the substitution String "-", the
   * resulting String returned by subst would be "-foo-garply-wacky-".
   *
   * @param substituteIn String to substitute within
   * @param substitution String to substitute for all matches of this regular expression.
   * @return The string substituteIn with zero or more occurrences of the current
   * regular expression replaced with the substitution String (if this regular
   * expression object doesn't match at any position, the original String is returned
   * unchanged).
   */
  String subst(String substituteIn, String substitution);


  /**
   * Substitutes a string for this regular expression in another string.
   * This method works like the Perl function of the same name.
   * Given a regular expression of "a*b", a String to substituteIn of
   * "aaaabfooaaabgarplyaaabwackyb" and the substitution String "-", the
   * resulting String returned by subst would be "-foo-garply-wacky-".
   * <p>
   * It is also possible to reference the contents of a parenthesized expression
   * with $0, $1, ... $9. A regular expression of "http://[\\.\\w\\-\\?/~_@&=%]+",
   * a String to substituteIn of "visit us: http://www.apache.org!" and the
   * substitution String "&lt;a href=\"$0\"&gt;$0&lt;/a&gt;", the resulting String
   * returned by subst would be
   * "visit us: &lt;a href=\"http://www.apache.org\"&gt;http://www.apache.org&lt;/a&gt;!".
   * <p>
   * <i>Note:</i> $0 represents the whole match.
   *
   * @param substituteIn String to substitute within
   * @param substitution String to substitute for matches of this regular expression
   * @param flags One or more bitwise flags from REPLACE_*.  If the REPLACE_FIRSTONLY
   * flag bit is set, only the first occurrence of this regular expression is replaced.
   * If the bit is not set (REPLACE_ALL), all occurrences of this pattern will be
   * replaced. If the flag REPLACE_BACKREFERENCES is set, all backreferences will
   * be processed.
   * @return The string substituteIn with zero or more occurrences of the current
   * regular expression replaced with the substitution String (if this regular
   * expression object doesn't match at any position, the original String is returned
   * unchanged).
   */
  String subst(String substituteIn, String substitution, int flags);

  /**
   * Splits a string into an array of strings on regular expression boundaries.
   * This function works the same way as the Perl function of the same name.
   * Given a regular expression of "[ab]+" and a string to split of
   * "xyzzyababbayyzabbbab123", the result would be the array of Strings
   * "[xyzzy, yyz, 123]".
   *
   * <p>Please note that the first string in the resulting array may be an empty
   * string. This happens when the very first character of input string is
   * matched by the pattern.
   *
   * @param s String to split on this regular exression
   * @return Array of strings
   */
  String[] split(String s);
  
  /**
   * 
   * @return the character index of the last matched group, or -1 if the group
   * was not matched. 
   */
  int getGroupStart(int groupIndex);
  
  /**
   * 
   * @return the character index of the end of the last matched group, or -1 if the group
   * was not matched. 
   */
  int getGroupEnd(int groupIndex);
  
}
