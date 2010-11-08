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

package r.lang;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ComparisonTest extends EvalTestCase {

  @Test
  public void scalarRealEquality() throws IOException {
    assertThat( evaluateToExpression("1 == 1"), equalTo(c(Logical.TRUE)) );
    assertThat( evaluateToExpression("1 > 2"), equalTo(c(Logical.FALSE)) );
    assertThat( evaluateToExpression("2 > 1"), equalTo(c(Logical.TRUE)) );
    assertThat( evaluateToExpression("NA_real_ > 1"), equalTo(c(Logical.NA)) );
    assertThat( evaluateToExpression("1 < 999"), equalTo(c(Logical.TRUE)) );
  }

  /**
   * Verify that LogicalExp and IntExp are implicitly converted to doubles
   */
  @Test
  public void integersImplicitlyCastToDoubles() throws IOException {

    assertThat( evaluateToExpression("1L == 1"), equalTo(c(Logical.TRUE)) );

  }

  @Test
  public void logicalsImplicitlyCastToDoubles() throws IOException {
    assertThat( evaluateToExpression("3 == NA"), equalTo(c(Logical.NA)) );
    assertThat( evaluateToExpression("0 < TRUE"), equalTo(c(Logical.TRUE)) );
    assertThat( evaluateToExpression("TRUE > FALSE"), equalTo(c(Logical.TRUE)) );
    assertThat( evaluateToExpression("TRUE <= TRUE"), equalTo(c(Logical.TRUE)) );
  }

  @Test
  public void realLists() throws IOException {
    assertThat( evaluateToExpression("c(1,2,3) < c(0, 99, 3)"), equalTo(c(false, true, false)));
  }

  @Test
  public void unequalSizeLists() throws IOException {
    assertThat( evaluateToExpression("c(1,2,3) <= 2"), equalTo(c(true, true, false)));
    assertThat( evaluateToExpression("2 != c(1,2,3)"), equalTo(c(true, false, true)));
  }
}
