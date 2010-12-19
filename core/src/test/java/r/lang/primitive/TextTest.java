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

package r.lang.primitive;

import org.junit.Test;
import r.lang.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TextTest extends EvalTestCase {

  @Test
  public void simplePaste() {
      assertThat( eval( ".Internal(paste(list(1, 'a', 'b'), '-', NULL)) "), equalTo(  c("1-a-b") )) ;
  }

  @Test
  public void pasteVectors() {
      assertThat( eval( ".Internal(paste(list(c('x', 'y'), 'a', 'b'), '-', NULL)) "),
          equalTo(  c("x-a-b", "y-a-b") )) ;
  }

  @Test
  public void pasteCollapse() {
      assertThat( eval( ".Internal(paste(list(c('x', 'y'), 'a', 'b'), '-', '+')) "),
          equalTo(  c("x-a-b+y-a-b") )) ;
  }

  @Test
  public void gettext() {
     assertThat( eval( ".Internal(gettext('hungarian', 'hello world'))"), equalTo( c("hello world")));
     assertThat( eval( ".Internal(gettext(NULL, 'hello world'))"), equalTo( c("hello world")));
  }

  @Test
  public void ngettext() {
      assertThat( eval( ".Internal(ngettext(1, 'baby', 'babies', 'hungarian'))"), equalTo( c("baby")));
      assertThat( eval( ".Internal(ngettext(1, 'baby', 'babies', NULL))"), equalTo( c("baby")));
  }

  @Test
  public void sub() {
    assertThat( eval(".Internal(sub('[[:blank:]]*([[:alnum:]]+)', '\\\\1', " +
        "c('datasets', 'utils', 'grDevices', 'graphics', 'stats', 'methods'), FALSE, TRUE, FALSE, FALSE, FALSE))"),
        equalTo( c("datasets", "utils", "grDevices", "graphics", "stats", "methods")) );
  }

  @Test
  public void sprintf() {
    eval("sprintf <- function (fmt, ...) .Internal(sprintf(fmt, ...))");
    eval("pi <- 3.14159265358979");

    assertThat( eval("sprintf('%f', pi)"), equalTo( c("3.141593")));
    assertThat( eval("sprintf('%.3f', pi)"), equalTo( c("3.142")));
    assertThat( eval("sprintf('%1.0f', pi)"), equalTo( c("3")));
    assertThat( eval("sprintf('%5.1f', pi)"), equalTo(  c("  3.1")));
    assertThat( eval("sprintf('%05.1f', pi)"), equalTo( c("003.1")));
    assertThat( eval("sprintf('% f', pi)"), equalTo( c(" 3.141593")));
    assertThat( eval("sprintf('%e', pi)"), equalTo( c("3.141593e+00")));
    assertThat( eval("sprintf('%E', pi)"), equalTo( c("3.141593E+00")));
   // assertThat( eval("sprintf('%g', pi)"), equalTo( c("3.14159")));

     // Argument Recyling
    assertThat( eval("sprintf(c('a%d', 'b%d'), c(1,2,3,4))"), equalTo( c("a1", "b2", "a3", "b4")));
  }

  @Test
  public void translateChars() {
    assertThat( eval(".Internal(chartr('abc', 'xyz', 'abcdefabc'))"), equalTo( c("xyzdefxyz")));
    assertThat( eval(".Internal(chartr('abc', 'xyz', c('abc', 'cba', 'ccc')))"),
        equalTo( c("xyz", "zyx", "zzz")));

    assertThat( eval(".Internal(tolower('ABCdeFG 123'))"), equalTo( c("abcdefg 123")));
    assertThat( eval(".Internal(toupper('ABCdeFG 123'))"), equalTo( c("ABCDEFG 123")));

  }

  @Test
  public void nchar() {
    eval("nchar <- function (x, type = 'chars', allowNA = FALSE) .Internal(nchar(x, type, allowNA))");

    assertThat( eval("nchar('xyz')"), equalTo( c_i(3) ));
    assertThat( eval("nchar(c('xyz', NA, 'a', '', 'abcde'))"), equalTo( c_i(3, 2, 1, 0, 5) ));
  }
}
