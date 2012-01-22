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

package r.base.optimize;

import org.junit.Test;
import r.EvalTestCase;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class OptimizationsTest extends EvalTestCase {

  @Test
  public void nlm() throws IOException {
    topLevelContext.init();

    eval("f <- function(x) sum((x-1:length(x))^2)");
    eval("x <- nlm(f, c(10,10))");

    assertThat(eval("x$estimate"), closeTo(c(1, 2), 0.00001));
    assertThat(eval("x$code"), equalTo(c_i(1)));
    assertThat(eval("x$minimum"), closeTo(c(4.303458e-26), 0.0001e-26));
    assertThat(eval("x$gradient"), closeTo(c( 2.757794e-13, -3.099743e-13), 0.00001e-13));
 }

  @Test
  public void nlmWithGradient() throws IOException {
    topLevelContext.init();
       eval("f <- function(x, a) {" +
           "    res <- sum((x-a)^2)\n" +
           "    attr(res, 'gradient') <- 2*(x-a)\n" +
           "    res }");

    eval("x <- nlm(f, c(10,10), a=c(3,5))");

    assertThat(eval("x$estimate"), closeTo(c(3, 5), 0.0001));
    assertThat(eval("x$minimum"), closeTo(c(0), 0.000001));
    assertThat(eval("x$code"), equalTo(c_i(1)));
    assertThat(eval("x$gradient"), closeTo(c(0,0), 0.00001));
  }

  @Test
  public void fmin() throws IOException {
    topLevelContext.init();

    eval("f <- function (x,a) (x-a)^2");
    eval("x <-  optimize(f, c(0, 1), tol = 0.0001, a = 1/3)");

    assertThat(eval("x$minimum"), closeTo(c(0.333333), 0.000001));
  }


}
