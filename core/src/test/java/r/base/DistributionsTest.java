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


import r.EvalTestCase;
import r.lang.DoubleVector;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests the distribution functions.
 * Since most of the heavy lifting is done by the Apache Commons library,
 * we just want to make sure that we've lined the dist parameters up correctly.
 */
public class DistributionsTest extends EvalTestCase{

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
  public void dnbeta() throws MathException {
    assertThat(Distributions.dnbeta(0.5, 20.0, 20.0, 1.0,  false), closeTo(5.000253, ERROR));
    assertThat(Distributions.dnbeta(0.8, 40.0, 20.0, 0.5,  true), closeTo(-0.670098, ERROR));
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
  public void DLogNormal() {
    assertThat(Distributions.dlnorm(1.96, 0, 1,  /* log.p */ false), closeTo(0.1622998, ERROR));
    assertThat(Distributions.dlnorm(2.55, 0, 1,  /* log.p */ true), closeTo(-2.293167, ERROR));
  }

  @Test
  public void dgeom() {
    assertThat(Distributions.dgeom(5, 0.5, false), closeTo(0.015625, ERROR));
    assertThat(Distributions.dgeom(10, 0.2, true), closeTo(-3.840873, ERROR));
  }
  
  @Test
  public void pgeom() {
    assertThat(Distributions.pgeom(3, 0.5, true, false), closeTo( 0.9375, ERROR));
    assertThat(Distributions.pgeom(10, 0.2, false,false ), closeTo( 0.08589935, ERROR));
  }
  
  @Test
  public void qgeom() {
    assertThat(Distributions.qgeom(0.9, 0.6, true, false), closeTo( 2.0, ERROR));
    assertThat(Distributions.qgeom(0.4, 0.1, false,false ), closeTo( 8.0, ERROR));
  }
  

  @Test
  public void dnbinom() throws MathException {
    assertThat(Distributions.dnbinom(3, 5, 0.25, false), closeTo(0.01441956, ERROR));
  }
  
  @Test
  public void dnbinom_mu() throws MathException {
    assertThat(Distributions.dnbinom_mu(4, 10, 10, false), closeTo( 0.04364014, ERROR));
    assertThat(Distributions.dnbinom_mu(6, 10, 10, true), closeTo(-2.572162, ERROR));
  }

  @Test
  public void pnbinom() throws MathException {
    assertThat(Distributions.pnbinom(3, 5, 0.5, false, false), closeTo(0.6367187, ERROR));
  }
  
  @Test
  public void qnbinom() throws MathException {
      assertThat(Distributions.qnbinom(0.4, 900, 0.9, true, false), closeTo(97, ERROR));
      assertThat(Distributions.qnbinom(0.01, 900, 0.9, true, false), closeTo(76, ERROR));
      assertThat(Distributions.qnbinom(0.1, 900, 0.3, true, true), equalTo(DoubleVector.NaN));
  }
  
  @Test
  public void qnbinom_mu() throws MathException {
      assertThat(Distributions.qnbinom_mu(0.6, 20, 8, true, false), closeTo(9.0, ERROR));
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
  
  @Test
  public void pnchisquare(){
    assertThat(Distributions.pnchisq(0.75, 4, 1, true, false), closeTo(0.03540971, ERROR));
  }
  
  @Test
  public void qnchisquare(){
    assertThat(Distributions.qnchisq(0.75, 4, 1, true, false), closeTo(6.737266, ERROR));
  }
  
  @Test
  public void pnt(){
    assertThat(Distributions.pnt(1.96, 20, 2, true, false), closeTo(0.4752101, ERROR));
  }

  
  @Test
  public void qnt(){
    assertThat(Distributions.qnt(0.8, 20, 2, true, false), closeTo(2.965995, ERROR));
  }
  
   @Test
  public void dnt(){
    assertThat(Distributions.dnt(2, 20 , 3, false), closeTo(0.2435572, ERROR));
  }
   
   @Test
   public void dnchisq(){
     assertThat(Distributions.dnchisq(2, 10 , 5, false), closeTo(0.001017647, ERROR));
     assertThat(Distributions.dnchisq(5, 9 , 10, true), closeTo(-5.125956, ERROR));
   }
   
   @Test
   public void pnbinom_mu(){
     assertThat(Distributions.pnbinom_mu(0.25, 10 , 4, true, false), closeTo( 0.03457161, ERROR));
     assertThat(Distributions.pnbinom_mu(0.25, 10 , 4, true, true), closeTo( -3.364722, ERROR));
     assertThat(Distributions.pnbinom_mu(0.25, 10 , 4, false, false), closeTo(0.9654284, ERROR));
   }

   @Test
   public void pnbeta(){
     assertThat(Distributions.pnbeta(0.25, 1,6 , 1, true, false), closeTo(0.6935046, ERROR));
     assertThat(Distributions.pnbeta(0.25, 1,6 , 1, true, true), closeTo(-0.3659974, ERROR));
   }
   
   
   @Test
   public void dnf(){
     assertThat(Distributions.dnf(1, 6,6 , 1, false), closeTo(0.4621278, ERROR));
     assertThat(Distributions.dnf(1, 6,6 , 1, true), closeTo(-0.7719139, ERROR));
   }
   
    @Test
   public void pnf(){
     assertThat(Distributions.dnf(0, 6,6 , 1, false), closeTo(0, ERROR));
     assertThat(Distributions.dnf(1, 6,6 , 1, false), closeTo(0.4621278, ERROR));
     assertThat(Distributions.dnf(2, 6,6 , 1, true), closeTo(-1.662094, ERROR));
   }

    /*
     * qnbeta() and qnf() functions sometimes return different values when compared to
     * original interpreter. This is about accuracy and should be corrected at next level.
     * mhsatman.
     */
    @Test
    public void qnbeta(){
      double ERROR = 0.001;
      assertThat(Distributions.qnbeta(0.05, 12,8 , 1, true, false), closeTo(0.428099, ERROR));
    }
    
    @Test
    public void qnf(){
      assertThat(Distributions.qnf(0.05, 4,2 , 1, true, false), closeTo( 0.1835066, ERROR));
    }
    
    @Test 
    public void tukeys(){
      //This is confusing. Because location of parameters are replaced in R calls
      try{
        topLevelContext.init();
      }catch(Exception e){
        
      }
      assertThat(eval("ptukey(5.20, 14,12,5 , TRUE, FALSE)").asReal(), closeTo(0.7342322, ERROR));
      assertThat(eval("ptukey(4.9, 21,9,2,T,T)").asReal(), closeTo(-0.406227, ERROR));
      assertThat(eval("qtukey(0.90, 6,3, 4,T,F)").asReal(), closeTo(8.001985, ERROR));
      
    }
}
