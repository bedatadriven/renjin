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

package r.base;

import org.junit.Test;
import r.EvalTestCase;
import r.lang.SEXP;
import r.lang.StringVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TextTest extends EvalTestCase {

  @Test
  public void simplePaste() {
      assertThat( eval( ".Internal(paste(list(1, 'a', 'b'), '-', NULL)) "), equalTo(  c("1-a-b") )) ;
  }

  @Test
  public void pasteWithEmptyVector() {
      assertThat( eval( ".Internal(paste(list('a', c()), '-', NULL)) "), equalTo(  c("a-") )) ;
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
      assertThat( eval( ".Internal(ngettext(2, 'baby', 'babies', NULL))"), equalTo( c("babies")));
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

    // empty list
    assertThat( eval("sprintf('%s', c())"), equalTo( (SEXP)new StringVector()));
  }
  
  @Test
  public void sprintfWithAsCharacter() {
    eval("as.character.foo <- function(x) 'FOO!' ");
    eval("sprintf <- function (fmt, ...) .Internal(sprintf(fmt, ...))");

    eval("x <- list(1,2,3) ");
    eval("class(x) <- 'foo' ");
    
    assertThat( eval("sprintf('i say %s', x)"), equalTo(c("i say FOO!")));
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
  public void substr() {

    assertThat( eval(" .Internal(substr('lazy cat', 2, 4))" ), equalTo(c("azy")));
    assertThat( eval(" .Internal(substr('foo', 1, 99))" ), equalTo(c("foo")));

  }


  @Test
  public void nchar() {
    eval("nchar <- function (x, type = 'chars', allowNA = FALSE) .Internal(nchar(x, type, allowNA))");

    assertThat( eval("nchar('xyz')"), equalTo( c_i(3) ));
    assertThat( eval("nchar(c('xyz', NA, 'a', '', 'abcde'))"), equalTo( c_i(3, 2, 1, 0, 5) ));
    assertThat( eval("nchar(NULL)"), equalTo( c_i() ));
  }
  
  @Test
  public void ncharWithNas() {
	  assertThat( eval(".Internal(nchar(c(NA,-5),'chars', FALSE))"), equalTo( c_i(2,2) ));
  }

  @Test
  public void split() {
    eval("strsplit <- function (x, split, extended = TRUE, fixed = FALSE, perl = FALSE, useBytes = FALSE) " +
            ".Internal(strsplit(x, as.character(split), as.logical(extended),  as.logical(fixed), " +
                      "as.logical(perl), as.logical(useBytes)))");

    assertThat( eval("strsplit('a,b', ',')"), equalTo( list( c("a","b") )));
    assertThat( eval("strsplit('the   slow lazy  dog etc', '\\\\s+')"),
        equalTo( list( c("the","slow", "lazy", "dog", "etc") )));
    assertThat( eval("strsplit(c('a b c d e', '1 2 3 4'), '\\\\s+')"),
        equalTo( list( c("a", "b", "c", "d", "e"), c("1","2", "3","4"))));

    assertThat( eval("strsplit('abc','')"), equalTo( list( c("a","b","c") )));

    assertThat( eval("strsplit('|ab|cf|q||','|',fixed=TRUE)"), equalTo( list( c("", "ab","cf","q", "", "") )));

  }

  @Test
  public void grepFixed() {
    eval(" grep <- function (pattern, x, ignore.case = FALSE, extended = TRUE, perl = FALSE, " +
        "    value = FALSE, fixed = FALSE, useBytes = FALSE, invert = FALSE)  " +
        " .Internal(grep(as.character(pattern), x, ignore.case, extended, " +
        "        value, perl, fixed, useBytes, invert))");


    assertThat( eval(" grep('[', '[[', fixed=TRUE) "), equalTo(c_i(1)));
  }

  @Test
  public void makeNames() {
    assertThat(
        eval(".Internal(make.names(c('a', '1', 'if', 'a', '.', NA_character_, '_a', '.2way', '.legal', '$#@foo_bar!'), FALSE))"),
        equalTo( c("a", "X1", "if.", "a", ".",  "NA.", "X_a", "X.2way", ".legal", "X...foo_bar.") ));
  }

  @Test
  public void makeUnique() {
    assertThat( eval(".Internal(make.unique(c('a', 'b', 'a'), '.'))"), equalTo(c("a", "b", "a.1")));
  }
  
  @Test
  public void strtrim(){
    assertThat( eval(".Internal(strtrim(c('abcdef', 'abcdef', 'abcdef'), c(1,5,10)))"), equalTo(c("a", "abcde", "abcdef")));
    assertThat( eval(".Internal(strtrim(\"abcdef\", 3))"), equalTo(c("abc")));
  }
  
}
