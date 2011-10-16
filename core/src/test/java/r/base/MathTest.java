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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import r.EvalTestCase;

public class MathTest extends EvalTestCase {

  @Test
  public void negativeNumbers() {
    assertThat( eval( "-1" ), equalTo( c( -1 )));
  }


  @Test
  public void positiveNumbers() {
    assertThat( eval( "+1" ), equalTo( c( 1 )));
  }

  @Test
  public void simpleOp() {
    assertThat( eval(" 1050 - 50"), equalTo( c(1000) ) );
  }
  
  @Test
  public void unaryCall() {
    assertThat( eval("atan(1)"), equalTo( c(Math.atan(1)) ) );
  }

  @Test
  public void gamma() {
    assertThat( MathExt.gamma(3), closeTo(2, 0.0001));
  }

  
  @Test
  public void log() {
      assertThat( eval("log(2,16)"), equalTo( c(0.25) ) );
      assertThat( eval("log(16, 2)"), equalTo( c(4) ) );
      assertThat( eval("log(2,-1)"), equalTo( c(Double.NaN) ) );
      assertThat( eval("log(2,0.5)"), equalTo( c(-1) ) );

      assertThat( eval("log(2)").asReal(),  closeTo(0.6931472, 0.0000001) );//R rounds to 7th decimal place
  }

  @Test
  public void log10() {
      assertThat( eval("log10(4)").asReal(),  closeTo(0.60206, 0.00000001) );
      assertThat( eval("log10(100)"), equalTo( c(2) ) );
      assertThat( eval("log10(-4)"), equalTo( c(Double.NaN) ) );
  }

  @Test
  public void log2() {
      assertThat( eval("log2(9)").asReal(),  closeTo(3.169925, 0.00000001) );
      assertThat( eval("log2(8)"), equalTo( c(3) ) );
      assertThat( eval("log2(-4)"), equalTo( c(Double.NaN) ) );
  }
 
  @Test
  public void transpose() {
    try {
      topLevelContext.init();
    } catch (Exception e) {
    }
    assertThat(eval("t(matrix(c(1,2,3,4,5,6),3,2))"), equalTo(c(1, 4, 2, 5, 3, 6)));
  }
  
  @Test
  public void hyperbolicInverse(){
    assertThat(eval("asinh(3.14)").asReal(), closeTo(1.861813, 0.000001));
    assertThat(eval("acosh(3.14)").asReal(), closeTo(1.810991, 0.000001));
    assertThat(eval("atanh(0.25)").asReal(), closeTo(0.2554128, 0.000001));
  }
  
  @Test
  public void atan2(){
    assertThat(eval(".Internal(atan2(-0.5, -0.5))").asReal(), closeTo(-2.356194, 0.000001));
    assertThat(eval(".Internal(atan2(0.5, 0))").asReal(), closeTo(1.570796, 0.000001));
  }
  
  @Test 
  public void signif(){
    try{
      topLevelContext.init();
    }catch(Exception e){
      
    }
    assertThat(eval("signif(123.006, 1)").asReal(), closeTo(100.0,0.0000001));
    assertThat(eval("signif(123.006, 2)").asReal(), closeTo(120.0,0.0000001));
    assertThat(eval("signif(123.006, 3)").asReal(), closeTo(123.0,0.0000001));
    assertThat(eval("signif(123.006, 4)").asReal(), closeTo(123.0,0.0000001));
    assertThat(eval("signif(123.006, 5)").asReal(), closeTo(123.01,0.0000001));
    assertThat(eval("signif(123.006, 6)").asReal(), closeTo(123.006,0.0000001));
    assertThat(eval("signif(123.456, 4)").asReal(), closeTo(123.5,0.0000001));
  }
  
  @Test
  public void expm1() {
    assertThat(eval("expm1(1.0001)").asReal(), closeTo(1.718554, 0.000001));
    assertThat(eval("expm1(1:3)[2]").asReal(), closeTo(6.389056, 0.0000001));
  }

  @Test
  public void log1p() {
    assertThat(eval("log1p(expm1(1))").asReal(), closeTo(1.00, 0.0000001));
    assertThat(eval("log1p(1:3)[2]").asReal(), closeTo(1.0986123, 0.0000001));
  }

  @Test
  public void beta() {
    assertThat(eval(".Internal(beta(5,5))").asReal(), closeTo(0.001587302, 0.0000001));
    assertThat(eval(".Internal(beta(1:5, 1:5))[2]").asReal(), closeTo(0.166666667, 0.0000001));
  }

  @Test
  public void lbeta() {
    assertThat(eval(".Internal(lbeta(5,5))").asReal(), closeTo(-6.44572, 0.00001));
    assertThat(eval(".Internal(lbeta(1:5, 1:5))[2]").asReal(), closeTo(-1.791759, 0.00001));
  }
  
  @Test
  public void choose(){
    assertThat(eval(".Internal(choose(5,2))").asReal(), closeTo(10, 0.00001));
    assertThat(eval(".Internal(choose(10.2,5))").asReal(), closeTo(286.2495, 0.0001));
  }
  
  @Test
  public void lchoose(){
    assertThat(eval(".Internal(lchoose(5,2))").asReal(), closeTo(2.302585, 0.00001));
    assertThat(eval(".Internal(lchoose(10.2,5))").asReal(), closeTo(5.656864, 0.00001));
  }
  
  @Test
  public void sign() {
    assertThat(eval("sign(c(-33,55,0))"), equalTo(c(-1,1,0)));
  }
   
}
