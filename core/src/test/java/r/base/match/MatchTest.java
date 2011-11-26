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

package r.base.match;

import org.junit.Test;
import r.EvalTestCase;
import r.lang.IntVector;

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
  public void anyDuplicated() {
    assertThat( eval(" .Internal(anyDuplicated(1, FALSE, FALSE)) "), equalTo( c_i(0) ));
    assertThat( eval(" .Internal(anyDuplicated(c(1,1,3), FALSE, FALSE)) "), equalTo( c_i(2) ));
    assertThat( eval(" .Internal(anyDuplicated(c(1,2,3,3), FALSE, FALSE)) "), equalTo( c_i(4) ));
    assertThat( eval(" .Internal(anyDuplicated(c(2,2,3,3), FALSE, TRUE)) "), equalTo( c_i(3) ));
  }

  @Test
  public void duplicated() {
    assertThat( eval(" .Internal(duplicated(1, FALSE, FALSE)) "), equalTo( c(false)) );
    assertThat( eval(" .Internal(duplicated(c(1,1,3), FALSE, FALSE)) "), equalTo( c(false,true,false) ));
    assertThat( eval(" .Internal(duplicated(c(1,2,3,3), FALSE, FALSE)) "), equalTo( c(false,false,false,true)) );
    assertThat( eval(" .Internal(duplicated(c(2,2,3,3), FALSE, TRUE)) "), equalTo( c(true, false,true,false) ));
  }

  
}
