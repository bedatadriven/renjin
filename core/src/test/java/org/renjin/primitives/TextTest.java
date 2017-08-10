/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.repackaged.guava.base.Charsets;
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
    assertThat( eval( ".Internal(paste(list(1, 'a', 'b'), '-', NULL)) "), elementsIdenticalTo(  c("1-a-b") )) ;
  }

  @Test
  public void pasteWithEmptyVector() {
    assertThat( eval( ".Internal(paste(list('a', c()), '-', NULL)) "), elementsIdenticalTo(  c("a-") )) ;
  }

  @Test
  public void pasteWithNas() {
    assertThat( eval(".Internal(paste(list('a', NA_character_), ' ', NULL))"), elementsIdenticalTo( c("a NA")));
  }

  @Test
  public void pasteVectors() {
    assertThat( eval( ".Internal(paste(list(c('x', 'y'), 'a', 'b'), '-', NULL)) "),
        elementsIdenticalTo(  c("x-a-b", "y-a-b") )) ;
  }

  @Test
  public void pasteCollapse() {
    assertThat( eval( ".Internal(paste(list(c('x', 'y'), 'a', 'b'), '-', '+')) "),
        elementsIdenticalTo(  c("x-a-b+y-a-b") )) ;
  }

  @Test
  public void gettext() {
    assertThat( eval( ".Internal(gettext('hungarian', 'hello world'))"), elementsIdenticalTo( c("hello world")));
    assertThat( eval( ".Internal(gettext(NULL, 'hello world'))"), elementsIdenticalTo( c("hello world")));
  }

  @Test
  public void ngettext() {
    assertThat( eval( ".Internal(ngettext(1, 'baby', 'babies', 'hungarian'))"), elementsIdenticalTo( c("baby")));
    assertThat( eval( ".Internal(ngettext(2, 'baby', 'babies', NULL))"), elementsIdenticalTo( c("babies")));
  }

  @Test
  public void sub() {
    assertThat( eval(".Internal(sub('[[:blank:]]*([[:alnum:]]+)', '\\\\1', " +
            "c('datasets', 'utils', 'grDevices', 'graphics', 'stats', 'methods'), FALSE, TRUE, FALSE, FALSE))"),
        elementsIdenticalTo( c("datasets", "utils", "grDevices", "graphics", "stats", "methods")) );
  }

  @Test
  public void posixCharacterClass() {

  }

  @Test
  public void sprintf() {
    eval("sprintf <- function (fmt, ...) .Internal(sprintf(fmt, ...))");
    eval("pi <- 3.14159265358979");

    assertThat( eval("sprintf('%f', pi)"), elementsIdenticalTo( c("3.141593")));
    assertThat( eval("sprintf('%.3f', pi)"), elementsIdenticalTo( c("3.142")));
    assertThat( eval("sprintf('%1.0f', pi)"), elementsIdenticalTo( c("3")));
    assertThat( eval("sprintf('%5.1f', pi)"), elementsIdenticalTo(  c("  3.1")));
    assertThat( eval("sprintf('%05.1f', pi)"), elementsIdenticalTo( c("003.1")));
    assertThat( eval("sprintf('% f', pi)"), elementsIdenticalTo( c(" 3.141593")));
    assertThat( eval("sprintf('%e', pi)"), elementsIdenticalTo( c("3.141593e+00")));
    assertThat( eval("sprintf('%E', pi)"), elementsIdenticalTo( c("3.141593E+00")));
    // assertThat( eval("sprintf('%g', pi)"), equalTo( c("3.14159")));

    // Argument Recyling
    assertThat( eval("sprintf(c('a%d', 'b%d'), c(1,2,3,4))"), elementsIdenticalTo( c("a1", "b2", "a3", "b4")));

    // empty list
    assertThat( eval("sprintf('%s', c())"), identicalTo( (SEXP)new StringArrayVector()));
  }

  @Test
  public void sprintfWithAsCharacter() {
    eval("as.character.foo <- function(x) 'FOO!' ");
    eval("sprintf <- function (fmt, ...) .Internal(sprintf(fmt, ...))");

    eval("x <- list(1,2,3) ");
    eval("class(x) <- 'foo' ");

    assertThat( eval("sprintf('i say %s', x)"), elementsIdenticalTo(c("i say FOO!")));
  }

  @Test
  public void translateChars() {
    assertThat(eval(".Internal(chartr('abc', 'xyz', 'abcdefabc'))"), elementsIdenticalTo(c("xyzdefxyz")));
    assertThat(eval(".Internal(chartr('abc', 'xyz', c('abc', 'cba', 'ccc')))"),
        elementsIdenticalTo(c("xyz", "zyx", "zzz")));

    assertThat(eval(".Internal(tolower('ABCdeFG 123'))"), elementsIdenticalTo(c("abcdefg 123")));
    assertThat( eval(".Internal(toupper('ABCdeFG 123'))"), elementsIdenticalTo( c("ABCDEFG 123")));

  }

  @Test
  public void substr() {

    assertThat( eval(" .Internal(substr('lazy cat', 2, 4))" ), elementsIdenticalTo(c("azy")));
    assertThat( eval(" .Internal(substr('foo', 1, 99))" ), elementsIdenticalTo(c("foo")));
    assertThat( eval("substr(c('ab', 'ab'), 1:2, 1:2)"), elementsIdenticalTo(c("a", "b")) );
    assertThat( eval("substr(c('ab', 'ab'), 1:3, 1:3)"), elementsIdenticalTo(c("a", "b")) );
    assertThat( eval("substr(c('ab', 'ab'), 1:2, 2:1)"), elementsIdenticalTo(c("ab", "")) );
    assertThat( eval("substr(c('ab', 'ab'), 1, 1)"), elementsIdenticalTo(c("a", "a")) );
    assertThat( eval("substring('abc', 1:3, 1:3)"), elementsIdenticalTo(c("a", "b", "c")));
    assertThat( eval("substring('abcdef', 1:4, 4:5)"), elementsIdenticalTo(c("abcd", "bcde", "cd", "de")));
  }
  
  @Test
  public void substrWithRecycling() {
    
  }
  
  @Test
  public void substrAssignWithRecycling() {
    eval(" x <- c('logical', 'vector') ");
    eval(" substr(x, 1, 1) <- c('L', 'V') ");
    
    assertThat(eval("x"), elementsIdenticalTo(c("Logical", "Vector")));
  }

  @Test
  public void nchar() {
    eval("nchar <- function (x, type = 'chars', allowNA = FALSE) .Internal(nchar(x, type, allowNA))");

    assertThat( eval("nchar('xyz')"), elementsIdenticalTo( c_i(3) ));
    assertThat( eval("nchar(c('xyz', NA, 'a', '', 'abcde'))"), elementsIdenticalTo( c_i(3, 2, 1, 0, 5) ));
    assertThat( eval("nchar(NULL)"), elementsIdenticalTo( c_i() ));
  }

  @Test
  public void ncharWithNas() {
    assertThat( eval(".Internal(nchar(c(NA,-5),'chars', FALSE))"), elementsIdenticalTo( c_i(2,2) ));
  }

  @Test
  public void split() {
    eval("strsplit <- function (x, split, extended = TRUE, fixed = FALSE, perl = FALSE, useBytes = FALSE) " +
        ".Internal(strsplit(x, as.character(split),  as.logical(fixed), " +
        "as.logical(perl), as.logical(useBytes)))");

    assertThat( eval("strsplit('a,b', ',')"), elementsIdenticalTo( list( c("a","b") )));
    assertThat( eval("strsplit('the   slow lazy  dog etc', '\\\\s+')"),
        elementsIdenticalTo( list( c("the","slow", "lazy", "dog", "etc") )));
    assertThat( eval("strsplit(c('a b c d e', '1 2 3 4'), '\\\\s+')"),
        elementsIdenticalTo( list( c("a", "b", "c", "d", "e"), c("1","2", "3","4"))));

    assertThat( eval("strsplit('abc','')"), elementsIdenticalTo( list( c("a","b","c") )));

    assertThat( eval("strsplit('|ab|cf|q||','|',fixed=TRUE)"), elementsIdenticalTo( list(c("", "ab", "cf", "q", ""))));
  }

  @Test
  public void splitByEmptyFixedPattern() {
    assertThat(eval("strsplit('hello', '', fixed=TRUE)"), elementsIdenticalTo(list(c("h", "e", "l", "l", "o"))));
    assertThat(eval("strsplit('hello', '', fixed=FALSE)"), elementsIdenticalTo(list(c("h", "e", "l", "l", "o"))));
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


    assertThat( eval(" grep('[', '[[', fixed=TRUE) "), elementsIdenticalTo(c_i(1)));
  }

  @Test
  public void makeNames() {
    assertThat(
        eval(".Internal(make.names(c('a', '1', 'if', 'a', '.', NA_character_, '_a', '.2way', '.legal', '$#@foo_bar!'), FALSE))"),
        elementsIdenticalTo( c("a", "X1", "if.", "a", ".",  "NA.", "X_a", "X.2way", ".legal", "X...foo_bar.") ));
  }

  @Test
  public void makeUnique() {
    assertThat( eval(".Internal(make.unique(c('a', 'b', 'a'), '.'))"), elementsIdenticalTo(c("a", "b", "a.1")));
  }

  @Test
  public void strtrim(){
    assertThat( eval(".Internal(strtrim(c('abcdef', 'abcdef', 'abcdef'), c(1,5,10)))"), elementsIdenticalTo(c("a", "abcde", "abcdef")));
    assertThat( eval(".Internal(strtrim(\"abcdef\", 3))"), elementsIdenticalTo(c("abc")));
  }

  @Test
  public void format() throws IOException {
    assumingBasePackagesLoad();

    assertThat( eval("format(c(1,10,1000))"), elementsIdenticalTo(c("   1", "  10", "1000")));
    assertThat( eval("format(c(1,10,1000),trim=T)"), elementsIdenticalTo(c("1", "10", "1000")));

  }

  @Test
  public void formatWithNAs() throws IOException {
    assumingBasePackagesLoad();

    assertThat(eval("format(c('Hello', NA, 'world', NA))"), elementsIdenticalTo(c( "Hello", "NA   ", "world", "NA   ")));
    assertThat(eval("format(c('Hello', NA, 'world', NA), na.encode=FALSE)"), elementsIdenticalTo(c( "Hello", null, "world", null)));
  }

  @Test
  public void intToUtf8() {

    byte[] encoded = new String("€").getBytes(Charsets.UTF_8);
    assertThat(encoded.length, equalTo(3));
    java.lang.System.out.println(Integer.toHexString(encoded[0]) + " " + Integer.toHexString(encoded[1]) + " " + Integer.toHexString(encoded[2]));

    assertThat(eval(".Internal(utf8ToInt('hello'))"), elementsIdenticalTo(c_i(104,101,108, 108, 111)));
    assertThat(eval(".Internal(utf8ToInt(NA_character_))"), elementsIdenticalTo(c_i(IntVector.NA)));


    // check special handling of 0s
    assertThat(eval(".Internal(intToUtf8(c(104L,0,101L), FALSE))"), elementsIdenticalTo(c("he")));
    assertThat(eval(".Internal(intToUtf8(c(104L,0,101L), TRUE))"), elementsIdenticalTo(c("h", "", "e")));

    // check special handling of NAs
    assertThat(eval(".Internal(intToUtf8(c(104L,NA), FALSE))"), elementsIdenticalTo(c(StringVector.NA)));
    assertThat(eval(".Internal(intToUtf8(c(104L,NA), TRUE))"), elementsIdenticalTo(c("h", StringVector.NA)));

  }

  @Test
  public void setSubstring(){
    assumingBasePackagesLoad();
    eval("x<-\"aaaa\"");
    eval("substr(x,2,3) <- \"xx\"");
    assertThat(eval("x"), elementsIdenticalTo(c("axxa")));
  }

  @Test
  public void setSubstringAtBeginning(){
    assumingBasePackagesLoad();
    eval("x<-\"aaaa\"");
    eval("substr(x,1,2) <- \"xx\"");
    assertThat(eval("x"), elementsIdenticalTo(c("xxaa")));
  }

  @Test
  public void setSubstringAtEnd(){
    assumingBasePackagesLoad();
    eval("x<-\"aaaa\"");
    eval("substr(x,3,4) <- \"xx\"");
    assertThat(eval("x"), elementsIdenticalTo(c("aaxx")));
  }

  @Test
  public void setSubstringNumbersDontMatchUp(){
    assumingBasePackagesLoad();
    eval("x<-\"aaaa\"");
    eval("substr(x,1,4) <- \"xx\"");
    assertThat(eval("x"), elementsIdenticalTo(c("xxaa")));
  }

  @Test
  public void iconv() {
    eval(".Internal(iconv(c('A','B'),'UTF-8','ASCII', as.character(NA), FALSE, FALSE))");
  }

  @Test
  public void strtoi() {
    assertThat(eval(".Internal(strtoi(c('0666','0xFF', '42'), 0L))"), elementsIdenticalTo(c_i(438, 255, 42)));
    assertThat(eval(".Internal(strtoi('0666', 10L))"), elementsIdenticalTo(c_i(666)));
    assertThat(eval(".Internal(strtoi('0xFF', 16L))"), elementsIdenticalTo(c_i(255)));
  }

  @Test
  public void regexReplace() {
    assumingBasePackagesLoad();
    eval("pat <- '^R[[:space:]]*\\\\(([[<>=!]+)[[:space:]]+(.*)\\\\)[[:space:]]*' ");
    eval("x <- c('R (>= 2.10)', 'R(>= 2.4.0)', 'R (>= 2.10)')");
    eval("ops <-  sub(pat, \"\\\\1\", x)");
    eval("v_t_1 <- sub(pat, \"\\\\2\", x)");
    eval("v_t <- split(v_t_1, ops)");

    assertThat(eval("ops"), elementsIdenticalTo(c(">=", ">=", ">=")));
    assertThat(eval("v_t_1"), elementsIdenticalTo(c("2.10", "2.4.0", "2.10")));
    assertThat(eval("length(v_t)"), elementsIdenticalTo(c_i(1)));
    assertThat(eval("names(v_t)"), elementsIdenticalTo(c(">=")));
  }

  @Test
  public void regexprTest() {
    eval("m <- .Internal(regexpr('[ABC]+', c('querty', 'BCA', 'AAAA'), ignore.case=FALSE, perl=FALSE, fixed=FALSE, useBytes=FALSE))");
    assertThat(eval("m"), elementsIdenticalTo(c_i(-1,1,1)));
    assertThat(eval("attr(m, 'match.length')"), elementsIdenticalTo(c_i(-1,3,4)));
  }

  @Test
  public void gregexprTest() {
    eval( "m <- .Internal(gregexpr('aa', c('aabbbbbaa', 'bbaabbbaa', 'ccaaccaaccaa'), ignore.case=FALSE, perl=FALSE, fixed=FALSE, useBytes=FALSE))" );

    eval( "n <- .Internal(gregexpr('', c('aabbbbbaa', 'bbaabbbaa', 'ccaaccaaccaa'), ignore.case=FALSE, perl=FALSE, fixed=FALSE, useBytes=FALSE))" );

    eval( "o <- .Internal(gregexpr('z', c('aabbbbbaa', 'bbaabbbaa', 'ccaaccaaccaa'), ignore.case=FALSE, perl=FALSE, fixed=FALSE, useBytes=FALSE))" );

    assertThat( eval("m[[3]]"), elementsIdenticalTo(c_i(3,7,11)));
    assertThat( eval("attr(m[[3]], \"match.length\")"), elementsIdenticalTo(c_i(2,2,2)));

    assertThat( eval("n[[3]]"), elementsIdenticalTo(c_i(1,2,3,4,5,6,7,8,9,10,11,12)));
    assertThat( eval("attr(n[[3]], \"match.length\")"), elementsIdenticalTo(c_i(0,0,0,0,0,0,0,0,0,0,0,0)));

    assertThat( eval("o[[3]]"), elementsIdenticalTo(c_i(-1)));
    assertThat( eval("attr(o[[3]], \"match.length\")"), elementsIdenticalTo(c_i(-1)));
  }
}
