/*
 * Renjin Regular Expression Library, based on gnu-regexp
 * Copyright (C) 1998-2001 Wes Biggs
 * Copyright (C) 2016 BeDataDriven Groep BV
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.renjin.regexp;

/**
 * UncheckedRE is a subclass of RE that allows programmers an easier means
 * of programmatically precompiling regular expressions.  It is constructed
 * and used in exactly the same manner as an instance of the RE class; the
 * only difference is that its constructors do not throw REException.
 * Instead, if a syntax error is encountered during construction, a
 * RuntimeException will be thrown.
 * <p>
 * Note that this makes UncheckedRE dangerous if constructed with
 * dynamic data.  Do not use UncheckedRE unless you are completely sure
 * that all input being passed to it contains valid, well-formed
 * regular expressions for the syntax specified.
 *
 * @author <A HREF="mailto:wes@cacas.org">Wes Biggs</A>
 * @see RE
 * @since gnu.regexp 1.1.4
 */

public final class UncheckedRE extends RE {
  /**
   * Constructs a regular expression pattern buffer without any compilation
   * flags set, and using the default syntax (RESyntax.RE_SYNTAX_PERL5).
   *
   * @param pattern A regular expression pattern, in the form of a String,
   *                StringBuffer or char[].  Other input types will be converted to
   *                strings using the toString() method.
   * @throws RuntimeException     The input pattern could not be parsed.
   * @throws NullPointerException The pattern was null.
   */
  public UncheckedRE(Object pattern) {
    this(pattern, 0, RESyntax.RE_SYNTAX_PERL5);
  }

  /**
   * Constructs a regular expression pattern buffer using the specified
   * compilation flags and the default syntax (RESyntax.RE_SYNTAX_PERL5).
   *
   * @param pattern A regular expression pattern, in the form of a String,
   *                StringBuffer, or char[].  Other input types will be converted to
   *                strings using the toString() method.
   * @param cflags  The logical OR of any combination of the compilation flags in the RE class.
   * @throws RuntimeException     The input pattern could not be parsed.
   * @throws NullPointerException The pattern was null.
   */
  public UncheckedRE(Object pattern, int cflags) {
    this(pattern, cflags, RESyntax.RE_SYNTAX_PERL5);
  }

  /**
   * Constructs a regular expression pattern buffer using the specified
   * compilation flags and regular expression syntax.
   *
   * @param pattern A regular expression pattern, in the form of a String,
   *                StringBuffer, or char[].  Other input types will be converted to
   *                strings using the toString() method.
   * @param cflags  The logical OR of any combination of the compilation flags in the RE class.
   * @param syntax  The type of regular expression syntax to use.
   * @throws RuntimeException     The input pattern could not be parsed.
   * @throws NullPointerException The pattern was null.
   */
  public UncheckedRE(Object pattern, int cflags, RESyntax syntax) {
    try {
      initialize(pattern, cflags, syntax, 0, 0);
    } catch (REException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}


