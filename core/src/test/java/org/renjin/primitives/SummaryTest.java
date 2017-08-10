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
package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Logical;

import static org.junit.Assert.assertThat;

public class SummaryTest extends EvalTestCase {

  @Test
  public void maxTest() {

    assertThat(eval("max(1,2,3,4)"), elementsIdenticalTo(c(4)));
    assertThat(eval("max(4,99,2,3,4)"), elementsIdenticalTo(c(99)));
    assertThat(eval("max(4,c(99,2,3),4,c(-1,400,33))"), elementsIdenticalTo(c(400)));
    assertThat(eval("max(FALSE, TRUE)"), elementsIdenticalTo(c_i(1)));
  }

  @Test
  public void rangeTest() {
    assertThat(eval("range(1,2,3,4)"), elementsIdenticalTo(c(1, 4)));
    assertThat(eval("range(4,99,2,3,4)"), elementsIdenticalTo(c(2,99)));
    assertThat(eval("range(4,c(99,2,3),4,c(-1,400,33))"), elementsIdenticalTo(c(-1, 400)));
    assertThat(eval("range(FALSE, TRUE)"), elementsIdenticalTo(c_i(0, 1)));
  }

  @Test
  public void rangeWorksRecursively() {
    assertThat(eval("range(list(1,2,1:10,list(-9,1:100)))"), elementsIdenticalTo(c(-9, 100)));
  }
  
  @Test
  public void rangeWithNAs() {
    assertThat(eval("range(1,99, NA)"), elementsIdenticalTo(c(DoubleVector.NA, DoubleVector.NA)));
    assertThat(eval("range(1L,99L, NA)"), elementsIdenticalTo(c_i(IntVector.NA, IntVector.NA)));
  }

  @Test
  public void maxTestWithNA() {
    assertThat(eval("max(4,NA)"), elementsIdenticalTo(c(DoubleVector.NA)));
  }

  @Test
  public void maxTestWithNAsRemoved() {
    assertThat(eval("max(4,99,2,3,NA,na.rm=TRUE)"), elementsIdenticalTo(c(99)));
  }

  @Test
  public void maxTestWithCharacters() {
    assertThat(eval("max('a', 'aaa', 'b', 'cc', 999)"), elementsIdenticalTo(c("cc")));
    assertThat(eval("max('a', 'aaa', 'b', 'cc', 999)"), elementsIdenticalTo(c("cc")));
  }

  @Test
  public void testProd() throws Exception {
    assertThat( eval("prod() "), elementsIdenticalTo(c(1))) ;
    assertThat( eval("prod(NULL) "), elementsIdenticalTo(c(1))) ;
    assertThat( eval("prod(2,4) "), elementsIdenticalTo(c(8))) ;
    assertThat( eval("prod(1, NA) "), elementsIdenticalTo(c(DoubleVector.NA)));
    assertThat( eval("prod(1, NA, na.rm=TRUE) "), elementsIdenticalTo(c(1)));
  }

  @Test
  public void testSum() throws Exception {
    assertThat( eval("sum(1, 2, 3) "), elementsIdenticalTo(c(6)));
    assertThat( eval("sum(1L, 2L, 3L) "), elementsIdenticalTo(c_i(6)));
    assertThat( eval("sum(1L, 2L, 3.4) "), elementsIdenticalTo(c(6.4)));
    assertThat( eval("sum(TRUE, TRUE, FALSE)"), elementsIdenticalTo(c_i(2)));
    assertThat( eval("sum(TRUE, TRUE, NA)"), elementsIdenticalTo(c_i(IntVector.NA)));
    assertThat( eval("sum(1073741824L, 1073741824L)"), elementsIdenticalTo(c_i(IntVector.NA)));
  }

  @Test
  public void testSumWithNAs() {
    assertThat( eval("sum(TRUE, TRUE, NA, na.rm=TRUE)"), elementsIdenticalTo(c_i(2)));
  }
  
  @Test
  public void testMean() {
    assertThat(eval(".Internal(mean(c(1,2,3,4)))"), elementsIdenticalTo(c(2.5)));
  }

  @Test
  public void testCumsum() {
    assertThat(eval("cumsum(1:10)"), elementsIdenticalTo(c_i(1, 3, 6, 10, 15, 21, 28, 36, 45, 55)));
    assertThat(eval("cumsum(c(1,2,NA,4))"), elementsIdenticalTo(c(1, 3, DoubleVector.NA, DoubleVector.NA)));
    assertThat(eval("is.na(cumsum(c(1, 2, NA, 3)))"), elementsIdenticalTo( c(Logical.FALSE, Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat(eval("is.na(cumsum(c(1, NaN, 3, 4)))"), elementsIdenticalTo( c(Logical.FALSE, Logical.TRUE, Logical.TRUE, Logical.TRUE)));
  }
  
  @Test
  public void testCumsumIntegerOverflow() {
    assertThat(eval("cumsum(c(1L, 2L, 2147483647L))"), elementsIdenticalTo(c_i(1, 3, IntVector.NA)));
  }

  @Test
  public void testCumprod() {
    assertThat(eval("cumprod(1:10)"), elementsIdenticalTo(c(1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800)));
    assertThat(eval("cumprod(c(4,7,NA,10))"), elementsIdenticalTo(c(4, 28, DoubleVector.NA, DoubleVector.NA)));
    assertThat(eval("is.na(cumprod(c(1, 2, NA, 3)))"), elementsIdenticalTo( c(Logical.FALSE, Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat(eval("is.na(cumprod(c(1, NaN, 3, 4)))"), elementsIdenticalTo( c(Logical.FALSE, Logical.TRUE, Logical.TRUE, Logical.TRUE)));
  }

  @Test
  public void testCummax() {
    assertThat(eval("cummax(c(5,4,6,7,4,2,10))"), elementsIdenticalTo(c(5, 5, 6, 7, 7, 7, 10)));
    assertThat(eval("cummax(c(5,4,6,NA,4,2,10))"), elementsIdenticalTo(c(5, 5, 6, DoubleVector.NA, DoubleVector.NA, DoubleVector.NA, DoubleVector.NA)));
    assertThat(eval("is.na(cummax(c(1, 2, NA, 3)))"), elementsIdenticalTo( c(Logical.FALSE, Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat(eval("is.na(cummax(c(1, NaN, 3, 4)))"), elementsIdenticalTo( c(Logical.FALSE, Logical.TRUE, Logical.TRUE, Logical.TRUE)));
  }
  
  @Test
  public void testCummin() {
    assertThat(eval("cummin(c(5,4,6,7,4,2,10))"), elementsIdenticalTo(c(5, 4, 4, 4, 4, 2, 2)));
    assertThat(eval("cummin(c(5,4,6,NA,4,2,10))"), elementsIdenticalTo(c(5, 4, 4, DoubleVector.NA, DoubleVector.NA, DoubleVector.NA, DoubleVector.NA)));
    assertThat(eval("is.na(cummin(c(1, 2, NA, 3)))"), elementsIdenticalTo( c(Logical.FALSE, Logical.FALSE, Logical.TRUE, Logical.TRUE)));
    assertThat(eval("is.na(cummin(c(1, NaN, 3, 4)))"), elementsIdenticalTo( c(Logical.FALSE, Logical.TRUE, Logical.TRUE, Logical.TRUE)));
  }
  
  @Test
  public void pmin() {
    assertThat(eval(".Internal(pmin(FALSE, 5:1, 3))"), elementsIdenticalTo(c(3, 3, 3, 2, 1)));
    assertThat(eval(".Internal(pmin(FALSE, c(1,2,3), c(0,NA,0)))"), elementsIdenticalTo(c(0, DoubleVector.NA, 0)));
    assertThat(eval(".Internal(pmin(TRUE, c(1,2,3), c(0,NA,0)))"), elementsIdenticalTo(c(0, 2, 0)));
  }
  
  @Test
  public void pmax() {
    assertThat(eval(".Internal(pmax(FALSE, 5:1, 3))"), elementsIdenticalTo(c(5, 4, 3, 3, 3)));
    assertThat(eval(".Internal(pmax(FALSE, c(1,2,3), c(0,NA,0)))"), elementsIdenticalTo(c(1, DoubleVector.NA, 3)));
    assertThat(eval(".Internal(pmax(TRUE, c(1,2,3), c(0,NA,0)))"), elementsIdenticalTo(c(1, 2, 3)));
  }
}
