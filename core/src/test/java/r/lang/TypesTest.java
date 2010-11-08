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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TypesTest extends EvalTestCase {

  @Test
  public void asCharacter() {
    assertThat( evaluateToExpression("as.character(1)"), equalTo( c("1") ));
    assertThat( evaluateToExpression("as.character(\"foobar\")"), equalTo( c("foobar") ));
    assertThat( evaluateToExpression("as.character(1L)"), equalTo( c("1") ));
    assertThat( evaluateToExpression("as.character(1.3333333333333333333333333333333333)"),
        equalTo(c("1.33333333333333")));
    assertThat( evaluateToExpression("as.character(TRUE)"), equalTo( c("TRUE") ));
    assertThat( evaluateToExpression("as.character(NA)"), equalTo( c( StringExp.NA )) );
  }

  @Test
  public void asDoubleFromDouble() {
    assertThat( evaluateToExpression("as.double(3.14)"), equalTo( c(3.14) ) );
    assertThat( evaluateToExpression("as.double(NA_real_)"), equalTo( c(RealExp.NA) ) );
  }

  @Test
  public void asDoubleFromInt() {
    assertThat( evaluateToExpression("as.double(3L)"), equalTo( c(3l) ));
  }

  @Test
  public void asDoubleFromLogical() {
    assertThat( evaluateToExpression("as.double(TRUE)"), equalTo( c(1d) ));
    assertThat( evaluateToExpression("as.double(FALSE)"), equalTo( c(0d) ));
  }

  @Test
  public void asDoubleFromString() {
    assertThat( evaluateToExpression("as.double(\"42\")"), equalTo( c(42d) ));
    assertThat( evaluateToExpression("as.double(\"not an integer\")"), equalTo( c(RealExp.NA) ));
  }

  @Test
  public void asIntFromDouble() {
    assertThat( evaluateToExpression("as.integer(3.1)"), equalTo( c_i( 3 )));
    assertThat( evaluateToExpression("as.integer(3.9)"), equalTo( c_i( 3 )));
    assertThat( evaluateToExpression("as.integer(NA_real_)"), equalTo( c_i( IntExp.NA )));
    assertThat( evaluateToExpression("as.integer(c(1, 9.32, 9.9, 5.0))"), equalTo( c_i(1, 9, 9, 5 )));
  }

}
