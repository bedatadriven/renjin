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
import org.renjin.sexp.S4Object;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class IdenticalTest extends EvalTestCase {

  @Test
  public void identical() {
    eval("identical <- function(x,y) .Internal(identical(x,y,TRUE,TRUE,TRUE,TRUE)) ");

    assertThat(eval("identical(1,1)"), equalTo(c(true)));
    assertThat(eval("identical(1,1L)"), equalTo(c(false)));
    assertThat(eval("identical(1,NA)"), equalTo(c(false)));
    assertThat(eval("identical(NA,NA)"), equalTo(c(true)));
    assertThat(eval("identical(NA_real_,NA_real_)"), equalTo(c(true)));
    assertThat(eval("identical(1:3,c(1L,2L,3L))"), equalTo(c(true)));
    assertThat(eval("identical(quote(x), quote(y))"), equalTo(c(false)));
    assertThat(eval("identical(quote(x), quote(x))"), equalTo(c(true)));
    assertThat(eval("identical(NULL, NULL)"), equalTo(c(true)));
    assertThat(eval("identical(NULL, 1)"), equalTo(c(false)));
    assertThat(eval("identical(list(x=1,y='foo',NA), list(x=1,y='foo',NA))"), equalTo(c(true)));
    assertThat(eval("identical(function(x) x, function(x) x)"), equalTo(c(false)));
    assertThat(eval("identical(1+3i, 1+4i)"), equalTo(c(false)));
    assertThat(eval("identical(1+3i, 2+3i)"), equalTo(c(false)));
    assertThat(eval("identical(1+3i, 1+3i)"), equalTo(c(true)));
    assertThat(eval("identical(NaN, NaN)"), equalTo(c(true)));  // strangely enough...

    eval("f<- function(x) x");
    assertThat(eval("identical(f,f)"), equalTo(c(true)));

    eval("y <- x <- 1:12");
    eval("dim(x) <- c(6,2)");
    eval("dim(y) <- c(3,4)");
    assertThat(eval("identical(x,y)"), equalTo(c(false)));

    eval("dim(y) <- c(6,2)");
    assertThat(eval("identical(x,y)"), equalTo(c(true)));

    eval("attr(x,'foo') <- 'bar'");
    assertThat(eval("identical(x,y)"), equalTo(c(false)));

  }

  @Test
  public void identicalS4() {
    topLevelContext.getGlobalEnvironment().setVariable("x", new S4Object());
    topLevelContext.getGlobalEnvironment().setVariable("y", new S4Object());
    eval("attr(x, 'foo') <- 'bar' ");
    eval("attr(y, 'foo') <- 'baz' ");

    assertThat(eval(".Internal(identical(x,y,TRUE,TRUE,TRUE,TRUE))"), equalTo(c(false)));

    eval("attr(y, 'foo') <- 'bar' ");

    assertThat(eval(".Internal(identical(x,y,TRUE,TRUE,TRUE,TRUE))"), equalTo(c(true)));
  }

  @Test
  public void doubleEquals() {
    boolean bitwiseComparison = false;
    boolean bitwiseComparisonNaN = false;

    assertTrue(Identical.equals(DoubleVector.NaN, DoubleVector.NaN, bitwiseComparison, bitwiseComparisonNaN));
    assertTrue(Identical.equals(DoubleVector.NA, DoubleVector.NA, bitwiseComparison, bitwiseComparisonNaN));
    assertFalse(Identical.equals(DoubleVector.NA, Double.POSITIVE_INFINITY, bitwiseComparison, bitwiseComparisonNaN));

    assertFalse(Identical.equals(DoubleVector.NA, DoubleVector.NaN, bitwiseComparison, bitwiseComparisonNaN));
    assertFalse(Identical.equals(DoubleVector.NaN, DoubleVector.NA, bitwiseComparison, bitwiseComparisonNaN));
    assertFalse(Identical.equals(Double.POSITIVE_INFINITY, DoubleVector.NA, bitwiseComparison, bitwiseComparisonNaN));

  }

}
