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
package r.base;

import org.apache.commons.math.MathException;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests the distribution functions.
 * Since most of the heavy lifting is done by the Apache Commons library,
 * we just want to make sure that we've lined the dist parameters up correctly.
 */
public class DistributionsTest {

  private static final double ERROR = 0.00001;

  @Test
  public void norm() throws MathException {
    assertThat(Distributions.dnorm(0, 0, 1, /* log */ false), closeTo(0.3989423, ERROR));
    assertThat(Distributions.dnorm(0, 0, 1, /* log */ true), closeTo(-0.9189385, ERROR));

    assertThat(Distributions.pnorm(0.25, 0, 1, /* lower.tail */ true, /* log.p */ false), closeTo(0.5987063, ERROR));
    assertThat(Distributions.pnorm(0.25, 0, 1, /* lower.tail */ false, /* log.p */ false), closeTo(0.4012937, ERROR));
    assertThat(Distributions.pnorm(0.25, 0, 1, /* lower.tail */ true, /* log.p */ true), closeTo(-0.5129841, ERROR));
    assertThat(Distributions.pnorm(0.25, 0, 1, /* lower.tail */ false, /* log.p */ true), closeTo(-0.9130618, ERROR));

    assertThat(Distributions.qnorm(0.25, 0, 1, /* lower.tail */ true, /* log.p */ false), closeTo(-0.6744898, ERROR));
    assertThat(Distributions.qnorm(0.25, 0, 1, /* lower.tail */ false, /* log.p */ false), closeTo(0.6744898, ERROR));
    assertThat(Distributions.qnorm(0.99, 0, 1, /* lower.tail */ false, /* log.p */ false), closeTo(-2.326348, ERROR));
    assertThat(Distributions.qnorm(0.99, 0, 1, /* lower.tail */ false, /* log.p */ true), equalTo(Double.NaN));
    assertThat(Distributions.qnorm(0, 0, 1, /* lower.tail */ true, /* log.p */ false), equalTo(Double.NEGATIVE_INFINITY));

  }

  @Test
  public void beta() throws MathException {
    assertThat(Distributions.dbeta(0.4, 5, 1, false), closeTo(0.128, ERROR));
  }

  @Test
  public void binom() throws MathException {
    assertThat(Distributions.dbinom(3, 5, 0.25, false), closeTo(0.08789063, ERROR));
  }
  
  @Test
  public void qbinom() throws MathException {
    assertThat(Distributions.qbinom(0.2, 114, 0.55, true, false), closeTo(58, ERROR));
    assertThat(Distributions.qbinom(0.1, 21, 0.2, true, false), closeTo(2, ERROR));
  }

  @Test
  public void exp() throws MathException {
    assertThat(Distributions.dexp(0.5, 0.25, false), closeTo(0.5413411, ERROR));
  }

  @Test
  public void hyper() throws MathException {
    assertThat(Distributions.dhyper(3, 5, 2, 3, false), closeTo(0.2857143, ERROR));
  }

  @Test
  public void QLogNormal() {
    assertThat(Distributions.qlnorm(0.95, 0, 1, /* lower.tail */ true, /* log.p */ false), closeTo(5.180252, ERROR));
    assertThat(Distributions.qlnorm(0.68, 0, 1, /* lower.tail */ false, /* log.p */ false), closeTo(0.6264422, ERROR));
  }

  @Test
  public void PLogNormal() {
    assertThat(Distributions.plnorm(1.96, 0, 1, /* lower.tail */ true, /* log.p */ false), closeTo(0.7495087, ERROR));
    assertThat(Distributions.plnorm(2.55, 0, 1, /* lower.tail */ false, /* log.p */ false), closeTo(0.1746126, ERROR));
  }

  @Test
  public void dgeom() {
    assertThat(Distributions.dgeom(5, 0.5, false), closeTo(0.015625, ERROR));
    assertThat(Distributions.dgeom(10, 0.2, true), closeTo(-3.840873, ERROR));
  }

  @Test
  public void dnbinom() throws MathException {
    assertThat(Distributions.dnbinom(3, 5, 0.25, false), closeTo(0.01441956, ERROR));
  }

  @Test
  public void pnbinom() throws MathException {
    assertThat(Distributions.pnbinom(3, 5, 0.5, false, false), closeTo(0.6367187, ERROR));
  }

  @Test
  public void plogis() throws MathException {
    assertThat(Distributions.plogis(2.55, 0, 1, false, false), closeTo(0.07242649, ERROR));
  }

  @Test
  public void dlogis() throws MathException {
    assertThat(Distributions.dlogis(3, 5, 0.25, false), closeTo(0.001340951, ERROR));
  }

  @Test
  public void qlogis() {
    assertThat(Distributions.qlogis(0.7, 0, 1, false, false), closeTo(-0.8472979, ERROR));
  }
  
  @Test
  public void qsignrank(){
    assertThat(Distributions.qsignrank(0.7, 10, false, false), closeTo(22, ERROR));
    assertThat(Distributions.qsignrank(0.7, 10, true, false), closeTo(33, ERROR));
  }
  
  @Test
  public void psignrank(){
    assertThat(Distributions.psignrank(0.7, 10, false, false), closeTo(0.99902, ERROR));
    assertThat(Distributions.psignrank(0.7, 10, true, false), closeTo(0.0009765, ERROR));
  }  
  
  @Test
  public void dsignrank(){
    assertThat(Distributions.dsignrank(2, 5, false), closeTo(0.03125, ERROR));
    assertThat(Distributions.dsignrank(2, 5, true), closeTo(-3.465736, ERROR));
  }
  
  @Test
  public void dwilcox(){
    assertThat(Distributions.dwilcox(10,5,3, false), closeTo(0.08928571, ERROR));
    assertThat(Distributions.dwilcox(20,6,4, true), closeTo(-3.73767, ERROR));
  }
  
   @Test
  public void pwilcox(){
    assertThat(Distributions.pwilcox(2,10,5, false, false), closeTo(0.998668, ERROR));
    assertThat(Distributions.pwilcox(7,15,7, true, false), closeTo(0.0002638615, ERROR));
  } 
   
   
  @Test
  public void qwilcox(){
    assertThat(Distributions.qwilcox(0.5,10,4, true, false), closeTo(20.0, ERROR));
    assertThat(Distributions.qwilcox(0.1,4, 10, false, false), closeTo(29.0, ERROR));
  } 
   
}
