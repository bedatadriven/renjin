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
import r.lang.exception.FunctionCallException;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static r.lang.Logical.*;

public class ComparisonTest extends EvalTestCase {

  @Test
  public void scalarRealEquality() throws IOException {
    assertThat( eval("1 == 1"), equalTo(c(TRUE)) );
    assertThat( eval("1 > 2"), equalTo(c(FALSE)) );
    assertThat( eval("2 > 1"), equalTo(c(TRUE)) );
    assertThat( eval("NA_real_ > 1"), equalTo(c(NA)) );
    assertThat( eval("1 < 999"), equalTo(c(TRUE)) );
  }

  /**
   * Verify that LogicalExp and IntExp are implicitly converted to doubles
   */
  @Test
  public void integersImplicitlyCastToDoubles() throws IOException {

    assertThat( eval("1L == 1"), equalTo(c(TRUE)) );

  }

  @Test
  public void logicalsImplicitlyCastToDoubles() throws IOException {
    assertThat( eval("3 == NA"), equalTo(c(NA)) );
    assertThat( eval("0 < TRUE"), equalTo(c(TRUE)) );
    assertThat( eval("TRUE > FALSE"), equalTo(c(TRUE)) );
    assertThat( eval("TRUE <= TRUE"), equalTo(c(TRUE)) );
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

  @Test
  public void or() {
    assertThat( eval("0 || 0"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("0 || 1"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("1 || 0"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("1 || 1"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("FALSE || FALSE"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("FALSE || TRUE"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE || FALSE"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE || TRUE"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(0) || c(0)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(0) || c(1)"), equalTo(c(Logical.TRUE)));
    assertThat( eval("c(1) || c(0)"), equalTo(c(Logical.TRUE)));
    assertThat( eval("c(1) || c(1)"), equalTo(c(Logical.TRUE)));
    
    assertThat( eval("c(0,2,3) || c(0, 99, 3)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(0,2,3) || c(1, 99, 3)"), equalTo(c(Logical.TRUE)));
    assertThat( eval("c(1,2,3) || c(0, 99, 3)"), equalTo(c(Logical.TRUE)));
    assertThat( eval("c(1,2,3) || c(1, 99, 3)"), equalTo(c(Logical.TRUE)));

    assertThat( eval("c(FALSE) || c(FALSE)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) || c(TRUE)"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || c(FALSE)"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || c(TRUE)"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE) || c(0)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) || c(1)"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || c(0)"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || c(1)"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(TRUE) || list()"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || 'a'"), equalTo(c(Logical.TRUE)) );
  }

  @Test(expected = FunctionCallException.class)
  public void orInvalid() {
    eval(" FALSE || quote(x) ");
  }

  @Test
  public void orWithNA() {
    assertThat(eval(" TRUE || NA "), equalTo(c(TRUE)));
    assertThat( eval(" FALSE || NA "), equalTo(c(NA)));
  }

  @Test
  public void bitwiseOr() {
    assertThat( eval("0 | 0"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("0 | 1"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("1 | 0"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("1 | 1"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("FALSE | FALSE"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("FALSE | TRUE"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE | FALSE"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE | TRUE"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(0) | c(0)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(0) | c(1)"), equalTo(c(Logical.TRUE)));
    assertThat( eval("c(1) | c(0)"), equalTo(c(Logical.TRUE)));
    assertThat( eval("c(1) | c(1)"), equalTo(c(Logical.TRUE)));

    assertThat( eval("c(0,2,3) | c(0, 99, 3)"), equalTo(c(Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(0,2,3) | c(1, 99, 3)"), equalTo(c(Logical.TRUE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(1,2,3) | c(0, 99, 3)"), equalTo(c(Logical.TRUE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(1,2,3) | c(1, 99, 3)"), equalTo(c(Logical.TRUE, Logical.TRUE, Logical.TRUE)));

    assertThat( eval("c(FALSE) | c(FALSE)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) | c(TRUE)"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) | c(FALSE)"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) | c(TRUE)"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE, FALSE) | c(FALSE)"), equalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(FALSE, FALSE) | c(TRUE)"), equalTo(c(Logical.TRUE, Logical.TRUE)) );
    assertThat( eval("c(TRUE, TRUE) | c(FALSE)"), equalTo(c(Logical.TRUE, Logical.TRUE)) );
    assertThat( eval("c(TRUE, TRUE) | c(TRUE)"), equalTo(c(Logical.TRUE, Logical.TRUE)) );

    assertThat( eval("c(FALSE, FALSE) | c(FALSE, FALSE)"), equalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, FALSE) | c(FALSE, TRUE)"), equalTo(c(Logical.TRUE, Logical.TRUE)) );
    assertThat( eval("c(TRUE, TRUE) | c(TRUE, TRUE)"), equalTo(c(Logical.TRUE, Logical.TRUE)) );

    assertThat( eval("c(FALSE) | c(0)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) | c(1)"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) | c(0)"), equalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) | c(1)"), equalTo(c(Logical.TRUE)) );
  }

  @Test
  public void elementWiseOrWithNA() {
    assertThat( eval("FALSE | NA"), equalTo( c(NA)));
    assertThat( eval("NA | FALSE"), equalTo( c(NA)));
    assertThat( eval("TRUE | NA"), equalTo(c(TRUE)));
    assertThat( eval("NA | TRUE"), equalTo(c(TRUE)));
    assertThat( eval("NA | NA"), equalTo(c(NA)));
  }

  @Test
  public void and() {
    assertThat( eval("0 && 0"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("0 && 1"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("1 && 0"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("1 && 1"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("FALSE && FALSE"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("FALSE && TRUE"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("TRUE && FALSE"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("TRUE && TRUE"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(0) && c(0)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(0) && c(1)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(1) && c(0)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(1) && c(1)"), equalTo(c(Logical.TRUE)));

    assertThat( eval("c(0,2,3) && c(0, 99, 3)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(0,2,3) && c(1, 99, 3)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(1,2,3) && c(0, 99, 3)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(1,2,3) && c(1, 99, 3)"), equalTo(c(Logical.TRUE)));

    assertThat( eval("c(FALSE) && c(FALSE)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) && c(TRUE)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) && c(FALSE)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) && c(TRUE)"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE) && c(0)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) && c(1)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) && c(0)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) && c(1)"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE) && list()"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) && 'a'"), equalTo(c(Logical.FALSE)) );
  }

  @Test
  public void andWithNAs() {
    assertThat( eval("NA && NA"), equalTo( c(Logical.NA)));
    assertThat( eval("TRUE && NA"), equalTo( c(Logical.NA)));
    assertThat( eval("NA && TRUE"), equalTo( c(Logical.NA)));
    assertThat( eval("FALSE && NA"), equalTo( c(Logical.FALSE)));
    assertThat( eval("NA && FALSE"), equalTo( c(Logical.FALSE)));
  }

  @Test
  public void bitwiseAnd() {
    assertThat( eval("0 & 0"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("0 & 1"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("1 & 0"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("1 & 1"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("FALSE & FALSE"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("FALSE & TRUE"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("TRUE & FALSE"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("TRUE & TRUE"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(0) & c(0)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(0) & c(1)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(1) & c(0)"), equalTo(c(Logical.FALSE)));
    assertThat( eval("c(1) & c(1)"), equalTo(c(Logical.TRUE)));

    assertThat( eval("c(0,2,3) & c(0, 99, 3)"), equalTo(c(Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(0,2,3) & c(1, 99, 3)"), equalTo(c(Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(1,2,3) & c(0, 99, 3)"), equalTo(c(Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(1,2,3) & c(1, 99, 3)"), equalTo(c(Logical.TRUE, Logical.TRUE, Logical.TRUE)));

    assertThat( eval("c(FALSE) & c(FALSE)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) & c(TRUE)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) & c(FALSE)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) & c(TRUE)"), equalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE, FALSE) & c(FALSE)"), equalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(FALSE, FALSE) & c(TRUE)"), equalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, TRUE) & c(FALSE)"), equalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, TRUE) & c(TRUE)"), equalTo(c(Logical.TRUE, Logical.TRUE)) );

    assertThat( eval("c(FALSE, FALSE) & c(FALSE, FALSE)"), equalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, FALSE) & c(FALSE, TRUE)"), equalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, TRUE) & c(TRUE, TRUE)"), equalTo(c(Logical.TRUE, Logical.TRUE)) );

    assertThat( eval("c(FALSE) & c(0)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) & c(1)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) & c(0)"), equalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) & c(1)"), equalTo(c(Logical.TRUE)) );
  }

  @Test
  public void elementWiseAndWithNA() {
    assertThat( eval("FALSE & NA"), equalTo( c(FALSE)));
    assertThat( eval("NA & FALSE"), equalTo( c(FALSE)));
    assertThat( eval("TRUE & NA"), equalTo(c(NA)));
    assertThat( eval("NA & TRUE"), equalTo(c(NA)));
    assertThat( eval("NA & NA"), equalTo(c(NA)));
  }

  @Test
  public void not() {
    assertThat( eval(" !TRUE"), equalTo( c(false)));
    assertThat( eval(" !c(TRUE, FALSE, NA) "), equalTo(c(FALSE, TRUE, NA)));
    assertThat( eval(" !c(1,0,1) "), equalTo( c(FALSE,TRUE,FALSE) ));
    assertThat( eval(" !c(1L,0L,4L) "), equalTo( c(FALSE,TRUE,FALSE) ));
  }

  @Test
  public void any() {
    assertThat( eval(" any(FALSE, NA) "), equalTo( c(NA)) );
    assertThat( eval(" any(FALSE, NA, na.rm=TRUE) "), equalTo( c(FALSE)) );
    assertThat( eval(" any(TRUE, NA) "), equalTo( c(TRUE)) );
    assertThat( eval(" any('TRUE') "), equalTo( c(TRUE)));
    assertThat( eval(" any(c(0,0,0,1), list())"), equalTo( c(TRUE)));
  }
}
