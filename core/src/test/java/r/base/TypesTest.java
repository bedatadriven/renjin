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

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import r.EvalTestCase;
import r.lang.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static r.lang.Logical.FALSE;
import static r.lang.Logical.TRUE;

public class TypesTest extends EvalTestCase {

  @Test
  public void asCharacter() {
    assertThat( eval("as.character(1)"), equalTo( c("1") ));
    assertThat( eval("as.character(\"foobar\")"), equalTo( c("foobar") ));
    assertThat( eval("as.character(1L)"), equalTo( c("1") ));
    assertThat( eval("as.character(1.3333333333333333333333333333333333)"),
        equalTo(c("1.33333333333333")));
    assertThat( eval("as.character(TRUE)"), equalTo( c("TRUE") ));
  }

  @Test
  public void asCharacterWithNA() {
    assertThat( eval("as.character(NA)"), equalTo( c( StringVector.NA )) );
  }

  @Test
  public void asCharacterFromList() {
    assertThat( eval("as.character(list(3, 'a', TRUE)) "), equalTo( c("3", "a", "TRUE" )));
    assertThat( eval("as.character(list(c(1,3), 'a', TRUE)) "), equalTo( c("c(1, 3)", "a", "TRUE" )));
  }

  @Test
  public void asCharacterFromSymbol() {
    assertThat( eval(" as.character(quote(x)) "), equalTo( c("x") ));
  }

  @Test
  public void asDoubleFromDouble() {
    assertThat( eval("as.double(3.14)"), equalTo( c(3.14) ) );
    assertThat( eval("as.double(NA_real_)"), equalTo( c(DoubleVector.NA) ) );
  }

  @Test
  public void asDoubleFromInt() {
    assertThat( eval("as.double(3L)"), equalTo( c(3l) ));
  }

  @Test
  public void asLogicalFromList() {
    assertThat( eval("as.logical(list(1, 99.4, 0, 0L, FALSE, 'TRUE', 'FOO', 'T', 'F', 'FALSE')) "),
        equalTo( c(TRUE, TRUE, FALSE, FALSE, FALSE, TRUE, Logical.NA, TRUE, FALSE, FALSE) ));
  }

  @Test
  public void asLogical() {
    assertThat( eval("as.logical(c(1, 99.4, 0, NA_real_)) "),
        equalTo( c(TRUE, TRUE, FALSE, Logical.NA) ));
  }

  @Test
  public void asDoubleFromLogical() {
    assertThat( eval("as.double(TRUE)"), equalTo( c(1d) ));
    assertThat( eval("as.double(FALSE)"), equalTo( c(0d) ));
  }

  @Test
  public void asDoubleFromString() {
    assertThat( eval("as.double(\"42\")"), equalTo( c(42d) ));
    assertThat( eval("as.double(\"not an integer\")"), equalTo( c(DoubleVector.NA) ));
  }

  @Test
  public void asIntFromDouble() {
    assertThat( eval("as.integer(3.1)"), equalTo( c_i( 3 )));
    assertThat( eval("as.integer(3.9)"), equalTo( c_i( 3 )));
    assertThat( eval("as.integer(NA_real_)"), equalTo( c_i( IntVector.NA )));
  }

  @Test
  public void asIntFromRecycledDouble() {
    assertThat( eval("as.integer(c(1, 9.32, 9.9, 5.0))"), equalTo( c_i(1, 9, 9, 5 )));
  }

  @Test
  public void attributesSetting() {
    eval( " v <- .Internal(Version())");
    eval( " attributes(v) <- c( class='simpleList', attributes(v)) ");

    assertThat( eval("v$minor"), not(CoreMatchers.equalTo(NULL)));
  }


  @Test
  public void na() {
    assertThat( eval(" is.na(TRUE) "), equalTo( c(FALSE)));
    assertThat( eval(" is.na(NA) "), equalTo( c(TRUE)));
    assertThat( eval(" is.na(c(1L, NA_integer_)) "), equalTo( c(FALSE, TRUE)));
    assertThat( eval(" is.na(c(NA_character_, '', 'foo')) "), equalTo( c(TRUE, FALSE, FALSE)));
  }

  @Test
  public void naList() {
    assertThat( eval(" is.na(list(NULL,  1,     FALSE, c(NA,4), NA_integer_, NA_real_)) "),
                       equalTo( c(FALSE, FALSE, FALSE, FALSE,   TRUE,        TRUE)) );
  }

  @Test
  public void naPreservesNames() {
    assertThat( eval(" names(is.na(c(x=1,y=2))) "), equalTo( c("x", "y")));
  }


  @Test
  public void naPreservesDimNames() {
    eval( " x <- .Internal(rbind(1, c(a=1,b=2))) ");
    eval( " x <- is.na(x) ");
    assertThat( eval(" dimnames(x)[[2]] "), equalTo( c("a", "b")));

    eval(" x <- !x ");
    assertThat( eval(" dimnames(x)[[2]] "), equalTo( c("a", "b")));

  }

  @Test
  public void unaryPreservesNames() {
    assertThat( eval(" names(!is.na(c(x=1,y=2)))"), equalTo( c("x", "y")));
  }

  @Test
  public void vector() {
    assertThat( eval(" .Internal(vector('list', 3)) "), equalTo( list(NULL, NULL, NULL)));
    assertThat( eval(" .Internal(vector('numeric', 2)) "), equalTo( c(0, 0)));
    assertThat( eval(" .Internal(vector('character', 3)) "), equalTo( c("","","")) );
    assertThat( eval(" .Internal(vector('logical', 2)) "), equalTo( c(FALSE, FALSE)) );
  }

  @Test
  public void environment() {
    assertThat( eval(".Internal(environment())"), CoreMatchers.is((SEXP) global.getGlobalEnvironment()));
  }

  @Test
  public void environmentOfRandomExp() {
    assertThat( eval(".Internal(environment(1))"), is((SEXP) Null.INSTANCE));
  }

  @Test
  public void environmentOfClosure() {
    eval("f <- function() { 1 } ");
    assertThat( eval(".Internal(environment( f ))"), is((SEXP) global.getGlobalEnvironment()));
  }

  @Test
  public void list() {
    assertThat( eval("list(\"a\")"), equalTo( list("a") ));
  }

  @Test
  public void listOfNulls() {
    assertThat( eval("list(NULL, NULL)"), equalTo( list(NULL, NULL) ));
  }

  @Test
  public void listOfNull() {
    assertThat( eval("list(NULL)"), equalTo( list(NULL) ));
  }

  @Test
  public void setClassWithAttrFunction() {
    eval(" x<-c(1,2,3) ");
    eval(" attr(x, 'class') <- 'foo' ");

    assertThat( eval(" class(x) "), equalTo( c("foo")));
  }

  @Test
  public void setNamesWithNonStrVector() {
    eval(" x<-c(1,2,3) ");
    eval(" names(x) <- c(4,5,6) ");

    assertThat( eval("names(x)"), equalTo( c("4", "5","6")));
  }

  @Test
  public void setNamesWithNonVector() {
    eval(" x<-c(1,2,3) ");
    eval(" names(x) <- quote(quote(z)) ");

    assertThat( eval("names(x)"), equalTo( c("z", StringVector.NA, StringVector.NA)));
  }

  @Test
  public void setAttributes() {
    eval(" x <- 1:5");
    eval(" attributes(x) <- list(names=c('a','b', 'c'), foo='bar') ");

    assertThat( eval(" names(x) "), equalTo(c("a","b","c",StringVector.NA,StringVector.NA)));
    assertThat( eval(" attr(x, 'foo') "), equalTo( c("bar")));

  }

  @Test
  public void asVector() {
    eval(" as.vector <- function (x, mode = 'any') .Internal(as.vector(x, mode)) ");

    assertThat( eval("as.vector(1, 'character')"), equalTo( c("1" )));
    assertThat( eval("as.vector(c(4,5,0), mode='logical')"), equalTo( c(true, true, false)));
  }

  @Test
  public void asPairList() {
    eval(" as.vector <- function (x, mode = 'any') .Internal(as.vector(x, mode)) ");
    eval(" x <- as.vector( c(a=1,b=2), mode = 'pairlist') ");

    PairList.Node head = (PairList.Node) global.getVariable("x");
    assertThat( head.length(), equalTo(2));
    assertThat( head.getNode(0).getTag(), equalTo( symbol("a")));
    assertThat( head.getElementAsSEXP(0), equalTo( c(1) ));
    assertThat(head.getNode(1).getTag(), equalTo( symbol("b") ));
    assertThat( head.getElementAsSEXP(1), equalTo( c(2) ));
  }

  @Test
  public void options() {
    eval(" .Internal(options(foo=TRUE)) ");
  }

}
