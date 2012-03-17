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

package org.renjin.primitives;

import org.junit.Test;
import org.renjin.primitives.Sequences;

import r.EvalTestCase;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.LogicalVector;
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
    assertTrue(range.count >= 3d && range.count < 4d );
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
    return fn.colonSequence(topLevelContext, new DoubleVector(n1), new DoubleVector(n2));
  }

  @Test
  public void assignment() {
    assertThat( eval( "x <- 1:3 "), equalTo( c_i(1,2,3)));
  }

  @Test
  public void combine() {
    assertThat( eval( "c(1:3) "), equalTo( c_i(1,2,3)));
  }

  @Test
  public void repInt() {
    assertThat( eval( ".Internal(rep.int(c('a', 'b', 'c'), 2))"), equalTo(c("a","b","c","a","b","c")));
    assertThat( eval( ".Internal(rep.int(c('a', 'b', 'c'), 0))"), equalTo( CHARACTER_0 ));
  }
  
  @Test
  public void repIntEach() {
    assertThat( eval(".Internal(rep.int(c('a', 'b', 'c'), c(3, 2, 4)))"),
        equalTo(c("a","a","a","b","b","c","c","c","c")));
  }
  
  @Test
  public void repRecycling() {
    assertThat( eval(" rep(c(1, 2, 3, 4, 5, 6, 7, 8, 9), c(9, 9, 9, 9, 9, 9, 9, 9, 9))"), equalTo(
        c(1, 1, 1, 1, 1, 1, 1, 1, 1, 
          2, 2, 2, 2, 2, 2, 2, 2, 2,
          3, 3, 3, 3, 3, 3, 3, 3, 3,
          4, 4, 4, 4, 4, 4, 4, 4, 4,
          5, 5, 5, 5, 5, 5, 5, 5, 5, 
          6, 6, 6, 6, 6, 6, 6, 6, 6,
          7, 7, 7, 7, 7, 7, 7, 7, 7,
          8, 8, 8, 8, 8, 8, 8, 8, 8,
          9, 9, 9, 9, 9, 9, 9, 9, 9)));
  }

  @Test
  public void seqInt() {
    assertThat( eval(" seq.int(to=6, from=3)" ), equalTo(c_i(3,4,5,6)));
    assertThat( eval(" seq.int(3,6)" ), equalTo(c_i(3,4,5,6)));
    assertThat( eval(" seq.int(from=10, length=4)" ), equalTo(c(10,11,12,13))); // this seems like it should be double
                                                                                // but R.2.10.1 returns double
    assertThat( eval(" seq.int(to=10, by=2)"), equalTo(c(1,3,5,7,9)));
    assertThat( eval(" seq.int(length=4)" ), equalTo(c_i(1,2,3,4)));
  }

  @Test
  public void rep() {
    assertThat( eval(" rep() "), equalTo(NULL));
    assertThat( eval(" rep(c(1,2,3)) "), equalTo(c(1,2,3)));
    assertThat( eval(" rep(1, length.out=5) "), equalTo(c(1,1,1,1,1)));
    assertThat( eval(" rep(c(1,2,3), length.out=5) "), equalTo(c(1,2,3,1,2)));

    assertThat( eval(" rep(1, times=5) "), equalTo(c(1,1,1,1,1)));
    assertThat( eval(" rep(c(1,2,3), times=2) "), equalTo(c(1,2,3,1,2,3)));

    assertThat( eval(" rep(1, each=4)" ), equalTo(c(1,1,1,1)));
    assertThat( eval(" rep(c(1,2), each=4)" ), equalTo(c(1,1,1,1,2,2,2,2)));
    assertThat( eval(" rep(c(1,2), each=4,l=6)" ), equalTo(c(1,1,1,1,2,2)));
  }

  @Test
  public void repWithZeroLengthOut() {
    assertThat( eval(" rep(NA, length.out=0) "), equalTo( (SEXP)new LogicalVector()));
  }


}
