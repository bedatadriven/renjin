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

import org.renjin.eval.EvalException;

import java.util.function.Predicate;

/**
 * Compiles a regular expression based on the supplied options.
 */
public class REFactory {

  /**
   * Compiles the pattern based on the supplied arguments.
   *
   * @param pattern    the matching pattern
   * @param ignoreCase whether case should be ignored
   * @param extended   true to use "extended" regular expression
   * @param perl       true to use "perl-style" regular expressions (not yet implemented)
   * @param fixed      true to treat the pattern as
   * @param useBytes   true to match on bytes (not implemented)
   * @return the compiled regular expression
   */
  public static RE compile(String pattern, boolean ignoreCase, boolean perl, boolean fixed,
                           boolean useBytes) {

    try {
      if (fixed) {
        if (pattern.length() == 0) {
          return new EmptyFixedRE();
        } else {
          return new FixedRE(pattern);
        }
      } else {
        // There are a few cases that are a bit illogical
        // but we need to handle like GNU R does...
        if (!perl && pattern.equals("*")) {
          return new EmptyFixedRE();
        }

        return new ExtendedRE(pattern, ignoreCase);
      }
    } catch (RESyntaxException e) {
      throw new EvalException("Invalid pattern '%s': %s (perl=%s, fixed=%s)",
          pattern,
          e.getMessage(),
          perl ? "TRUE" : "FALSE",
          fixed ? "TRUE" : "FALSE");
    }
  }

  public static Predicate<String> asPredicate(final RE re) {
    return input -> re.match(input);
  }
}
