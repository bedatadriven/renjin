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
package org.renjin.primitives.match;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MatchTest extends EvalTestCase {

  @Test
  public void matchDoubles() {
    assertThat( eval( ".Internal(match(92, c(91,92,93), NA_integer_, NULL ))"), equalTo( c_i(2) ));
    assertThat( eval( ".Internal(match(44, c(91,92,93), NA_integer_, NULL ))"), equalTo( c_i(IntVector.NA) ));
  }

  @Test
  public void dontMatchIncomparables() {
    assertThat( eval( ".Internal(match(92, c(91,92,93), NA_integer_, c(92L) ))"), equalTo( c_i(IntVector.NA) ));
  }

  @Test
  public void matchStrings() {
    assertThat( eval( ".Internal(match( c(1,2), c('z', 'y', '1', '2'), NA_integer_, FALSE)) "), equalTo( c_i(3, 4)));
  }
  
  @Test
  public void matchAgainstList() {
    assertThat( eval( ".Internal(match( c(1,2), list('z', 'y', '1', '2'), NA_integer_, FALSE)) "), equalTo( c_i(3, 4)));
  }
  
  @Test
  public void matchSymbols() {
    assertThat( eval(" .Internal(match( list(quote(x)), list(quote(z), quote(y), quote(x)), NA_integer_, FALSE)) "), equalTo( c_i(3)));
  }
  

  @Test
  public void matchNA() {
    assertThat( eval(" .Internal(match(as.character(c(1,2,NA)), NA_real_, NA_integer_, NULL))"), 
        equalTo(c_i(IntVector.NA, IntVector.NA, 1)));
    
  }
  
  @Test
  public void matchAgainstRowNamesSequence() {
    assertThat( eval(" match(1.5, as.character(1:1000))"), equalTo(c_i(IntVector.NA)));
    assertThat( eval(" match(c('3', '4', '99'), as.character(1:1000))"), equalTo(c_i(3, 4, 99)));
    assertThat( eval(" match(c('3', '4', '99'), as.character(3:1000))"), equalTo(c_i(1, 2, 97)));
  }
  
  @Test
  public void pmatch() {
    eval(" pmatch <- function (x, table, nomatch = NA_integer_, duplicates.ok = FALSE) \n" +
        ".Internal(pmatch(as.character(x), as.character(table), nomatch, \n" +
        "    duplicates.ok))");

    assertThat( eval("pmatch(c('he', 'hello', 'foo'), c('hello world')) "), equalTo(c_i(1, IntVector.NA, IntVector.NA)));
    assertThat( eval("pmatch(c('he', 'hello', 'foo'), c('hello world'),duplicates.ok=TRUE) "),
        equalTo(c_i(1, 1, IntVector.NA)));

    assertThat( eval("pmatch('hello', NULL) "), equalTo(c_i(IntVector.NA)));

  }
  
  @Test
  public void charMatch() {
    eval(" charmatch <- function (x, table, nomatch = NA_integer_) .Internal(charmatch(as.character(x), as.character(table), nomatch))");

    assertThat( eval(" charmatch('','')  "), equalTo(c_i(1)));
    assertThat( eval(" charmatch('m',   c('mean', 'median', 'mode'))  "), equalTo(c_i(0)));
    assertThat( eval(" charmatch('med', c('mean', 'median', 'mode'))  "), equalTo(c_i(2)));
    assertThat( eval(" charmatch('x',   c('mean', 'median', 'mode'))  "), equalTo(c_i(IntVector.NA)));
  }

  @Test
  public void anyDuplicated() {
    assertThat( eval(" .Internal(anyDuplicated(1, FALSE, FALSE)) "), equalTo( c_i(0) ));
    assertThat( eval(" .Internal(anyDuplicated(c(1,1,3), FALSE, FALSE)) "), equalTo( c_i(2) ));
    assertThat( eval(" .Internal(anyDuplicated(c(1,2,3,3), FALSE, FALSE)) "), equalTo( c_i(4) ));
    assertThat( eval(" .Internal(anyDuplicated(c(2,2,3,3), FALSE, TRUE)) "), equalTo( c_i(3) ));
  }

  @Test
  public void anyDuplicatedInLists() {
    assertThat( eval(" .Internal(anyDuplicated(list(1,1,3), FALSE, FALSE)) "), equalTo( c_i(2) ));
    assertThat( eval(" .Internal(anyDuplicated(list(1,2,3,3), FALSE, FALSE)) "), equalTo( c_i(4) ));
    assertThat( eval(" .Internal(anyDuplicated(list('a','b','c','c'), FALSE, FALSE)) "), equalTo( c_i(4) ));
  }


  @Test
  public void duplicated() {
    assertThat( eval(" .Internal(duplicated(1, FALSE, FALSE)) "), equalTo( c(false)) );
    assertThat( eval(" .Internal(duplicated(c(1,1,3), FALSE, FALSE)) "), equalTo( c(false,true,false) ));
    assertThat( eval(" .Internal(duplicated(c(1,2,3,3), FALSE, FALSE)) "), equalTo( c(false,false,false,true)) );
    assertThat( eval(" .Internal(duplicated(c(2,2,3,3), FALSE, TRUE)) "), equalTo( c(true, false,true,false) ));
  }
 
  @Test
  public void whichWithEmptyNames() {
    eval("x <- which(c(a=FALSE, b=FALSE, c=FALSE))");
    assertThat(eval("length(x)"), equalTo(c_i(0)));
    assertThat(eval("names(x)"), equalTo((SEXP)StringArrayVector.EMPTY));
  }
  
}
