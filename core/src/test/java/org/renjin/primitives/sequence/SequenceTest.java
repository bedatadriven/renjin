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
package org.renjin.primitives.sequence;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.*;

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
    assertTrue(range.count >= 3d && range.count < 4d);
  }

  @Test
  public void ascendingInts() {
    assertThat(colon(199, 201), elementsEqualTo(199, 200, 201));
  }

  @Test
  public void descendingInts() {
    assertThat(colon(9, 5), elementsEqualTo(9, 8, 7, 6, 5));
  }

  @Test
  public void ascendingReals() {
    assertThat(colon(1.2, 5), elementsEqualTo(1.2, 2.2, 3.2, 4.2));
  }

  @Test
  public void descendingReals() {
    assertThat(colon(9.1, 5.1), elementsEqualTo(9.1, 8.1, 7.1, 6.1, 5.1));
  }


  @Test
  public void singleReal() {
    assertThat(colon(1.2, 1.2), elementsEqualTo(1.2));
  }


  private SEXP colon(double n1, double n2) {
    Sequences fn = new Sequences();
    return fn.colonSequence(topLevelContext, new DoubleArrayVector(n1), new DoubleArrayVector(n2));
  }

  @Test
  public void assignment() {
    assertThat( eval( "x <- 1:3 "), elementsEqualTo(1, 2, 3));
  }

  @Test
  public void combine() {
    assertThat( eval( "c(1:3) "), elementsEqualTo(1, 2, 3));
  }

  @Test
  public void repInt() {
    assertThat( eval( ".Internal(rep.int(c('a', 'b', 'c'), 2))"), equalTo(c("a","b","c","a","b","c")));
    assertThat( eval( ".Internal(rep.int(c('a', 'b', 'c'), 0))"), equalTo(CHARACTER_0));
  }
  
  @Test
  public void zeroLengthRep() {
    // Deferred calculation in x...
    eval(" x <- (1:1e8)*3");
    
    // Repeat it, but with length 0...
    eval(" x <- rep(x, length=0)");
  }
  
  @Test
  public void repOneDimensionalArray() {
    eval("a <- 1:3");
    eval("dim(a) <- 3");
    eval("names(a) <- c('a','b','c')");
    
    assertThat(eval("names(rep(a, length=4))"), equalTo(c("a", "b", "c", "a")));
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
  public void testRepWithEmptyArg() {
    assertThat(eval("rep(integer(0), length.out=3)"), equalTo(c_i(IntVector.NA, IntVector.NA, IntVector.NA)));
  }

  @Test
  public void testRepWithNullArg() {
    assertThat(eval("rep(NULL, length.out=3)"), equalTo((SEXP)Null.INSTANCE));
  }
  
  @Test
  public void seqInt() {
    assertThat( eval(" seq.int(to=6, from=3)" ), elementsEqualTo(3, 4, 5, 6));
    assertThat( eval(" seq.int(3,6)" ), elementsEqualTo(3, 4, 5, 6));
    assertThat( eval(" seq.int(from=10, length=4)" ), elementsEqualTo(10, 11, 12, 13));
    assertThat( eval(" seq.int(to=10, by=2)"), elementsEqualTo(1, 3, 5, 7, 9));
    assertThat( eval(" seq.int(length=4)" ), elementsEqualTo(1, 2, 3, 4));
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
  public void repIssue61() {
    assumingBasePackagesLoad();
    eval(" f <- function () {  b <- 0; a <- rep(1.1,1000); for (i in 1:100000) " +
        "{ a <- sqrt(a+7); b <- b + sum(a); sum <- mean; }; b;  } ");

    eval("print(system.time(print(x <- f())))");
    assertThat(eval("x"), closeTo(c(322101), 0.1));

  }

  @Test
  public void repWithZeroLengthOut() {
    assertThat( eval(" rep(NA, length.out=0) "), equalTo( (SEXP) LogicalVector.EMPTY));
  }
  
  @Test
  public void seqIntFrom() {
    eval(" x <- seq.int(from=1, length.out=1) ");
    assertThat( eval("x[1]"), equalTo( c_i(1)));
    
  }
}
