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

package org.renjin.primitives;

import com.google.common.base.Charsets;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;

import java.io.IOException;

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
  public void pasteWithNas() {
    assertThat( eval(".Internal(paste(list('a', NA_character_), ' ', NULL))"), equalTo( c("a NA")));
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
        "c('datasets', 'utils', 'grDevices', 'graphics', 'stats', 'methods'), FALSE, TRUE, FALSE, FALSE))"),
        equalTo( c("datasets", "utils", "grDevices", "graphics", "stats", "methods")) );
  }
  
  @Test
  public void posixCharacterClass() {
   
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
    assertThat( eval("sprintf('%s', c())"), equalTo( (SEXP)new StringArrayVector()));
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
    assertThat( eval("substr(c('ab', 'ab'), 1:2, 1:2)"), equalTo(c("a", "b")) );
    assertThat( eval("substr(c('ab', 'ab'), 1:3, 1:3)"), equalTo(c("a", "b")) );
    assertThat( eval("substr(c('ab', 'ab'), 1:2, 2:1)"), equalTo(c("ab", "")) );
    assertThat( eval("substr(c('ab', 'ab'), 1, 1)"), equalTo(c("a", "a")) );
    assertThat( eval("substring('abc', 1:3, 1:3)"), equalTo(c("a", "b", "c")));
    assertThat( eval("substring('abcdef', 1:4, 4:5)"), equalTo(c("abcd", "bcde", "cd", "de")));

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
            ".Internal(strsplit(x, as.character(split),  as.logical(fixed), " +
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
  public void emptyFixedRe() {
    eval("strsplit <- function (x, split, extended = TRUE, fixed = FALSE, perl = FALSE, useBytes = FALSE) " +
      ".Internal(strsplit(x, as.character(split),  as.logical(fixed), " +
      "as.logical(perl), as.logical(useBytes)))");

    eval("strsplit( c('helloh',  'hi'), c('h',''), fixed=TRUE)");
  }

  @Test
  public void grepFixed() {
    eval(" grep <- function (pattern, x, ignore.case = FALSE, perl = FALSE, " +
        "    value = FALSE, fixed = FALSE, useBytes = FALSE, invert = FALSE)  " +
        " .Internal(grep(as.character(pattern), x, ignore.case, " +
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
  
  @Test
  public void format() throws IOException {
    assumingBasePackagesLoad();
    
    assertThat( eval("format(c(1,10,1000))"), equalTo(c("   1", "  10", "1000")));
    assertThat( eval("format(c(1,10,1000),trim=T)"), equalTo(c("1", "10", "1000")));

  }

  @Test
  public void formatWithNAs() throws IOException {
    assumingBasePackagesLoad();
    
    assertThat(eval("format(c('Hello', NA, 'world', NA))"), equalTo(c( "Hello", "NA   ", "world", "NA   ")));    
    assertThat(eval("format(c('Hello', NA, 'world', NA), na.encode=FALSE)"), equalTo(c( "Hello", null, "world", null)));
  }
  
  @Test
  public void intToUtf8() {
    
    byte[] encoded = new String("€").getBytes(Charsets.UTF_8);
    assertThat(encoded.length, equalTo(3));
    java.lang.System.out.println(Integer.toHexString(encoded[0]) + " " + Integer.toHexString(encoded[1]) + " " + Integer.toHexString(encoded[2]));
    
    assertThat(eval(".Internal(utf8ToInt('hello'))"), equalTo(c_i(104,101,108, 108, 111)));
    assertThat(eval(".Internal(utf8ToInt(NA_character_))"), equalTo(c_i(IntVector.NA)));

    
    // check special handling of 0s
    assertThat(eval(".Internal(intToUtf8(c(104L,0,101L), FALSE))"), equalTo(c("he")));
    assertThat(eval(".Internal(intToUtf8(c(104L,0,101L), TRUE))"), equalTo(c("h", "", "e")));

    // check special handling of NAs
    assertThat(eval(".Internal(intToUtf8(c(104L,NA), FALSE))"), equalTo(c(StringVector.NA)));
    assertThat(eval(".Internal(intToUtf8(c(104L,NA), TRUE))"), equalTo(c("h", StringVector.NA)));
    
  }
  
  @Test
  public void setSubstring(){
    assumingBasePackagesLoad();
    eval("x<-\"aaaa\"");
    eval("substr(x,2,3) <- \"xx\"");
    assertThat(eval("x").toString(),equalTo("\"axxa\""));
  }

  @Test
  public void setSubstringAtBeginning(){
    assumingBasePackagesLoad();
    eval("x<-\"aaaa\"");
    eval("substr(x,1,2) <- \"xx\"");
    assertThat(eval("x").toString(),equalTo("\"xxaa\""));
  }
  
  @Test
  public void setSubstringAtEnd(){
    assumingBasePackagesLoad();
    eval("x<-\"aaaa\"");
    eval("substr(x,3,4) <- \"xx\"");
    assertThat(eval("x").toString(),equalTo("\"aaxx\""));
  }
  
  @Test
  public void setSubstringNumbersDontMatchUp(){
    assumingBasePackagesLoad();
    eval("x<-\"aaaa\"");
    eval("substr(x,1,4) <- \"xx\"");
    assertThat(eval("x").toString(),equalTo("\"xxaa\""));
  }

  @Test 
  public void iconv() {
    eval(".Internal(iconv(c('A','B'),'UTF-8','ASCII', as.character(NA), FALSE, FALSE))");
  }
  
  @Test
  public void strtoi() {
    assertThat(eval(".Internal(strtoi(c('0666','0xFF', '42'), 0L))"), equalTo(c_i(438, 255, 42)));
    assertThat(eval(".Internal(strtoi('0666', 10L))"), equalTo(c_i(666)));
    assertThat(eval(".Internal(strtoi('0xFF', 16L))"), equalTo(c_i(255)));    
  }

  @Test
  public void regexReplace() {
    assumingBasePackagesLoad();
    eval("pat <- '^R[[:space:]]*\\\\(([[<>=!]+)[[:space:]]+(.*)\\\\)[[:space:]]*' ");
    eval("x <- c('R (>= 2.10)', 'R(>= 2.4.0)', 'R (>= 2.10)')");
    eval("ops <-  sub(pat, \"\\\\1\", x)");
    eval("v_t_1 <- sub(pat, \"\\\\2\", x)");
    eval("v_t <- split(v_t_1, ops)");

    assertThat(eval("ops"), equalTo(c(">=", ">=", ">=")));
    assertThat(eval("v_t_1"), equalTo(c("2.10", "2.4.0", "2.10")));
    assertThat(eval("length(v_t)"), equalTo(c_i(1)));
    assertThat(eval("names(v_t)"), equalTo(c(">=")));
  }
  
  @Test
  public void regexprTest() {
    eval("m <- .Internal(regexpr('[ABC]+', c('querty', 'BCA', 'AAAA'), ignore.case=FALSE, perl=FALSE, fixed=FALSE, useBytes=FALSE))");
    assertThat(eval("m"), equalTo(c_i(-1,1,1)));
    assertThat(eval("attr(m, 'match.length')"), equalTo(c_i(-1,3,4)));
    
  }
}
