/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.eval.EvalException;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalArrayVector;
import org.renjin.sexp.SEXP;

import java.io.IOException;

import static org.junit.Assert.assertThat;
import static org.renjin.sexp.Logical.*;

public class ComparisonTest extends EvalTestCase {

  @Test
  public void scalarRealEquality() throws IOException {
    assertThat( eval("1 == 1"), elementsIdenticalTo(c(TRUE)) );
    assertThat( eval("1 > 2"), elementsIdenticalTo(c(FALSE)) );
    assertThat( eval("2 > 1"), elementsIdenticalTo(c(TRUE)) );
    assertThat( eval("2 >= 2"), elementsIdenticalTo(c(TRUE)) );
    assertThat( eval("is.na(NA_real_ > 1)"), elementsIdenticalTo(c(TRUE)) );
    assertThat( eval("1 < 999"), elementsIdenticalTo(c(TRUE)) );
  }

  /**
   * Verify that LogicalExp and IntExp are implicitly converted to doubles
   */
  @Test
  public void integersImplicitlyCastToDoubles() throws IOException {

    assertThat( eval("1L == 1"), elementsIdenticalTo(c(TRUE)) );

  }

  @Test
  public void logicalsImplicitlyCastToDoubles() throws IOException {
    assertThat( eval("3 == NA"), elementsIdenticalTo(c(NA)) );
    assertThat( eval("0 < TRUE"), elementsIdenticalTo(c(TRUE)) );
    assertThat( eval("TRUE > FALSE"), elementsIdenticalTo(c(TRUE)) );
    assertThat( eval("TRUE <= TRUE"), elementsIdenticalTo(c(TRUE)) );
  }

  @Test
  public void realLists() throws IOException {
    assertThat( eval("c(1,2,3) < c(0, 99, 3)"), elementsIdenticalTo(c(false, true, false)));
  }

  @Test
  public void unequalSizeLists() throws IOException {
    assertThat( eval("c(1,2,3) <= 2"), elementsIdenticalTo(c(true, true, false)));
    assertThat( eval("2 != c(1,2,3)"), elementsIdenticalTo(c(true, false, true)));
  }

  @Test
  public void platform() throws IOException {
    // this was failing in dynaload.R
    assertThat( eval("if(.Platform$OS.type == \"windows\") { 1 } else { 42 }"), elementsIdenticalTo(c(42)) );
  }

  @Test
  public void or() {
    assertThat( eval("0 || 0"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("0 || 1"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("1 || 0"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("1 || 1"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("FALSE || FALSE"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("FALSE || TRUE"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE || FALSE"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE || TRUE"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(0) || c(0)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(0) || c(1)"), elementsIdenticalTo(c(Logical.TRUE)));
    assertThat( eval("c(1) || c(0)"), elementsIdenticalTo(c(Logical.TRUE)));
    assertThat( eval("c(1) || c(1)"), elementsIdenticalTo(c(Logical.TRUE)));
    
    assertThat( eval("c(0,2,3) || c(0, 99, 3)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(0,2,3) || c(1, 99, 3)"), elementsIdenticalTo(c(Logical.TRUE)));
    assertThat( eval("c(1,2,3) || c(0, 99, 3)"), elementsIdenticalTo(c(Logical.TRUE)));
    assertThat( eval("c(1,2,3) || c(1, 99, 3)"), elementsIdenticalTo(c(Logical.TRUE)));

    assertThat( eval("c(FALSE) || c(FALSE)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) || c(TRUE)"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || c(FALSE)"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || c(TRUE)"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE) || c(0)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) || c(1)"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || c(0)"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || c(1)"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(TRUE) || list()"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) || 'a'"), elementsIdenticalTo(c(Logical.TRUE)) );
  }

  @Test(expected = EvalException.class)
  public void orInvalid() {
    eval(" FALSE || quote(x) ");
  }

  @Test
  public void orWithNA() {
    assertThat(eval(" TRUE || NA "), elementsIdenticalTo(c(TRUE)));
    assertThat( eval(" FALSE || NA "), elementsIdenticalTo(c(NA)));
  }

  @Test
  public void bitwiseOr() {
    assertThat( eval("0 | 0"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("0 | 1"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("1 | 0"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("1 | 1"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("FALSE | FALSE"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("FALSE | TRUE"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE | FALSE"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("TRUE | TRUE"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(0) | c(0)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(0) | c(1)"), elementsIdenticalTo(c(Logical.TRUE)));
    assertThat( eval("c(1) | c(0)"), elementsIdenticalTo(c(Logical.TRUE)));
    assertThat( eval("c(1) | c(1)"), elementsIdenticalTo(c(Logical.TRUE)));

    assertThat( eval("c(0,2,3) | c(0, 99, 3)"), elementsIdenticalTo(c(Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(0,2,3) | c(1, 99, 3)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(1,2,3) | c(0, 99, 3)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(1,2,3) | c(1, 99, 3)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE, Logical.TRUE)));

    assertThat( eval("c(FALSE) | c(FALSE)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) | c(TRUE)"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) | c(FALSE)"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) | c(TRUE)"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE, FALSE) | c(FALSE)"), elementsIdenticalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(FALSE, FALSE) | c(TRUE)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE)) );
    assertThat( eval("c(TRUE, TRUE) | c(FALSE)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE)) );
    assertThat( eval("c(TRUE, TRUE) | c(TRUE)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE)) );

    assertThat( eval("c(FALSE, FALSE) | c(FALSE, FALSE)"), elementsIdenticalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, FALSE) | c(FALSE, TRUE)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE)) );
    assertThat( eval("c(TRUE, TRUE) | c(TRUE, TRUE)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE)) );

    assertThat( eval("c(FALSE) | c(0)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) | c(1)"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) | c(0)"), elementsIdenticalTo(c(Logical.TRUE)) );
    assertThat( eval("c(TRUE) | c(1)"), elementsIdenticalTo(c(Logical.TRUE)) );
  }

  @Test
  public void elementWiseOrWithNA() {
    assertThat( eval("FALSE | NA"), elementsIdenticalTo( c(NA)));
    assertThat( eval("NA | FALSE"), elementsIdenticalTo( c(NA)));
    assertThat( eval("TRUE | NA"), elementsIdenticalTo(c(TRUE)));
    assertThat( eval("NA | TRUE"), elementsIdenticalTo(c(TRUE)));
    assertThat( eval("NA | NA"), elementsIdenticalTo(c(NA)));
  }

  @Test
  public void and() {
    assertThat( eval("0 && 0"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("0 && 1"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("1 && 0"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("1 && 1"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("FALSE && FALSE"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("FALSE && TRUE"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("TRUE && FALSE"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("TRUE && TRUE"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(0) && c(0)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(0) && c(1)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(1) && c(0)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(1) && c(1)"), elementsIdenticalTo(c(Logical.TRUE)));

    assertThat( eval("c(0,2,3) && c(0, 99, 3)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(0,2,3) && c(1, 99, 3)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(1,2,3) && c(0, 99, 3)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(1,2,3) && c(1, 99, 3)"), elementsIdenticalTo(c(Logical.TRUE)));

    assertThat( eval("c(FALSE) && c(FALSE)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) && c(TRUE)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) && c(FALSE)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) && c(TRUE)"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE) && c(0)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) && c(1)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) && c(0)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) && c(1)"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE) && list()"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) && 'a'"), elementsIdenticalTo(c(Logical.FALSE)) );
  }

  @Test
  public void andWithNAs() {
    assertThat( eval("NA && NA"), elementsIdenticalTo( c(Logical.NA)));
    assertThat( eval("TRUE && NA"), elementsIdenticalTo( c(Logical.NA)));
    assertThat( eval("NA && TRUE"), elementsIdenticalTo( c(Logical.NA)));
    assertThat( eval("FALSE && NA"), elementsIdenticalTo( c(Logical.FALSE)));
    assertThat( eval("NA && FALSE"), elementsIdenticalTo( c(Logical.FALSE)));
  }

  @Test
  public void bitwiseAnd() {
    assertThat( eval("0 & 0"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("0 & 1"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("1 & 0"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("1 & 1"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("FALSE & FALSE"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("FALSE & TRUE"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("TRUE & FALSE"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("TRUE & TRUE"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(0) & c(0)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(0) & c(1)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(1) & c(0)"), elementsIdenticalTo(c(Logical.FALSE)));
    assertThat( eval("c(1) & c(1)"), elementsIdenticalTo(c(Logical.TRUE)));

    assertThat( eval("c(0,2,3) & c(0, 99, 3)"), elementsIdenticalTo(c(Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(0,2,3) & c(1, 99, 3)"), elementsIdenticalTo(c(Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(1,2,3) & c(0, 99, 3)"), elementsIdenticalTo(c(Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat( eval("c(1,2,3) & c(1, 99, 3)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE, Logical.TRUE)));

    assertThat( eval("c(FALSE) & c(FALSE)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) & c(TRUE)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) & c(FALSE)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) & c(TRUE)"), elementsIdenticalTo(c(Logical.TRUE)) );

    assertThat( eval("c(FALSE, FALSE) & c(FALSE)"), elementsIdenticalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(FALSE, FALSE) & c(TRUE)"), elementsIdenticalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, TRUE) & c(FALSE)"), elementsIdenticalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, TRUE) & c(TRUE)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE)) );

    assertThat( eval("c(FALSE, FALSE) & c(FALSE, FALSE)"), elementsIdenticalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, FALSE) & c(FALSE, TRUE)"), elementsIdenticalTo(c(Logical.FALSE, Logical.FALSE)) );
    assertThat( eval("c(TRUE, TRUE) & c(TRUE, TRUE)"), elementsIdenticalTo(c(Logical.TRUE, Logical.TRUE)) );

    assertThat( eval("c(FALSE) & c(0)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(FALSE) & c(1)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) & c(0)"), elementsIdenticalTo(c(Logical.FALSE)) );
    assertThat( eval("c(TRUE) & c(1)"), elementsIdenticalTo(c(Logical.TRUE)) );
  }

  @Test
  public void elementWiseAndWithNA() {
    assertThat( eval("FALSE & NA"), elementsIdenticalTo( c(FALSE)));
    assertThat( eval("NA & FALSE"), elementsIdenticalTo( c(FALSE)));
    assertThat( eval("TRUE & NA"), elementsIdenticalTo(c(NA)));
    assertThat( eval("NA & TRUE"), elementsIdenticalTo(c(NA)));
    assertThat( eval("NA & NA"), elementsIdenticalTo(c(NA)));
  }

  @Test
  public void not() {
    assertThat( eval(" !TRUE"), elementsIdenticalTo( c(false)));
    assertThat( eval(" !c(TRUE, FALSE, NA) "), elementsIdenticalTo(c(FALSE, TRUE, NA)));
    assertThat( eval(" !c(1,0,1) "), elementsIdenticalTo( c(FALSE,TRUE,FALSE) ));
    assertThat( eval(" !c(1L,0L,4L) "), elementsIdenticalTo( c(FALSE,TRUE,FALSE) ));
  }

  @Test
  public void any() {
    assertThat( eval(" any(FALSE, NA) "), elementsIdenticalTo( c(NA)) );
    assertThat( eval(" any(FALSE, NA, na.rm=TRUE) "), elementsIdenticalTo( c(FALSE)) );
    assertThat( eval(" any(TRUE, NA) "), elementsIdenticalTo( c(TRUE)) );
    assertThat( eval(" any('TRUE') "), elementsIdenticalTo( c(TRUE)));
    assertThat( eval(" any(c(0,0,0,1), list())"), elementsIdenticalTo( c(TRUE)));
  }

  @Test
  public void notEmptyList() {
    assertThat( eval( "!list()"), identicalTo( (SEXP) LogicalArrayVector.EMPTY));
  }
}
