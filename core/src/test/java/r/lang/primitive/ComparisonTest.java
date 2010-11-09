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
import r.lang.Logical;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ComparisonTest extends EvalTestCase {

  @Test
  public void scalarRealEquality() throws IOException {
    assertThat( eval("1 == 1"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("1 > 2"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("2 > 1"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("NA_real_ > 1"), equalTo(c(Logical.NA)) );
    assertThat( eval("1 < 999"), equalTo(c(Logical.TRUE)) );
  }

  /**
   * Verify that LogicalExp and IntExp are implicitly converted to doubles
   */
  @Test
  public void integersImplicitlyCastToDoubles() throws IOException {

    assertThat( eval("1L == 1"), equalTo(c(Logical.TRUE)) );

  }

  @Test
  public void logicalsImplicitlyCastToDoubles() throws IOException {
    assertThat( eval("3 == NA"), equalTo(c(Logical.NA)) );
    assertThat( eval("0 < TRUE"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE > FALSE"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE <= TRUE"), equalTo(c(Logical.TRUE)) );
  }

  @Test
  public void realLists() throws IOException {
    assertThat( eval("c(1,2,3) < c(0, 99, 3)"), equalTo(c(false, true, false)));
  }

  @Test
  public void unequalSizeLists() throws IOException {
    assertThat( eval("c(1,2,3) <= 2"), equalTo(c(true, true, false)));
    assertThat( eval("2 != c(1,2,3)"), equalTo(c(true, false, true)));
  }

  @Test
  public void platform() throws IOException {
    // this was failing in dynaload.R
    assertThat( eval("if(.Platform$OS.type == \"windows\") { 1 } else { 42 }"), equalTo(c(42)) );
  }
}
