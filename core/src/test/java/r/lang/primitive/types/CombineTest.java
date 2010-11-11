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

package r.lang.primitive.types;

import org.junit.Test;
import r.lang.EvalTestCase;
import r.lang.Logical;
import r.lang.SEXP;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CombineTest extends EvalTestCase {

  @Test
  public void realList() {
    assertThat( eval("c(1,2,3)"), equalTo( c(1,2,3) ));
  }

  @Test
  public void logicals() {
    assertThat( eval("c(TRUE, FALSE, NA)"), equalTo( c(Logical.TRUE, Logical.FALSE, Logical.NA)) );
  }

  @Test
  public void ints() {
    assertThat( eval("c(1L,2L, 3L) "), equalTo( c_i(1,2,3)));
  }

  @Test
  public void nullValues() {
    assertThat( eval("c(NULL, NULL)"), equalTo( (SEXP) NULL) );
  }

  @Test
  public void realAndLogicalsMixed() {
    assertThat( eval("c(1,2,NULL,FALSE)"), equalTo( c(1,2,0) ));
  }

  @Test
  public void twoLists() {
    assertThat( eval("c( list(1,2), list(3,4) ) "), equalTo( list(1d,2d,3d,4d)));
  }

  @Test
  public void nullsInList() {
    assertThat( eval("c( list(NULL), NULL, list(NULL,1) ) "),
        equalTo( list(NULL, NULL, 1d)));
  }

}
