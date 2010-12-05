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
import r.lang.DoubleVector;
import r.lang.EvalTestCase;
import r.lang.IntVector;
import r.lang.SEXP;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SequenceTest extends EvalTestCase {

  @Test
  public void integerRange() {
    Sequences.Range range = new Sequences.Range(1, 9999);
    assertThat(range.useInteger, equalTo(true));
  }
  @Test
  public void fpRange() {
    Sequences.Range range = new Sequences.Range(1.2, 4.2);
    assertThat(range.useInteger, equalTo(false));
  }

  @Test
  public void count() {
    Sequences.Range range = new Sequences.Range(11, 13);
    assertTrue(range.count > 3d && range.count < 4d );
  }

  @Test
  public void ascendingInts() {
    assertThat(colon(199,201),
        equalTo((SEXP)new IntVector(199, 200, 201)));
  }

  @Test
  public void descendingInts() {
    assertThat(colon(9, 5),
        equalTo((SEXP)new IntVector(9, 8, 7, 6, 5)));
  }

  @Test
  public void ascendingReals() {
    assertThat(colon(1.2, 5).toString(),
        equalTo(new DoubleVector(1.2, 2.2, 3.2, 4.2).toString()));
  }

  @Test
  public void descendingReals() {
    assertThat(colon(9.1, 5.1).toString(),
        equalTo(new DoubleVector(9.1, 8.1, 7.1, 6.1, 5.1).toString()));
  }


  private SEXP colon(double n1, double n2) {
    Sequences fn = new Sequences();                 
    return fn.colonSequence(new DoubleVector(n1), new DoubleVector(n2));
  }

  @Test
  public void assignment() {
    assertThat( eval( "x <- 1:3 "), equalTo( c_i(1,2,3)));
  }

  @Test
  public void combine() {
    assertThat( eval( "c(1:3) "), equalTo( c_i(1,2,3)));
  }

}
