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

import org.junit.Test;
import r.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

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
    assertThat(eval("t(matrix(c(1,2,3,4,5,6),3,2))"), equalTo(c(1, 3, 5, 2, 4, 6)));
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
}
