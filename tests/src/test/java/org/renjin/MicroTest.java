/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
// Auto-generated from micro-tests.in
package org.renjin;

import org.junit.Ignore;
import org.junit.Test;

public class MicroTest extends AbstractMicroTest {
  @Test
  public void micro2() {
    assertIdentical("{ (0+2i)^0 }", "1+0i");
  }
  @Test
  public void micro3() {
    assertIdentical("{ x<-c(1,2,3);x }", "c(1, 2, 3)");
  }
  @Test
  public void micro4() {
    assertIdentical("{ x<-c(1,2,3);x*2 }", "c(2, 4, 6)");
  }
  @Test
  public void micro5() {
    assertIdentical("{ x<-c(1,2,3);x+2 }", "c(3, 4, 5)");
  }
  @Test
  public void micro6() {
    assertIdentical("{ x<-c(1,2,3);x+FALSE }", "c(1, 2, 3)");
  }
  @Test
  public void micro7() {
    assertIdentical("{ x<-c(1,2,3);x+TRUE }", "c(2, 3, 4)");
  }
  @Test
  public void micro8() {
    assertIdentical("{ x<-c(1,2,3);x*x+x }", "c(2, 6, 12)");
  }
  @Test
  public void micro9() {
    assertIdentical("{ x<-c(1,2);y<-c(3,4,5,6);x+y }", "c(4, 6, 6, 8)");
  }
  @Test
  public void micro10() {
    assertIdentical("{ x<-c(1,2);y<-c(3,4,5,6);x*y }", "c(3, 8, 5, 12)");
  }
  @Test
  public void micro11() {
    assertIdentical("{ x<-c(1,2);z<-c();x==z }", "logical(0)");
  }
  @Test
  public void micro12() {
    assertIdentical("{ x<-1+NA; c(1,2,3,4)+c(x,10) }", "c(NA, 12, NA, 14)");
  }
  @Test
  public void micro13() {
    assertIdentical("{ c(1L,2L,3L)+TRUE }", "2:4");
  }
  @Test
  public void micro14() {
    assertIdentical("{ c(1L,2L,3L)*c(10L) }", "c(10L, 20L, 30L)");
  }
  @Test
  public void micro15() {
    assertIdentical("{ c(1L,2L,3L)*c(10,11,12) }", "c(10, 22, 36)");
  }
  @Test
  public void micro16() {
    assertIdentical("{ c(1L,2L,3L,4L)-c(TRUE,FALSE) }", "c(0L, 2L, 2L, 4L)");
  }
  @Test
  public void micro17() {
    assertIdentical("{ ia<-c(1L,2L);ib<-c(3L,4L);d<-c(5,6);ia+ib+d }", "c(9, 12)");
  }
  @Test
  public void micro18() {
    assertIdentical("{ z <- c(-1.5-1i,10) ; (z * z)[1] }", "1.25+3i");
  }
  @Test
  public void micro19() {
    assertIdentical("{ c(1,2,3+1i)^3 }", "c(1+0i, 8+0i, 18+26i)");
  }
  @Test
  public void micro20() {
    assertIdentical("{ round( 3^c(1,2,3+1i), digits=5 ) }", "c(3+0i, 9+0i, 12.28048+24.04558i)");
  }
  @Test
  public void micro21() {
    assertIdentical("{ 1L + 1:2 }", "2:3");
  }
  @Test
  public void micro22() {
    assertIdentical("{ 4:3 + 2L }", "c(6L, 5L)");
  }
  @Test
  public void micro23() {
    assertIdentical("{ 1:2 + 3:4 }", "c(4L, 6L)");
  }
  @Test
  public void micro24() {
    assertIdentical("{ 1:2 + c(1L, 2L) }", "c(2L, 4L)");
  }
  @Test
  public void micro25() {
    assertIdentical("{ c(1L, 2L) + 1:4 }", "c(2L, 4L, 4L, 6L)");
  }
  @Test
  public void micro26() {
    assertIdentical("{ 1:4 + c(1L, 2L) }", "c(2L, 4L, 4L, 6L)");
  }
  @Test
  public void micro27() {
    assertIdentical("{ 2L + 1:2 }", "3:4");
  }
  @Test
  public void micro28() {
    assertIdentical("{ 1:2 + 2L }", "3:4");
  }
  @Test
  public void micro29() {
    assertIdentical("{ c(1L, 2L) + 2L }", "3:4");
  }
  @Test
  public void micro30() {
    assertIdentical("{ 2L + c(1L, 2L) }", "3:4");
  }
  @Test
  public void micro31() {
    assertIdentical("{ 1 + 1:2 }", "c(2, 3)");
  }
  @Test
  public void micro32() {
    assertIdentical("{ c(1,2) + 1:2 }", "c(2, 4)");
  }
  @Test
  public void micro33() {
    assertIdentical("{ c(1,2,3,4) + 1:2 }", "c(2, 4, 4, 6)");
  }
  @Test
  public void micro34() {
    assertIdentical("{ c(1,2,3,4) + c(1L,2L) }", "c(2, 4, 4, 6)");
  }
  @Test
  public void micro35() {
    assertIdentical("{ 1:2 + 1 }", "c(2, 3)");
  }
  @Test
  public void micro36() {
    assertIdentical("{ 1:2 + c(1,2) }", "c(2, 4)");
  }
  @Test
  public void micro37() {
    assertIdentical("{ 1:2 + c(1,2,3,4) }", "c(2, 4, 4, 6)");
  }
  @Test
  public void micro38() {
    assertIdentical("{ c(1L,2L) + c(1,2,3,4) }", "c(2, 4, 4, 6)");
  }
  @Test
  public void micro39() {
    assertIdentical("{ 1L + c(1,2) }", "c(2, 3)");
  }
  @Test
  public void micro40() {
    assertIdentical("{ a <- c(1,3) ; b <- c(2,4) ; a ^ b }", "c(1, 81)");
  }
  @Test
  public void micro41() {
    assertIdentical("{ a <- c(1,3) ; a ^ 3 }", "c(1, 27)");
  }
  @Test
  public void micro42() {
    assertIdentical("{ a <- c(1+1i,3+2i) ; a - (4+3i) }", "c(-3-2i, -1-1i)");
  }
  @Test
  public void micro43() {
    assertIdentical("{ c(1,3) - 4 }", "c(-3, -1)");
  }
  @Test
  public void micro44() {
    assertIdentical("{ c(1+1i,3+2i) * c(1,2) }", "c(1+1i, 6+4i)");
  }
  @Test
  public void micro45() {
    assertIdentical("{ z <- c(1+1i,3+2i) ; z * c(1,2) }", "c(1+1i, 6+4i)");
  }
  @Test
  public void micro46() {
    assertIdentical("{ round(c(1+1i,2+3i)^c(1+1i,3+4i), digits = 5) }", "c(0.27396+0.5837i, -0.20455+0.89662i)");
  }
  @Test
  public void micro47() {
    assertIdentical("{ c(1+1i,3+2i) / 2 }", "c(0.5+0.5i, 1.5+1i)");
  }
  @Test
  public void micro48() {
    assertIdentical("{ c(1,3) / c(2,4) }", "c(0.5, 0.75)");
  }
  @Test
  public void micro49() {
    assertIdentical("{ c(1,3) %/% c(2,4) }", "c(0, 0)");
  }
  @Test
  public void micro50() {
    assertIdentical("{ integer()+1 }", "numeric(0)");
  }
  @Test
  public void micro51() {
    assertIdentical("{ 1+integer() }", "numeric(0)");
  }
  @Test
  public void micro52() {
    assertIdentical("{ 1:2+1:3 }", "c(2L, 4L, 4L)");
  }
  @Test
  public void micro53() {
    assertIdentical("{ 1:3*1:2 }", "c(1L, 4L, 3L)");
  }
  @Test
  public void micro54() {
    assertIdentical("{ 1:3+c(1,2+2i) }", "c(2+0i, 4+2i, 4+0i)");
  }
  @Test
  public void micro55() {
    assertIdentical("{ c(1,2+2i)+1:3 }", "c(2+0i, 4+2i, 4+0i)");
  }
  @Test
  public void micro56() {
    assertIdentical("{ NA+1:3 }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro57() {
    assertIdentical("{ 1:3+NA }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro58() {
    assertIdentical("{ NA+c(1L, 2L, 3L) }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro59() {
    assertIdentical("{ c(1L, 2L, 3L)+NA }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro60() {
    assertIdentical("{ c(NA,NA,NA)+1:3 }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro61() {
    assertIdentical("{ 1:3+c(NA, NA, NA) }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro62() {
    assertIdentical("{ c(NA,NA,NA)+c(1L,2L,3L) }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro63() {
    assertIdentical("{ c(1L,2L,3L)+c(NA, NA, NA) }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro64() {
    assertIdentical("{ c(NA,NA)+1:4 }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro65() {
    assertIdentical("{ 1:4+c(NA, NA) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro66() {
    assertIdentical("{ c(NA,NA,NA,NA)+1:2 }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro67() {
    assertIdentical("{ 1:2+c(NA,NA,NA,NA) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro68() {
    assertIdentical("{ c(NA,NA)+c(1L,2L,3L,4L) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro69() {
    assertIdentical("{ c(1L,2L,3L,4L)+c(NA, NA) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro70() {
    assertIdentical("{ c(NA,NA,NA,NA)+c(1L,2L) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro71() {
    assertIdentical("{ c(1L,2L)+c(NA,NA,NA,NA) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro72() {
    assertIdentical("{ c(1L,NA)+1 }", "c(2, NA)");
  }
  @Test
  public void micro73() {
    assertIdentical("{ c(1L,NA) + c(2,3) }", "c(3, NA)");
  }
  @Test
  public void micro74() {
    assertIdentical("{ c(2,3) + c(1L,NA) }", "c(3, NA)");
  }
  @Test
  public void micro75() {
    assertIdentical("{ 1:4+c(1,2) }", "c(2, 4, 4, 6)");
  }
  @Test
  public void micro76() {
    assertIdentical("{ c(1,2)+1:4 }", "c(2, 4, 4, 6)");
  }
  @Test
  public void micro77() {
    assertIdentical("{ 1:4+c(1,2+2i) }", "c(2+0i, 4+2i, 4+0i, 6+2i)");
  }
  @Test
  public void micro78() {
    assertIdentical("{ c(1,2+2i)+1:4 }", "c(2+0i, 4+2i, 4+0i, 6+2i)");
  }
  @Test
  public void micro79() {
    assertIdentical("{ c(3,4) %% 2 }", "c(1, 0)");
  }
  @Test
  public void micro80() {
    assertIdentical("{ c(3,4) %% c(2,5) }", "c(1, 4)");
  }
  @Test
  public void micro81() {
    assertIdentical("{ c(3,4) %/% 2 }", "c(1, 2)");
  }
  @Test
  public void micro82() {
    assertIdentical("{ 3L %/% 2L }", "1L");
  }
  @Test
  public void micro83() {
    assertIdentical("{ 3L %/% 0L }", "NA_integer_");
  }
  @Test
  public void micro84() {
    assertIdentical("{ ((1+0i)/(0+0i)) ^ (-3) }", "0+0i");
  }
  @Test
  public void micro85() {
    assertIdentical("{ ((1+1i)/(0+0i)) }", "complex(real=Inf, i=Inf)");
  }
  @Test
  public void micro86() {
    assertIdentical("{ ((1+1i)/(0+0i)) ^ (-3) }", "0+0i");
  }
  @Test
  public void micro87() {
    assertIdentical("{ round( ((1+1i)/(0+1i)) ^ (-3.54), digits=5) }", "-0.27428+0.10364i");
  }
  @Test
  public void micro88() {
    assertIdentical("{ 0/0 - 4i }", "complex(re=NaN, im=-4)");
  }
  @Test
  public void micro89() {
    assertIdentical("{ 4i + 0/0 }", "complex(re=NaN,im=4)");
  }
  @Test
  public void micro90() {
    assertIdentical("{ a <- 1 + 2i; b <- 0/0 - 4i; a + b }", "complex(re=NaN,im=-2)");
  }
  @Test
  public void micro91() {
    assertIdentical("{ 1L+1 }", "2");
  }
  @Test
  public void micro92() {
    assertIdentical("{ 1L+1L }", "2L");
  }
  @Test
  public void micro93() {
    assertIdentical("{ (1+1)*(3+2) }", "10");
  }
  @Test
  public void micro94() {
    assertIdentical("{ 1000000000*100000000000 }", "1e+20");
  }
  @Test
  public void micro95() {
    assertIdentical("{ 1000000000L*1000000000L }", "NA_integer_");
  }
  @Test
  public void micro96() {
    assertIdentical("{ 1000000000L*1000000000 }", "1e+18");
  }
  @Test
  public void micro97() {
    assertIdentical("{ 1+TRUE }", "2");
  }
  @Test
  public void micro98() {
    assertIdentical("{ 1L+TRUE }", "2L");
  }
  @Test
  public void micro99() {
    assertIdentical("{ 1+FALSE<=0 }", "FALSE");
  }
  @Test
  public void micro100() {
    assertIdentical("{ 1L+FALSE<=0 }", "FALSE");
  }
  @Test
  public void micro101() {
    assertIdentical("{ TRUE+TRUE+TRUE*TRUE+FALSE+4 }", "7");
  }
  @Test
  public void micro102() {
    assertIdentical("{ 1L*NA }", "NA_integer_");
  }
  @Test
  public void micro103() {
    assertIdentical("{ 1+NA }", "NA_real_");
  }
  @Test
  public void micro104() {
    assertIdentical("{ 2L^10L }", "1024");
  }
  @Test
  public void micro105() {
    assertIdentical("{ 3 %/% 2 }", "1");
  }
  @Test
  public void micro106() {
    assertIdentical("{ 3L %/% 2L }", "1L");
  }
  @Test
  public void micro107() {
    assertIdentical("{ 3L %/% -2L }", "-2L");
  }
  @Test
  public void micro108() {
    assertIdentical("{ 3 %/% -2 }", "-2");
  }
  @Test
  public void micro109() {
    assertIdentical("{ 3 %/% 0 }", "Inf");
  }
  @Test
  public void micro110() {
    assertIdentical("{ 3L %/% 0L }", "NA_integer_");
  }
  @Test
  public void micro111() {
    assertIdentical("{ 3 %% 2 }", "1");
  }
  @Test
  public void micro112() {
    assertIdentical("{ 3L %% 2L }", "1L");
  }
  @Test
  public void micro113() {
    assertIdentical("{ 3L %% -2L }", "-1L");
  }
  @Test
  public void micro114() {
    assertIdentical("{ 3 %% -2 }", "-1");
  }
  @Test
  public void micro115() {
    assertIdentical("{ is.nan(3 %% 0) }", "TRUE");
  }
  @Test
  public void micro116() {
    assertIdentical("{ 3L %% 0L }", "NA_integer_");
  }
  @Test
  public void micro117() {
    assertIdentical("{ 0x10 + 0x10L + 1.28 }", "33.28");
  }
  @Test
  public void micro118() {
    assertIdentical("{ 1/0 }", "Inf");
  }
  @Test
  public void micro119() {
    assertIdentical("{ (1+2i)*(3+4i) }", "-5+10i");
  }
  @Test
  public void micro120() {
    assertIdentical("{ x <- 1+2i; y <- 3+4i; x*y }", "-5+10i");
  }
  @Test
  public void micro121() {
    assertIdentical("{ x <- 1+2i; y <- 3+4i; x/y }", "0.44+0.08i");
  }
  @Test
  public void micro122() {
    assertIdentical("{ x <- 1+2i; y <- 3+4i; x-y }", "-2-2i");
  }
  @Test
  public void micro123() {
    assertIdentical("{ x <- 1+2i; y <- 3+4i; identical(round(x*x*y/(x+y), digits=5), -1.92308+2.88462i) }", "TRUE");
  }
  @Test
  public void micro124() {
    assertIdentical("{ x <- c(-1.5-1i,-1.3-1i) ; y <- c(0+0i, 0+0i) ; y*y+x }", "c(-1.5-1i, -1.3-1i)");
  }
  @Test
  public void micro125() {
    assertIdentical("{ x <- c(-1.5-1i,-1.3-1i) ; y <- c(0+0i, 0+0i) ; y-x }", "c(1.5+1i, 1.3+1i)");
  }
  @Test
  public void micro126() {
    assertIdentical("{ x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; y-x }", "c(4+3i, -7-5i)");
  }
  @Test
  public void micro127() {
    assertIdentical("{ x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; y+x }", "c(2-1i, -1+15i)");
  }
  @Test
  public void micro128() {
    assertIdentical("{ x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; y*x }", "c(-1-7i, -62-25i)");
  }
  @Test
  public void micro129() {
    assertIdentical("{ x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; round(y/x, digits=5) }", "c(-1+1i, 0.34862+0.50459i)");
  }
  @Test
  public void micro130() {
    assertIdentical("{ round( (1+2i)^(3+4i), digits=5 ) }", "0.12901+0.03392i");
  }
  @Test
  public void micro131() {
    assertIdentical("{ (1+2i)^2 }", "-3+4i");
  }
  @Test
  public void micro132() {
    assertIdentical("{ (1+2i)^(-2) }", "-0.12-0.16i");
  }
  @Test
  public void micro133() {
    assertIdentical("{ (1+2i)^0 }", "1+0i");
  }
  @Test
  public void micro134() {
    assertIdentical("{ 0^(-1+1i) }", "complex(real=NaN, i=NaN)");
  }
  @Test
  public void micro135() {
    assertIdentical("{ (0+0i)/(0+0i) }", "complex(real=NaN, i=NaN)");
  }
  @Test
  public void micro136() {
    assertIdentical("{ (1+0i)/(0+0i) }", "complex(real=Inf, i=NaN)");
  }
  @Test
  public void micro137() {
    assertIdentical("{ (0+1i)/(0+0i) }", "complex(real=NaN, i=Inf)");
  }
  @Test
  public void micro138() {
    assertIdentical("{ (1+1i)/(0+0i) }", "complex(real=Inf, i=Inf)");
  }
  @Test
  public void micro139() {
    assertIdentical("{ (-1+0i)/(0+0i) }", "complex(real=-Inf, i=NaN)");
  }
  @Test
  public void micro140() {
    assertIdentical("{ (-1-1i)/(0+0i) }", "complex(real=-Inf, i=-Inf)");
  }
  @Test
  public void micro141() {
    assertIdentical("{ ((0+1i)/0) * ((0+1i)/0) }", "complex(real=-Inf, i=NaN)");
  }
  @Test
  public void micro142() {
    assertIdentical("{ ((0-1i)/0) * ((0+1i)/0) }", "complex(real=Inf, i=NaN)");
  }
  @Test
  public void micro143() {
    assertIdentical("{ ((0-1i)/0) * ((0-1i)/0) }", "complex(real=-Inf, i=NaN)");
  }
  @Test
  public void micro144() {
    assertIdentical("{ ((0-1i)/0) * ((1-1i)/0) }", "complex(real=-Inf, i=-Inf)");
  }
  @Test
  public void micro145() {
    assertIdentical("{ ((0-1i)/0) * ((-1-1i)/0) }", "complex(real=-Inf, i=Inf)");
  }
  @Test
  public void micro146() {
    assertIdentical("{ 1/((1+0i)/(0+0i)) }", "0+0i");
  }
  @Test
  public void micro147() {
    assertIdentical("{ (1+2i) / ((0-0i)/(0+0i)) }", "complex(real=NaN, i=NaN)");
  }
  @Test
  public void micro148() {
    assertIdentical("{ 1^(1/0) }", "1");
  }
  @Test
  public void micro149() {
    assertIdentical("{ (-2)^(1/0) }", "NaN");
  }
  @Test
  public void micro150() {
    assertIdentical("{ (-2)^(-1/0) }", "NaN");
  }
  @Test
  public void micro151() {
    assertIdentical("{ (1)^(-1/0) }", "1");
  }
  @Test
  public void micro152() {
    assertIdentical("{ 0^(-1/0) }", "Inf");
  }
  @Test
  public void micro153() {
    assertIdentical("{ 0^(1/0) }", "0");
  }
  @Test
  public void micro154() {
    assertIdentical("{ 0^(0/0) }", "NaN");
  }
  @Test
  public void micro155() {
    assertIdentical("{ 1^(0/0) }", "1");
  }
  @Test
  public void micro156() {
    assertIdentical("{ (-1)^(0/0) }", "NaN");
  }
  @Test
  public void micro157() {
    assertIdentical("{ (-1/0)^(0/0) }", "NaN");
  }
  @Test
  public void micro158() {
    assertIdentical("{ (1/0)^(0/0) }", "NaN");
  }
  @Test
  public void micro159() {
    assertIdentical("{ (0/0)^(1/0) }", "NaN");
  }
  @Test
  public void micro160() {
    assertIdentical("{ (-1/0)^3 }", "-Inf");
  }
  @Test
  public void micro161() {
    assertIdentical("{ (1/0)^(-4) }", "0");
  }
  @Test
  public void micro162() {
    assertIdentical("{ (-1/0)^(-4) }", "0");
  }
  @Test
  public void micro163() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1+2i, 3+4i) ; f(1, 2) }", "3");
  }
  @Test
  public void micro164() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(2, 3+4i) ; f(1, 2) }", "3");
  }
  @Test
  public void micro165() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1+2i, 3) ; f(1, 2) }", "3");
  }
  @Test
  public void micro166() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(2, 3+4i) ; f(1, 2) }", "3");
  }
  @Test
  public void micro167() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1+2i, 3) ; f(1, 2) }", "3");
  }
  @Test
  public void micro168() {
    assertIdentical("{ 1L / 2L }", "0.5");
  }
  @Test
  public void micro169() {
    assertIdentical("{ f <- function(a, b) { a / b } ; f(1L, 2L) ; f(1, 2) }", "0.5");
  }
  @Test
  public void micro170() {
    assertIdentical("{ (1:2)[3] / 2L }", "NA_real_");
  }
  @Test
  public void micro171() {
    assertIdentical("{ 2L / (1:2)[3] }", "NA_real_");
  }
  @Test
  public void micro172() {
    assertIdentical("{ a <- (1:2)[3] ; b <- 2L ; a / b }", "NA_real_");
  }
  @Test
  public void micro173() {
    assertIdentical("{ a <- 2L ; b <- (1:2)[3] ; a / b }", "NA_real_");
  }
  @Test
  public void micro174() {
    assertIdentical("{ (1:2)[3] + 2L }", "NA_integer_");
  }
  @Test
  public void micro175() {
    assertIdentical("{ 2L + (1:2)[3] }", "NA_integer_");
  }
  @Test
  public void micro176() {
    assertIdentical("{ a <- (1:2)[3] ; b <- 2L ; a + b }", "NA_integer_");
  }
  @Test
  public void micro177() {
    assertIdentical("{ a <- 2L ; b <- (1:2)[3] ; a + b }", "NA_integer_");
  }
  @Test
  public void micro178() {
    assertIdentical("{ a <- (1:2)[3] ; b <- 2 ; a + b }", "NA_real_");
  }
  @Test
  public void micro179() {
    assertIdentical("{ a <- 2 ; b <- (1:2)[3] ; a + b }", "NA_real_");
  }
  @Test
  public void micro180() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(c(1,2), c(3,4)) ; f(c(1,2), 3:4) }", "c(4, 6)");
  }
  @Test
  public void micro181() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1:2, c(3,4)) ; f(c(1,2), 3:4) }", "c(4, 6)");
  }
  @Test
  public void micro182() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1:2, 3:4) ; f(c(1,2), 3:4) }", "c(4, 6)");
  }
  @Test
  public void micro183() {
    assertIdentical("{ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2L,4) }", "0.5");
  }
  @Test
  public void micro184() {
    assertIdentical("{ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2L,4L) }", "0.5");
  }
  @Test
  public void micro185() {
    assertIdentical("{ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2L,(1:2)[3]) }", "NA_real_");
  }
  @Test
  public void micro186() {
    assertIdentical("{ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f((1:2)[3], 2L) }", "NA_real_");
  }
  @Test
  public void micro187() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f(2L,4) }", "6");
  }
  @Test
  public void micro188() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f(2L,4L) }", "6L");
  }
  @Test
  public void micro189() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f(2L,(1:2)[3]) }", "NA_integer_");
  }
  @Test
  public void micro190() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f((1:2)[3], 2L) }", "NA_integer_");
  }
  @Test
  public void micro191() {
    assertIdentical("{ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2,(1:2)[3]) }", "NA_real_");
  }
  @Test
  public void micro192() {
    assertIdentical("{ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f((1:2)[3],2) }", "NA_real_");
  }
  @Test
  public void micro193() {
    assertIdentical("{ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2+1i,(1:2)[3]) }", "NA_complex_");
  }
  @Test
  public void micro194() {
    assertIdentical("{ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1+2i) ; f(TRUE, 2) }", "3");
  }
  @Test
  public void micro195() {
    assertIdentical("{ f <- function(b) { 1 / b } ; f(1) ; f(1L) ; f(4) }", "0.25");
  }
  @Test
  public void micro196() {
    assertIdentical("{ f <- function(b) { 1 / b } ; f(1+1i) ; f(1L) }", "1");
  }
  @Test
  public void micro197() {
    assertIdentical("{ f <- function(b) { 1 / b } ; f(1) ; f(1L) }", "1");
  }
  @Test
  public void micro198() {
    assertIdentical("{ f <- function(b) { 1 / b } ; f(1L) ; f(1) }", "1");
  }
  @Test
  public void micro199() {
    assertIdentical("{ f <- function(b) { 1 / b } ; f(TRUE) ; f(1L) }", "1");
  }
  @Test
  public void micro200() {
    assertIdentical("{ f <- function(b) { 1i / b } ; f(1) ; f(1L) ; f(4) }", "0+0.25i");
  }
  @Test
  public void micro201() {
    assertIdentical("{ f <- function(b) { 1i / b } ; f(1+1i) ; f(1L) }", "0+1i");
  }
  @Test
  public void micro202() {
    assertIdentical("{ f <- function(b) { 1i / b } ; f(1) ; f(1L) }", "0+1i");
  }
  @Test
  public void micro203() {
    assertIdentical("{ f <- function(b) { 1i / b } ; f(TRUE) ; f(1L) }", "0+1i");
  }
  @Test
  public void micro204() {
    assertIdentical("{ f <- function(b) { b / 1 } ; f(1) ; f(1L) ; f(4) }", "4");
  }
  @Test
  public void micro205() {
    assertIdentical("{ f <- function(b) { b / 2 } ; f(1+1i) ; f(1L) }", "0.5");
  }
  @Test
  public void micro206() {
    assertIdentical("{ f <- function(b) { b / 2 } ; f(1) ; f(1L) }", "0.5");
  }
  @Test
  public void micro207() {
    assertIdentical("{ f <- function(b) { b / 4 } ; f(1L) ; f(1) }", "0.25");
  }
  @Test
  public void micro208() {
    assertIdentical("{ f <- function(b) { b / 4i } ; f(1) ; f(1L) }", "0-0.25i");
  }
  @Test
  public void micro209() {
    assertIdentical("{ f <- function(b) { 4L / b } ; f(1L) ; f(2) }", "2");
  }
  @Test
  public void micro210() {
    assertIdentical("{ f <- function(b) { 4L + b } ; f(1L) ; f(2) }", "6");
  }
  @Test
  public void micro211() {
    assertIdentical("{ f <- function(b) { b / 2L } ; f(1L) ; f(2) }", "1");
  }
  @Test
  public void micro212() {
    assertIdentical("{ f <- function(b) { 4L / b } ; f(1L) ; f(2) ; f(TRUE) }", "4");
  }
  @Test
  public void micro213() {
    assertIdentical("{ f <- function(b) { 4L + b } ; f(1L) ; f(2) ; f(TRUE) }", "5L");
  }
  @Test
  public void micro214() {
    assertIdentical("{ f <- function(b) { 4L + b } ; f(1L) ; f(2) ; f((1:2)[3]) }", "NA_integer_");
  }
  @Test
  public void micro215() {
    assertIdentical("{ f <- function(b) { 4L / b } ; f(1L) ; f(2) ; f((1:2)[3]) }", "NA_real_");
  }
  @Test
  public void micro216() {
    assertIdentical("{ f <- function(b) { (1:2)[3] + b } ; f(1L) ; f(2) }", "NA_real_");
  }
  @Test
  public void micro217() {
    assertIdentical("{ f <- function(b) { (1:2)[3] + b } ; f(1) ; f(2L) }", "NA_integer_");
  }
  @Test
  public void micro218() {
    assertIdentical("{ f <- function(b) { b + 4L } ; f(1L) ; f(2) ; f(TRUE) }", "5L");
  }
  @Test
  public void micro219() {
    assertIdentical("{ f <- function(b) { b + 4L } ; f(1L) ; f(2) ; f((1:2)[3]) }", "NA_integer_");
  }
  @Test
  public void micro220() {
    assertIdentical("{ f <- function(b) { b / 4L } ; f(1L) ; f(2) ; f(TRUE) }", "0.25");
  }
  @Test
  public void micro221() {
    assertIdentical("{ f <- function(b) { b / 4L } ; f(1L) ; f(2) ; f((1:2)[3]) }", "NA_real_");
  }
  @Test
  public void micro222() {
    assertIdentical("{ f <- function(b) { 1 + b } ; f(1L) ; f(TRUE) }", "2");
  }
  @Test
  public void micro223() {
    assertIdentical("{ f <- function(b) { FALSE + b } ; f(1L) ; f(2) }", "2");
  }
  @Test
  public void micro224() {
    assertIdentical("{ f <- function(b) { b + 1 } ; f(1L) ; f(TRUE) }", "2");
  }
  @Test
  public void micro225() {
    assertIdentical("{ f <- function(b) { b + FALSE } ; f(1L) ; f(2) }", "2");
  }
  @Test
  public void micro226() {
    assertIdentical("{ !TRUE }", "FALSE");
  }
  @Test
  public void micro227() {
    assertIdentical("{ !FALSE }", "TRUE");
  }
  @Test
  public void micro228() {
    assertIdentical("{ !NA }", "NA");
  }
  @Test
  public void micro229() {
    assertIdentical("{ !c(TRUE,TRUE,FALSE,NA) }", "c(FALSE, FALSE, TRUE, NA)");
  }
  @Test
  public void micro230() {
    assertIdentical("{ !c(1,2,3,4,0,0,NA) }", "c(FALSE, FALSE, FALSE, FALSE, TRUE, TRUE, NA)");
  }
  @Test
  public void micro231() {
    assertIdentical("{ !((0-3):3) }", "c(FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE)");
  }
  @Test
  public void micro232() {
    assertIdentical("{ !as.raw(1:3) }", "as.raw(c(0xfe, 0xfd, 0xfc))");
  }
  @Test
  public void micro233() {
    assertIdentical("{ a <- as.raw(201) ; !a }", "as.raw(0x36)");
  }
  @Test
  public void micro234() {
    assertIdentical("{ a <- as.raw(12) ; !a }", "as.raw(0xf3)");
  }
  @Test
  public void micro235() {
    assertIdentical("{ l <- list(); !l }", "logical(0)");
  }
  @Test
  public void micro236() {
    assertIdentical("{ f <- function(arg) { !arg } ; f(as.raw(10)) ; f(as.raw(c(a=1,b=2))) }", "as.raw(c(0xfe, 0xfd))");
  }
  @Test
  public void micro239() {
    assertIdentical("{ -(0/0) }", "NaN");
  }
  @Test
  public void micro240() {
    assertIdentical("{ -(1/0) }", "-Inf");
  }
  @Test
  public void micro241() {
    assertIdentical("{ -(1[2]) }", "NA_real_");
  }
  @Test
  public void micro242() {
    assertIdentical("{ -(2+1i) }", "-2-1i");
  }
  @Test
  public void micro243() {
    assertIdentical("{ -((0+1i)/0) }", "complex(real=NaN, i=-Inf)");
  }
  @Test
  public void micro244() {
    assertIdentical("{ -((1+0i)/0) }", "complex(real=-Inf, i=NaN)");
  }
  @Test
  public void micro245() {
    assertIdentical("{ -c((1+0i)/0,2) }", "c(complex(real=-Inf, i=NaN), -2+0i)");
  }
  @Test
  public void micro246() {
    assertIdentical("{ f <- function(z) { -z } ; f(1+1i) ; f(1L) }", "-1L");
  }
  @Test
  public void micro247() {
    assertIdentical("{ f <- function(z) { -z } ; f(TRUE) ; f(1L) }", "-1L");
  }
  @Test
  public void micro248() {
    assertIdentical("{ f <- function(z) { -z } ; f(1L) ; f(1) }", "-1");
  }
  @Test
  public void micro249() {
    assertIdentical("{ f <- function(z) { -z } ; f(1) ; f(1L) }", "-1L");
  }
  @Test
  public void micro250() {
    assertIdentical("{ f <- function(z) { -z } ; f(1L) ; f(1+1i) }", "-1-1i");
  }
  @Test
  public void micro251() {
    assertIdentical("{ f <- function(z) { -z } ; f(1L) ; f(TRUE) }", "-1L");
  }
  @Test
  public void micro252() {
    assertIdentical("{ f <- function(z) { -z } ; f(1:3) ; f(1L) }", "-1L");
  }
  @Test
  public void micro253() {
    assertIdentical("{ f <- function(z) { -z } ; f(1:3) ; f(TRUE) }", "-1L");
  }
  @Test
  public void micro254() {
    assertIdentical("{ f <- function(z) { -z } ; f(1:3) ; f(c((0+0i)/0,1+1i)) }", "c(complex(real=NaN, i=NaN), -1-1i)");
  }
  @Test
  public void micro259() {
    assertIdentical("{ x <- 1:3 %*% 9:11 ; x[1] }", "62");
  }
  @Test
  public void micro278() {
    assertIdentical("{ 1.1 || 3.15 }", "TRUE");
  }
  @Test
  public void micro279() {
    assertIdentical("{ 0 || 0 }", "FALSE");
  }
  @Test
  public void micro280() {
    assertIdentical("{ 1 || 0 }", "TRUE");
  }
  @Test
  public void micro281() {
    assertIdentical("{ NA || 1 }", "TRUE");
  }
  @Test
  public void micro282() {
    assertIdentical("{ NA || 0 }", "NA");
  }
  @Test
  public void micro283() {
    assertIdentical("{ 0 || NA }", "NA");
  }
  @Test
  public void micro284() {
    assertIdentical("{ x <- 1 ; f <- function(r) { x <<- 2; r } ; NA || f(NA) ; x }", "2");
  }
  @Test
  public void micro286() {
    assertIdentical("{ TRUE && FALSE }", "FALSE");
  }
  @Test
  public void micro287() {
    assertIdentical("{ FALSE && FALSE }", "FALSE");
  }
  @Test
  public void micro288() {
    assertIdentical("{ FALSE && TRUE }", "FALSE");
  }
  @Test
  public void micro289() {
    assertIdentical("{ TRUE && TRUE }", "TRUE");
  }
  @Test
  public void micro290() {
    assertIdentical("{ TRUE && NA }", "NA");
  }
  @Test
  public void micro291() {
    assertIdentical("{ FALSE && NA }", "FALSE");
  }
  @Test
  public void micro292() {
    assertIdentical("{ NA && TRUE }", "NA");
  }
  @Test
  public void micro293() {
    assertIdentical("{ NA && FALSE }", "FALSE");
  }
  @Test
  public void micro294() {
    assertIdentical("{ NA && NA }", "NA");
  }
  @Test
  public void micro297() {
    assertIdentical("{ f <- function(a,b) { a || b } ; f(1,2) ; f(1,2) ; f(1L,2L) }", "TRUE");
  }
  @Test
  public void micro298() {
    assertIdentical("{ f <- function(a,b) { a || b } ; f(1L,2L) ; f(1L,2L) ; f(0,FALSE) }", "FALSE");
  }
  @Test
  public void micro299() {
    assertIdentical("{ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), TRUE) }", "TRUE");
  }
  @Test
  public void micro300() {
    assertIdentical("{ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) }", "NA");
  }
  @Test
  public void micro301() {
    assertIdentical("{ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(1,2) }", "TRUE");
  }
  @Test
  public void micro302() {
    assertIdentical("{ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(double(),2) }", "NA");
  }
  @Test
  public void micro303() {
    assertIdentical("{ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(integer(),2) }", "NA");
  }
  @Test
  public void micro304() {
    assertIdentical("{ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(2+3i,1/0) }", "TRUE");
  }
  @Test
  public void micro305() {
    assertIdentical("{ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(2+3i,logical()) }", "NA");
  }
  @Test
  public void micro306() {
    assertIdentical("{ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(1,2) ; f(logical(),4) }", "NA");
  }
  @Test
  public void micro307() {
    assertIdentical("{ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(TRUE, c(TRUE,TRUE,FALSE)) ; f(1,2) }", "TRUE");
  }
  @Test
  public void micro308() {
    assertIdentical("{ FALSE && \"hello\" }", "FALSE");
  }
  @Test
  public void micro309() {
    assertIdentical("{ TRUE || \"hello\" }", "TRUE");
  }
  @Test
  public void micro310() {
    assertIdentical("{ c(TRUE,FALSE) | logical() }", "logical(0)");
  }
  @Test
  public void micro311() {
    assertIdentical("{ logical() | c(TRUE,FALSE) }", "logical(0)");
  }
  @Test
  public void micro312() {
    assertIdentical("{ as.raw(c(1,4)) | raw() }", "raw(0)");
  }
  @Test
  public void micro314() {
    assertIdentical("{ as.raw(c(1,4)) | as.raw(c(1,5,4)) }", "as.raw(c(0x01, 0x05, 0x05))");
  }
  @Test
  public void micro315() {
    assertIdentical("{ as.raw(c(1,5,4)) | as.raw(c(1,4)) }", "as.raw(c(0x01, 0x05, 0x05))");
  }
  @Test
  public void micro316() {
    assertIdentical("{ c(TRUE, FALSE, FALSE) & c(TRUE,TRUE) }", "c(TRUE, FALSE, FALSE)");
  }
  @Test
  public void micro317() {
    assertIdentical("{ c(TRUE, TRUE) & c(TRUE, FALSE, FALSE) }", "c(TRUE, FALSE, FALSE)");
  }
  @Test
  public void micro318() {
    assertIdentical("{ c(a=TRUE, TRUE) | c(TRUE, b=FALSE, FALSE) }", "structure(c(TRUE, TRUE, TRUE), .Names = c(\"\", \"b\", \"\"))");
  }
  @Test
  public void micro319() {
    assertIdentical("{ 1.1 | 3.15 }", "TRUE");
  }
  @Test
  public void micro320() {
    assertIdentical("{ 0 | 0 }", "FALSE");
  }
  @Test
  public void micro321() {
    assertIdentical("{ 1 | 0 }", "TRUE");
  }
  @Test
  public void micro322() {
    assertIdentical("{ NA | 1 }", "TRUE");
  }
  @Test
  public void micro323() {
    assertIdentical("{ NA | 0 }", "NA");
  }
  @Test
  public void micro324() {
    assertIdentical("{ 0 | NA }", "NA");
  }
  @Test
  public void micro325() {
    assertIdentical("{ x <- 1 ; f <- function(r) { x <<- 2; r } ; NA | f(NA) ; x }", "2");
  }
  @Test
  public void micro326() {
    assertIdentical("{ x <- 1 ; f <- function(r) { x <<- 2; r } ; TRUE | f(FALSE) ; x }", "2");
  }
  @Test
  public void micro327() {
    assertIdentical("{ TRUE & FALSE }", "FALSE");
  }
  @Test
  public void micro328() {
    assertIdentical("{ FALSE & FALSE }", "FALSE");
  }
  @Test
  public void micro329() {
    assertIdentical("{ FALSE & TRUE }", "FALSE");
  }
  @Test
  public void micro330() {
    assertIdentical("{ TRUE & TRUE }", "TRUE");
  }
  @Test
  public void micro331() {
    assertIdentical("{ TRUE & NA }", "NA");
  }
  @Test
  public void micro332() {
    assertIdentical("{ FALSE & NA }", "FALSE");
  }
  @Test
  public void micro333() {
    assertIdentical("{ NA & TRUE }", "NA");
  }
  @Test
  public void micro334() {
    assertIdentical("{ NA & FALSE }", "FALSE");
  }
  @Test
  public void micro335() {
    assertIdentical("{ NA & NA }", "NA");
  }
  @Test
  public void micro336() {
    assertIdentical("{ x <- 1 ; f <- function(r) { x <<- 2; r } ; NA & f(NA) ; x }", "2");
  }
  @Test
  public void micro337() {
    assertIdentical("{ x <- 1 ; f <- function(r) { x <<- 2; r } ; FALSE & f(FALSE) ; x }", "2");
  }
  @Test
  public void micro338() {
    assertIdentical("{ 1:4 & c(FALSE,TRUE) }", "c(FALSE, TRUE, FALSE, TRUE)");
  }
  @Test
  public void micro339() {
    assertIdentical("{ 1+2i | 0 }", "TRUE");
  }
  @Test
  public void micro340() {
    assertIdentical("{ 1+2i & 0 }", "FALSE");
  }
  @Test
  public void micro341() {
    assertIdentical("{ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(FALSE, FALSE) }", "FALSE");
  }
  @Test
  public void micro342() {
    assertIdentical("{ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(as.raw(10), as.raw(11)) }", "as.raw(0x0a)");
  }
  @Test
  public void micro343() {
    assertIdentical("{ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, 0L) }", "FALSE");
  }
  @Test
  public void micro344() {
    assertIdentical("{ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, 0) }", "FALSE");
  }
  @Test
  public void micro345() {
    assertIdentical("{ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, TRUE) }", "TRUE");
  }
  @Test
  public void micro346() {
    assertIdentical("{ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, 3+4i) }", "TRUE");
  }
  @Test
  public void micro347() {
    assertIdentical("{ f <- function(a,b) { a & b } ; f(TRUE, FALSE) ; f(1L, 3+4i) }", "TRUE");
  }
  @Test
  public void micro348() {
    assertIdentical("{ f <- function(a,b) { a & b } ; f(TRUE, FALSE) ; f(TRUE, 3+4i) }", "TRUE");
  }
  @Test
  public void micro349() {
    assertIdentical("{ f <- function(a,b) { a | b } ; f(c(TRUE, FALSE), FALSE) ; f(1L, 3+4i) }", "TRUE");
  }
  @Test
  public void micro350() {
    assertIdentical("{ f <- function(a,b) { a | b } ; f(c(TRUE, FALSE), FALSE) ; f(c(FALSE,FALSE), 3+4i) }", "c(TRUE, TRUE)");
  }
  @Test
  public void micro351() {
    assertIdentical("{ f <- function(a,b) { a | b } ; f(as.raw(c(1,4)), as.raw(3)) ; f(4, FALSE) }", "TRUE");
  }
  @Test
  public void micro352() {
    assertIdentical("{ a <- as.raw(200) ; b <- as.raw(255) ; a | b }", "as.raw(0xff)");
  }
  @Test
  public void micro353() {
    assertIdentical("{ a <- as.raw(200) ; b <- as.raw(1) ; a | b }", "as.raw(0xc9)");
  }
  @Test
  public void micro354() {
    assertIdentical("{ a <- as.raw(201) ; b <- as.raw(1) ; a & b }", "as.raw(0x01)");
  }
  @Test
  public void micro355() {
    assertIdentical("{ x <- 2147483647L ; x + 1L }", "NA_integer_");
  }
  @Test
  public void micro356() {
    assertIdentical("{ x <- 2147483647L ; x * x }", "NA_integer_");
  }
  @Test
  public void micro357() {
    assertIdentical("{ x <- -2147483647L ; x - 2L }", "NA_integer_");
  }
  @Test
  public void micro358() {
    assertIdentical("{ x <- -2147483647L ; x - 1L }", "NA_integer_");
  }
  @Test
  public void micro359() {
    assertIdentical("{ 3L %/% 0L }", "NA_integer_");
  }
  @Test
  public void micro360() {
    assertIdentical("{ 3L %% 0L }", "NA_integer_");
  }
  @Test
  public void micro361() {
    assertIdentical("{ c(3L,3L) %/% 0L }", "c(NA_integer_, NA_integer_)");
  }
  @Test
  public void micro362() {
    assertIdentical("{ c(3L,3L) %% 0L }", "c(NA_integer_, NA_integer_)");
  }
  @Test
  public void micro363() {
    assertIdentical("{ 2147483647L + 1:3 }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro364() {
    assertIdentical("{ 2147483647L + c(1L,2L,3L) }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro365() {
    assertIdentical("{ 1:3 + 2147483647L }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro366() {
    assertIdentical("{ c(1L,2L,3L) + 2147483647L }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro367() {
    assertIdentical("{ 1:3 + c(2147483647L,2147483647L,2147483647L) }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro368() {
    assertIdentical("{ c(2147483647L,2147483647L,2147483647L) + 1:3 }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro369() {
    assertIdentical("{ c(1L,2L,3L) + c(2147483647L,2147483647L,2147483647L) }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro370() {
    assertIdentical("{ c(2147483647L,2147483647L,2147483647L) + c(1L,2L,3L) }", "c(NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro371() {
    assertIdentical("{ 1:4 + c(2147483647L,2147483647L) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro372() {
    assertIdentical("{ c(2147483647L,2147483647L) + 1:4 }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro373() {
    assertIdentical("{ c(1L,2L,3L,4L) + c(2147483647L,2147483647L) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro374() {
    assertIdentical("{ c(2147483647L,2147483647L) + c(1L,2L,3L,4L) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro375() {
    assertIdentical("{ x <- 3 ; f <- function(z) { if (z) { x <- 1 } ; x <- x + 1L ; x } ; f(FALSE) }", "4");
  }
  @Test
  public void micro376() {
    assertIdentical("{ x <- 3 ; f <- function(z) { if (z) { x <- 1 } ; x <- 1L + x ; x } ; f(FALSE) }", "4");
  }
  @Test
  public void micro377() {
    assertIdentical("{ x <- 3 ; f <- function(z) { if (z) { x <- 1 } ; x <- x - 1L ; x } ; f(FALSE) }", "2");
  }
  @Test
  public void micro378() {
    assertIdentical("{ a = array(); length(a) == 1; }", "TRUE");
  }
  @Test
  public void micro379() {
    assertIdentical("{ a = array(); is.na(a[1]); }", "TRUE");
  }
  @Test
  public void micro380() {
    assertIdentical("{ a <- array(); dim(a) == 1; }", "TRUE");
  }
  @Test
  public void micro381() {
    assertIdentical("{ a = array(1:10, dim = c(2,6)); length(a) == 12; }", "TRUE");
  }
  @Test
  public void micro382() {
    assertIdentical("{ length(array(dim=c(1,0,2,3))) == 0; }", "TRUE");
  }
  @Test
  public void micro383() {
    assertIdentical("{ a = dim(array(dim=c(2.1,2.9,3.1,4.7))); a[1] == 2 && a[2] == 2 && a[3] == 3 && a[4] == 4; }", "TRUE");
  }
  @Test
  public void micro384() {
    assertIdentical("{ length(matrix()) == 1; }", "TRUE");
  }
  @Test
  public void micro385() {
    assertIdentical("{ a = array(1:27,c(3,3,3)); a[1,1,1] == 1 && a[3,3,3] == 27 && a[1,2,3] == 22 && a[3,2,1] == 6; }", "TRUE");
  }
  @Test
  public void micro386() {
    assertIdentical("{ a = array(1:27, c(3,3,3)); b = a[,,]; d = dim(b); d[1] == 3 && d[2] == 3 && d[3] == 3; }", "TRUE");
  }
  @Test
  public void micro387() {
    assertIdentical("{ a = array(1,c(3,3,3)); a = dim(a[,1,]); length(a) == 2 && a[1] == 3 && a[2] == 3; }", "TRUE");
  }
  @Test
  public void micro388() {
    assertIdentical("{ a = array(1,c(3,3,3)); is.null(dim(a[1,1,1])); }", "TRUE");
  }
  @Test
  public void micro389() {
    assertIdentical("{ a = array(1,c(3,3,3)); is.null(dim(a[1,1,])); }", "TRUE");
  }
  @Test
  public void micro390() {
    assertIdentical("{ a = array(1,c(3,3,3)); a = dim(a[1,1,1, drop = FALSE]); length(a) == 3 && a[1] == 1 && a[2] == 1 && a[3] == 1; }", "TRUE");
  }
  @Test
  public void micro391() {
    assertIdentical("{ m <- array(1:4, dim=c(4,1,1)) ; x <- m[[2,1,1,drop=FALSE]] ; is.null(dim(x)) }", "TRUE");
  }
  @Test
  public void micro392() {
    assertIdentical("{ a = array(1:27, c(3,3,3)); a[1] == 1 && a[27] == 27 && a[22] == 22 && a[6] == 6; }", "TRUE");
  }
  @Test
  public void micro393() {
    assertIdentical("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,1] ; x[1] == 1 && x[2] == 2 }", "TRUE");
  }
  @Test
  public void micro394() {
    assertIdentical("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- dim(m[1:2,1,1]) ; is.null(x) }", "TRUE");
  }
  @Test
  public void micro395() {
    assertIdentical("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- dim(m[1:2,1,1,drop=FALSE]) ; x[1] == 2 && x[2] == 1 && x[3] == 1 }", "TRUE");
  }
  @Test
  public void micro396() {
    assertIdentical("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,integer()] ; d <- dim(x) ; length(x) == 0 }", "TRUE");
  }
  @Test
  public void micro397() {
    assertIdentical("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,integer()] }", "structure(numeric(0), .Dim = c(2L, 0L))");
    assertIdentical("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,integer()] ; d <- dim(x) ; d[1] == 2 && d[2] == 0 }", "TRUE");
  }
  @Test
  public void micro398() {
    assertIdentical("{ array(1,c(3,3,3))[1,1,1] == 1; }", "TRUE");
  }
  @Test
  public void micro399() {
    assertIdentical("{ array(1,c(3,3,3))[[1,1,1]] == 1; }", "TRUE");
  }
  @Test
  public void micro402() {
    assertIdentical("{ m <- array(1:24, dim=c(2,3,4)) ; f <- function(i) { m[,,i] } ; f(1) ; f(2) ; dim(f(1:2)) }", "c(2L, 3L, 2L)");
  }
  @Test
  public void micro404() {
    assertIdentical("{ matrix(1,3,3)[1,1] == 1; }", "TRUE");
  }
  @Test
  public void micro405() {
    assertIdentical("{ matrix(1,3,3)[[1,1]] == 1; }", "TRUE");
  }
  @Test
  public void micro406() {
    assertIdentical("{  m <- matrix(1:6, nrow=2) ;  m[1,NULL] }", "integer(0)");
  }
  @Test
  public void micro407() {
    assertIdentical("{ a = matrix(1,2,2); a[1,2] = 3; a[1,2] == 3; }", "TRUE");
  }
  @Test
  public void micro408() {
    assertIdentical("{ a = array(1,c(3,3,3)); a[1,2,3] = 3; a[1,2,3] == 3; }", "TRUE");
  }
  @Test
  public void micro409() {
    assertIdentical("{ a = array(1,c(3,3,3)); (a[1,2,3] = 3) == 3; }", "TRUE");
  }
  @Test
  public void micro410() {
    assertIdentical("{ a = array(1,c(3,3,3)); b = a; b[1,2,3] = 3; a[1,2,3] == 1 && b[1,2,3] == 3; }", "TRUE");
  }
  @Test
  public void micro411() {
    assertIdentical("{ x <- array(c(1,2,3), dim=c(3,1,1)) ; x[1:2,1,1] <- sqrt(x[2:1]) ; x[1] == sqrt(2) && x[2] == 1 && x[3] == 3 }", "TRUE");
  }
  @Test
  public void micro412() {
    assertIdentical("{ a = array(TRUE,c(3,3,3)); a[1,2,3] = 8L; a[1,2,3] == 8L; }", "TRUE");
  }
  @Test
  public void micro413() {
    assertIdentical("{ a = array(TRUE,c(3,3,3)); a[1,2,3] = 8.1; a[1,2,3] == 8.1; }", "TRUE");
  }
  @Test
  public void micro414() {
    assertIdentical("{ a = array(1L,c(3,3,3)); a[1,2,3] = 8.1; a[1,2,3] == 8.1; }", "TRUE");
  }
  @Test
  public void micro415() {
    assertIdentical("{ a = array(TRUE,c(3,3,3)); a[1,2,3] = 2+3i; a[1,2,3] == 2+3i; }", "TRUE");
  }
  @Test
  public void micro416() {
    assertIdentical("{ a = array(1L,c(3,3,3)); a[1,2,3] = 2+3i; a[1,2,3] == 2+3i; }", "TRUE");
  }
  @Test
  public void micro417() {
    assertIdentical("{ a = array(1.3,c(3,3,3)); a[1,2,3] = 2+3i; a[1,2,3] == 2+3i; }", "TRUE");
  }
  @Test
  public void micro418() {
    assertIdentical("{ a = array(TRUE,c(3,3,3)); a[1,2,3] = \"2+3i\"; a[1,2,3] == \"2+3i\" && a[1,1,1] == \"TRUE\"; }", "TRUE");
  }
  @Test
  public void micro419() {
    assertIdentical("{ a = array(1L,c(3,3,3)); a[1,2,3] = \"2+3i\"; a[1,2,3] == \"2+3i\" && a[1,1,1] == \"1L\"; }", "FALSE");
  }
  @Test
  public void micro420() {
    assertIdentical("{ a = array(1.5,c(3,3,3)); a[1,2,3] = \"2+3i\"; a[1,2,3] == \"2+3i\" && a[1,1,1] == \"1.5\"; }", "TRUE");
  }
  @Test
  public void micro421() {
    assertIdentical("{ a = array(7L,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == 1L && a[1,1,1] == 7L; }", "TRUE");
  }
  @Test
  public void micro422() {
    assertIdentical("{ a = array(1.7,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == 1 && a[1,1,1] == 1.7; }", "TRUE");
  }
  @Test
  public void micro423() {
    assertIdentical("1+0i==1", "TRUE");
    assertIdentical("dim(array(3+2i,c(3,3,3)))", "c(3L,3L,3L)");
    assertIdentical("{ a = array(3+2i,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3]==1 }", "TRUE");
    assertIdentical("{ a = array(3+2i,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,1,1] }", "3+2i");

  }
  @Test
  public void micro424() {
    assertIdentical("{ a = array(\"3+2i\",c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == \"TRUE\" && a[1,1,1] == \"3+2i\"; }", "TRUE");
  }
  @Test
  public void micro425() {
    assertIdentical("{ a = array(1.7,c(3,3,3)); b = 3L; a[1,2,3] = b; a[1,2,3] == 3 && a[1,1,1] == 1.7; }", "TRUE");
  }
  @Test
  public void micro426() {
    assertIdentical("{ a = array(3+2i,c(3,3,3)); b = 4L; a[1,2,3] = b; a[1,2,3] == 4 && a[1,1,1] == 3+2i; }", "TRUE");
  }
  @Test
  public void micro427() {
    assertIdentical("{ m <- array(c(1+1i,2+2i,3+3i), dim=c(3,1,1)) ; m[1:2,1,1] <- c(100L,101L) ; m ; m[1,1,1] == 100 && m[2,1,1] == 101 }", "TRUE");
  }
  @Test
  public void micro428() {
    assertIdentical("{ a = array(\"3+2i\",c(3,3,3)); b = 7L; a[1,2,3] = b; a[1,2,3] == \"7L\" && a[1,1,1] == \"3+2i\"; }", "FALSE");
  }
  @Test
  public void micro429() {
    assertIdentical("{ a = array(3+2i,c(3,3,3)); b = 4.2; a[1,2,3] = b; a[1,2,3] == 4.2 && a[1,1,1] == 3+2i; }", "TRUE");
  }
  @Test
  @Ignore("not sure about the right answer here...")
  public void micro430() {
    assertIdentical("{ a = array(\"3+2i\",c(3,3,3)); b = 2+3i; a[1,2,3] = b; a[1,2,3] }", "\"2.0+3.0i\"");
    assertIdentical("{ a = array(\"3+2i\",c(3,3,3)); b = 2+3i; a[1,2,3] = b; a[1,1,1] }", "\"3+2i\"");

    assertIdentical("{ a = array(\"3+2i\",c(3,3,3)); b = 2+3i; a[1,2,3] = b; a[1,2,3] == \"2.0+3.0i\" && a[1,1,1] == \"3+2i\"; }", "FALSE");
  }
  @Test
  public void micro431() {
    assertIdentical("{ a = matrix(1,3,3); a[1,] = c(3,4,5); a[1,1] == 3 && a[1,2] == 4 && a[1,3] == 5; }", "TRUE");
  }
  @Test
  public void micro432() {
    assertIdentical("{ a = matrix(1,3,3); a[,1] = c(3,4,5); a[1,1] == 3 && a[2,1] == 4 && a[3,1] == 5; }", "TRUE");
  }
  @Test
  public void micro433() {
    assertIdentical("{ a = array(1,c(3,3,3)); a[1,1,] = c(3,4,5); a[1,1,1] == 3 && a[1,1,2] == 4 && a[1,1,3] == 5; }", "TRUE");
  }
  @Test
  public void micro434() {
    assertIdentical("{ a = array(1,c(3,3,3)); a[1,,1] = c(3,4,5); a[1,1,1] == 3 && a[1,2,1] == 4 && a[1,3,1] == 5; }", "TRUE");
  }
  @Test
  public void micro435() {
    assertIdentical("{ a = array(1,c(3,3,3)); a[,1,1] = c(3,4,5); a[1,1,1] == 3 && a[2,1,1] == 4 && a[3,1,1] == 5; }", "TRUE");
  }
  @Test
  public void micro436() {
    assertIdentical("{ a = array(1,c(3,3,3)); a[1,,] = matrix(1:9,3,3); a[1,1,1] == 1 && a[1,3,1] == 3 && a[1,3,3] == 9; }", "TRUE");
  }
  @Test
  public void micro437() {
    assertIdentical("{ m <- array(1:3, dim=c(3,1,1)) ; f <- function(x,v) { x[1:2,1,1] <- v ; x } ; f(m,10L) ; f(m,10) ; f(m,c(11L,12L)); m[1,1,1] == 1L && m[2,1,1] == 2L && m[3,1,1] == 3L }", "TRUE");
  }
  @Test
  public void micro438() {
    assertIdentical("{ a = matrix(1,3,3); is.null(dim(a[1,])); }", "TRUE");
  }
  @Test
  public void micro450() {
    assertIdentical("{ m <- matrix(1:4,nrow=2) ; m[2,2,drop=TRUE] }", "4L");
  }
  @Test
  public void micro453() {
    assertIdentical("{ m <- matrix(1:4,nrow=2) ; m[,2,drop=TRUE] }", "3:4");
  }
  @Test
  public void micro459() {
    assertIdentical("{ f <- function(b,x,y) { b[1:2,2:2,drop=TRUE] } ; f(matrix(1:4,nrow=2)) }", "3:4");
  }
  @Test
  public void micro460() {
    assertIdentical("{ f <- function(b,x,y) { b[1:1,2:1,drop=TRUE] } ; f(matrix(1:4,nrow=2)) }", "c(3L, 1L)");
  }
  @Test
  public void micro463() {
    assertIdentical("length(1e100:1e100)", "1L");
    assertIdentical("{ f <- function(b,x,y) { b[1e100:1e100,2:2] } ; f(matrix(1:4,nrow=2)) }", "NA_integer_");
  }
  @Test
  public void micro464() {
    assertIdentical("{ f <- function(b,x,y) { b[-2L:-2L,2:2] } ; f(matrix(1:4,nrow=2)) }", "3L");
  }
  @Test
  public void micro465() {
    assertIdentical("{ f <- function(b,x,y) { b[TRUE:FALSE,2:2] } ; f(matrix(1:4,nrow=2)) }", "3L");
  }
  @Test
  public void micro466() {
    assertIdentical("{ f <- function(b,x,y) { b[[2,1]] } ; f(matrix(1:4,nrow=2)) }", "2L");
  }
  @Test
  public void micro467() {
    assertIdentical("{ f <- function(b,x,y) { b[[2,1]] } ; f(matrix(as.list(1:4),nrow=2)) }", "2L");
  }
  @Test
  public void micro469() {
    assertIdentical("{ f <- function(d) { b <- matrix(1:4,nrow=2,ncol=2) ; b[,drop=d,2] } ; f(0) ; f(1L) }", "3:4");
  }
  @Test
  public void micro470() {
    assertIdentical("{ z <- 1 ; f <- function(d) { b <- matrix(1:4,nrow=2,ncol=2) ; b[{z<<-z+1;1},drop=z<<-z*10,{z<<-z*2;2}] } ; f(0) ; f(1L) ; z }", "820");
  }
  @Test
  public void micro471() {
    assertIdentical("{ b <- 1:4 ; dim(b) <- c(1,1,4,1); x <- b[,,,1] ; x }", "1:4");
  }
  @Test
  public void micro477() {
    assertIdentical("{ x <- list(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,1L) ; f(x,2) }", "2");
  }
  @Test
  public void micro478() {
    assertIdentical("{ x <- c(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,TRUE) }", "1");
  }
  @Test
  public void micro479() {
    assertIdentical("{ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4,-6), 1) }", "c(2, 3)");
  }
  @Test
  public void micro480() {
    assertIdentical("{ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4,-6), 1) ; f(x,c(-2,-4,-4,-6), 1) }", "c(1, 3)");
  }
  @Test
  public void micro481() {
    assertIdentical("{ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4,-6), 1) ; y <- 1:8 ; dim(y) <- c(1,8,1) ; f(y,c(-2,-4,-4,-6,-8), 1) }", "c(1L, 3L, 5L, 7L)");
  }
  @Test
  public void micro483() {
    assertIdentical("{ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-1),c(-3,-3,-3,-4)) }", "c(2, 4)");
  }
  @Test
  public void micro484() {
    assertIdentical("{ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(TRUE,FALSE),c(NA)) }", "c(NA_real_, NA_real_)");
  }
  @Test
  public void micro500() {
    assertIdentical("{  m <- array(1:3, dim=c(3,1,1)) ; f <- function(x,v) { x[[2,1,1]] <- v ; x } ; f(m,10L) ; f(m,10) ; x <- f(m,11L) ; x[1] == 1 && x[2] == 11 && x[3] == 3 }", "TRUE");
  }
  @Test
  public void micro501() {
    assertIdentical("{ m <- matrix(1:100, nrow=10) ; z <- 1; s <- 0 ; for(i in 1:3) { m[z <- z + 1,z <- z + 1] <- z * z * 1000 } ; sum(m) }", "39918");
  }
  @Test
  public void micro502() {
    assertIdentical("{ m <- matrix(as.double(1:6), nrow=2) ; mi <- matrix(1:6, nrow=2) ; f <- function(v,i,j) { v[i,j] <- 100 ; v[i,j] * i * j } ; f(m, 1L, 2L) ; f(m,1L,TRUE)  }", "c(100, 100, 100)");
  }
  @Test
  public void micro503() {
    assertIdentical("{ m <- matrix(as.double(1:6), nrow=2) ; mi <- matrix(1:6, nrow=2) ; f <- function(v,i,j) { v[i,j] <- 100 ; v[i,j] * i * j } ; f(m, 1L, 2L) ; f(m,1L,-1)  }", "c(-100, -100)");
  }
  @Test
  public void micro519() {
    assertIdentical("{ x <- list(1+2i,3+4i,5+6i,4+5i) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- as.raw(c(1,2)) ; unlist(x) }", "c(1+2i, 1+0i, 5+6i, 2+0i)");
  }
  @Test
  public void micro539() {
    assertIdentical("{ r <- 0 ; for (i in 1:5 ) { x <- c(11:14) ; if (i==2 || i==3) { x <- c(1,2,10+1i,100) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(15L*i,-12L+i) ; r <- r + sum(x) } ; r }", "274+2i");
  }
  @Test
  public void micro561() {
    assertIdentical("{ x <- c(1L,3L,4L,NA) ; dim(x) <- c(2,1,2); x[[2,1,1]] <- list(10+1i); x[2] }", "list(list(10+1i))");
  }
  @Test
  public void micro564() {
    assertIdentical("{ x <- c(\"a\",\"b\",\"c\",\"d\") ; dim(x) <- c(2,1,2); x[2,1,1] <- list(1); dim(x) <- NULL; x }", "list(\"a\", 1, \"c\", \"d\")");
  }
  @Test
  public void micro572() {
    assertIdentical("{ x <- list(1,10,-1/0,0/0) ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(list(TRUE)) ; z <- f(NA) ; unlist(z) }", "c(1, NA, -Inf, NaN)");
  }
  @Test
  public void micro578() {
    assertIdentical("{ for(i in 1:2) { if (i==1) { b <- as.list(11:14) } else { b <- c(1/0,-3/0,0/0,4) }; dim(b) <- c(2,1,2); b[[2,1,1]] <- list(111) } ; dim(b) <- NULL ; b }", "list(Inf, list(111), NaN, 4)");
  }
  @Test
  public void micro580() {
    assertIdentical("{ for(i in 1:2) { x <- 1:4 ; dim(x) <- c(1,1,4); if (i==2) { z <- x } ; x[,,1] <- 12L } ; as.integer(z) }", "1:4");
  }
  @Test
  public void micro581() {
    assertIdentical("{ x <- 1:4 ; dim(x) <- c(1,1,4); x[,,NA] <- 12L ; as.integer(x) }", "1:4");
  }
  @Test
  public void micro582() {
    assertIdentical("{ for (i in 1:3) { if (i==1) { z <- 1 } ; if (i==2) { z <- c(-1,-1) } ; x <- 1:4 ; dim(x) <- c(1,1,4)  ; x[,,z] <- 12L } ; as.integer(x) }", "c(1L, 12L, 12L, 12L)");
  }
  @Test
  public void micro592() {
    assertIdentical("{ for (i in c(1,-1)) { x <- 1:4 ; dim(x) <- c(2,2) ; x[i,2] <- list(12) } ; unlist(x) }", "c(1, 2, 3, 12)");
  }
  @Test
  public void micro625() {
    assertIdentical("{ for(b in list(as.raw(11:14),list(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- as.raw(3:4)[2:1] } ; dim(b) <- NULL ; b }", "list(as.raw(0x03), as.raw(0x04), NA, NA)");
  }
  @Test
  public void micro626() {
    assertIdentical("{ for(b in list(as.list(11:14),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- list(1,2) } ; dim(b) <- NULL ; b }", "list(2, 1, NA, NA)");
  }
  @Test
  public void micro632() {
    assertIdentical("{ typeof({ for(v in list(as.list(13:14),c(TRUE,NA))) { x <- list(1,1L,TRUE,NA) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }) }", "\"list\"");
  }
  @Test
  public void micro699() {
    assertIdentical("{ typeof({ for(b in list(1:4, list(TRUE,1, 3+4i, 0/0))) { dim(b) <- c(2,2,1); b[2:1,1,1] <- list(1L,TRUE) };  b }) }", "\"list\"");
  }
  @Test
  public void micro701() {
    assertIdentical("{ for(b in list(list(TRUE,1, 3+4i, 0/0), 1:4)) { dim(b) <- c(2,2,1); s <- b ; b[2:1,1,1] <- list(1L,TRUE) };  dim(b) <- NULL ; b }", "list(TRUE, 1L, 3L, 4L)");
  }
  @Test
  public void micro711() {
    assertIdentical("{ l <- quote(x <- 1) ; f <- function() { eval(l) } ; x <- 10 ; f() ; x }", "10");
  }
  @Test
  public void micro712() {
    assertIdentical("{ l <- quote(x <- 1) ; f <- function() { eval(l) ; x <<- 10 ; get(\"x\") } ; f() }", "1");
  }
  @Test
  public void micro713() {
    assertIdentical("{ a<-1 }", "1");
  }
  @Test
  public void micro714() {
    assertIdentical("{ a<-FALSE ; b<-a }", "FALSE");
  }
  @Test
  public void micro716() {
    assertIdentical("{ x <<- 1 }", "1");
  }
  @Test
  public void micro717() {
    assertIdentical("{ x <<- 1 ; x }", "1");
  }
  @Test
  public void micro718() {
    assertIdentical("{ f <- function() { x <<- 2 } ; f() ; x }", "2");
  }
  @Test
  public void micro719() {
    assertIdentical("{ x <- 10 ; f <- function() { x <<- 2 } ; f() ; x }", "2");
  }
  @Test
  public void micro720() {
    assertIdentical("{ x <- 10 ; f <- function() { x <<- 2 ; x } ; c(f(), f()) }", "c(2, 2)");
  }
  @Test
  public void micro721() {
    assertIdentical("{ x <- 10 ; f <- function() { x <- x ; x <<- 2 ; x } ; c(f(), f()) }", "c(10, 2)");
  }
  @Test
  public void micro722() {
    assertIdentical("{ x <- 10 ; g <- function() { f <- function() { x <- x ; x <<- 2 ; x } ; c(f(), f()) } ; g() }", "c(10, 2)");
  }
  @Test
  public void micro723() {
    assertIdentical("{ x <- 10 ; g <- function() { x ; f <- function() { x <- x ; x <<- 2 ; x } ; c(f(), f()) } ; g() }", "c(10, 2)");
  }
  @Test
  public void micro724() {
    assertIdentical("{ x <- 10 ; g <- function() { x <- 100 ; f <- function() { x <- x ; x <<- 2 ; x } ; c(f(), f()) } ; g() }", "c(100, 2)");
  }
  @Test
  public void micro725() {
    assertIdentical("{ h <- function() { x <- 10 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { x <<- 3 ; x } ; f() } ; g() } ; h() }", "3");
  }
  @Test
  public void micro726() {
    assertIdentical("{ x <- 3 ; f <- function() { assign(\"x\", 4) ; h <- function() { assign(\"z\", 5) ; g <- function() { x <<- 10 ; x } ; g() } ; h() } ; f() ; x }", "3");
  }
  @Test
  public void micro727() {
    assertIdentical("{ f <- function(i) { if (i==1) { c <- 1 } ; c } ; f(1) ; typeof(f(2)) }", "\"builtin\"");
  }
  @Test
  public void micro728() {
    assertIdentical("{ f <- function(i) { if (i==1) { c <- 1 ; x <- 1 } ; if (i!=2) { x } else { c }} ; f(1) ; f(1) ; typeof(f(2)) }", "\"builtin\"");
  }
  @Test
  public void micro729() {
    assertIdentical("{ x <- 3 ; f <- function() { assign(\"x\", 4) ; g <- function() { assign(\"y\", 3) ; hh <- function() { assign(\"z\", 6) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x } ; h() } ; hh() } ; g()  } ; f() }", "4");
  }
  @Test
  public void micro730() {
    assertIdentical("{ f <- function() { if (FALSE) { c <- 1 } ; g <- function() { c } ; g() } ; typeof(f()) }", "\"builtin\"");
  }
  @Test
  public void micro742() {
    assertIdentical("{ x <- 1:2;  attr(x, \"hi\") <- 2 ;  x+1:4 }", "c(2L, 4L, 4L, 6L)");
  }
  @Test
  public void micro746() {
    assertIdentical("{ x <- 1+1i;  attr(x, \"hi\") <- 1+2 ; y <- 2:3 ;  x+y }", "c(3+1i, 4+1i)");
  }
  @Test
  public void micro748() {
    assertIdentical("{ x <- c(a=1) ; y <- c(b=2,c=3) ; x + y }", "structure(c(3, 4), .Names = c(\"b\", \"c\"))");
  }
  @Test
  public void micro749() {
    assertIdentical("{ x <- c(a=1) ; y <- c(b=2,c=3) ; y + x }", "structure(c(3, 4), .Names = c(\"b\", \"c\"))");
  }
  @Test
  public void micro755() {
    assertIdentical("{ x <- 1:2;  attr(x, \"hi\") <- 2 ;  x & x }", "c(TRUE, TRUE)");
  }
  @Test
  public void micro756() {
    assertIdentical("{ x <- as.raw(1:2);  attr(x, \"hi\") <- 2 ;  x & x }", "as.raw(c(0x01, 0x02))");
  }
  @Test
  public void micro757() {
    assertIdentical("{ x <- 1:2 ;  attr(x, \"hi\") <- 2 ;  !x  }", "c(FALSE, FALSE)");
  }
  @Test
  public void micro759() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1 ; as.character(x) }", "c(\"1\", \"2\")");
  }
  @Test
  public void micro760() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1 ; as.double(x) }", "c(1, 2)");
  }
  @Test
  public void micro761() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1 ; as.integer(x) }", "1:2");
  }
  @Test
  public void micro762() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; x[c(1,1)] }", "structure(c(1, 1), .Names = c(\"a\", \"a\"))");
  }
  @Test
  public void micro772() {
    assertIdentical("{ x <- c(a=1) ; attr(x, \"myatt\") <- 1 ; lapply(1:2, function(z) {x}) }", "list(structure(1, .Names = \"a\", myatt = 1), structure(1, .Names = \"a\", myatt = 1))");
  }
  @Test
  public void micro776() {
    assertIdentical("{ x <- 1 ; attr(x, \"myatt\") <- 1; x:x }", "1L");
  }
  @Test
  public void micro777() {
    assertIdentical("{ x <- 1 ; attr(x, \"myatt\") <- 1; c(x, x, x) }", "c(1, 1, 1)");
  }
  @Test
  public void micro778() {
    assertIdentical("{ x <- 1 ; attr(x, \"myatt\") <- 1; cumsum(c(x, x, x)) }", "c(1, 2, 3)");
  }
  @Test
  public void micro780() {
    assertIdentical("{ m <- matrix(c(1,1,1,1), nrow=2) ; attr(m,\"a\") <- 1 ;  r <- eigen(m) ; r$vectors <- round(r$vectors, digits=5) ; r  }", "structure(list(values = c(2, 0), vectors = structure(c(0.70711, 0.70711, -0.70711, 0.70711), .Dim = c(2L, 2L))), .Names = c(\"values\", \"vectors\"))");
  }
  @Test
  public void micro782() {
    assertIdentical("{ x <- 1 ; attr(x, \"myatt\") <- 1; min(x) }", "1");
  }
  @Test
  public void micro784() {
    assertIdentical("{ x <- c(a=1) ; attr(x, \"myatt\") <- 1; nchar(x) }", "structure(1L, .Names = \"a\")");
  }
  @Test
  public void micro786() {
    assertIdentical("{ x <- 1 ; attr(x, \"myatt\") <- 1; rep(x,2) }", "c(1, 1)");
  }
  @Test
  public void micro787() {
    assertIdentical("{ x <- c(a=TRUE) ; attr(x, \"myatt\") <- 1; rep(x,2) }", "structure(c(TRUE, TRUE), .Names = c(\"a\", \"a\"))");
  }
  @Test
  public void micro788() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; rev(x) }", "structure(c(2, 1), .Names = c(\"b\", \"a\"))");
  }
  @Test
  public void micro789() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; seq(x) }", "1:2");
  }
  @Test
  public void micro790() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; order(x) }", "1:2");
  }
  @Test
  public void micro792() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; sum(x) }", "3");
  }
  @Test
  public void micro797() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; unlist(list(x,x)) }", "structure(c(1, 2, 1, 2), .Names = c(\"a\", \"b\", \"a\", \"b\"))");
  }
  @Test
  public void micro798() {
    assertIdentical("{ x <- 1:2;  attr(x, \"hi\") <- 2 ;  x == x }", "c(TRUE, TRUE)");
  }
  @Test
  public void micro799() {
    assertIdentical("{ as.integer(c(1,2,3)) }", "1:3");
  }
  @Test
  public void micro800() {
    assertIdentical("{ as.integer(list(c(1),2,3)) }", "1:3");
  }
  @Test
  public void micro801() {
    assertIdentical("{ as.integer(list(integer(),2,3)) }", "c(NA, 2L, 3L)");
  }
  @Test
  public void micro802() {
    assertIdentical("{ as.integer(list(list(1),2,3)) }", "c(NA, 2L, 3L)");
  }
  @Test
  public void micro803() {
    assertIdentical("{ as.integer(list(1,2,3,list())) }", "c(1L, 2L, 3L, NA)");
  }
  @Test
  public void micro804() {
    assertIdentical("{ m<-matrix(1:6, nrow=3) ; as.integer(m) }", "1:6");
  }
  @Test
  public void micro805() {
    assertIdentical("{ m<-matrix(1:6, nrow=3) ; as.vector(m, 'any') }", "1:6");
  }
  @Test
  public void micro806() {
    assertIdentical("{ m<-matrix(1:6, nrow=3) ; as.vector(mode = 'integer', x=m) }", "1:6");
  }
  @Test
  public void micro807() {
    assertIdentical("{ as.vector(list(1,2,3), mode='integer') }", "1:3");
  }
  @Test
  public void micro808() {
    assertIdentical("{ as.double('1.27') }", "1.27");
  }
  @Test
  public void micro809() {
    assertIdentical("{ as.double(1L) }", "1");
  }
  @Test
  public void micro810() {
    assertIdentical("{ as.double('TRUE') }", "NA_real_");
  }
  @Test
  public void micro811() {
    assertIdentical("{ as.double(c('1','hello')) }", "c(1, NA)");
  }
  @Test
  public void micro812() {
    assertIdentical("{ as.character(1L) }", "\"1\"");
  }
  @Test
  public void micro813() {
    assertIdentical("{ as.character(TRUE) }", "\"TRUE\"");
  }
  @Test
  public void micro814() {
    assertIdentical("{ as.character(1:3) }", "c(\"1\", \"2\", \"3\")");
  }
  @Test
  public void micro815() {
    assertIdentical("{ as.character(NULL) }", "character(0)");
  }
  @Test
  public void micro816() {
    assertIdentical("{ as.character(list(c('hello', 'hi'))) }", "\"c(\\\"hello\\\", \\\"hi\\\")\"");
  }
  @Test
  public void micro817() {
    assertIdentical("{ as.character(list(list(c('hello', 'hi')))) }", "\"list(c(\\\"hello\\\", \\\"hi\\\"))\"");
  }
  @Test
  public void micro818() {
    assertIdentical("{ as.character(list(1,2,3)) }", "c(\"1\", \"2\", \"3\")");
  }
  @Test
  public void micro819() {
    assertIdentical("{ as.character(list(c(2L, 3L))) }", "\"2:3\"");
  }
  @Test
  public void micro821() {
    assertIdentical("{ as.raw(list(1,2,3)) }", "as.raw(c(0x01, 0x02, 0x03))");
  }
  @Test
  public void micro822() {
    assertIdentical("{ as.raw(list('1', 2L, 3.4)) }", "as.raw(c(0x01, 0x02, 0x03))");
  }
  @Test
  public void micro823() {
    assertIdentical("{ as.raw(c(1,1000,NA)) }", "as.raw(c(0x01, 0x00, 0x00))");
  }
  @Test
  public void micro824() {
    assertIdentical("{ as.logical(1) }", "TRUE");
  }
  @Test
  public void micro825() {
    assertIdentical("{ as.logical('false') }", "FALSE");
  }
  @Test
  public void micro826() {
    assertIdentical("{ as.logical('dummy') }", "NA");
  }
  @Test
  public void micro827() {
    assertIdentical("{ as.complex(0) }", "0+0i");
  }
  @Test
  public void micro828() {
    assertIdentical("{ as.complex(TRUE) }", "1+0i");
  }
  @Test
  public void micro829() {
    assertIdentical("{ as.complex('1+5i') }", "1+5i");
  }
  @Test
  public void micro830() {
    assertIdentical("{ as.complex('1e10+5i') }", "1e+10+5e+00i");
  }
  @Test
  public void micro831() {
    assertIdentical("{ as.complex('-1+5i') }", "-1+5i");
  }
  @Test
  public void micro832() {
    assertIdentical("{ as.complex('-1-5i') }", "-1-5i");
  }
  @Test
  public void micro833() {
    assertIdentical("{ as.complex('-.1e10+5i') }", "-1e+09+5e+00i");
  }
  @Test
  public void micro834() {
    assertIdentical("{ as.complex('1e-2+3i') }", "0.01+3i");
  }
  @Test
  public void micro835() {
    assertIdentical("{ as.complex('+.1e+2-3i') }", "10-3i");
  }
  @Test
  public void micro836() {
    assertIdentical("{ as.complex(0/0) }", " complex(real=NaN,im=0)");
  }
  @Test
  public void micro837() {
    assertIdentical("{ as.complex(c(0/0, 0/0)) }", "complex(real=c(NaN, NaN), im=c(0,0))");
  }
  @Test
  public void micro839() {
    assertIdentical("{ l <- 1 ; attr(l, 'my') <- 1; as.list(l) }", "list(1)");
  }
  @Test
  public void micro840() {
    assertIdentical("{ l <- c(x=1) ; as.list(l) }", "structure(list(x = 1), .Names = \"x\")");
  }
  @Test
  public void micro841() {
    assertIdentical("{ as.complex(as.character(c(1+1i,1+1i))) }", "c(1+1i, 1+1i)");
  }
  @Test
  public void micro842() {
    assertIdentical("{ as.complex(as.double(c(1+1i,1+1i))) }", "c(1+0i, 1+0i)");
  }
  @Test
  public void micro843() {
    assertIdentical("{ as.complex(as.integer(c(1+1i,1+1i))) }", "c(1+0i, 1+0i)");
  }
  @Test
  public void micro844() {
    assertIdentical("{ as.complex(as.logical(c(1+1i,1+1i))) }", "c(1+0i, 1+0i)");
  }
  @Test
  public void micro845() {
    assertIdentical("{ as.complex(as.raw(c(1+1i,1+1i))) }", "c(1+0i, 1+0i)");
  }
  @Test
  public void micro846() {
    assertIdentical("{ as.double(as.logical(c(10,10))) }", "c(1, 1)");
  }
  @Test
  public void micro847() {
    assertIdentical("{ as.integer(as.logical(-1:1)) }", "c(1L, 0L, 1L)");
  }
  @Test
  public void micro848() {
    assertIdentical("{ as.raw(as.logical(as.raw(c(1,2)))) }", "as.raw(c(0x01, 0x01))");
  }
  @Test
  public void micro849() {
    assertIdentical("{ as.character(as.double(1:5)) }", "c(\"1\", \"2\", \"3\", \"4\", \"5\")");
  }
  @Test
  public void micro850() {
    assertIdentical("{ as.character(as.complex(1:2)) }", "c(\"1+0i\", \"2+0i\")");
  }
  @Test
  public void micro851() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; as.double(m) }", "c(1, 2, 3, 4, 5, 6)");
  }
  @Test
  public void micro852() {
    assertIdentical("{ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; as.integer(m) }", "1:6");
  }
  @Test
  public void micro853() {
    assertIdentical("{ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; as.logical(m) }", "c(TRUE, TRUE, TRUE, TRUE, TRUE, TRUE)");
  }
  @Test
  public void micro854() {
    assertIdentical("{ x <- 1:2; names(x) <- c('hello','hi') ; as.double(x) }", "c(1, 2)");
  }
  @Test
  public void micro855() {
    assertIdentical("{ x <- c(1,2); names(x) <- c('hello','hi') ; as.integer(x) }", "1:2");
  }
  @Test
  public void micro856() {
    assertIdentical("{ x <- c(0,2); names(x) <- c('hello','hi') ; as.logical(x) }", "c(FALSE, TRUE)");
  }
  @Test
  public void micro859() {
    assertIdentical("{ x <- 1:3; z <- as.matrix(x); x }", "1:3");
  }
  @Test
  public void micro860() {
    assertIdentical("{ x <- 1:3 ; attr(x,'my') <- 10 ; attributes(as.matrix(x)) }", "structure(list(dim = c(3L, 1L)), .Names = \"dim\")");
  }
  @Test
  public void micro861() {
    assertIdentical("{ as.raw('09') }", "as.raw(0x09)");
  }
  @Test
  public void micro862() {
    assertIdentical("{ as.raw('077') }", "as.raw(0x4d)");
  }
  @Test
  public void micro863() {
    assertIdentical("{ as.raw('0004') }", "as.raw(0x04)");
  }
  @Test
  public void micro864() {
    assertIdentical("{ 5L:10L }", "5:10");
  }
  @Test
  public void micro865() {
    assertIdentical("{ 5L:(0L-5L) }", "c(5L, 4L, 3L, 2L, 1L, 0L, -1L, -2L, -3L, -4L, -5L)");
  }
  @Test
  public void micro866() {
    assertIdentical("{ 1:10 }", "1:10");
  }
  @Test
  public void micro867() {
    assertIdentical("{ 1:(0-10) }", "c(1L, 0L, -1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L)");
  }
  @Test
  public void micro868() {
    assertIdentical("{ 1L:(0-10) }", "c(1L, 0L, -1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L)");
  }
  @Test
  public void micro869() {
    assertIdentical("{ 1:(0L-10L) }", "c(1L, 0L, -1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L)");
  }
  @Test
  public void micro870() {
    assertIdentical("{ (0-12):1.5 }", "-12:1");
  }
  @Test
  public void micro871() {
    assertIdentical("{ 1.5:(0-12) }", "c(1.5, 0.5, -0.5, -1.5, -2.5, -3.5, -4.5, -5.5, -6.5, -7.5, -8.5, -9.5, -10.5, -11.5)");
  }
  @Test
  public void micro872() {
    assertIdentical("{ (0-1.5):(0-12) }", "c(-1.5, -2.5, -3.5, -4.5, -5.5, -6.5, -7.5, -8.5, -9.5, -10.5, -11.5)");
  }
  @Test
  public void micro873() {
    assertIdentical("{ 10:1 }", "c(10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L)");
  }
  @Test
  public void micro874() {
    assertIdentical("{ (0-5):(0-9) }", "c(-5L, -6L, -7L, -8L, -9L)");
  }
  @Test
  public void micro875() {
    assertIdentical("{ seq(1,10) }", "1:10");
  }
  @Test
  public void micro876() {
    assertIdentical("{ seq(10,1) }", "c(10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L)");
  }
  @Test
  public void micro877() {
    assertIdentical("{ seq(from=1,to=3) }", "1:3");
  }
  @Test
  public void micro878() {
    assertIdentical("{ seq(to=-1,from=-10) }", "-10:-1");
  }
  @Test
  public void micro879() {
    assertIdentical("{ seq(length.out=13.4) }", "1:14");
  }
  @Test
  public void micro880() {
    assertIdentical("{ seq(length.out=0) }", "integer(0)");
  }
  @Test
  public void micro881() {
    assertIdentical("{ seq(length.out=1) }", "1L");
  }
  @Test
  public void micro882() {
    assertIdentical("{ seq(along.with=10) }", "1L");
  }
  @Test
  public void micro883() {
    assertIdentical("{ seq(along.with=NA) }", "1L");
  }
  @Test
  public void micro884() {
    assertIdentical("{ seq(along.with=1:10) }", "1:10");
  }
  @Test
  public void micro885() {
    assertIdentical("{ seq(along.with=-3:-5) }", "1:3");
  }
  @Test
  public void micro886() {
    assertIdentical("{ seq(from=1.4) }", "1L");
  }
  @Test
  public void micro887() {
    assertIdentical("{ seq(from=1.7) }", "1L");
  }
  @Test
  public void micro888() {
    assertIdentical("{ seq(from=10:12) }", "1:3");
  }
  @Test
  public void micro889() {
    assertIdentical("{ seq(from=c(TRUE, FALSE)) }", "1:2");
  }
  @Test
  public void micro890() {
    assertIdentical("{ seq(from=TRUE, to=TRUE, length.out=0) }", "integer(0)");
  }
  @Test
  public void micro891() {
    assertIdentical("{ round(seq(from=10.5, to=15.4, length.out=4), digits=5) }", "c(10.5, 12.13333, 13.76667, 15.4)");
  }
  @Test
  public void micro892() {
    assertIdentical("{ seq(from=11, to=12, length.out=2) }", "c(11, 12)");
  }
  @Test
  public void micro893() {
    assertIdentical("{ seq(from=1,to=3,by=1) }", "c(1, 2, 3)");
  }
  @Test
  public void micro894() {
    assertIdentical("{ seq(from=-10,to=-5,by=2) }", "c(-10, -8, -6)");
  }
  @Test
  public void micro895() {
    assertIdentical("{ seq(from=-10.4,to=-5.8,by=2.1) }", "c(-10.4, -8.3, -6.2)");
  }
  @Test
  public void micro896() {
    assertIdentical("{ round(seq(from=3L,to=-2L,by=-4.2), digits=5) }", "c(3, -1.2)");
  }
  @Test
  public void micro897() {
    assertIdentical("{ seq(along=c(10,11,12)) }", "1:3");
  }
  @Test
  public void micro898() {
    assertIdentical("{ seq(1L,4L,2L) }", "c(1L, 3L)");
  }
  @Test
  public void micro899() {
    assertIdentical("{ seq(1,-4,-2) }", "c(1, -1, -3)");
  }
  @Test
  public void micro900() {
    assertIdentical("{ integer() }", "integer(0)");
  }
  @Test
  public void micro901() {
    assertIdentical("{ double() }", "numeric(0)");
  }
  @Test
  public void micro902() {
    assertIdentical("{ logical() }", "logical(0)");
  }
  @Test
  public void micro903() {
    assertIdentical("{ double(3) }", "c(0, 0, 0)");
  }
  @Test
  public void micro904() {
    assertIdentical("{ logical(3L) }", "c(FALSE, FALSE, FALSE)");
  }
  @Test
  public void micro905() {
    assertIdentical("{ character(1L) }", "\"\"");
  }
  @Test
  public void micro906() {
    assertIdentical("{ max(1:10, 100:200, c(4.0, 5.0)) }", "200");
  }
  @Test
  public void micro907() {
    assertIdentical("{ max((-1):100) }", "100L");
  }
  @Test
  public void micro908() {
    assertIdentical("{ max(1:10, 100:200, c(4.0, 5.0), c(TRUE,FALSE,NA)) }", "NA_real_");
  }
  @Test
  public void micro909() {
    assertIdentical("{ max(2L, 4L) }", "4L");
  }
  @Test
  public void micro910() {
    assertIdentical("{ max() }", "-Inf");
  }
  @Test
  public void micro911() {
    assertIdentical("{ max(c('hi','abbey','hello')) }", "\"hi\"");
  }
  @Test
  public void micro912() {
    assertIdentical("{ max('hi','abbey','hello') }", "\"hi\"");
  }
  @Test
  public void micro913() {
    assertIdentical("{ min((-1):100) }", "-1L");
  }
  @Test
  public void micro914() {
    assertIdentical("{ min(1:10, 100:200, c(4.0, -5.0)) }", "-5");
  }
  @Test
  public void micro915() {
    assertIdentical("{ min(1:10, 100:200, c(4.0, 5.0), c(TRUE,FALSE,NA)) }", "NA_real_");
  }
  @Test
  public void micro916() {
    assertIdentical("{ min(2L, 4L) }", "2L");
  }
  @Test
  public void micro917() {
    assertIdentical("{ min() }", "Inf");
  }
  @Test
  public void micro918() {
    assertIdentical("{ min(c('hi','abbey','hello')) }", "\"abbey\"");
  }
  @Test
  public void micro919() {
    assertIdentical("{ min('hi','abbey','hello') }", "\"abbey\"");
  }
  @Test
  public void micro920() {
    assertIdentical("{ min('hi',100) }", "\"100\"");
  }
  @Test
  public void micro921() {
    assertIdentical("{ min(c(1,2,0/0)) }", "NaN");
  }
  @Test
  public void micro922() {
    assertIdentical("{ max(c(1,2,0/0)) }", "NaN");
  }
  @Test
  public void micro923() {
    assertIdentical("{ rep(1,3) }", "c(1, 1, 1)");
  }
  @Test
  public void micro924() {
    assertIdentical("{ rep(1:3,2) }", "c(1L, 2L, 3L, 1L, 2L, 3L)");
  }
  @Test
  public void micro925() {
    assertIdentical("{ rep(c(1,2),0) }", "numeric(0)");
  }
  @Test
  public void micro926() {
    assertIdentical("{ rep(1:3, length.out=4) }", "c(1L, 2L, 3L, 1L)");
  }
  @Test
  public void micro927() {
    assertIdentical("{ rep(1:3, length.out=NA) }", "1:3");
  }
  @Test
  public void micro928() {
    assertIdentical("{ rep(as.raw(14), 4) }", "as.raw(c(0x0e, 0x0e, 0x0e, 0x0e))");
  }
  @Test
  public void micro929() {
    assertIdentical("{ x <- as.raw(11) ; names(x) <- c('X') ; rep(x, 3) }", "structure(as.raw(c(0x0b, 0x0b, 0x0b)), .Names = c(\"X\", \"X\", \"X\"))");
  }
  @Test
  public void micro930() {
    assertIdentical("{ x <- as.raw(c(11,12)) ; names(x) <- c('X','Y') ; rep(x, 2) }", "structure(as.raw(c(0x0b, 0x0c, 0x0b, 0x0c)), .Names = c(\"X\", \"Y\", \"X\", \"Y\"))");
  }
  @Test
  public void micro931() {
    assertIdentical("{ x <- c(TRUE,NA) ; names(x) <- c('X',NA) ; rep(x, length.out=3) }", "structure(c(TRUE, NA, TRUE), .Names = c(\"X\", NA, \"X\"))");
  }
  @Test
  public void micro933() {
    assertIdentical("{ x <- 1 ; names(x) <- c('X') ; rep(x, times=0) }", "structure(numeric(0), .Names = character(0))");
  }
  @Test
  public void micro934() {
    assertIdentical("{ x <- 1+1i ; names(x) <- c('X') ; rep(x, times=2) }", "structure(c(1+1i, 1+1i), .Names = c(\"X\", \"X\"))");
  }
  @Test
  public void micro935() {
    assertIdentical("{ x <- c(1+1i,1+2i) ; names(x) <- c('X') ; rep(x, times=2) }", "structure(c(1+1i, 1+2i, 1+1i, 1+2i), .Names = c(\"X\", NA, \"X\", NA))");
  }
  @Test
  public void micro936() {
    assertIdentical("{ x <- c('A','B') ; names(x) <- c('X') ; rep(x, length.out=3) }", "structure(c(\"A\", \"B\", \"A\"), .Names = c(\"X\", NA, \"X\"))");
  }
  @Test
  public void micro937() {
    assertIdentical("{ c(1.0,1L) }", "c(1, 1)");
  }
  @Test
  public void micro938() {
    assertIdentical("{ c(1L,1.0) }", "c(1, 1)");
  }
  @Test
  public void micro939() {
    assertIdentical("{ c(TRUE,1L,1.0,list(3,4)) }", "list(TRUE, 1L, 1, 3, 4)");
  }
  @Test
  public void micro940() {
    assertIdentical("{ c(TRUE,1L,1.0,list(3,list(4,5))) }", "list(TRUE, 1L, 1, 3, list(4, 5))");
  }
  @Test
  public void micro943() {
    assertIdentical("{ c(NULL,1,2,3) }", "c(1, 2, 3)");
  }
  @Test
  public void micro944() {
    assertIdentical("{ f <- function(x,y) { c(x,y) } ; f(1,1) ; f(1, TRUE) }", "c(1, 1)");
  }
  @Test
  public void micro946() {
    assertIdentical("{ c('hello', 'hi') }", "c(\"hello\", \"hi\")");
  }
  @Test
  public void micro947() {
    assertIdentical("{ c(1+1i, as.raw(10)) }", "c(1+1i, 10+0i)");
  }
  @Test
  public void micro948() {
    assertIdentical("{ c(as.raw(10), as.raw(20)) }", "as.raw(c(0x0a, 0x14))");
  }
  @Test
  public void micro949() {
    assertIdentical("{ c(x=1,y=2) }", "structure(c(1, 2), .Names = c(\"x\", \"y\"))");
  }
  @Test
  public void micro950() {
    assertIdentical("{ c(x=1,2) }", "structure(c(1, 2), .Names = c(\"x\", \"\"))");
  }
  @Test
  public void micro951() {
    assertIdentical("{ x <- 1:2 ; names(x) <- c('A',NA) ; c(x,test=x) }", "structure(c(1L, 2L, 1L, 2L), .Names = c(\"A\", NA, \"test.A\", \"test.NA\"))");
  }
  @Test
  public void micro952() {
    assertIdentical("{ c(a=1,b=2:3,list(x=FALSE)) }", "structure(list(a = 1, b1 = 2L, b2 = 3L, x = FALSE), .Names = c(\"a\", \"b1\", \"b2\", \"x\"))");
  }
  @Test
  public void micro953() {
    assertIdentical("{ c(1,z=list(1,b=22,3)) }", "structure(list(1, z1 = 1, z.b = 22, z3 = 3), .Names = c(\"\", \"z1\", \"z.b\", \"z3\"))");
  }
  @Test
  public void micro954() {
    assertIdentical("{ c(1i,0/0) }", "c(0+1i, complex(real=NaN, i=0))");
  }
  @Test
  public void micro955() {
    assertIdentical("{ is.na(c(1,2,3,4)) }", "c(FALSE, FALSE, FALSE, FALSE)");
  }
  @Test
  public void micro956() {
    assertIdentical("{ is.na(1[10]) }", "TRUE");
  }
  @Test
  public void micro957() {
    assertIdentical("{ is.na(c(1[10],2[10],3)) }", "c(TRUE, TRUE, FALSE)");
  }
  @Test
  public void micro958() {
    assertIdentical("{ is.na(list(1[10],1L[10],list(),integer())) }", "c(TRUE, TRUE, FALSE, FALSE)");
  }
  @Test
  public void micro959() {
    assertIdentical("{ sum(1:6, 3, 4) }", "28");
  }
  @Test
  public void micro960() {
    assertIdentical("{ sum(1:6, 3L, TRUE) }", "25L");
  }
  @Test
  public void micro961() {
    assertIdentical("{ sum() }", "0L");
  }
  @Test
  public void micro962() {
    assertIdentical("{ sum(0, 1[3]) }", "NA_real_");
  }
  @Test
  public void micro963() {
    assertIdentical("{ sum(na.rm=FALSE, 0, 1[3]) }", "NA_real_");
  }
  @Test
  public void micro964() {
    assertIdentical("{ sum(0, na.rm=FALSE, 1[3]) }", "NA_real_");
  }
  @Test
  public void micro965() {
    assertIdentical("{ sum(0, 1[3], na.rm=FALSE) }", "NA_real_");
  }
  @Test
  public void micro966() {
    assertIdentical("{ sum(0, 1[3], na.rm=TRUE) }", "0");
  }
  @Test
  public void micro967() {
    assertIdentical("{ `sum`(1:10) }", "55L");
  }
  @Test
  public void micro968() {
    assertIdentical("{ sum(1+1i,2,NA, na.rm=TRUE) }", "3+1i");
  }
  @Test
  public void micro969() {
    assertIdentical("{ lapply(1:3, function(x) { 2*x }) }", "list(2, 4, 6)");
  }
  @Test
  public void micro970() {
    assertIdentical("{ lapply(1:3, function(x,y) { x*y }, 2) }", "list(2, 4, 6)");
  }
  @Test
  public void micro971() {
    assertIdentical("{ sapply(1:3,function(x){x*2}) }", "c(2, 4, 6)");
  }
  @Test
  public void micro972() {
    assertIdentical("{ sapply(c(1,2,3),function(x){x*2}) }", "c(2, 4, 6)");
  }
  @Test
  public void micro973() {
    assertIdentical("{ sapply(list(1,2,3),function(x){x*2}) }", "c(2, 4, 6)");
  }
  @Test
  public void micro974() {
    assertIdentical("{ sapply(1:3, function(x) { if (x==1) { 1 } else if (x==2) { integer() } else { TRUE } }) }", "list(1, integer(0), TRUE)");
  }
  @Test
  public void micro975() {
    assertIdentical("{ f<-function(g) { sapply(1:3, g) } ; f(function(x) { x*2 }) }", "c(2, 4, 6)");
  }
  @Test
  public void micro976() {
    assertIdentical("{ f<-function(g) { sapply(1:3, g) } ; f(function(x) { x*2 }) ; f(function(x) { TRUE }) }", "c(TRUE, TRUE, TRUE)");
  }
  @Test
  public void micro977() {
    assertIdentical("{ sapply(1:3, function(x) { if (x==1) { list(1) } else if (x==2) { list(NULL) } else { list(2) } }) }", "list(1, NULL, 2)");
  }
  @Test
  public void micro978() {
    assertIdentical("{ sapply(1:3, function(x) { if (x==1) { list(1) } else if (x==2) { list(NULL) } else { list() } }) }", "list(list(1), list(NULL), list())");
  }
  @Test
  public void micro979() {
    assertIdentical("{ f<-function() { x<-2 ; sapply(1, function(i) { x }) } ; f() }", "2");
  }
  @Test
  public void micro980() {
    assertIdentical("{ sapply(1:3, length) }", "c(1L, 1L, 1L)");
  }
  @Test
  public void micro981() {
    assertIdentical("{ f<-length; sapply(1:3, f) }", "c(1L, 1L, 1L)");
  }
  @Test
  public void micro982() {
    assertIdentical("{ sapply(1:3, `-`, 2) }", "c(-1, 0, 1)");
  }
  @Test
  public void micro983() {
    assertIdentical("{ sapply(1:3, '-', 2) }", "c(-1, 0, 1)");
  }
  @Test
  public void micro984() {
    assertIdentical("{ sapply(1:2, function(i) { if (i==1) { as.raw(0) } else { as.raw(10) } }) }", "as.raw(c(0x00, 0x0a))");
  }
  @Test
  public void micro986() {
    assertIdentical("{ sapply(1:2, function(i) { if (i==1) { as.raw(0) } else { 5+10i } }) }", "c(0+0i, 5+10i)");
  }
  @Test
  public void micro990() {
    assertIdentical("{ ( sapply(1:3, function(i) { if (i < 3) { list(xxx=1) } else {list(zzz=2)} })) }", "structure(list(xxx = 1, xxx = 1, zzz = 2), .Names = c(\"xxx\", \"xxx\", \"zzz\"))");
  }
  @Test
  public void micro991() {
    assertIdentical("{ ( sapply(1:3, function(i) { list(xxx=1:i) } )) }", "structure(list(xxx = 1L, xxx = 1:2, xxx = 1:3), .Names = c(\"xxx\", \"xxx\", \"xxx\"))");
  }
  @Test
  public void micro992() {
    assertIdentical("{ sapply(1:3, function(i) { if (i < 3) { list(xxx=1) } else {list(2)} }) }", "structure(list(xxx = 1, xxx = 1, 2), .Names = c(\"xxx\", \"xxx\", \"\"))");
  }
  @Test
  public void micro993() {
    assertIdentical("{ ( sapply(1:3, function(i) { if (i < 3) { c(xxx=1) } else {c(2)} })) }", "structure(c(1, 1, 2), .Names = c(\"xxx\", \"xxx\", \"\"))");
  }
  @Test
  public void micro994() {
    assertIdentical("{ f <- function() { lapply(c(X='a',Y='b'), function(x) { c(a=x) })  } ; f() }", "structure(list(X = structure(\"a\", .Names = \"a\"), Y = structure(\"b\", .Names = \"a\")), .Names = c(\"X\", \"Y\"))");
  }
  @Test
  public void micro995() {
    assertIdentical("{ f <- function() { sapply(c(1,2), function(x) { c(a=x) })  } ; f() }", "structure(c(1, 2), .Names = c(\"a\", \"a\"))");
  }
  @Test
  public void micro996() {
    assertIdentical("{ f <- function() { sapply(c(X=1,Y=2), function(x) { c(a=x) })  } ; f() }", "structure(c(1, 2), .Names = c(\"X.a\", \"Y.a\"))");
  }
  @Test
  public void micro997() {
    assertIdentical("{ f <- function() { sapply(c('a','b'), function(x) { c(a=x) })  } ; f() }", "structure(c(\"a\", \"b\"), .Names = c(\"a.a\", \"b.a\"))");
  }
  @Test
  public void micro998() {
    assertIdentical("{ f <- function() { sapply(c(X='a',Y='b'), function(x) { c(a=x) })  } ; f() }", "structure(c(\"a\", \"b\"), .Names = c(\"X.a\", \"Y.a\"))");
  }
  @Test
  public void micro999() {
    assertIdentical("{ sapply(c('a','b','c'), function(x) { x }) }", "structure(c(\"a\", \"b\", \"c\"), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro1000() {
    assertIdentical("{ sapply(c(a=1,b=2,`c+`=3), function(x) { c(x=x*x) }) }", "structure(c(1, 4, 9), .Names = c(\"a.x\", \"b.x\", \"c+.x\"))");
  }
  @Test
  public void micro1001() {
    assertIdentical("{ sapply(c(a=1,2,3,`c+`=3), function(x) { c(x=x*x) }) }", "structure(c(1, 4, 9, 9), .Names = c(\"a.x\", \"x\", \"x\", \"c+.x\"))");
  }
  @Test
  public void micro1002() {
    assertIdentical("{ sapply(c(a=1,2,3,`c+`=3), function(x) { c(x*x) }) }", "structure(c(1, 4, 9, 9), .Names = c(\"a\", \"\", \"\", \"c+\"))");
  }
  @Test
  public void micro1003() {
    assertIdentical("{ sapply(c(a=1,2,3,`c+`=3), function(x) { rep(x,x) }) }", "structure(list(a = 1, c(2, 2), c(3, 3, 3), \"c+\" = c(3, 3, 3)), .Names = c(\"a\", \"\", \"\", \"c+\"))");
  }
  @Test
  public void micro1004() {
    assertIdentical("{ l <- (sapply(c(a=1,2,3,`c+`=4), function(i) { if (i==1) { c(x=5) } else if (i==2) {c(z=5) } else if (i==3) { c(1) } else { list(`c+`=3) } })) ; names(l) }", "c(\"a.x\", \"z\", \"\", \"c+.c+\")");
  }
  @Test
  public void micro1005() {
    assertIdentical("{ l <- (sapply(c(a=1,2,3,`c+`=4), function(i) { if (i==1) { c(x=5) } else if (i==2) {c(z=5) } else if (i==3) { c(1) } else { list(`c+`=3,d=5) } })) ; l }", "structure(list(a = structure(5, .Names = \"x\"), structure(5, .Names = \"z\"),     1, \"c+\" = structure(list(\"c+\" = 3, d = 5), .Names = c(\"c+\",     \"d\"))), .Names = c(\"a\", \"\", \"\", \"c+\"))");
  }
  @Test
  public void micro1006() {
    assertIdentical("{ l <- (sapply(c(a=1,2,3,`c+`=4), function(i) { if (i==1) { list(x=5) } else if (i==2) {list(z=5) } else if (i==3) { list(1) } else { list(`c+`=3) } })) ; l }", "structure(list(a.x = 5, z = 5, 1, \"c+.c+\" = 3), .Names = c(\"a.x\", \"z\", \"\", \"c+.c+\"))");
  }
  @Test
  public void micro1007() {
    assertIdentical("{ sapply(c(a=1,2,3,`c+`=3), function(x) { as.raw(x) }) }", "structure(as.raw(c(0x01, 0x02, 0x03, 0x03)), .Names = c(\"a\", \"\", \"\", \"c+\"))");
  }
  @Test
  public void micro1008() {
    assertIdentical("{ sapply(c(a=1,2,3,`c+`=3), function(x) { list(z=NULL) }) }", "structure(list(a.z = NULL, z = NULL, z = NULL, \"c+.z\" = NULL), .Names = c(\"a.z\", \"z\", \"z\", \"c+.z\"))");
  }
  @Test
  public void micro1009() {
    assertIdentical("{ sapply(c(a=1,2,3,`c+`=3), function(x) { NULL }) }", "structure(list(a = NULL, NULL, NULL, \"c+\" = NULL), .Names = c(\"a\", \"\", \"\", \"c+\"))");
  }
  @Test
  public void micro1014() {
    assertIdentical("{ l <- list(as.raw(11), TRUE, 2L, 3, 4+1i, 'a') ; sapply(1:6, function(i) { l[[i]] } ) }", "c(\"0b\", \"TRUE\", \"2\", \"3\", \"4+1i\", \"a\")");
  }
  @Test
  public void micro1015() {
    assertIdentical("{ l <- list(as.raw(11), TRUE, 2L, 3, 4+1i) ; sapply(1:5, function(i) { l[[i]] } ) }", "c(11+0i, 1+0i, 2+0i, 3+0i, 4+1i)");
  }
  @Test
  public void micro1016() {
    assertIdentical("{ l <- list(as.raw(11), TRUE, 2L, 4) ; sapply(1:4, function(i) { l[[i]] } ) }", "c(11, 1, 2, 4)");
  }
  @Test
  public void micro1017() {
    assertIdentical("{ l <- list(as.raw(11), TRUE, 2L) ; sapply(1:3, function(i) { l[[i]] } ) }", "c(11L, 1L, 2L)");
  }
  @Test
  public void micro1018() {
    assertIdentical("{ l <- list(as.raw(11), TRUE) ; sapply(1:2, function(i) { l[[i]] } ) }", "c(TRUE, TRUE)");
  }
  @Test
  public void micro1019() {
    assertIdentical("{ sapply(1:3, function(i) { rep(i, i+1) }) }", "list(c(1L, 1L), c(2L, 2L, 2L), c(3L, 3L, 3L, 3L))");
  }
  @Test
  public void micro1020() {
    assertIdentical("{ for (z in list(1:3,list(1L,5,10))) { x <- sapply(1:3, function(i,z) { i+z[[i]] }, z) } ; x }", "c(2, 7, 13)");
  }
  @Test
  public void micro1021() {
    assertIdentical("{ for (z in list(list(list(10,11),list(11,12)),1:2)) { x <- sapply(1:2, function(i,z) { z[[i]] }, z) } ; x }", "1:2");
  }
  @Test
  public void micro1022() {
    assertIdentical("{ for (z in list(list(1:2,1:3),list(list(10,11),list(11,12)),1:2)) { x <- sapply(1:2, function(i,z) { z[[i]] }, z) } ; x }", "1:2");
  }
  @Test
  public void micro1023() {
    assertIdentical("{ for(i in 1:2) { x <- sapply(1:2, function(i) { l <- list(1:2,NULL) ; l[[i]] }) } ; x }", "list(1:2, NULL)");
  }
  @Test
  public void micro1024() {
    assertIdentical("{ typeof({ for(i in 1:2) { x <- sapply(1:2, function(i) { l <- list(1:2,list(1,2)) ; l[[i]] }) } ; x }) }", "\"list\"");
  }
  @Test
  public void micro1026() {
    assertIdentical("{ for (z in list(c(TRUE,FALSE,NA),c(NA,FALSE,FALSE),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) } ; x }", "c(1, 2, 3)");
  }
  @Test
  public void micro1027() {
    assertIdentical("{ for (z in list(c('a','b','x'),c('z','z','y'),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) } ; x }", "c(1, 2, 3)");
  }
  @Test
  public void micro1028() {
    assertIdentical("{ for (z in list(c(1+2i,3+4i,5+6i),c(2+2i,3,4),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) } ; x }", "c(1, 2, 3)");
  }
  @Test
  public void micro1029() {
    assertIdentical("{ y <- NULL ; for (z in list(c(1+2i,3+4i,5+6i),c(2+2i,3,4),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) ; if (is.null(y)) { y <- x } } ; y }", "c(1+2i, 3+4i, 5+6i)");
  }
  @Test
  public void micro1030() {
    assertIdentical("{ y <- NULL ; for (z in list(as.raw(11:13),as.raw(21:23),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) ; if (is.null(y)) { y <- x } } ; y }", "as.raw(c(0x0b, 0x0c, 0x0d))");
  }
  @Test
  public void micro1031() {
    assertIdentical("{ for (idxs in list(1:3, c(1,2,3), c('a','x','z'))) { x <- sapply(idxs, function(i) { i }) } ; x }", "structure(c(\"a\", \"x\", \"z\"), .Names = c(\"a\", \"x\", \"z\"))");
  }
  @Test
  public void micro1032() {
    assertIdentical("{ for (idxs in list(1:3, c(1,2,3), c(Z='a','x','z'))) { x <- lapply(idxs, function(i) { i }) } ; x }", "structure(list(Z = \"a\", \"x\", \"z\"), .Names = c(\"Z\", \"\", \"\"))");
  }
  @Test
  public void micro1033() {
    assertIdentical("{ for (idxs in list(as.list(1:3), c(1,2,3), c(Z='a','x','z'))) { x <- lapply(idxs, function(i) { i }) } ; x }", "structure(list(Z = \"a\", \"x\", \"z\"), .Names = c(\"Z\", \"\", \"\"))");
  }
  @Test
  public void micro1034() {
    assertIdentical("{ for (idxs in list(as.list(1:3), c(1,2,3), list(Z='a','x','z'))) { x <- lapply(idxs, function(i) { i }) } ; x }", "structure(list(Z = \"a\", \"x\", \"z\"), .Names = c(\"Z\", \"\", \"\"))");
  }
  @Test
  public void micro1047() {
    assertIdentical("{ `+`(1,2) }", "3");
  }
  @Test
  public void micro1048() {
    assertIdentical("{ `-`(1,2) }", "-1");
  }
  @Test
  public void micro1049() {
    assertIdentical("{ `*`(1,2) }", "2");
  }
  @Test
  public void micro1050() {
    assertIdentical("{ `/`(1,2) }", "0.5");
  }
  @Test
  public void micro1051() {
    assertIdentical("{ `%/%`(1,2) }", "0");
  }
  @Test
  public void micro1052() {
    assertIdentical("{ `%%`(1,2) }", "1");
  }
  @Test
  public void micro1053() {
    assertIdentical("{ `^`(1,2) }", "1");
  }
  @Test
  public void micro1054() {
    assertIdentical("{ `!`(TRUE) }", "FALSE");
  }
  @Test
  public void micro1055() {
    assertIdentical("{ `||`(TRUE, FALSE) }", "TRUE");
  }
  @Test
  public void micro1056() {
    assertIdentical("{ `&&`(TRUE, FALSE) }", "FALSE");
  }
  @Test
  public void micro1057() {
    assertIdentical("{ `|`(TRUE, FALSE) }", "TRUE");
  }
  @Test
  public void micro1058() {
    assertIdentical("{ `&`(TRUE, FALSE) }", "FALSE");
  }
  @Test
  public void micro1061() {
    assertIdentical("{ x <- `+` ; x(2,3) }", "5");
  }
  @Test
  public void micro1062() {
    assertIdentical("{ x <- `+` ; f <- function() { x <- 1 ; x(2,3) } ; f() }", "5");
  }
  @Test
  public void micro1079() {
    assertIdentical("{ m <- matrix(1:6, nrow=3) ; dim(m) }", "c(3L, 2L)");
  }
  @Test
  public void micro1082() {
    assertIdentical("{ m <- matrix(1:6, nrow=3) ; nrow(m) }", "3L");
  }
  @Test
  public void micro1085() {
    assertIdentical("{ m <- matrix(1:6, nrow=3) ; ncol(m) }", "2L");
  }
  @Test
  public void micro1087() {
    assertIdentical("{ z <- 1 ; dim(z) <- c(1,1) ; dim(z) <- NULL ; z }", "1");
  }
  @Test
  public void micro1090() {
    assertIdentical("{ cumsum(1:10) }", "c(1L, 3L, 6L, 10L, 15L, 21L, 28L, 36L, 45L, 55L)");
  }
  @Test
  public void micro1091() {
    assertIdentical("{ cumsum(c(1,2,3)) }", "c(1, 3, 6)");
  }
  @Test
  public void micro1092() {
    assertIdentical("{ cumsum(rep(1e308, 3) ) }", "c(1e+308, Inf, Inf)");
  }
  @Test
  public void micro1093() {
    assertIdentical("{ cumsum(NA) }", "NA_integer_");
  }
  @Test
  public void micro1094() {
    assertIdentical("{ cumsum(c(1e308, 1e308, NA, 1, 2)) }", "c(1e+308, Inf, NA, NA, NA)");
  }
  @Test
  public void micro1095() {
    assertIdentical("{ cumsum(c(2000000000L, 2000000000L)) }", "c(2000000000L, NA)");
  }
  @Test
  public void micro1096() {
    assertIdentical("{ cumsum(c(2000000000L, NA, 2000000000L)) }", "c(2000000000L, NA, NA)");
  }
  @Test
  public void micro1097() {
    assertIdentical("{ cumsum(as.logical(-2:2)) }", "c(1L, 2L, 2L, 3L, 4L)");
  }
  @Test
  public void micro1098() {
    assertIdentical("{ cumsum((1:6)*(1+1i)) }", "c(1+1i, 3+3i, 6+6i, 10+10i, 15+15i, 21+21i)");
  }
  @Test
  public void micro1099() {
    assertIdentical("{ cumsum(as.raw(1:6)) }", "c(1, 3, 6, 10, 15, 21)");
  }
  @Test
  public void micro1100() {
    assertIdentical("{ cumsum(c(1,2,3,0/0,5)) }", "c(1, 3, 6, NaN, NaN)");
  }
  @Test
  public void micro1101() {
    assertIdentical("{ cumsum(c(1,0/0,5+1i)) }", "c(1+0i, complex(real=NaN, i=0), complex(real=NaN, i=1))");
  }
  @Test
  public void micro1102() {
    assertIdentical("{ which(c(TRUE, FALSE, NA, TRUE)) }", "c(1L, 4L)");
  }
  @Test
  public void micro1103() {
    assertIdentical("{ which(logical()) }", "integer(0)");
  }
  @Test
  public void micro1104() {
    assertIdentical("{ which(c(a=TRUE,b=FALSE,c=TRUE)) }", "structure(c(1L, 3L), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro1105() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; colMeans(m) }", "c(1.5, 3.5, 5.5)");
  }
  @Test
  public void micro1106() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; colSums(na.rm = FALSE, x = m) }", "c(3, 7, 11)");
  }
  @Test
  public void micro1107() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; rowMeans(x = m, na.rm = TRUE) }", "c(3, 4)");
  }
  @Test
  public void micro1108() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; rowSums(x = m) }", "c(9, 12)");
  }
  @Test
  public void micro1109() {
    assertIdentical("{ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; colMeans(m) }", "c(1.5, 3.5, 5.5)");
  }
  @Test
  public void micro1110() {
    assertIdentical("{ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; colSums(m) }", "c(3, 7, 11)");
  }
  @Test
  public void micro1111() {
    assertIdentical("{ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; rowMeans(m) }", "c(3, 4)");
  }
  @Test
  public void micro1112() {
    assertIdentical("{ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; rowSums(m) }", "c(9, 12)");
  }
  @Test
  public void micro1113() {
    assertIdentical("{ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; rowSums(m) }", "c(NA, 12)");
  }
  @Test
  public void micro1114() {
    assertIdentical("{ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; rowSums(m, na.rm = TRUE) }", "c(3, 12)");
  }
  @Test
  public void micro1115() {
    assertIdentical("{ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; rowMeans(m, na.rm = TRUE) }", "c(3, 4)");
  }
  @Test
  public void micro1116() {
    assertIdentical("{ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colSums(m) }", "c(NA, 7, NA)");
  }
  @Test
  public void micro1117() {
    assertIdentical("{ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colSums(na.rm = TRUE, m) }", "c(2, 7, 6)");
  }
  @Test
  public void micro1118() {
    assertIdentical("{ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colMeans(m) }", "c(NA, 3.5, NA)");
  }
  @Test
  public void micro1119() {
    assertIdentical("{ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colMeans(m, na.rm = TRUE) }", "c(2, 3.5, 6)");
  }
  @Test
  public void micro1120() {
    assertIdentical("{ colSums(matrix(as.complex(1:6), nrow=2)) }", "c(3+0i, 7+0i, 11+0i)");
  }
  @Test
  public void micro1121() {
    assertIdentical("{ colSums(matrix((1:6)*(1+1i), nrow=2)) }", "c(3+3i, 7+7i, 11+11i)");
  }
  @Test
  public void micro1122() {
    assertIdentical("{ colMeans(matrix(as.complex(1:6), nrow=2)) }", "c(1.5+0i, 3.5+0i, 5.5+0i)");
  }
  @Test
  public void micro1123() {
    assertIdentical("{ colMeans(matrix((1:6)*(1+1i), nrow=2)) }", "c(1.5+1.5i, 3.5+3.5i, 5.5+5.5i)");
  }
  @Test
  public void micro1124() {
    assertIdentical("{ rowSums(matrix(as.complex(1:6), nrow=2)) }", "c(9+0i, 12+0i)");
  }
  @Test
  public void micro1125() {
    assertIdentical("{ rowSums(matrix((1:6)*(1+1i), nrow=2)) }", "c(9+9i, 12+12i)");
  }
  @Test
  public void micro1126() {
    assertIdentical("{ rowMeans(matrix(as.complex(1:6), nrow=2)) }", "c(3+0i, 4+0i)");
  }
  @Test
  public void micro1127() {
    assertIdentical("{ rowMeans(matrix((1:6)*(1+1i), nrow=2)) }", "c(3+3i, 4+4i)");
  }
  @Test
  public void micro1128() {
    assertIdentical("{ o <- outer(1:3, 1:4, '<') ; colSums(o) }", "c(0, 1, 2, 3)");
  }
  @Test
  public void micro1129() {
    assertIdentical("{ nchar(c('hello', 'hi')) }", "c(5L, 2L)");
  }
  @Test
  public void micro1130() {
    assertIdentical("{ nchar(c('hello', 'hi', 10, 130)) }", "c(5L, 2L, 2L, 3L)");
  }
  @Test
  public void micro1131() {
    assertIdentical("{ nchar(c(10,130)) }", "2:3");
  }
  @Test
  public void micro1132() {
    assertIdentical("{ strsplit('helloh', 'h', fixed=TRUE) }", "list(c(\"\", \"ello\"))");
  }
  @Test
  public void micro1133() {
    assertIdentical("{ strsplit( c('helloh', 'hi'), c('h',''), fixed=TRUE) }", "list(c(\"\", \"ello\"), c(\"h\", \"i\"))");
  }
  @Test
  public void micro1134() {
    assertIdentical("{ strsplit('helloh', '', fixed=TRUE) }", "list(c(\"h\", \"e\", \"l\", \"l\", \"o\", \"h\"))");
  }
  @Test
  public void micro1135() {
    assertIdentical("{ strsplit('helloh', 'h') }", "list(c(\"\", \"ello\"))");
  }
  @Test
  public void micro1136() {
    assertIdentical("{ strsplit( c('helloh', 'hi'), c('h','')) }", "list(c(\"\", \"ello\"), c(\"h\", \"i\"))");
  }
  @Test
  public void micro1137() {
    assertIdentical("{ strsplit('ahoj', split='') [[c(1,2)]] }", "\"h\"");
  }
  @Test
  public void micro1138() {
    assertIdentical("{ paste(1:2, 1:3, FALSE, collapse=NULL) }", "c(\"1 1 FALSE\", \"2 2 FALSE\", \"1 3 FALSE\")");
  }
  @Test
  public void micro1139() {
    assertIdentical("{ paste(1:2, 1:3, FALSE, collapse='-', sep='+') }", "\"1+1+FALSE-2+2+FALSE-1+3+FALSE\"");
  }
  @Test
  public void micro1140() {
    assertIdentical("{ paste() }", "character(0)");
  }
  @Test
  public void micro1141() {
    assertIdentical("{ paste(sep='') }", "character(0)");
  }
  @Test
  public void micro1142() {
    assertIdentical("{ a <- as.raw(200) ; b <- as.raw(255) ; paste(a, b) }", "\"c8 ff\"");
  }
  @Test
  public void micro1143() {
    assertIdentical("{ file.path('a', 'b', c('d','e','f')) }", "c(\"a/b/d\", \"a/b/e\", \"a/b/f\")");
  }
  @Test
  public void micro1144() {
    assertIdentical("{ file.path() }", "character(0)");
  }
  @Test
  public void micro1145() {
    assertIdentical("{ substr('123456', start=2, stop=4) }", "\"234\"");
  }
  @Test
  public void micro1146() {
    assertIdentical("{ substr('123456', start=2L, stop=4L) }", "\"234\"");
  }
  @Test
  public void micro1147() {
    assertIdentical("{ substr('123456', start=2.8, stop=4) }", "\"234\"");
  }
  @Test
  public void micro1148() {
    assertIdentical("{ substr(c('hello', 'bye'), start=c(1,2,3), stop=4) }", "c(\"hell\", \"ye\")");
  }
  @Test
  public void micro1149() {
    assertIdentical("{ substr('fastr', start=NA, stop=2) }", "NA_character_");
  }
  @Test
  public void micro1150() {
    assertIdentical("{ substring('123456', first=2, last=4) }", "\"234\"");
  }
  @Test
  public void micro1151() {
    assertIdentical("{ substring('123456', first=2.8, last=4) }", "\"234\"");
  }
  @Test
  public void micro1152() {
    assertIdentical("{ substring(c('hello', 'bye'), first=c(1,2,3), last=4) }", "c(\"hell\", \"ye\", \"ll\")");
  }
  @Test
  public void micro1153() {
    assertIdentical("{ substring('fastr', first=NA, last=2) }", "NA_character_");
  }
  @Test
  public void micro1154() {
    assertIdentical("{ order(1:3) }", "1:3");
  }
  @Test
  public void micro1155() {
    assertIdentical("{ order(3:1) }", "c(3L, 2L, 1L)");
  }
  @Test
  public void micro1156() {
    assertIdentical("{ order(c(1,1,1), 3:1) }", "c(3L, 2L, 1L)");
  }
  @Test
  public void micro1157() {
    assertIdentical("{ order(c(1,1,1), 3:1, decreasing=FALSE) }", "c(3L, 2L, 1L)");
  }
  @Test
  public void micro1158() {
    assertIdentical("{ order(c(1,1,1), 3:1, decreasing=TRUE, na.last=TRUE) }", "1:3");
  }
  @Test
  public void micro1159() {
    assertIdentical("{ order(c(1,1,1), 3:1, decreasing=TRUE, na.last=NA) }", "1:3");
  }
  @Test
  public void micro1160() {
    assertIdentical("{ order(c(1,1,1), 3:1, decreasing=TRUE, na.last=FALSE) }", "1:3");
  }
  @Test
  public void micro1162() {
    assertIdentical("{ order(c(NA,NA,1), c(2,1,3)) }", "c(3L, 2L, 1L)");
  }
  @Test
  public void micro1163() {
    assertIdentical("{ order(c(NA,NA,1), c(1,2,3)) }", "c(3L, 1L, 2L)");
  }
  @Test
  public void micro1164() {
    assertIdentical("{ order(c(1,2,3,NA)) }", "1:4");
  }
  @Test
  public void micro1165() {
    assertIdentical("{ order(c(1,2,3,NA), na.last=FALSE) }", "c(4L, 1L, 2L, 3L)");
  }
  @Test
  public void micro1166() {
    assertIdentical("{ order(c(1,2,3,NA), na.last=FALSE, decreasing=TRUE) }", "c(4L, 3L, 2L, 1L)");
  }
  @Test
  public void micro1167() {
    assertIdentical("{ order(c(0/0, -1/0, 2)) }", "c(2L, 3L, 1L)");
  }
  @Test
  public void micro1168() {
    assertIdentical("{ order(c(0/0, -1/0, 2), na.last=NA) }", "2:3");
  }
  @Test
  public void micro1172() {
    assertIdentical("{ round( log(10,2), digits = 5 ) }", "3.32193");
  }
  @Test
  public void micro1173() {
    assertIdentical("{ round( log(10,10), digits = 5 ) }", "1");
  }
  @Test
  public void micro1175() {
    assertIdentical("{ x <- c(a=1, b=10) ; round( c(log(x), log10(x), log2(x)), digits=5 ) }", "structure(c(0, 2.30259, 0, 1, 0, 3.32193), .Names = c(\"a\", \"b\", \"a\", \"b\", \"a\", \"b\"))");
  }
  @Test
  public void micro1176() {
    assertIdentical("{ sqrt(c(a=9,b=81)) }", "structure(c(3, 9), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro1177() {
    assertIdentical("{ round( exp(c(1+1i,-2-3i)), digits=5 ) }", "c(1.46869+2.28736i, -0.13398-0.0191i)");
  }
  @Test
  public void micro1178() {
    assertIdentical("{ round( exp(1+2i), digits=5 ) }", "-1.1312+2.47173i");
  }
  @Test
  public void micro1181() {
    assertIdentical("{ abs(NA+0.1) }", "NA_real_");
  }
  @Test
  public void micro1182() {
    assertIdentical("{ abs(0/0) }", "NaN");
  }
  @Test
  public void micro1184() {
    assertIdentical("{ abs(c(0/0,1i)) }", "c(NaN, 1)");
  }
  @Test
  public void micro1185() {
    assertIdentical("{ abs((0+0i)/0) }", "NaN");
  }
  @Test
  public void micro1187() {
    assertIdentical("{ floor(c(0.2,-3.4)) }", "c(0, -4)");
  }
  @Test
  public void micro1188() {
    assertIdentical("{ ceiling(c(0.2,-3.4,NA,0/0,1/0)) }", "c(1, -3, NA, NaN, Inf)");
  }
  @Test
  public void micro1189() {
    assertIdentical("{ toupper(c('hello','bye')) }", "c(\"HELLO\", \"BYE\")");
  }
  @Test
  public void micro1190() {
    assertIdentical("{ tolower(c('Hello','ByE')) }", "c(\"hello\", \"bye\")");
  }
  @Test
  public void micro1193() {
    assertIdentical("{ tolower(c()) }", "character(0)");
  }
  @Test
  public void micro1194() {
    assertIdentical("{ tolower(NA) }", "NA_character_");
  }
  @Test
  public void micro1196() {
    assertIdentical("{ toupper(c(a='hi', 'hello')) }", "structure(c(\"HI\", \"HELLO\"), .Names = c(\"a\", \"\"))");
  }
  @Test
  public void micro1197() {
    assertIdentical("{ tolower(c(a='HI', 'HELlo')) }", "structure(c(\"hi\", \"hello\"), .Names = c(\"a\", \"\"))");
  }
  @Test
  public void micro1198() {
    assertIdentical("{ typeof(1) }", "\"double\"");
  }
  @Test
  public void micro1199() {
    assertIdentical("{ typeof(1L) }", "\"integer\"");
  }
  @Test
  public void micro1200() {
    assertIdentical("{ typeof(sum) }", "\"builtin\"");
  }
  @Test
  public void micro1201() {
    assertIdentical("{ typeof(function(){}) }", "\"closure\"");
  }
  @Test
  public void micro1202() {
    assertIdentical("{ typeof('hi') }", "\"character\"");
  }
  @Test
  public void micro1203() {
    assertIdentical("{ gsub('a','aa', 'prague alley', fixed=TRUE) }", "\"praague aalley\"");
  }
  @Test
  public void micro1204() {
    assertIdentical("{ sub('a','aa', 'prague alley', fixed=TRUE) }", "\"praague alley\"");
  }
  @Test
  public void micro1205() {
    assertIdentical("{ gsub('a','aa', 'prAgue alley', fixed=TRUE) }", "\"prAgue aalley\"");
  }
  @Test
  public void micro1206() {
    assertIdentical("{ gsub('a','aa', 'prAgue alley', fixed=TRUE, ignore.case=TRUE) }", "\"prAgue aalley\"");
  }
  @Test
  public void micro1207() {
    assertIdentical("{ gsub('h','', c('hello', 'hi', 'bye'), fixed=TRUE) }", "c(\"ello\", \"i\", \"bye\")");
  }
  @Test
  public void micro1208() {
    assertIdentical("{ gsub('a','aa', 'prague alley') }", "\"praague aalley\"");
  }
  @Test
  public void micro1209() {
    assertIdentical("{ sub('a','aa', 'prague alley') }", "\"praague alley\"");
  }
  @Test
  public void micro1210() {
    assertIdentical("{ gsub('a','aa', 'prAgue alley') }", "\"prAgue aalley\"");
  }
  @Test
  public void micro1211() {
    assertIdentical("{ gsub('a','aa', 'prAgue alley', ignore.case=TRUE) }", "\"praague aalley\"");
  }
  @Test
  public void micro1213() {
    assertIdentical("{ gsub('([a-e])','\\\\1\\\\1', 'prague alley') }", "\"praaguee aalleey\"");
  }
  @Test
  public void micro1216() {
    assertIdentical("{ x <- 1:4 ; length(x) <- 2 ; x }", "1:2");
  }
  @Test
  public void micro1217() {
    assertIdentical("{ x <- 1:2 ; length(x) <- 4 ; x }", "c(1L, 2L, NA, NA)");
  }
  @Test
  public void micro1218() {
    assertIdentical("{ x <- 1:2 ; z <- (length(x) <- 4) ; z }", "4");
  }
  @Test
  public void micro1219() {
    assertIdentical("{ length(c(z=1:4)) }", "4L");
  }
  @Test
  public void micro1220() {
    assertIdentical("{ x <- 1 ; f <- function() { length(x) <<- 2 } ; f() ; x }", "c(1, NA)");
  }
  @Test
  public void micro1222() {
    assertIdentical("{ x <- 1:2 ; names(x) <- c('hello'); names(x) }", "c(\"hello\", NA)");
  }
  @Test
  public void micro1223() {
    assertIdentical("{ x <- 1:2; names(x) <- c('hello', 'hi') ; x }", "structure(1:2, .Names = c(\"hello\", \"hi\"))");
  }
  @Test
  public void micro1224() {
    assertIdentical("{ x <- c(1,9); names(x) <- c('hello','hi') ; sqrt(x) }", "structure(c(1, 3), .Names = c(\"hello\", \"hi\"))");
  }
  @Test
  public void micro1225() {
    assertIdentical("{ x <- c(1,9); names(x) <- c('hello','hi') ; is.na(x) }", "structure(c(FALSE, FALSE), .Names = c(\"hello\", \"hi\"))");
  }
  @Test
  public void micro1226() {
    assertIdentical("{ x <- c(1,NA); names(x) <- c('hello','hi') ; cumsum(x) }", "structure(c(1, NA), .Names = c(\"hello\", \"hi\"))");
  }
  @Test
  public void micro1227() {
    assertIdentical("{ x <- c(1,NA); names(x) <- c(NA,'hi') ; cumsum(x) }", "structure(c(1, NA), .Names = c(NA, \"hi\"))");
  }
  @Test
  public void micro1228() {
    assertIdentical("{ x <- c(1,2); names(x) <- c('A', 'B') ; x + 1 }", "structure(c(2, 3), .Names = c(\"A\", \"B\"))");
  }
  @Test
  public void micro1229() {
    assertIdentical("{ x <- 1:2; names(x) <- c('A', 'B') ; y <- c(1,2,3,4) ; names(y) <- c('X', 'Y', 'Z') ; x + y }", "structure(c(2, 4, 4, 6), .Names = c(\"X\", \"Y\", \"Z\", NA))");
  }
  @Test
  public void micro1230() {
    assertIdentical("{ x <- 1:2; names(x) <- c('A', 'B') ; abs(x) }", "structure(1:2, .Names = c(\"A\", \"B\"))");
  }
  @Test
  public void micro1231() {
    assertIdentical("{ z <- c(a=1, b=2) ; names(z) <- NULL ; z }", "c(1, 2)");
  }
  @Test
  public void micro1232() {
    assertIdentical("{ rev(c(1+1i, 2+2i)) }", "c(2+2i, 1+1i)");
  }
  @Test
  public void micro1233() {
    assertIdentical("{ rev(1:3) }", "c(3L, 2L, 1L)");
  }
  @Test
  public void micro1234() {
    assertIdentical("{ f <- function() { assign('x', 1) ; x } ; f() }", "1");
  }
  @Test
  public void micro1235() {
    assertIdentical("{ f <- function() { x <- 2 ; g <- function() { x <- 3 ; assign('x', 1, inherits=FALSE) ; x } ; g() } ; f() }", "1");
  }
  @Test
  public void micro1236() {
    assertIdentical("{ f <- function() { x <- 2 ; g <- function() { assign('x', 1, inherits=FALSE) } ; g() ; x } ; f() }", "2");
  }
  @Test
  public void micro1237() {
    assertIdentical("{ f <- function() { x <- 2 ; g <- function() { assign('x', 1, inherits=TRUE) } ; g() ; x } ; f() }", "1");
  }
  @Test
  public void micro1238() {
    assertIdentical("{ f <- function() {  g <- function() { assign('x', 1, inherits=TRUE) } ; g() } ; f() ; x }", "1");
  }
  @Test
  public void micro1239() {
    assertIdentical("{ x <- 3 ; g <- function() { x } ; f <- function() { assign('x', 2) ; g() } ; f() }", "3");
  }
  @Test
  public void micro1240() {
    assertIdentical("{ x <- 3 ; f <- function() { assign('x', 2) ; g <- function() { x } ; g() } ; f() }", "2");
  }
  @Test
  public void micro1241() {
    assertIdentical("{ h <- function() { x <- 3 ; g <- function() { x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; h() }", "3");
  }
  @Test
  public void micro1242() {
    assertIdentical("{ h <- function() { x <- 3  ; f <- function() { assign('x', 2) ; g <- function() { x } ; g() } ; f() }  ; h() }", "2");
  }
  @Test
  public void micro1243() {
    assertIdentical("{ x <- 3 ; h <- function() { g <- function() { x } ; f <- function() { assign('x', 2, inherits=TRUE) } ; f() ; g() }  ; h() }", "2");
  }
  @Test
  public void micro1244() {
    assertIdentical("{ x <- 3 ; h <- function(s) { if (s == 2) { assign('x', 2) } ; x }  ; h(1) ; h(2) }", "2");
  }
  @Test
  public void micro1245() {
    assertIdentical("{ x <- 3 ; h <- function(s) { y <- x ; if (s == 2) { assign('x', 2) } ; c(y,x) }  ; c(h(1),h(2)) }", "c(3, 3, 3, 2)");
  }
  @Test
  public void micro1246() {
    assertIdentical("{ g <- function() { x <- 2 ; f <- function() { x ; exists('x') }  ; f() } ; g() }", "TRUE");
  }
  @Test
  public void micro1247() {
    assertIdentical("{ g <- function() { f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() }", "FALSE");
  }
  @Test
  public void micro1248() {
    assertIdentical("{ g <- function() { f <- function() { if (FALSE) { x } ; assign('x', 1) ; exists('x') }  ; f() } ; g() }", "TRUE");
  }
  @Test
  public void micro1249() {
    assertIdentical("{ g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() }", "FALSE");
  }
  @Test
  public void micro1250() {
    assertIdentical("{ g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; assign('x', 2) ; exists('x') }  ; f() } ; g() }", "TRUE");
  }
  @Test
  public void micro1251() {
    assertIdentical("{ h <- function() { g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }", "FALSE");
  }
  @Test
  public void micro1252() {
    assertIdentical("{ h <- function() { x <- 3 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }", "TRUE");
  }
  @Test
  public void micro1253() {
    assertIdentical("{ f <- function(z) { exists('z') } ; f() }", "TRUE");
  }
  @Test
  public void micro1254() {
    assertIdentical("{ f <- function(z) { exists('z') } ; f(a) }", "TRUE");
  }
  @Test
  public void micro1255() {
    assertIdentical("{ f <- function() { x <- 3 ; exists('x', inherits=FALSE) } ; f() }", "TRUE");
  }
  @Test
  public void micro1256() {
    assertIdentical("{ f <- function() { z <- 3 ; exists('x', inherits=FALSE) } ; f() }", "FALSE");
  }
  @Test
  public void micro1257() {
    assertIdentical("{ f <- function() { if (FALSE) { x <- 3 } ; exists('x', inherits=FALSE) } ; f() }", "FALSE");
  }
  @Test
  public void micro1258() {
    assertIdentical("{ f <- function() { assign('x', 2) ; exists('x', inherits=FALSE) } ; f() }", "TRUE");
  }
  @Test
  public void micro1259() {
    assertIdentical("{ g <- function() { x <- 2 ; f <- function() { if (FALSE) { x <- 3 } ; exists('x') }  ; f() } ; g() }", "TRUE");
  }
  @Test
  public void micro1260() {
    assertIdentical("{ g <- function() { x <- 2 ; f <- function() { x <- 5 ; exists('x') }  ; f() } ; g() }", "TRUE");
  }
  @Test
  public void micro1261() {
    assertIdentical("{ g <- function() { f <- function() { assign('x', 3) ; if (FALSE) { x } ; exists('x') }  ; f() } ; g() }", "TRUE");
  }
  @Test
  public void micro1262() {
    assertIdentical("{ g <- function() { f <- function() { assign('z', 3) ; if (FALSE) { x } ; exists('x') }  ; f() } ; g() }", "FALSE");
  }
  @Test
  public void micro1263() {
    assertIdentical("{ h <- function() { assign('x', 1) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }", "TRUE");
  }
  @Test
  public void micro1264() {
    assertIdentical("{ h <- function() { assign('z', 1) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }", "FALSE");
  }
  @Test
  public void micro1265() {
    assertIdentical("{ h <- function() { x <- 3 ; g <- function() { f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }", "TRUE");
  }
  @Test
  public void micro1268() {
    assertIdentical("{ x <- 3 ; f <- function() { exists('x') } ; f() }", "TRUE");
  }
  @Test
  public void micro1269() {
    assertIdentical("{ x <- 3 ; f <- function() { exists('x', inherits=FALSE) } ; f() }", "FALSE");
  }
  @Test
  public void micro1270() {
    assertIdentical("{ h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; assign('y', 2, h) ; ls(h) }", "c(\"x\", \"y\")");
  }
  @Test
  public void micro1271() {
    assertIdentical("{ f <- function() { assign('x', 1) ; y <- 2 ; ls() } ; f() }", "c(\"x\", \"y\")");
  }
  @Test
  public void micro1272() {
    assertIdentical("{ f <- function() { x <- 1 ; y <- 2 ; ls() } ; f() }", "c(\"x\", \"y\")");
  }
  @Test
  public void micro1273() {
    assertIdentical("{ f <- function() { assign('x', 1) ; y <- 2 ; if (FALSE) { z <- 3 } ; ls() } ; f() }", "c(\"x\", \"y\")");
  }
  @Test
  public void micro1274() {
    assertIdentical("{ f <- function() { if (FALSE) { x <- 1 } ; y <- 2 ; ls() } ; f() }", "\"y\"");
  }
  @Test
  public void micro1275() {
    assertIdentical("{ f <- function() { for (i in rev(1:10)) { assign(as.character(i), i) } ; ls() } ; length(f()) }", "11L");
  }
  @Test
  public void micro1276() {
    assertIdentical("{ f <- function() { x <- 2 ; get('x') } ; f() }", "2");
  }
  @Test
  public void micro1277() {
    assertIdentical("{ x <- 3 ; f <- function() { get('x') } ; f() }", "3");
  }
  @Test
  public void micro1278() {
    assertIdentical("{ x <- 3 ; f <- function() { x <- 2 ; get('x') } ; f() }", "2");
  }
  @Test
  public void micro1279() {
    assertIdentical("{ x <- 3 ; f <- function() { x <- 2; h <- function() {  get('x') }  ; h() } ; f() }", "2");
  }
  @Test
  public void micro1280() {
    assertIdentical("{ x <- 3 ; f <- function() { assign('x', 4) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x } ; h() } ; f() }", "4");
  }
  @Test
  public void micro1281() {
    assertIdentical("{ x <- 3 ; f <- function() { assign('x', 4) ; g <- function() { assign('y', 3) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x } ; h() } ; g()  } ; f() }", "4");
  }
  @Test
  public void micro1282() {
    assertIdentical("{ f <- function() { assign('x', 2, inherits=TRUE) ; assign('x', 1) ; h <- function() { x } ; h() } ; f() }", "1");
  }
  @Test
  public void micro1283() {
    assertIdentical("{ x <- 3 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x } ; h() } ; f() } ; g() }", "3");
  }
  @Test
  public void micro1284() {
    assertIdentical("{ x <- 3 ; gg <- function() {  g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x } ; h() } ; f() } ; g() } ; gg() }", "3");
  }
  @Test
  public void micro1285() {
    assertIdentical("{ h <- function() { x <- 2 ; f <- function() { if (FALSE) { x <- 1 } ; g <- function() { x } ; g() } ; f() } ; h() }", "2");
  }
  @Test
  public void micro1286() {
    assertIdentical("{ f <- function() { g <- function() { get('x', inherits=TRUE) } ; g() } ; x <- 3 ; f() }", "3");
  }
  @Test
  public void micro1287() {
    assertIdentical("{ f <- function() { assign('z', 2) ; g <- function() { get('x', inherits=TRUE) } ; g() } ; x <- 3 ; f() }", "3");
  }
  @Test
  public void micro1288() {
    assertIdentical("{ f <- function() { assign('x', 3) ; g <- function() { x } ; g() } ; x <- 10 ; f() }", "3");
  }
  @Test
  public void micro1289() {
    assertIdentical("{ f <- function() { assign('x', 3) ; h <- function() { assign('z', 4) ; g <- function() { x } ; g() } ; h() } ; x <- 10 ; f() }", "3");
  }
  @Test
  public void micro1290() {
    assertIdentical("{ f <- function() { assign('x', 3) ; h <- function() { g <- function() { x } ; g() } ; h() } ; x <- 10 ; f() }", "3");
  }
  @Test
  public void micro1291() {
    assertIdentical("{ f <- function() { assign('x', 1) ; g <- function() { assign('z', 2) ; x } ; g() } ; f() }", "1");
  }
  @Test
  public void micro1292() {
    assertIdentical("{ h <- function() { x <- 3 ; g <- function() { assign('z', 2) ; x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; h() }", "3");
  }
  @Test
  public void micro1293() {
    assertIdentical("{ h <- function() { x <- 3 ; g <- function() { assign('x', 5) ; x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; h() }", "5");
  }
  @Test
  public void micro1294() {
    assertIdentical("{ x <- 10 ; g <- function() { x <- 100 ; z <- 2 ; f <- function() { assign('z', 1); x <- x ; x } ; f() } ; g() }", "100");
  }
  @Test
  public void micro1295() {
    assertIdentical("{ f <- function() { x <- 22 ; get('x', inherits=FALSE) } ; f() }", "22");
  }
  @Test
  public void micro1296() {
    assertIdentical("{ x <- 33 ; f <- function() { assign('x', 44) ; get('x', inherits=FALSE) } ; f() }", "44");
  }
  @Test
  public void micro1297() {
    assertIdentical("{ hh <- new.env() ; assign('z', 3, hh) ; h <- new.env(parent=hh) ; assign('y', 2, h) ; get('z', h) }", "3");
  }
  @Test
  public void micro1298() {
    assertIdentical("{ g <- function() { if (FALSE) { x <- 2 ; y <- 3} ; f <- function() { if (FALSE) { x } ; assign('y', 2) ; exists('x') }  ; f() } ; g() }", "FALSE");
  }
  @Test
  public void micro1299() {
    assertIdentical("{ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; exists('x') }  ; f() } ; g() }", "TRUE");
  }
  @Test
  public void micro1300() {
    assertIdentical("{ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; h <- function() { exists('x') } ; h() }  ; f() } ; g() }", "TRUE");
  }
  @Test
  public void micro1301() {
    assertIdentical("{ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('y', 2) ; h <- function() { exists('x') } ; h() }  ; f() } ; g() }", "FALSE");
  }
  @Test
  public void micro1302() {
    assertIdentical("{ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; gg <- function() { h <- function() { get('x') } ; h() } ; gg() } ; f() } ; g() }", "2");
  }
  @Test
  public void micro1303() {
    assertIdentical("{ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; gg <- function() { h <- function() { exists('x') } ; h() } ; gg() } ; f() } ; g() }", "TRUE");
  }
  @Test
  public void micro1304() {
    assertIdentical("{ x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function() { x } } ; f1 <- f(1) ; f2 <- f(2) ; f1() }", "4");
  }
  @Test
  public void micro1305() {
    assertIdentical("{ x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function() { x } } ; f1 <- f(1) ; f2 <- f(2) ; f2() ; f1() }", "4");
  }
  @Test
  public void micro1306() {
    assertIdentical("{ f <- function() { x <- 2 ; g <- function() { if (FALSE) { x <- 2 } ; assign('x', 1, inherits=TRUE) } ; g() ; x } ; f() }", "1");
  }
  @Test
  public void micro1307() {
    assertIdentical("{ h <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; g <- function() { assign('z', 3) ; if (FALSE) { x <- 4 } ;  f <- function() { exists('x') } ; f() } ; g() } ; h() }", "FALSE");
  }
  @Test
  public void micro1308() {
    assertIdentical("{ x <- function(){3} ; f <- function() { assign('x', function(){4}) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x() } ; h() } ; f() }", "4");
  }
  @Test
  public void micro1309() {
    assertIdentical("{ f <- function() { assign('x', function(){2}, inherits=TRUE) ; assign('x', function(){1}) ; h <- function() { x() } ; h() } ; f() }", "1");
  }
  @Test
  public void micro1310() {
    assertIdentical("{ x <- function(){3} ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() }", "3");
  }
  @Test
  public void micro1311() {
    assertIdentical("{ x <- function(){3} ; gg <- function() {  g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() } ; gg() }", "3");
  }
  @Test
  public void micro1312() {
    assertIdentical("{ h <- function() { x <- function(){2} ; f <- function() { if (FALSE) { x <- 1 } ; g <- function() { x } ; g() } ; f() } ; z <- h() ; z() }", "2");
  }
  @Test
  public void micro1313() {
    assertIdentical("{ h <- function() { g <- function() {4} ; f <- function() { if (FALSE) { g <- 4 } ; g() } ; f() } ; h() }", "4");
  }
  @Test
  public void micro1314() {
    assertIdentical("{ h <- function() { assign('f', function() {4}) ; f() } ; h() }", "4");
  }
  @Test
  public void micro1315() {
    assertIdentical("{ f <- function() { 4 } ; h <- function() { assign('f', 5) ; f() } ; h() }", "4");
  }
  @Test
  public void micro1316() {
    assertIdentical("{ f <- function() { 4 } ; h <- function() { assign('z', 5) ; f() } ; h() }", "4");
  }
  @Test
  public void micro1317() {
    assertIdentical("{ gg <- function() {  assign('x', function(){11}) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() } ; gg() }", "11");
  }
  @Test
  public void micro1318() {
    assertIdentical("{ x <- function(){3} ; gg <- function() { assign('x', 4) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() } ; gg() }", "3");
  }
  @Test
  public void micro1319() {
    assertIdentical("{ h <- function() { x <- function() {3} ; g <- function() { assign('z', 2) ; x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; z <- h() ; z() }", "3");
  }
  @Test
  public void micro1320() {
    assertIdentical("{ h <- function() { x <- function() {3} ; g <- function() { assign('x', function() {5} ) ; x() } ; g() } ; h() }", "5");
  }
  @Test
  public void micro1321() {
    assertIdentical("{ h <- function() { z <- 3 ; x <- function() {3} ; g <- function() { x <- 1 ; assign('z', 5) ; x() } ; g() } ; h() }", "3");
  }
  @Test
  public void micro1322() {
    assertIdentical("{ h <- function() { x <- function() {3} ; gg <- function() { assign('x', 5) ; g <- function() { x() } ; g() } ; gg() } ; h() }", "3");
  }
  @Test
  public void micro1323() {
    assertIdentical("{ h <- function() { z <- 2 ; x <- function() {3} ; gg <- function() { assign('z', 5) ; g <- function() { x() } ; g() } ; gg() } ; h() }", "3");
  }
  @Test
  public void micro1324() {
    assertIdentical("{ h <- function() { x <- function() {3} ; g <- function() { assign('x', function() {4}) ; x() } ; g() } ; h() }", "4");
  }
  @Test
  public void micro1325() {
    assertIdentical("{ h <- function() { z <- 2 ; x <- function() {3} ; g <- function() { assign('z', 1) ; x() } ; g() } ; h() }", "3");
  }
  @Test
  public void micro1326() {
    assertIdentical("{ x <- function() { 3 } ; h <- function() { if (FALSE) { x <- 2 } ;  z <- 2  ; g <- function() { assign('z', 1) ; x() } ; g() } ; h() }", "3");
  }
  @Test
  public void micro1327() {
    assertIdentical("{ x <- function() { 3 } ; h <- function() { g <- function() { f <- function() { x <- 1 ; x() } ; f() } ; g() } ; h() }", "3");
  }
  @Test
  public void micro1328() {
    assertIdentical("{ g <- function() { assign('myfunc', function(i) { sum(i) });  f <- function() { lapply(2, 'myfunc') } ; f() } ; g() }", "list(2)");
  }
  @Test
  public void micro1329() {
    assertIdentical("{ myfunc <- function(i) { sum(i) } ; g <- function() { assign('z', 1);  f <- function() { lapply(2, 'myfunc') } ; f() } ; g() }", "list(2)");
  }
  @Test
  public void micro1330() {
    assertIdentical("{ g <- function() { f <- function() { assign('myfunc', function(i) { sum(i) }); lapply(2, 'myfunc') } ; f() } ; g() }", "list(2)");
  }
  @Test
  public void micro1331() {
    assertIdentical("{ h <- function() { myfunc <- function(i) { sum(i) } ; g <- function() { myfunc <- 2 ; f <- function() { myfunc(2) } ; f() } ; g() } ; h() }", "2");
  }
  @Test
  public void micro1332() {
    assertIdentical("{ x <- function() {11} ; g <- function() { f <- function() { assign('x', 2) ; x() } ; f() } ; g() }", "11");
  }
  @Test
  public void micro1333() {
    assertIdentical("{ g <- function() { myfunc <- function(i) { i+i } ; f <- function() { lapply(2, 'myfunc') } ; f() } ; g() }", "list(4)");
  }
  @Test
  public void micro1334() {
    assertIdentical("{ x <- function() {3} ; f <- function(i) { if (i == 1) { assign('x', function() {4}) } ; function() { x() } } ; f1 <- f(1) ; f2 <- f(2) ; f1() }", "4");
  }
  @Test
  public void micro1335() {
    assertIdentical("{ x <- function() {3} ; f <- function(i) { if (i == 1) { assign('x', function() {4}) } ; function() { x() } } ; f1 <- f(1) ; f2 <- f(2) ; f2() ; f1() }", "4");
  }
  @Test
  public void micro1336() {
    assertIdentical("{ x <- function() {3} ; f <- function(i) { if (i == 1) { assign('x', function() {4}) } ; function() { x() } } ; f1 <- f(1) ; f2 <- f(2) ; f1() ; f2() }", "3");
  }
  @Test
  public void micro1337() {
    assertIdentical("{ x <- 3 ; f <- function() { assign('x', 4) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x <<- 6 } ; h() ; get('x') } ; f() }", "6");
  }
  @Test
  public void micro1338() {
    assertIdentical("{ x <- 3 ; f <- function() { assign('x', 4) ; hh <- function() { if (FALSE) { x <- 100 } ; h <- function() { x <<- 6 } ; h() } ; hh() ; get('x') } ; f() }", "6");
  }
  @Test
  public void micro1339() {
    assertIdentical("{ x <- 3 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x ; hh <- function() { x <<- 4 } ; hh() } ; h() } ; f() } ; g() ; x }", "4");
  }
  @Test
  public void micro1340() {
    assertIdentical("{ f <- function() { x <- 1 ; g <- function() { h <- function() { x <<- 2 } ; h() } ; g() ; x } ; f() }", "2");
  }
  @Test
  public void micro1341() {
    assertIdentical("{ g <- function() { if (FALSE) { x <- 2 } ; f <- function() { assign('x', 4) ; x <<- 3 } ; f() } ; g() ; x }", "3");
  }
  @Test
  public void micro1342() {
    assertIdentical("{ g <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; h <- function() { if (FALSE) { x <- 1 } ; assign('z', 10) ; f <- function() { assign('x', 4) ; x <<- 3 } ; f() } ; h() } ; g() ; x }", "3");
  }
  @Test
  public void micro1343() {
    assertIdentical("{ gg <- function() { assign('x', 100) ; g <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; h <- function() { if (FALSE) { x <- 1 } ; assign('z', 10) ; f <- function() { assign('x', 4) ; x <<- 3 } ; f() } ; h() } ; g() } ; x <- 10 ; gg() ; x }", "10");
  }
  @Test
  public void micro1344() {
    assertIdentical("{ gg <- function() { if (FALSE) { x <- 100 } ; g <- function() { if (FALSE) { x <- 100 } ; h <- function() { f <- function() { x <<- 3 } ; f() } ; h() } ; g() } ; x <- 10 ; gg() ; x }", "3");
  }
  @Test
  public void micro1345() {
    assertIdentical("{ g <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; h <- function() { assign('z', 10) ; f <- function() { x <<- 3 } ; f() } ; h() } ; g() ; x }", "3");
  }
  @Test
  public void micro1346() {
    assertIdentical("{ g <- function() { x <- 2 ; z <- 3 ; hh <- function() { assign('z', 2) ; h <- function() { f <- function() { x <<- 3 } ; f() } ; h() } ; hh() } ; x <- 10 ; g() ; x }", "10");
  }
  @Test
  public void micro1347() {
    assertIdentical("{ g <- function() { x <- 2 ; z <- 3 ; hh <- function() { assign('z', 2) ; h <- function() { assign('x', 1); f <- function() { x <<- 3 } ; f() } ; h() } ; hh() ; x } ; x <- 10 ; g() }", "2");
  }
  @Test
  public void micro1348() {
    assertIdentical("{ x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function(v) { x <<- v} } ; f1 <- f(1) ; f2 <- f(2) ; f1(10) ; f2(11) ; x }", "11");
  }
  @Test
  public void micro1349() {
    assertIdentical("{ x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function(v) { x <<- v} } ; f1 <- f(1) ; f2 <- f(2) ; f2(10) ; f1(11) ; x }", "10");
  }
  @Test
  public void micro1350() {
    assertIdentical("{ h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; exists('x', h) }", "TRUE");
  }
  @Test
  public void micro1351() {
    assertIdentical("{ h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; exists('xx', h) }", "FALSE");
  }
  @Test
  public void micro1352() {
    assertIdentical("{ hh <- new.env() ; assign('z', 3, hh) ; h <- new.env(parent=hh) ; assign('y', 2, h) ; exists('z', h) }", "TRUE");
  }
  @Test
  public void micro1353() {
    assertIdentical("{ ph <- new.env() ; h <- new.env(parent=ph) ; assign('x', 2, ph) ; assign('x', 10, h, inherits=TRUE) ; get('x', ph) }", "10");
  }
  @Test
  public void micro1354() {
    assertIdentical("{ ph <- new.env() ; h <- new.env(parent=ph) ; assign('x', 10, h, inherits=TRUE) ; x }", "10");
  }
  @Test
  public void micro1355() {
    assertIdentical("{ assign('z', 10, inherits=TRUE) ; z }", "10");
  }
  @Test
  public void micro1356() {
    assertIdentical("{ h <- new.env(parent=globalenv()) ; assign('x', 10, h, inherits=TRUE) ; x }", "10");
  }
  @Test
  public void micro1357() {
    assertIdentical("{ h <- new.env() ; assign('x', 1, h) ; assign('x', 1, h) ; get('x', h) }", "1");
  }
  @Test
  public void micro1358() {
    assertIdentical("{ h <- new.env() ; assign('x', 1, h) ; assign('x', 2, h) ; get('x', h) }", "2");
  }
  @Test
  public void micro1359() {
    assertIdentical("{ h <- new.env() ; u <- 1 ; assign('x', u, h) ; assign('x', u, h) ; get('x', h) }", "1");
  }
  @Test
  public void micro1360() {
    assertIdentical("{ exists('sum') }", "TRUE");
  }
  @Test
  public void micro1361() {
    assertIdentical("{ exists('sum', inherits = FALSE) }", "FALSE");
  }
  @Test
  public void micro1362() {
    assertIdentical("{ x <- 1; exists('x', inherits = FALSE) }", "TRUE");
  }
  @Test
  public void micro1363() {
    assertIdentical("{ ls() }", "character(0)");
  }
  @Test
  public void micro1364() {
    assertIdentical("{ x <- 1 ; ls(globalenv()) }", "\"x\"");
  }
  @Test
  public void micro1365() {
    assertIdentical("{ ls(.GlobalEnv) }", "character(0)");
  }
  @Test
  public void micro1366() {
    assertIdentical("{ x <- 1 ; ls(.GlobalEnv) }", "\"x\"");
  }
  @Test
  public void micro1374() {
    assertIdentical("{ m <- matrix(1:49, nrow=7) ; sum(m * t(m)) }", "33369L");
  }
  @Test
  public void micro1375() {
    assertIdentical("{ m <- matrix(1:81, nrow=9) ; sum(m * t(m)) }", "145881L");
  }
  @Test
  public void micro1376() {
    assertIdentical("{ m <- matrix(-5000:4999, nrow=100) ; sum(m * t(m)) }", "1666502500L");
  }
  @Test
  public void micro1377() {
    assertIdentical("{ m <- matrix(c(rep(1:10,100200),100L), nrow=1001) ; sum(m * t(m)) }", "38587000L");
  }
  @Test
  public void micro1388() {
    assertIdentical("{ is.double(10L) }", "FALSE");
  }
  @Test
  public void micro1389() {
    assertIdentical("{ is.double(10) }", "TRUE");
  }
  @Test
  public void micro1390() {
    assertIdentical("{ is.double('10') }", "FALSE");
  }
  @Test
  public void micro1391() {
    assertIdentical("{ is.numeric(10L) }", "TRUE");
  }
  @Test
  public void micro1392() {
    assertIdentical("{ is.numeric(10) }", "TRUE");
  }
  @Test
  public void micro1393() {
    assertIdentical("{ is.numeric(TRUE) }", "FALSE");
  }
  @Test
  public void micro1394() {
    assertIdentical("{ is.character('hi') }", "TRUE");
  }
  @Test
  public void micro1395() {
    assertIdentical("{ is.list(NULL) }", "FALSE");
  }
  @Test
  public void micro1396() {
    assertIdentical("{ is.logical(NA) }", "TRUE");
  }
  @Test
  public void micro1397() {
    assertIdentical("{ is.logical(1L) }", "FALSE");
  }
  @Test
  public void micro1398() {
    assertIdentical("{ is.integer(1) }", "FALSE");
  }
  @Test
  public void micro1399() {
    assertIdentical("{ is.integer(1L) }", "TRUE");
  }
  @Test
  public void micro1400() {
    assertIdentical("{ is.complex(1i) }", "TRUE");
  }
  @Test
  public void micro1401() {
    assertIdentical("{ is.complex(1) }", "FALSE");
  }
  @Test
  public void micro1402() {
    assertIdentical("{ is.raw(raw()) }", "TRUE");
  }
  @Test
  public void micro1403() {
    assertIdentical("{ is.matrix(1) }", "FALSE");
  }
  @Test
  public void micro1404() {
    assertIdentical("{ is.matrix(matrix(1:6, nrow=2)) }", "TRUE");
  }
  @Test
  public void micro1405() {
    assertIdentical("{ is.matrix(NULL) }", "FALSE");
  }
  @Test
  public void micro1406() {
    assertIdentical("{ sub <- function(x,y) { x - y }; sub(10,5) }", "5");
  }
  @Test
  public void micro1407() {
    assertIdentical("{ sub('a','aa', 'prague alley', fixed=TRUE) }", "\"praague alley\"");
  }
  @Test
  public void micro1409() {
    assertIdentical("{ r <- eigen(matrix(rep(1,4), nrow=2), only.values=FALSE) ; round( r$values, digits=5 ) }", "c(2, 0)");
  }
  @Test
  public void micro1410() {
    assertIdentical("{ eigen(10, only.values=FALSE) }", "structure(list(values = 10, vectors = structure(1, .Dim = c(1L, 1L))), .Names = c(\"values\", \"vectors\"))");
  }
  @Test
  public void micro1412() {
    assertIdentical("{ r <- eigen(matrix(c(1,2,2,3), nrow=2), only.values=FALSE); round( r$values, digits=5 ) }", "c(4.23607, -0.23607)");
  }
  @Test
  public void micro1414() {
    assertIdentical("{ r <- eigen(matrix(c(1,2,3,4), nrow=2), only.values=FALSE); round( r$values, digits=5 ) }", "c(5.37228, -0.37228)");
  }
  @Test
  public void micro1416() {
    assertIdentical("{ r <- eigen(matrix(c(3,-2,4,-1), nrow=2), only.values=FALSE); round( r$values, digits=5 ) }", "c(1+2i, 1-2i)");
  }
  @Test
  public void micro1418() {
    assertIdentical("{ x <- 1; names(x) <- 'hello' ; attributes(x) }", "structure(list(names = \"hello\"), .Names = \"names\")");
  }
  @Test
  public void micro1419() {
    assertIdentical("{ x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; attributes(x) }", "structure(list(myatt = 2:4), .Names = \"myatt\")");
  }
  @Test
  public void micro1421() {
    assertIdentical("{ x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; y <- x; attr(x, 'myatt1') <- 'hello' ; attributes(y) }", "structure(list(myatt = 2:4), .Names = \"myatt\")");
  }
  @Test
  public void micro1422() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, 'myatt') <- 2:4 ; y <- x; attr(x, 'myatt1') <- 'hello' ; attributes(y) }", "structure(list(names = c(\"a\", \"b\"), myatt = 2:4), .Names = c(\"names\", \"myatt\"))");
  }
  @Test
  public void micro1423() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, 'names') }", "c(\"a\", \"b\")");
  }
  @Test
  public void micro1424() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, 'na') }", "c(\"a\", \"b\")");
  }
  @Test
  public void micro1425() {
    assertIdentical("{ x <- c(a=1, b=2) ; attr(x, 'mya') <- 1; attr(x, 'b') <- 2; attr(x, 'm') }", "1");
  }
  @Test
  public void micro1426() {
    assertIdentical("{ x <- 1:2; attr(x, 'aa') <- 1 ; attr(x, 'ab') <- 2; attr(x, 'bb') <- 3; attr(x, 'b') }", "3");
  }
  @Test
  public void micro1430() {
    assertIdentical("{ x <- c(hello=1) ; attributes(x) <- list(names=NULL) ; x }", "1");
  }
  @Test
  public void micro1432() {
    assertIdentical("{ x <- c(hello=1) ; attributes(x) <- list(hi=1) ;  attributes(x) <- NULL ; x }", "1");
  }
  @Test
  public void micro1434() {
    assertIdentical("{ unlist(list('hello', 'hi')) }", "c(\"hello\", \"hi\")");
  }
  @Test
  public void micro1435() {
    assertIdentical("{ unlist(list(a='hello', b='hi')) }", "structure(c(\"hello\", \"hi\"), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro1436() {
    assertIdentical("{ x <- list(a=1,b=2:3,list(x=FALSE)) ; unlist(x, recursive=FALSE) }", "structure(list(a = 1, b1 = 2L, b2 = 3L, x = FALSE), .Names = c(\"a\", \"b1\", \"b2\", \"x\"))");
  }
  @Test
  public void micro1437() {
    assertIdentical("{ x <- list(1,z=list(1,b=22,3)) ; unlist(x, recursive=FALSE) }", "structure(list(1, z1 = 1, z.b = 22, z3 = 3), .Names = c(\"\", \"z1\", \"z.b\", \"z3\"))");
  }
  @Test
  public void micro1438() {
    assertIdentical("{ x <- list(1,z=list(1,b=22,3)) ; unlist(x, recursive=FALSE, use.names=FALSE) }", "list(1, 1, 22, 3)");
  }
  @Test
  public void micro1439() {
    assertIdentical("{ x <- list('a', c('b', 'c'), list('d', list('e'))) ; unlist(x) }", "c(\"a\", \"b\", \"c\", \"d\", \"e\")");
  }
  @Test
  public void micro1440() {
    assertIdentical("{ x <- list(NULL, list('d', list(), character())) ; unlist(x) }", "\"d\"");
  }
  @Test
  public void micro1441() {
    assertIdentical("{ x <- list(a=list('1','2',b='3','4')) ; unlist(x) }", "structure(c(\"1\", \"2\", \"3\", \"4\"), .Names = c(\"a1\", \"a2\", \"a.b\", \"a4\"))");
  }
  @Test
  public void micro1442() {
    assertIdentical("{ x <- list(a=list('1','2',b=list('3'))) ; unlist(x) }", "structure(c(\"1\", \"2\", \"3\"), .Names = c(\"a1\", \"a2\", \"a.b\"))");
  }
  @Test
  public void micro1443() {
    assertIdentical("{ x <- list(a=list(1,FALSE,b=list(2:4))) ; unlist(x) }", "structure(c(1, 0, 2, 3, 4), .Names = c(\"a1\", \"a2\", \"a.b1\", \"a.b2\", \"a.b3\"))");
  }
  @Test
  public void micro1444() {
    assertIdentical("{ rev.mine <- function(x) { if (length(x)) x[length(x):1L] else x } ; rev.mine(1:3) }", "c(3L, 2L, 1L)");
  }
  @Test
  public void micro1448() {
    assertIdentical("{ a = array(1:24,c(2,3,4)); b = aperm(a, c(2,3,1)); a[1,2,3] == b[2,3,1]; }", "TRUE");
  }
  @Test
  public void micro1449() {
    assertIdentical("{ a = array(1:24,c(3,3,3)); b = aperm(a, c(2,3,1)); a[1,2,3] == b[2,3,1] && a[2,3,1] == b[3,1,2] && a[3,1,2] == b[1,2,3]; }", "TRUE");
  }
  @Test
  public void micro1452() {
    assertIdentical("{ aperm(array(1:27,c(3,3,3)), c(1+1i,3+3i,2+2i))[1,2,3] == array(1:27,c(3,3,3))[1,3,2]; }", "TRUE");
  }
  @Test
  public void micro1453() {
    assertIdentical("{ a = colSums(matrix(1:12,3,4)); is.null(dim(a)); }", "TRUE");
  }
  @Test
  public void micro1454() {
    assertIdentical("{ a = colSums(matrix(1:12,3,4)); length(a) == 4; }", "TRUE");
  }
  @Test
  public void micro1455() {
    assertIdentical("{ a = colSums(matrix(1:12,3,4)); a[1] == 6 && a[2] == 15 && a[3] == 24 && a[4] == 33; }", "TRUE");
  }
  @Test
  public void micro1456() {
    assertIdentical("{ a = colSums(array(1:24,c(2,3,4))); d = dim(a); d[1] == 3 && d[2] == 4; }", "TRUE");
  }
  @Test
  public void micro1457() {
    assertIdentical("{ a = colSums(array(1:24,c(2,3,4))); length(a) == 12; }", "TRUE");
  }
  @Test
  public void micro1458() {
    assertIdentical("{ a = colSums(array(1:24,c(2,3,4))); a[1,1] == 3 && a[2,2] == 19 && a[3,3] == 35 && a[3,4] == 47; }", "TRUE");
  }
  @Test
  public void micro1459() {
    assertIdentical("{ a = rowSums(matrix(1:12,3,4)); is.null(dim(a)); }", "TRUE");
  }
  @Test
  public void micro1460() {
    assertIdentical("{ a = rowSums(matrix(1:12,3,4)); length(a) == 3; }", "TRUE");
  }
  @Test
  public void micro1461() {
    assertIdentical("{ a = rowSums(matrix(1:12,3,4)); a[1] == 22 && a[2] == 26 && a[3] == 30; }", "TRUE");
  }
  @Test
  public void micro1462() {
    assertIdentical("{ a = rowSums(array(1:24,c(2,3,4))); is.null(dim(a)); }", "TRUE");
  }
  @Test
  public void micro1463() {
    assertIdentical("{ a = rowSums(array(1:24,c(2,3,4))); length(a) == 2; }", "TRUE");
  }
  @Test
  public void micro1464() {
    assertIdentical("{ a = rowSums(array(1:24,c(2,3,4))); a[1] == 144 && a[2] == 156; }", "TRUE");
  }
  @Test
  public void micro1465() {
    assertIdentical("{ f<-function(i) { if(i<=1) 1 else i*Recall(i-1) } ; f(10) }", "3628800");
  }
  @Test
  public void micro1466() {
    assertIdentical("{ f<-function(i) { if(i<=1) 1 else i*Recall(i-1) } ; g <- f ; f <- sum ; g(10) }", "3628800");
  }
  @Test
  public void micro1467() {
    assertIdentical("{ f<-function(i) { if (i==1) { 1 } else if (i==2) { 1 } else { Recall(i-1) + Recall(i-2) } } ; f(10) }", "55");
  }
  @Test
  public void micro1473() {
    assertIdentical("{ sort(c(1L,10L,2L)) }", "c(1L, 2L, 10L)");
  }
  @Test
  public void micro1474() {
    assertIdentical("{ sort(c(3,10,2)) }", "c(2, 3, 10)");
  }
  @Test
  public void micro1475() {
    assertIdentical("{ sort(c(1,2,0/0,NA)) }", "c(1, 2)");
  }
  @Test
  public void micro1476() {
    assertIdentical("{ sort(c(2,1,0/0,NA), na.last=NA) }", "c(1, 2)");
  }
  @Test
  public void micro1477() {
    assertIdentical("{ sort(c(3,0/0,2,NA), na.last=TRUE) }", "c(2, 3, NaN, NA)");
  }
  @Test
  public void micro1478() {
    assertIdentical("{ sort(c(3,NA,0/0,2), na.last=FALSE) }", "c(NA, NaN, 2, 3)");
  }
  @Test
  public void micro1479() {
    assertIdentical("{ sort(c(3L,NA,2L)) }", "2:3");
  }
  @Test
  public void micro1480() {
    assertIdentical("{ sort(c(3L,NA,-2L), na.last=TRUE) }", "c(-2L, 3L, NA)");
  }
  @Test
  public void micro1481() {
    assertIdentical("{ sort(c(3L,NA,-2L), na.last=FALSE) }", "c(NA, -2L, 3L)");
  }
  @Test
  public void micro1482() {
    assertIdentical("{ sort(c(a=NA,b=NA,c=3,d=1),na.last=TRUE, decreasing=TRUE) }", "structure(c(3, 1, NA, NA), .Names = c(\"c\", \"d\", \"a\", \"b\"))");
  }
  @Test
  public void micro1483() {
    assertIdentical("{ sort(c(a=NA,b=NA,c=3,d=1),na.last=FALSE, decreasing=FALSE) }", "structure(c(NA, NA, 1, 3), .Names = c(\"a\", \"b\", \"d\", \"c\"))");
  }
  @Test
  public void micro1484() {
    assertIdentical("{ sort(c(a=0/0,b=1/0,c=3,d=NA),na.last=TRUE, decreasing=FALSE) }", "structure(c(3, Inf, NaN, NA), .Names = c(\"c\", \"b\", \"a\", \"d\"))");
  }
  @Test
  public void micro1485() {
    assertIdentical("{ sort(double()) }", "numeric(0)");
  }
  @Test
  public void micro1486() {
    assertIdentical("{ sort(c(a=NA,b=NA,c=3L,d=-1L),na.last=TRUE, decreasing=FALSE) }", "structure(c(-1L, 3L, NA, NA), .Names = c(\"d\", \"c\", \"a\", \"b\"))");
  }
  @Test
  public void micro1487() {
    assertIdentical("{ sort(c(3,NA,1,d=10), decreasing=FALSE, index.return=TRUE) }", "structure(list(x = structure(c(1, 3, 10), .Names = c(\"\", \"\", \"d\")), ix = c(2L, 1L, 3L)), .Names = c(\"x\", \"ix\"))");
  }
  @Test
  public void micro1488() {
    assertIdentical("{ sort(3:1, index.return=TRUE) }", "structure(list(x = 1:3, ix = c(3L, 2L, 1L)), .Names = c(\"x\", \"ix\"))");
  }
  @Test
  public void micro1489() {
    assertIdentical("{ sort(c(TRUE,FALSE,FALSE,NA,FALSE), index.return=TRUE)$ix }", "c(2L, 3L, 4L, 1L)");
  }
  @Test
  public void micro1491() {
    assertIdentical("{ sort(c(a=NA,1,b=NA,0/0,2,3), na.last=TRUE, decreasing=FALSE) }", "structure(c(1, 2, 3, NA, NA, NaN), .Names = c(\"\", \"\", \"\", \"a\", \"b\", \"\"))");
  }
  @Test
  public void micro1492() {
    assertIdentical("{ sort(c(a=NA,1L,b=NA,0L,2L,-3L), na.last=TRUE, decreasing=TRUE) }", "structure(c(2L, 1L, 0L, -3L, NA, NA), .Names = c(\"\", \"\", \"\", \"\", \"a\", \"b\"))");
  }
  @Test
  public void micro1493() {
    assertIdentical("{ sort(c(a=NA,1L,b=NA,0L,2L,-3L), na.last=FALSE, decreasing=TRUE) }", "structure(c(NA, NA, 2L, 1L, 0L, -3L), .Names = c(\"a\", \"b\", \"\", \"\", \"\", \"\"))");
  }
  @Test
  public void micro1494() {
    assertIdentical("{ sort(c(a=NA,1L,b=NA,0L,2L,-3L), na.last=NA, decreasing=TRUE) }", "structure(c(2L, 1L, 0L, -3L), .Names = c(\"\", \"\", \"\", \"\"))");
  }
  @Test
  public void micro1507() {
    assertIdentical("{ rank(c(10,100,100,1000)) }", "c(1, 2.5, 2.5, 4)");
  }
  @Test
  public void micro1508() {
    assertIdentical("{ rank(c(1000,100,100,100, 10)) }", "c(5, 3, 3, 3, 1)");
  }
  @Test
  public void micro1509() {
    assertIdentical("{ rank(c(a=2,b=1,c=3,40)) }", "structure(c(2, 1, 3, 4), .Names = c(\"a\", \"b\", \"c\", \"\"))");
  }
  @Test
  public void micro1510() {
    assertIdentical("{ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=NA) }", "structure(c(2, 1, 3, 4), .Names = c(\"a\", \"b\", \"c\", \"e\"))");
  }
  @Test
  public void micro1511() {
    assertIdentical("{ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last='keep') }", "structure(c(2, 1, 3, NA, 4), .Names = c(\"a\", \"b\", \"c\", \"d\", \"e\"))");
  }
  @Test
  public void micro1512() {
    assertIdentical("{ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=TRUE) }", "structure(c(2, 1, 3, 5, 4), .Names = c(\"a\", \"b\", \"c\", \"d\", \"e\"))");
  }
  @Test
  public void micro1513() {
    assertIdentical("{ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=FALSE) }", "structure(c(3, 2, 4, 1, 5), .Names = c(\"a\", \"b\", \"c\", \"d\", \"e\"))");
  }
  @Test
  public void micro1514() {
    assertIdentical("{ rank(c(a=1,b=1,c=3,d=NA,e=3), na.last=FALSE, ties.method='max') }", "structure(c(3L, 3L, 5L, 1L, 5L), .Names = c(\"a\", \"b\", \"c\", \"d\", \"e\"))");
  }
  @Test
  public void micro1515() {
    assertIdentical("{ rank(c(a=1,b=1,c=3,d=NA,e=3), na.last=NA, ties.method='min') }", "structure(c(1L, 1L, 3L, 3L), .Names = c(\"a\", \"b\", \"c\", \"e\"))");
  }
  @Test
  public void micro1516() {
    assertIdentical("{ rank(c(1000, 100, 100, NA, 1, 20), ties.method='first') }", "c(5L, 3L, 4L, 6L, 1L, 2L)");
  }
  @Test
  public void micro1524() {
    assertIdentical("{ fft(1:4) }", "c(10+0i, -2+2i, -2+0i, -2-2i)");
  }
  @Test
  public void micro1526() {
    assertIdentical("{ fft(10) }", "10+0i");
  }
  @Test
  public void micro1533() {
    assertIdentical("{ qr(matrix(1:6,nrow=2), LAPACK=FALSE)$pivot }", "1:3");
  }
  @Test
  public void micro1534() {
    assertIdentical("{ qr(matrix(1:6,nrow=2), LAPACK=FALSE)$rank }", "2L");
  }
  @Test
  public void micro1535() {
    assertIdentical("{ round( qr(matrix(1:6,nrow=2), LAPACK=FALSE)$qraux, digits=5 ) }", "c(1.44721, 0.89443, 1.78885)");
  }
  @Test
  public void micro1540() {
    assertIdentical("{ x <- qr(t(cbind(1:10,2:11)), LAPACK=FALSE) ; qr.coef(x, 1:2) }", "c(1, 0, NA, NA, NA, NA, NA, NA, NA, NA)");
  }
  @Test
  public void micro1541() {
    assertIdentical("{ x <- qr(c(3,1,2), LAPACK=FALSE) ; round( qr.coef(x, c(1,3,2)), digits=5 ) }", "0.71429");
  }
  @Test
  public void micro1542() {
    assertIdentical("{ m <- matrix(c(1,0,0,0,1,0,0,0,1),nrow=3) ; x <- qr(m, LAPACK=FALSE) ; qr.coef(x, 1:3) }", "c(1, 2, 3)");
  }
  @Test
  public void micro1543() {
    assertIdentical("{ x <- qr(cbind(1:3,2:4), LAPACK=FALSE) ; round( qr.coef(x, 1:3), digits=5 ) }", "c(1, 0)");
  }
  @Test
  public void micro1544() {
    assertIdentical("{ round( qr.solve(qr(c(1,3,4,2)), c(1,2,3,4)), digits=5 ) }", "0.9");
  }
  @Test
  public void micro1545() {
    assertIdentical("{ round( qr.solve(c(1,3,4,2), c(1,2,3,4)), digits=5) }", "0.9");
  }
  @Test
  public void micro1550() {
    assertIdentical("{ round(0.4) }", "0");
  }
  @Test
  public void micro1551() {
    assertIdentical("{ round(0.5) }", "0");
  }
  @Test
  public void micro1552() {
    assertIdentical("{ round(0.6) }", "1");
  }
  @Test
  public void micro1553() {
    assertIdentical("{ round(1.5) }", "2");
  }
  @Test
  public void micro1554() {
    assertIdentical("{ round(1L) }", "1");
  }
  @Test
  public void micro1556() {
    assertIdentical("{ round(1/0) }", "Inf");
  }
  @Test
  public void micro1557() {
    assertIdentical("{ delayedAssign('x', y); y <- 10; x }", "10");
  }
  @Test
  public void micro1558() {
    assertIdentical("{ delayedAssign('x', a+b); a <- 1 ; b <- 3 ; x }", "4");
  }
  @Test
  public void micro1559() {
    assertIdentical("{ f <- function() { delayedAssign('x', y); y <- 10; x  } ; f() }", "10");
  }
  @Test
  public void micro1560() {
    assertIdentical("{ h <- new.env(parent=emptyenv()) ; delayedAssign('x', y, h, h) ; assign('y', 2, h) ; get('x', h) }", "2");
  }
  @Test
  public void micro1561() {
    assertIdentical("{ h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; delayedAssign('x', y, h, h) ; assign('y', 2, h) ; get('x', h) }", "2");
  }
  @Test
  public void micro1562() {
    assertIdentical("{ f <- function(...) { delayedAssign('x', ..1) ; y <<- x } ; f(10) ; y }", "10");
  }
  @Test
  public void micro1563() {
    assertIdentical("{ f <- function() { delayedAssign('x', 3); delayedAssign('x', 2); x } ; f() }", "2");
  }
  @Test
  public void micro1564() {
    assertIdentical("{ f <- function() { x <- 4 ; delayedAssign('x', y); y <- 10; x  } ; f() }", "10");
  }
  @Test
  public void micro1565() {
    assertIdentical("{ f <- function(a = 2 + 3) { missing(a) } ; f() }", "TRUE");
  }
  @Test
  public void micro1566() {
    assertIdentical("{ f <- function(a = z) { missing(a) } ; f() }", "TRUE");
  }
  @Test
  public void micro1567() {
    assertIdentical("{ f <- function(a = 2 + 3) { a;  missing(a) } ; f() }", "TRUE");
  }
  @Test
  public void micro1568() {
    assertIdentical("{ f <- function(a) { g(a) } ;  g <- function(b) { missing(b) } ; f() }", "TRUE");
  }
  @Test
  public void micro1569() {
    assertIdentical("{ f <- function(a = 2) { g(a) } ; g <- function(b) { missing(b) } ; f() }", "FALSE");
  }
  @Test
  public void micro1570() {
    assertIdentical("{ f <- function(a = z) {  g(a) } ; g <- function(b) { missing(b) } ; f() }", "FALSE");
  }
  @Test
  public void micro1571() {
    assertIdentical("{ f <- function(a = z, z) {  g(a) } ; g <- function(b) { missing(b) } ; f() }", "TRUE");
  }
  @Test
  public void micro1572() {
    assertIdentical("{ f <- function(a) { g(a) } ; g <- function(b=2) { missing(b) } ; f() }", "TRUE");
  }
  @Test
  public void micro1573() {
    assertIdentical("{ " +
        "g <- function(x, y) { missing(x) } ; " +
        "f <- function(x = y, y = x) { g(x, y) } ; " +
        "f() }", "TRUE");
  }
  @Test
  public void micro1574() {
    assertIdentical("{ f <- function(a,b,c) { missing(b) } ; f(1,,2) }", "TRUE");
  }
  @Test
  public void micro1575() {
    assertIdentical("{ g <- function(a, b, c) { b } ; f <- function(a,b,c) { g(a,b=2,c) } ; f(1,,2) }", "2");
  }
  @Test
  public void micro1576() {
    assertIdentical("{ f <- function(x) { missing(x) } ; f(a) }", "FALSE");
  }
  @Test
  public void micro1577() {
    assertIdentical("{ f <- function(a) { g <- function(b) { before <- missing(b) ; a <<- 2 ; after <- missing(b) ; c(before, after) } ; g(a) } ; f() }", "c(TRUE, FALSE)");
  }
  @Test
  public void micro1578() {
    assertIdentical("{ f <- function(...) { g(...) } ;  g <- function(b=2) { missing(b) } ; f() }", "TRUE");
  }
  @Test
  public void micro1579() {
    assertIdentical("{ f <- function(...) { missing(..2) } ; f(x + z, a * b) }", "FALSE");
  }
  @Test
  public void micro1582() {
    assertIdentical("{ typeof(quote(1)) }", "\"double\"");
  }
  @Test
  public void micro1583() {
    assertIdentical("{ typeof(quote(x + y)) }", "\"language\"");
  }
  @Test
  public void micro1585() {
    assertIdentical("{ typeof(quote(x)) }", "\"symbol\"");
  }
  @Test
  public void micro1603() {
    assertIdentical("{ f <- function(y) { substitute(y) } ; typeof(f()) }", "\"symbol\"");
  }
  @Test
  public void micro1621() {
    assertIdentical("{ f <- function(...) { g <- function() { list(...)$a } ; g() } ; f(a=1) }", "1");
  }
  @Test
  public void micro1622() {
    assertIdentical("{ f <- function(...) { l <- list(...) ; l[[1]] <- 10; ..1 } ; f(11,12,13) }", "11");
  }
  @Test
  public void micro1623() {
    assertIdentical("{ g <- function(...) { length(list(...)) } ; f <- function(...) { g(..., ...) } ; f(z = 1, g = 31) }", "4L");
  }
  @Test
  public void micro1624() {
    assertIdentical("{ g <- function(...) { max(...) } ; g(1,2) }", "2");
  }
  @Test
  public void micro1625() {
    assertIdentical("{ g <- function(...) { `-`(...) } ; g(1,2) }", "-1");
  }
  @Test
  public void micro1626() {
    assertIdentical("{ f <- function(...) { list(a=1,...) } ; f(b=2,3) }", "structure(list(a = 1, b = 2, 3), .Names = c(\"a\", \"b\", \"\"))");
  }
  @Test
  public void micro1628() {
    assertIdentical("{ f <- function(a, ...) { list(...) } ; f(1) }", "list()");
  }
  @Test
  public void micro1629() {
    assertIdentical("{ f <- function(...) { args <- list(...) ; args$name } ; f(name = 42) }", "42");
  }
  @Test
  public void micro1631() {
    assertIdentical("{ eval(quote(x+x), list(x=1)) }", "2");
  }
  @Test
  public void micro1632() {
    assertIdentical("{ y <- 2; eval(quote(x+y), list(x=1)) }", "3");
  }
  @Test
  public void micro1633() {
    assertIdentical("{ y <- 2; x <- 4; eval(x + y, list(x=1)) }", "6");
  }
  @Test
  public void micro1634() {
    assertIdentical("{ y <- 2; x <- 2 ; eval(quote(x+y), -1) }", "4");
  }
  @Test
  public void micro1635() {
    assertIdentical("{ f <- function(x) { deparse(substitute(x)) } ; f(a + b * (c - d)) }", "\"a + b * (c - d)\"");
  }
  @Test
  public void micro1636() {
    assertIdentical("{ sprintf('%d', 10) }", "\"10\"");
  }
  @Test
  public void micro1637() {
    assertIdentical("{ sprintf('%7.3f', 10.1) }", "\" 10.100\"");
  }
  @Test
  public void micro1638() {
    assertIdentical("{ sprintf('%03d', 1:3) }", "c(\"001\", \"002\", \"003\")");
  }
  @Test
  public void micro1639() {
    assertIdentical("{ sprintf('%3d', 1:3) }", "c(\"  1\", \"  2\", \"  3\")");
  }
  @Test
  public void micro1640() {
    assertIdentical("{ sprintf('Hello %*d', 3, 2) }", "\"Hello   2\"");
  }
  @Test
  public void micro1642() {
    assertIdentical("{ sprintf('Hello %2$*2$d', 3, 2) }", "\"Hello  2\"");
  }
  @Test
  public void micro1643() {
    assertIdentical("{ sprintf('%4X', 26) }", "\"  1A\"");
  }
  @Test
  public void micro1644() {
    assertIdentical("{ sprintf('%04X', 26) }", "\"001A\"");
  }
  @Test
  public void micro1645() {
    assertIdentical("{ sprintf('%s',NULL) }", "character(0)");
  }
  @Test
  public void micro1646() {
    assertIdentical("{ sprintf(c('%f','%e'),1) }", "c(\"1.000000\", \"1.000000e+00\")");
  }
  @Test
  public void micro1647() {
    assertIdentical("{ sprintf(c('%f','%% %e'),1) }", "c(\"1.000000\", \"% 1.000000e+00\")");
  }
  @Test
  public void micro1648() {
    assertIdentical("{ sprintf(c('%f','%e %%'),1) }", "c(\"1.000000\", \"1.000000e+00 %\")");
  }
  @Test
  public void micro1649() {
    assertIdentical("{ sprintf('second %2$1.0f, first %1$5.2f, third %3$1.0f', 3.141592, 2, 3) }", "\"second 2, first  3.14, third 3\"");
  }
  @Test
  public void micro1650() {
    assertIdentical("{ sprintf('res %4$6d',1,2,3,4,5,6,7,8,9,10,11) }", "\"res      4\"");
  }
  @Test
  public void micro1651() {
    assertIdentical("{ sprintf('res %11$06d',1,2,3,4,5,6,7,8,9,10,11) }", "\"res 000011\"");
  }
  @Test
  public void micro1652() {
    assertIdentical("{ sprintf('Hello %1$*11$d', 3, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12) }", "\"Hello            3\"");
  }
  @Test
  public void micro1653() {
    assertIdentical("{ sprintf('Hello %1$*3$d', 3, 2, 4L) }", "\"Hello    3\"");
  }
  @Test
  public void micro1654() {
    assertIdentical("{ sprintf('Hello %*i', 2, 3) }", "\"Hello  3\"");
  }
  @Test
  public void micro1655() {
    assertIdentical("{ sprintf('Hello %d == %s', TRUE, TRUE) }", "\"Hello 1 == TRUE\"");
  }
  @Test
  public void micro1656() {
    assertIdentical("{ sprintf('Hello %d == %s', 1L, 1L) }", "\"Hello 1 == 1\"");
  }
  @Test
  public void micro1657() {
    assertIdentical("{ sprintf('Hello %s', 'World!') }", "\"Hello World!\"");
  }
  @Test
  public void micro1658() {
    assertIdentical("{ sprintf('Hello %d', 100) }", "\"Hello 100\"");
  }
  @Test
  public void micro1659() {
    assertIdentical("{ sprintf('Hello %f %f %f %f', 0/0, -1/0, 1/0, 1[2]) }", "\"Hello NaN -Inf Inf NA\"");
  }
  @Test
  public void micro1661() {
    assertIdentical("{ sprintf('% f',1.234556) }", "\" 1.234556\"");
  }
  @Test
  public void micro1662() {
    assertIdentical("{ sprintf('Hello %s', 0/0) }", "\"Hello NaN\"");
  }
  @Test
  public void micro1663() {
    assertIdentical("{ sprintf('Hello %x', 1L[2]) }", "\"Hello NA\"");
  }
  @Test
  public void micro1664() {
    assertIdentical("{ sprintf('Hello %g', NA) }", "\"Hello NA\"");
  }
  @Test
  public void micro1665() {
    assertIdentical("{ sprintf('Hello %g', 1L[2]) }", "\"Hello NA\"");
  }
  @Test
  public void micro1666() {
    assertIdentical("{ sprintf('Hello %i', NA) }", "\"Hello NA\"");
  }
  @Test
  public void micro1667() {
    assertIdentical("{ identical(1,1) }", "TRUE");
  }
  @Test
  public void micro1668() {
    assertIdentical("{ identical(1L,1) }", "FALSE");
  }
  @Test
  public void micro1669() {
    assertIdentical("{ identical(1:3, c(1L,2L,3L)) }", "TRUE");
  }
  @Test
  public void micro1671() {
    assertIdentical("{ identical(list(1, list(2)), list(list(1), 1)) }", "FALSE");
  }
  @Test
  public void micro1672() {
    assertIdentical("{ identical(list(1, list(2)), list(1, list(2))) }", "TRUE");
  }
  @Test
  public void micro1673() {
    assertIdentical("{ x <- 1 ; attr(x, 'my') <- 10; identical(x, 1) }", "FALSE");
  }
  @Test
  public void micro1674() {
    assertIdentical("{ x <- 1 ; attr(x, 'my') <- 10; y <- 1 ; attr(y, 'my') <- 10 ; identical(x,y) }", "TRUE");
  }
  @Test
  public void micro1675() {
    assertIdentical("{ x <- 1 ; attr(x, 'my') <- 10; y <- 1 ; attr(y, 'my') <- 11 ; identical(x,y) }", "FALSE");
  }
  @Test
  public void micro1676() {
    assertIdentical("{ x <- 1 ; attr(x, 'hello') <- 2 ; attr(x, 'my') <- 10;  attr(x, 'hello') <- NULL ; y <- 1 ; attr(y, 'my') <- 10 ; identical(x,y) }", "TRUE");
  }
  @Test
  public void micro1677() {
    assertIdentical("{ identical(1,c) }", "FALSE");
  }
  @Test
  public void micro1678() {
    assertIdentical("{ identical(c,1) }", "FALSE");
  }
  @Test
  public void micro1679() {
    assertIdentical("{ identical(1:4, matrix(1:4,nrow=2)) }", "FALSE");
  }
  @Test
  public void micro1680() {
    assertIdentical("{ identical(1:4, c(a=1L,b=2L,3L,4L)) }", "FALSE");
  }
  @Test
  public void micro1681() {
    assertIdentical("{ identical(as.list(1:4), c(a=1L,b=2L,3L,4L)) }", "FALSE");
  }
  @Test
  public void micro1682() {
    assertIdentical("{ identical(as.list(1:4),1:4) }", "FALSE");
  }
  @Test
  public void micro1683() {
    assertIdentical("{ identical(c,c) }", "TRUE");
  }
  @Test
  public void micro1684() {
    assertIdentical("{ identical('1+2i',1+2i) }", "FALSE");
  }
  @Test
  public void micro1685() {
    assertIdentical("{ identical(1L, 1:1) }", "TRUE");
  }
  @Test
  public void micro1686() {
    assertIdentical("{ identical('hello', 'hello') }", "TRUE");
  }
  @Test
  public void micro1687() {
    assertIdentical("{ identical(1+2i, 0+1.0+2.0i-0) }", "TRUE");
  }
  @Test
  public void micro1688() {
    assertIdentical("{ identical(1+2i, 0+1.0+2.0i-0.001) }", "FALSE");
  }
  @Test
  public void micro1689() {
    assertIdentical("{ identical(0+0i,0) }", "FALSE");
  }
  @Test
  public void micro1690() {
    assertIdentical("{ identical(TRUE,as.logical(10)) }", "TRUE");
  }
  @Test
  public void micro1691() {
    assertIdentical("{ identical(TRUE,1L) }", "FALSE");
  }
  @Test
  public void micro1692() {
    assertIdentical("{ identical(as.raw(11), as.raw(10+1)) }", "TRUE");
  }
  @Test
  public void micro1693() {
    assertIdentical("{ identical(as.raw(11), 11) }", "FALSE");
  }
  @Test
  public void micro1694() {
    assertIdentical("{ identical(11, as.raw(11)) }", "FALSE");
  }
  @Test
  public void micro1695() {
    assertIdentical("{ identical(NULL,0) }", "FALSE");
  }
  @Test
  public void micro1696() {
    assertIdentical("{ identical(list(list(1,2),list(3)), list(list(1,2),list(3+0))) }", "TRUE");
  }
  @Test
  public void micro1697() {
    assertIdentical("{ identical(list(list(1,2),list(3)), list(list(1,2),list(3+0), list(4))) }", "FALSE");
  }
  @Test
  public void micro1698() {
    assertIdentical("{ identical(c('hello','hi'),c('hello','hI')) }", "FALSE");
  }
  @Test
  public void micro1699() {
    assertIdentical("{ identical(c('hello','hi'),c('hello',NA)) }", "FALSE");
  }
  @Test
  public void micro1700() {
    assertIdentical("{ identical(c('hello',NA),c('hello',NA)) }", "TRUE");
  }
  @Test
  public void micro1701() {
    assertIdentical("{ x <- 'hi' ; identical(c('hello',x),c('hello',x)) }", "TRUE");
  }
  @Test
  public void micro1702() {
    assertIdentical("{ identical(c('hello',NA),c('hello','x')) }", "FALSE");
  }
  @Test
  public void micro1703() {
    assertIdentical("{ identical(c('hello',NA),c('hello')) }", "FALSE");
  }
  @Test
  public void micro1704() {
    assertIdentical("{ identical(c(0/0,NA),c(NA,0/0)) }", "FALSE");
  }
  @Test
  public void micro1705() {
    assertIdentical("{ identical(c(1/0,-3/0),c(2/0,-1e100/0)) }", "TRUE");
  }
  @Test
  public void micro1706() {
    assertIdentical("{ identical(c(1/0,-3/0),c(0/0,NA)) }", "FALSE");
  }
  @Test
  public void micro1707() {
    assertIdentical("{ identical(c(0/0,NA),c(1/0,-3/0)) }", "FALSE");
  }
  @Test
  public void micro1708() {
    assertIdentical("{ identical(c(1+1,NA),c(2,NA)) }", "TRUE");
  }
  @Test
  public void micro1709() {
    assertIdentical("{ identical(c(1+2i,3+4i), c(1+2i,3+2i)) }", "FALSE");
  }
  @Test
  public void micro1710() {
    assertIdentical("{ identical(c(1+2i,3+4i), c(1+2i,2+4i)) }", "FALSE");
  }
  @Test
  public void micro1711() {
    assertIdentical("{ identical(c(1+2i,3+4i), c(1+2i)) }", "FALSE");
  }
  @Test
  public void micro1712() {
    assertIdentical("{ identical(c(1,2),c(1)) }", "FALSE");
  }
  @Test
  public void micro1713() {
    assertIdentical("{ identical(1:2,1:1) }", "FALSE");
  }
  @Test
  public void micro1714() {
    assertIdentical("{ identical(1:2,c(1L,3L)) }", "FALSE");
  }
  @Test
  public void micro1715() {
    assertIdentical("{ identical(c(TRUE,FALSE), c(TRUE,NA)) }", "FALSE");
  }
  @Test
  public void micro1716() {
    assertIdentical("{ identical(c(TRUE,FALSE), c(TRUE)) }", "FALSE");
  }
  @Test
  public void micro1717() {
    assertIdentical("{ identical(as.raw(11:12), as.raw(11)) }", "FALSE");
  }
  @Test
  public void micro1718() {
    assertIdentical("{ identical(as.raw(11:12), as.raw(c(11,13))) }", "FALSE");
  }
  @Test
  public void micro1719() {
    assertIdentical("{ x <- 1 ; attr(x,'my') <- 1 ; identical(1, x) }", "FALSE");
  }
  @Test
  public void micro1720() {
    assertIdentical("{ x <- 1 ; attr(x,'my') <- 1 ; attr(x,'my') <- NULL ; identical(1, x) }", "TRUE");
  }
  @Test
  public void micro1721() {
    assertIdentical("{ x <- 1 ; attr(x,'my') <- 1 ; attr(x,'my') <- NULL ; identical(x, 1) }", "TRUE");
  }
  @Test
  public void micro1722() {
    assertIdentical("{ x <- 1 ; attr(x,'my') <- 1 ; attr(x,'my') <- NULL ; y <- 1 ; attr(y,'hi') <- 2 ; identical(x, y) }", "FALSE");
  }
  @Test
  public void micro1723() {
    assertIdentical("{ x <- 1 ; attr(x,'my') <- 1 ; y <- 1 ; attr(y,'my') <- 2 ; identical(x, y) }", "FALSE");
  }
  @Test
  public void micro1724() {
    assertIdentical("{ l <- list(1,2,3); l[[2]] <- NULL; identical(l, list(1,3)) }", "TRUE");
  }
  @Test
  public void micro1725() {
    assertIdentical("{ x <- 1 ; attr(x,'my') <- 1 ; y <- 1 ; attr(y,'hi') <- 1 ; identical(x, y) }", "FALSE");
  }
  @Test
  public void micro1726() {
    assertIdentical("{ identical(c(a=1,b=2,c=3),c(a=1,aa=2,c=3)) }", "FALSE");
  }
  @Test
  public void micro1727() {
    assertIdentical("{ identical(c(a=1,b=2,c=3),c(a=1,b=2)) }", "FALSE");
  }
  @Test
  public void micro1728() {
    assertIdentical("{ identical(c(a=1,b=2),c(a=1,b=2)) }", "TRUE");
  }
  @Test
  public void micro1729() {
    assertIdentical("{ identical(c(a=1,b=2),c(1,2)) }", "FALSE");
  }
  @Test
  public void micro1730() {
    assertIdentical("{ identical(c(1,2), c(a=1,b=2)) }", "FALSE");
  }
  @Test
  public void micro1731() {
    assertIdentical("{ x <- list(1,b=2,3) ; x[[2]] <- NULL ; identical(x,list(1,3)) }", "FALSE");
  }
  @Test
  public void micro1732() {
    assertIdentical("{ cur <- getwd(); cur1 <- setwd(getwd()) ; cur2 <- getwd() ; cur == cur1 && cur == cur2 }", "TRUE");
  }
  @Test
  public void micro1734() {
    assertIdentical("{ list.files('test/r/simple/data/tree1') }", "character(0)");
  }
  @Test
  public void micro1735() {
    assertIdentical("{ list.files('test/r/simple/data/tree1', recursive=TRUE) }", "character(0)");
  }
  @Test
  public void micro1736() {
    assertIdentical("{ list.files('test/r/simple/data/tree1', recursive=TRUE, pattern='.*dummy.*') }", "character(0)");
  }
  @Test
  public void micro1737() {
    assertIdentical("{ list.files('test/r/simple/data/tree1', recursive=TRUE, pattern='dummy') }", "character(0)");
  }
  @Test
  public void micro1739() {
    assertIdentical("{ all(TRUE, FALSE, NA,  na.rm=FALSE) }", "FALSE");
  }
  @Test
  public void micro1740() {
    assertIdentical("{ all(TRUE, FALSE, NA,  na.rm=TRUE) }", "FALSE");
  }
  @Test
  public void micro1741() {
    assertIdentical("{ all(TRUE, TRUE, NA,  na.rm=TRUE) }", "TRUE");
  }
  @Test
  public void micro1742() {
    assertIdentical("{ all(TRUE, TRUE, NA,  na.rm=FALSE) }", "NA");
  }
  @Test
  public void micro1743() {
    assertIdentical("{ all() }", "TRUE");
  }
  @Test
  public void micro1744() {
    assertIdentical("{ any() }", "FALSE");
  }
  @Test
  public void micro1745() {
    assertIdentical("{ any(TRUE, TRUE, NA,  na.rm=TRUE) }", "TRUE");
  }
  @Test
  public void micro1746() {
    assertIdentical("{ any(TRUE, FALSE, NA,  na.rm=TRUE) }", "TRUE");
  }
  @Test
  public void micro1747() {
    assertIdentical("{ any(FALSE, NA,  na.rm=TRUE) }", "FALSE");
  }
  @Test
  public void micro1748() {
    assertIdentical("{ any(FALSE, NA,  na.rm=FALSE) }", "NA");
  }
  @Test
  public void micro1755() {
    assertIdentical("{ f <- function(a, b) { a + b } ; l <- call('f', 2, 3) ; eval(l) }", "5");
  }
  @Test
  public void micro1757() {
    assertIdentical("{ s <- proc.time()[3] ; e <- proc.time()[3] ; e >= s }", "structure(TRUE, .Names = \"elapsed\")");
  }
  @Test
  public void micro1765() {
    assertIdentical("{ x<-c(1,2,3,4);y<-c(10,2); x<=y }", "c(TRUE, TRUE, TRUE, FALSE)");
  }
  @Test
  public void micro1766() {
    assertIdentical("{ x<-c(1,2,3,4);y<-2.5; x<=y }", "c(TRUE, TRUE, FALSE, FALSE)");
  }
  @Test
  public void micro1767() {
    assertIdentical("{ x<-c(1,2,3,4);y<-c(2.5+NA,2.5); x<=y }", "c(NA, TRUE, NA, FALSE)");
  }
  @Test
  public void micro1768() {
    assertIdentical("{ x<-c(1L,2L,3L,4L);y<-c(2.5+NA,2.5); x<=y }", "c(NA, TRUE, NA, FALSE)");
  }
  @Test
  public void micro1769() {
    assertIdentical("{ x<-c(1L,2L,3L,4L);y<-c(TRUE,FALSE); x<=y }", "c(TRUE, FALSE, FALSE, FALSE)");
  }
  @Test
  public void micro1770() {
    assertIdentical("{ x<-c(1L,2L,3L,4L);y<-1.5; x<=y }", "c(TRUE, FALSE, FALSE, FALSE)");
  }
  @Test
  public void micro1771() {
    assertIdentical("{ c(1:3,4,5)==1:5 }", "c(TRUE, TRUE, TRUE, TRUE, TRUE)");
  }
  @Test
  public void micro1772() {
    assertIdentical("{ 3 != 1:2 }", "c(TRUE, TRUE)");
  }
  @Test
  public void micro1773() {
    assertIdentical("{ b <- 1:3 ; z <- FALSE ; b[2==2] }", "1:3");
  }
  @Test
  public void micro1774() {
    assertIdentical("{ 1:3 == TRUE }", "c(TRUE, FALSE, FALSE)");
  }
  @Test
  public void micro1775() {
    assertIdentical("{ TRUE == 1:3 }", "c(TRUE, FALSE, FALSE)");
  }
  @Test
  public void micro1776() {
    assertIdentical("{ c(1,2) < c(2,1,4) }", "c(TRUE, FALSE, TRUE)");
  }
  @Test
  public void micro1777() {
    assertIdentical("{ c(2,1,4) < c(1,2) }", "c(FALSE, TRUE, FALSE)");
  }
  @Test
  public void micro1778() {
    assertIdentical("{ c(1L,2L) < c(2L,1L,4L) }", "c(TRUE, FALSE, TRUE)");
  }
  @Test
  public void micro1779() {
    assertIdentical("{ c(2L,1L,4L) < c(1L,2L) }", "c(FALSE, TRUE, FALSE)");
  }
  @Test
  public void micro1780() {
    assertIdentical("{ c(TRUE,FALSE,FALSE) < c(TRUE,TRUE) }", "c(FALSE, TRUE, TRUE)");
  }
  @Test
  public void micro1781() {
    assertIdentical("{ c(TRUE,TRUE) == c(TRUE,FALSE,FALSE) }", "c(TRUE, FALSE, FALSE)");
  }
  @Test
  public void micro1782() {
    assertIdentical("{ as.raw(c(1,2)) < as.raw(c(2,1,4)) }", "c(TRUE, FALSE, TRUE)");
  }
  @Test
  public void micro1783() {
    assertIdentical("{ as.raw(c(2,1,4)) < as.raw(c(1,2)) }", "c(FALSE, TRUE, FALSE)");
  }
  @Test
  public void micro1784() {
    assertIdentical("{ c('hi','hello','bye') > c('cau', 'ahoj') }", "c(TRUE, TRUE, FALSE)");
  }
  @Test
  public void micro1785() {
    assertIdentical("{ c('cau', 'ahoj') != c('hi','hello','bye') }", "c(TRUE, TRUE, TRUE)");
  }
  @Test
  public void micro1786() {
    assertIdentical("{ c(1+1i,2+2i) == c(2+1i,1+2i,1+1i) }", "c(FALSE, FALSE, TRUE)");
  }
  @Test
  public void micro1787() {
    assertIdentical("{ c(2+1i,1+2i,1+1i) == c(1+1i, 2+2i) }", "c(FALSE, FALSE, TRUE)");
  }
  @Test
  public void micro1788() {
    assertIdentical("{ as.raw(c(2,1,4)) < raw() }", "logical(0)");
  }
  @Test
  public void micro1789() {
    assertIdentical("{ raw() < as.raw(c(2,1,4)) }", "logical(0)");
  }
  @Test
  public void micro1790() {
    assertIdentical("{ 1:3 < integer() }", "logical(0)");
  }
  @Test
  public void micro1791() {
    assertIdentical("{ integer() < 1:3 }", "logical(0)");
  }
  @Test
  public void micro1792() {
    assertIdentical("{ c(1,2,3) < double() }", "logical(0)");
  }
  @Test
  public void micro1793() {
    assertIdentical("{ double() == c(1,2,3) }", "logical(0)");
  }
  @Test
  public void micro1794() {
    assertIdentical("{ c(TRUE,FALSE) < logical() }", "logical(0)");
  }
  @Test
  public void micro1795() {
    assertIdentical("{ logical() == c(FALSE, FALSE) }", "logical(0)");
  }
  @Test
  public void micro1796() {
    assertIdentical("{ c(1+2i, 3+4i) == (1+2i)[0] }", "logical(0)");
  }
  @Test
  public void micro1797() {
    assertIdentical("{ (1+2i)[0] == c(2+3i, 4+1i) }", "logical(0)");
  }
  @Test
  public void micro1798() {
    assertIdentical("{ c('hello', 'hi') == character() }", "logical(0)");
  }
  @Test
  public void micro1799() {
    assertIdentical("{ character() > c('hello', 'hi') }", "logical(0)");
  }
  @Test
  public void micro1800() {
    assertIdentical("{ c(1,2,3,4) != c(1,NA) }", "c(FALSE, NA, TRUE, NA)");
  }
  @Test
  public void micro1801() {
    assertIdentical("{ c(1,2,NA,4) != 2 }", "c(TRUE, FALSE, NA, TRUE)");
  }
  @Test
  public void micro1802() {
    assertIdentical("{ 2 != c(1,2,NA,4) }", "c(TRUE, FALSE, NA, TRUE)");
  }
  @Test
  public void micro1803() {
    assertIdentical("{ c(1,2,NA,4) == 2 }", "c(FALSE, TRUE, NA, FALSE)");
  }
  @Test
  public void micro1804() {
    assertIdentical("{ 2 == c(1,2,NA,4) }", "c(FALSE, TRUE, NA, FALSE)");
  }
  @Test
  public void micro1805() {
    assertIdentical("{ c('hello', NA) < c('hi', NA) }", "c(TRUE, NA)");
  }
  @Test
  public void micro1806() {
    assertIdentical("{ c('hello', NA) >= 'hi' }", "c(FALSE, NA)");
  }
  @Test
  public void micro1807() {
    assertIdentical("{ 'hi' > c('hello', NA)  }", "c(TRUE, NA)");
  }
  @Test
  public void micro1808() {
    assertIdentical("{ c('hello', NA) > c(NA, 'hi') }", "c(NA, NA)");
  }
  @Test
  public void micro1809() {
    assertIdentical("{ c(1L, NA) > c(NA, 2L) }", "c(NA, NA)");
  }
  @Test
  public void micro1810() {
    assertIdentical("{ c(TRUE, NA) > c(NA, FALSE) }", "c(NA, NA)");
  }
  @Test
  public void micro1811() {
    assertIdentical("{ 'hi' > c('hello', 'hi')  }", "c(TRUE, FALSE)");
  }
  @Test
  public void micro1812() {
    assertIdentical("{ NA > c('hello', 'hi') }", "c(NA, NA)");
  }
  @Test
  public void micro1813() {
    assertIdentical("{ c('hello', 'hi') < NA }", "c(NA, NA)");
  }
  @Test
  public void micro1814() {
    assertIdentical("{ 1:3 < NA }", "c(NA, NA, NA)");
  }
  @Test
  public void micro1815() {
    assertIdentical("{ NA > 1:3 }", "c(NA, NA, NA)");
  }
  @Test
  public void micro1816() {
    assertIdentical("{ 2L > c(1L,NA,2L) }", "c(TRUE, NA, FALSE)");
  }
  @Test
  public void micro1817() {
    assertIdentical("{ c(1L,NA,2L) < 2L }", "c(TRUE, NA, FALSE)");
  }
  @Test
  public void micro1818() {
    assertIdentical("{ c(0/0+1i,2+1i) == c(1+1i,2+1i) }", "c(NA, TRUE)");
  }
  @Test
  public void micro1819() {
    assertIdentical("{ c(1+1i,2+1i) == c(0/0+1i,2+1i) }", "c(NA, TRUE)");
  }
  @Test
  public void micro1820() {
    assertIdentical("{ integer() == 2L }", "logical(0)");
  }
  @Test
  public void micro1821() {
    assertIdentical("{ 1==1 }", "TRUE");
  }
  @Test
  public void micro1822() {
    assertIdentical("{ 2==1 }", "FALSE");
  }
  @Test
  public void micro1823() {
    assertIdentical("{ 1L<=1 }", "TRUE");
  }
  @Test
  public void micro1824() {
    assertIdentical("{ 1<=0L }", "FALSE");
  }
  @Test
  public void micro1825() {
    assertIdentical("{ x<-2; f<-function(z=x) { if (z<=x) {z} else {x} } ; f(1.4)}", "1.4");
  }
  @Test
  public void micro1826() {
    assertIdentical("{ 1==NULL }", "logical(0)");
  }
  @Test
  public void micro1827() {
    assertIdentical("{ 1L==1 }", "TRUE");
  }
  @Test
  public void micro1828() {
    assertIdentical("{ TRUE==1 }", "TRUE");
  }
  @Test
  public void micro1829() {
    assertIdentical("{ TRUE==1L }", "TRUE");
  }
  @Test
  public void micro1830() {
    assertIdentical("{ 2L==TRUE }", "FALSE");
  }
  @Test
  public void micro1831() {
    assertIdentical("{ TRUE==FALSE }", "FALSE");
  }
  @Test
  public void micro1832() {
    assertIdentical("{ FALSE<=TRUE }", "TRUE");
  }
  @Test
  public void micro1833() {
    assertIdentical("{ FALSE<TRUE }", "TRUE");
  }
  @Test
  public void micro1834() {
    assertIdentical("{ TRUE>FALSE }", "TRUE");
  }
  @Test
  public void micro1835() {
    assertIdentical("{ TRUE>=FALSE }", "TRUE");
  }
  @Test
  public void micro1836() {
    assertIdentical("{ TRUE!=FALSE }", "TRUE");
  }
  @Test
  public void micro1837() {
    assertIdentical("{ 2L==NA }", "NA");
  }
  @Test
  public void micro1838() {
    assertIdentical("{ NA==2L }", "NA");
  }
  @Test
  public void micro1839() {
    assertIdentical("{ 2L==as.double(NA) }", "NA");
  }
  @Test
  public void micro1840() {
    assertIdentical("{ as.double(NA)==2L }", "NA");
  }
  @Test
  public void micro1841() {
    assertIdentical("{ 1+1i == 1-1i }", "FALSE");
  }
  @Test
  public void micro1842() {
    assertIdentical("{ 1+1i == 1+1i }", "TRUE");
  }
  @Test
  public void micro1843() {
    assertIdentical("{ 1+1i == 2+1i }", "FALSE");
  }
  @Test
  public void micro1844() {
    assertIdentical("{ 1+1i != 1+1i }", "FALSE");
  }
  @Test
  public void micro1845() {
    assertIdentical("{ 1+1i != 1-1i }", "TRUE");
  }
  @Test
  public void micro1846() {
    assertIdentical("{ 1+1i != 2+1i }", "TRUE");
  }
  @Test
  public void micro1847() {
    assertIdentical("{ 'hello' < 'hi' }", "TRUE");
  }
  @Test
  public void micro1848() {
    assertIdentical("{ 'hello' > 'hi' }", "FALSE");
  }
  @Test
  public void micro1849() {
    assertIdentical("{ 'hi' <= 'hello' }", "FALSE");
  }
  @Test
  public void micro1850() {
    assertIdentical("{ 'hi' >= 'hello' }", "TRUE");
  }
  @Test
  public void micro1851() {
    assertIdentical("{ 'hi' < 'hello' }", "FALSE");
  }
  @Test
  public void micro1852() {
    assertIdentical("{ 'hi' > 'hello' }", "TRUE");
  }
  @Test
  public void micro1853() {
    assertIdentical("{ 'hi' == 'hello' }", "FALSE");
  }
  @Test
  public void micro1854() {
    assertIdentical("{ 'hi' != 'hello' }", "TRUE");
  }
  @Test
  public void micro1855() {
    assertIdentical("{ 'hello' <= 'hi' }", "TRUE");
  }
  @Test
  public void micro1856() {
    assertIdentical("{ 'hello' >= 'hi' }", "FALSE");
  }
  @Test
  public void micro1857() {
    assertIdentical("{ 'hello' < 'hi' }", "TRUE");
  }
  @Test
  public void micro1858() {
    assertIdentical("{ 'hello' > 'hi' }", "FALSE");
  }
  @Test
  public void micro1859() {
    assertIdentical("{ 'hello' == 'hello' }", "TRUE");
  }
  @Test
  public void micro1860() {
    assertIdentical("{ 'hello' != 'hello' }", "FALSE");
  }
  @Test
  public void micro1861() {
    assertIdentical("{ 'a' <= 'b' }", "TRUE");
  }
  @Test
  public void micro1862() {
    assertIdentical("{ 'a' > 'b' }", "FALSE");
  }
  @Test
  public void micro1863() {
    assertIdentical("{ '2.0' == 2 }", "FALSE");
  }
  @Test
  public void micro1864() {
    assertIdentical("{ as.raw(15) > as.raw(10) }", "TRUE");
  }
  @Test
  public void micro1865() {
    assertIdentical("{ as.raw(15) < as.raw(10) }", "FALSE");
  }
  @Test
  public void micro1866() {
    assertIdentical("{ as.raw(15) >= as.raw(10) }", "TRUE");
  }
  @Test
  public void micro1867() {
    assertIdentical("{ as.raw(15) <= as.raw(10) }", "FALSE");
  }
  @Test
  public void micro1868() {
    assertIdentical("{ as.raw(10) >= as.raw(15) }", "FALSE");
  }
  @Test
  public void micro1869() {
    assertIdentical("{ as.raw(10) <= as.raw(15) }", "TRUE");
  }
  @Test
  public void micro1870() {
    assertIdentical("{ as.raw(15) == as.raw(10) }", "FALSE");
  }
  @Test
  public void micro1871() {
    assertIdentical("{ as.raw(15) != as.raw(10) }", "TRUE");
  }
  @Test
  public void micro1872() {
    assertIdentical("{ as.raw(15) == as.raw(15) }", "TRUE");
  }
  @Test
  public void micro1873() {
    assertIdentical("{ as.raw(15) != as.raw(15) }", "FALSE");
  }
  @Test
  public void micro1874() {
    assertIdentical("{ a <- as.raw(1) ; b <- as.raw(2) ; a < b }", "TRUE");
  }
  @Test
  public void micro1875() {
    assertIdentical("{ a <- as.raw(1) ; b <- as.raw(2) ; a > b }", "FALSE");
  }
  @Test
  public void micro1876() {
    assertIdentical("{ a <- as.raw(1) ; b <- as.raw(2) ; a == b }", "FALSE");
  }
  @Test
  public void micro1877() {
    assertIdentical("{ a <- as.raw(1) ; b <- as.raw(200) ; a < b }", "TRUE");
  }
  @Test
  public void micro1878() {
    assertIdentical("{ a <- as.raw(200) ; b <- as.raw(255) ; a < b }", "TRUE");
  }
  @Test
  public void micro1879() {
    assertIdentical("{ a <- 1 ; b <- a[2] ; a == b }", "NA");
  }
  @Test
  public void micro1880() {
    assertIdentical("{ a <- 1 ; b <- a[2] ; b > a }", "NA");
  }
  @Test
  public void micro1881() {
    assertIdentical("{ a <- 1L ; b <- a[2] ; a == b }", "NA");
  }
  @Test
  public void micro1882() {
    assertIdentical("{ a <- 1L ; b <- a[2] ; b > a }", "NA");
  }
  @Test
  public void micro1883() {
    assertIdentical("{ a <- 1L ; b <- 1[2] ; a == b }", "NA");
  }
  @Test
  public void micro1884() {
    assertIdentical("{ a <- 1L[2] ; b <- 1 ; a == b }", "NA");
  }
  @Test
  public void micro1885() {
    assertIdentical("{ a <- 1L[2] ; b <- 1 ; b > a }", "NA");
  }
  @Test
  public void micro1886() {
    assertIdentical("{ a <- 1 ; b <- 1L[2] ; a == b }", "NA");
  }
  @Test
  public void micro1887() {
    assertIdentical("{ a <- 1[2] ; b <- 1L ; b > a }", "NA");
  }
  @Test
  public void micro1888() {
    assertIdentical("{ a <- 1L ; b <- TRUE[2] ; a == b }", "NA");
  }
  @Test
  public void micro1889() {
    assertIdentical("{ a <- 1L[2] ; b <- TRUE ; a != b }", "NA");
  }
  @Test
  public void micro1890() {
    assertIdentical("{ a <- TRUE ; b <- 1L[2] ; a > b }", "NA");
  }
  @Test
  public void micro1891() {
    assertIdentical("{ a <- TRUE[2] ; b <- 1L ; a == b }", "NA");
  }
  @Test
  public void micro1892() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) }", "FALSE");
  }
  @Test
  public void micro1893() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1,2L) }", "FALSE");
  }
  @Test
  public void micro1894() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1L,2L) ; f(1,2) }", "FALSE");
  }
  @Test
  public void micro1895() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1L,2L) ; f(1L,2) }", "FALSE");
  }
  @Test
  public void micro1896() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1L,2) ; f(1,2) }", "FALSE");
  }
  @Test
  public void micro1897() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1L,2) ; f(1L,2L) }", "FALSE");
  }
  @Test
  public void micro1898() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2L) ; f(1,2) }", "FALSE");
  }
  @Test
  public void micro1899() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2L) ; f(1L,2L) }", "FALSE");
  }
  @Test
  public void micro1900() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(TRUE,FALSE) ; f(TRUE,2) }", "FALSE");
  }
  @Test
  public void micro1901() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(TRUE,FALSE) ; f(1L,2L) }", "FALSE");
  }
  @Test
  public void micro1902() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(0L,TRUE) ; f(FALSE,2) }", "FALSE");
  }
  @Test
  public void micro1903() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(0L,TRUE) ; f(0L,2L) }", "FALSE");
  }
  @Test
  public void micro1904() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(0L,TRUE) ; f(2L,TRUE) }", "TRUE");
  }
  @Test
  public void micro1905() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(TRUE,2L) ; f(FALSE,2) }", "FALSE");
  }
  @Test
  public void micro1906() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(TRUE,2L) ; f(0L,2L) }", "FALSE");
  }
  @Test
  public void micro1907() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f('hello', 'hi'[2]) }", "NA");
  }
  @Test
  public void micro1908() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f('hello'[2], 'hi') }", "NA");
  }
  @Test
  public void micro1909() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2, 1L[2]) }", "NA");
  }
  @Test
  public void micro1910() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2[2], 1L) }", "NA");
  }
  @Test
  public void micro1911() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2, 1[2]) }", "NA");
  }
  @Test
  public void micro1912() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2[2], 1) }", "NA");
  }
  @Test
  public void micro1913() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L, 1[2]) }", "NA");
  }
  @Test
  public void micro1914() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L[2], 1) }", "NA");
  }
  @Test
  public void micro1915() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L, 1L[2]) }", "NA");
  }
  @Test
  public void micro1916() {
    assertIdentical("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L[2], 1L) }", "NA");
  }
  @Test
  public void micro1917() {
    assertIdentical("{ z <- TRUE; dim(z) <- c(1) ; dim(z == TRUE) }", "1L");
  }
  @Test
  public void micro1921() {
    assertIdentical("{ 0/0 < c(1,2,3,4) }", "c(NA, NA, NA, NA)");
  }
  @Test
  public void micro1922() {
    assertIdentical("{ 0/0 == c(1,2,3,4) }", "c(NA, NA, NA, NA)");
  }
  @Test
  public void micro1923() {
    assertIdentical("{ x<-function(){1} ; x() }", "1");
  }
  @Test
  public void micro1924() {
    assertIdentical("{ x<-function(z){z} ; x(TRUE) }", "TRUE");
  }
  @Test
  public void micro1925() {
    assertIdentical("{ x<-1 ; f<-function(){x} ; x<-2 ; f() }", "2");
  }
  @Test
  public void micro1926() {
    assertIdentical("{ x<-1 ; f<-function(x){x} ; f(TRUE) }", "TRUE");
  }
  @Test
  public void micro1927() {
    assertIdentical("{ x<-1 ; f<-function(x){a<-1;b<-2;x} ; f(TRUE) }", "TRUE");
  }
  @Test
  public void micro1928() {
    assertIdentical("{ f<-function(x){g<-function(x) {x} ; g(x) } ; f(TRUE) }", "TRUE");
  }
  @Test
  public void micro1929() {
    assertIdentical("{ x<-1 ; f<-function(x){a<-1; b<-2; g<-function(x) {b<-3;x} ; g(b) } ; f(TRUE) }", "2");
  }
  @Test
  public void micro1930() {
    assertIdentical("{ x<-1 ; f<-function(z) { if (z) { x<-2 } ; x } ; x<-3 ; f(FALSE) }", "3");
  }
  @Test
  public void micro1931() {
    assertIdentical("{ f<-function() {z} ; z<-2 ; f() }", "2");
  }
  @Test
  public void micro1932() {
    assertIdentical("{ x<-1 ; g<-function() { x<-12 ; f<-function(z) { if (z) { x<-2 } ; x } ; x<-3 ; f(FALSE) } ; g() }", "3");
  }
  @Test
  public void micro1933() {
    assertIdentical("{ x<-function() { z<-211 ; function(a) { if (a) { z } else { 200 } } } ; f<-x() ; z<-1000 ; f(TRUE) }", "211");
  }
  @Test
  public void micro1934() {
    assertIdentical("{ f<-function(a=1,b=2,c=3) {TRUE} ; f(,,) }", "TRUE");
  }
  @Test
  public void micro1935() {
    assertIdentical("{ f<-function(x=2) {x} ; f() }", "2");
  }
  @Test
  public void micro1936() {
    assertIdentical("{ f<-function(a,b,c=2,d) {c} ; f(1,2,c=4,d=4) }", "4");
  }
  @Test
  public void micro1937() {
    assertIdentical("{ f<-function(a,b,c=2,d) {c} ; f(1,2,d=8,c=1) }", "1");
  }
  @Test
  public void micro1938() {
    assertIdentical("{ f<-function(a,b,c=2,d) {c} ; f(1,d=8,2,c=1) }", "1");
  }
  @Test
  public void micro1939() {
    assertIdentical("{ f<-function(a,b,c=2,d) {c} ; f(d=8,1,2,c=1) }", "1");
  }
  @Test
  public void micro1940() {
    assertIdentical("{ f<-function(a,b,c=2,d) {c} ; f(d=8,c=1,2,3) }", "1");
  }
  @Test
  public void micro1941() {
    assertIdentical("{ f<-function(a=10,b,c=20,d=20) {c} ; f(4,3,5,1) }", "5");
  }
  @Test
  public void micro1942() {
    assertIdentical("{ x<-1 ; z<-TRUE ; f<-function(y=x,a=z,b) { if (z) {y} else {z}} ; f(b=2) }", "1");
  }
  @Test
  public void micro1943() {
    assertIdentical("{ x<-1 ; z<-TRUE ; f<-function(y=x,a=z,b) { if (z) {y} else {z}} ; f(2) }", "2");
  }
  @Test
  public void micro1944() {
    assertIdentical("{ x<-1 ; f<-function(x=x) { x } ; f(x=x) }", "1");
  }
  @Test
  public void micro1945() {
    assertIdentical("{ f<-function(z, x=if (z) 2 else 3) {x} ; f(FALSE) }", "3");
  }
  @Test
  public void micro1946() {
    assertIdentical("{f<-function(a,b,c=2,d) {c} ; g <- function() f(d=8,c=1,2,3) ; g() ; g() }", "1");
  }
  @Test
  public void micro1948() {
    assertIdentical("{ f<-function() { return(2) ; 3 } ; f() }", "2");
  }
  @Test
  public void micro1949() {
    assertIdentical("{ x <- function(y) { sum(y) } ; f <- function() { x <- 1 ; x(1:10) } ; f() }", "55L");
  }
  @Test
  public void micro1950() {
    assertIdentical("{ f <- sum ; f(1:10) }", "55L");
  }
  @Test
  public void micro1951() {
    assertIdentical("{ x <- function(a,b) { a^b } ; f <- function() { x <- \"sum\" ; sapply(1, x, 2) } ; f() }", "3");
  }
  @Test
  public void micro1952() {
    assertIdentical("{ x <- function(a,b) { a^b } ; g <- function() { x <- \"sum\" ; f <- function() { sapply(1, x, 2) } ; f() }  ; g() }", "3");
  }
  @Test
  public void micro1953() {
    assertIdentical("{ x <- function(a,b) { a^b } ; f <- function() { x <- 211 ; sapply(1, x, 2) } ; f() }", "1");
  }
  @Test
  public void micro1954() {
    assertIdentical("{ x <- function(a,b) { a^b } ; dummy <- sum ; f <- function() { x <- \"dummy\" ; sapply(1, x, 2) } ; f() }", "3");
  }
  @Test
  public void micro1955() {
    assertIdentical("{ x <- function(a,b) { a^b } ; dummy <- sum ; f <- function() { x <- \"dummy\" ; dummy <- 200 ; sapply(1, x, 2) } ; f() }", "3");
  }
  @Test
  public void micro1958() {
    assertIdentical("{ myapp <- function(f, x, y) { f(x,y) } ; myapp(function(x,y) { x + y }, 1, 2) ; myapp(sum, 1, 2) }", "3");
  }
  @Test
  public void micro1959() {
    assertIdentical("{ myapp <- function(f, x, y) { f(x,y) } ; myapp(f = function(x,y) { x + y }, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) }", "3");
  }
  @Test
  public void micro1960() {
    assertIdentical("{ myapp <- function(f, x, y) { f(x,y) } ; myapp(f = function(x,y) { x + y }, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) ; myapp(f = c, y = 10, x = 3) }", "c(3, 10)");
  }
  @Test
  public void micro1961() {
    assertIdentical("{ myapp <- function(f, x, y) { f(x,y) } ; myapp(f = function(x,y) { x + y }, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) ; myapp(f = function(x,y) { x - y }, y = 10, x = 3) }", "-7");
  }
  @Test
  public void micro1962() {
    assertIdentical("{ myapp <- function(f, x, y) { f(x,y) } ; g <- function(x,y) { x + y } ; myapp(f = g, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) ; myapp(f = g, y = 10, x = 3) ;  myapp(f = g, y = 11, x = 2) }", "13");
  }
  @Test
  public void micro1963() {
    assertIdentical("{ f <- function(i) { if (i==2) { c <- sum }; c(1,2) } ; f(1) ; f(2) }", "3");
  }
  @Test
  public void micro1964() {
    assertIdentical("{ f <- function(i) { if (i==2) { assign(\"c\", sum) }; c(1,2) } ; f(1) ; f(2) }", "3");
  }
  @Test
  public void micro1965() {
    assertIdentical("{ f <- function(i) { c(1,2) } ; f(1) ; c <- sum ; f(2) }", "3");
  }
  @Test
  public void micro1966() {
    assertIdentical("{ f <- function(func, arg) { func(arg) } ; f(sum, c(3,2)) ; f(length, 1:4) }", "4L");
  }
  @Test
  public void micro1967() {
    assertIdentical("{ f <- function(func, arg) { func(arg) } ; f(sum, c(3,2)) ; f(length, 1:4) ; f(length,1:3) }", "3L");
  }
  @Test
  public void micro1968() {
    assertIdentical("{ f <- function(func, arg) { func(arg) } ; f(sum, c(3,2)) ; f(length, 1:4) ; f(function(i) {3}, 1) ; f(length,1:3) }", "3L");
  }
  @Test
  public void micro1969() {
    assertIdentical("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; f(function(x) {TRUE}, 5) ; f(is.na, 4) }", "2");
  }
  @Test
  public void micro1970() {
    assertIdentical("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) }", "1");
  }
  @Test
  public void micro1971() {
    assertIdentical("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; h <- function(x) { x == x } ; f(h, 3) }", "1");
  }
  @Test
  public void micro1972() {
    assertIdentical("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(is.na, 10) }", "2");
  }
  @Test
  public void micro1973() {
    assertIdentical("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(c, 10) }", "1");
  }
  @Test
  public void micro1975() {
    assertIdentical("{ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(is.na, 10) }", "2");
  }
  @Test
  public void micro1976() {
    assertIdentical("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4)  }", "FALSE");
  }
  @Test
  public void micro1977() {
    assertIdentical("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(is.na, 10) }", "FALSE");
  }
  @Test
  public void micro1978() {
    assertIdentical("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(length, 10) }", "TRUE");
  }
  @Test
  public void micro1979() {
    assertIdentical("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 10) ; f(is.na,5) }", "FALSE");
  }
  @Test
  public void micro1980() {
    assertIdentical("{ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(function(x) { x + x }, 10) }", "TRUE");
  }
  @Test
  public void micro1981() {
    assertIdentical("{ f<-function(i) { if(i==1) { 1 } else { j<-i-1 ; f(j) } } ; f(10) }", "1");
  }
  @Test
  public void micro1982() {
    assertIdentical("{ f<-function(i) { if(i==1) { 1 } else { f(i-1) } } ; f(10) }", "1");
  }
  @Test
  public void micro1983() {
    assertIdentical("{ f<-function(i) { if(i<=1) 1 else i*f(i-1) } ; f(10) }", "3628800");
  }
  @Test
  public void micro1984() {
    assertIdentical("{ f<-function(i) { if(i<=1L) 1L else i*f(i-1L) } ; f(10L) }", "3628800L");
  }
  @Test
  public void micro1985() {
    assertIdentical("{ f<-function(i) { if (i==1) { 1 } else if (i==2) { 1 } else { f(i-1) + f(i-2) } } ; f(10) }", "55");
  }
  @Test
  public void micro1986() {
    assertIdentical("{ f<-function(i) { if (i==1L) { 1L } else if (i==2L) { 1L } else { f(i-1L) + f(i-2L) } } ; f(10L) }", "55L");
  }
  @Test
  public void micro1987() {
    assertIdentical("{ f <- function(x = z) { z = 1 ; x } ; f() }", "1");
  }
  @Test
  public void micro1988() {
    assertIdentical("{ z <- 1 ; f <- function(c = z) {  z <- z + 1 ; c  } ; f() }", "2");
  }
  @Test
  public void micro1989() {
    assertIdentical("{ z <- 1 ; f <- function(c = z) { c(1,2) ; z <- z + 1 ; c  } ; f() }", "1");
  }
  @Test
  public void micro1990() {
    assertIdentical("{ f <- function(a) { g <- function(b) { x <<- 2; b } ; g(a) } ; x <- 1 ; f(x) }", "2");
  }
  @Test
  public void micro1991() {
    assertIdentical("{ f <- function(a) { g <- function(b) { a <<- 3; b } ; g(a) } ; x <- 1 ; f(x) }", "3");
  }
  @Test
  public void micro1992() {
    assertIdentical("{ f <- function(x) { function() {x} } ; a <- 1 ; b <- f(a) ; a <- 10 ; b() }", "10");
  }
  @Test
  public void micro1993() {
    assertIdentical("{ x<-function(foo,bar){foo*bar} ; x(f=10,2) }", "20");
  }
  @Test
  public void micro1994() {
    assertIdentical("{ x<-function(foo,bar){foo*bar} ; x(fo=10, bar=2) }", "20");
  }
  @Test
  public void micro1995() {
    assertIdentical("{ f <- function(...) { ..1 } ;  f(10) }", "10");
  }
  @Test
  public void micro1996() {
    assertIdentical("{ f <- function(...) { x <<- 10 ; ..1 } ; x <- 1 ; f(x) }", "10");
  }
  @Test
  public void micro1997() {
    assertIdentical("{ f <- function(...) { ..1 ; x <<- 10 ; ..1 } ; x <- 1 ; f(x) }", "1");
  }
  @Test
  public void micro1998() {
    assertIdentical("{ f <- function(...) { ..1 ; x <<- 10 ; ..2 } ; x <- 1 ; f(100,x) }", "10");
  }
  @Test
  public void micro1999() {
    assertIdentical("{ f <- function(...) { ..2 ; x <<- 10 ; ..1 } ; x <- 1 ; f(x,100) }", "10");
  }
  @Test
  public void micro2000() {
    assertIdentical("{ g <- function(...) { 0 } ; f <- function(...) { g(...) ; x <<- 10 ; ..1 } ; x <- 1 ; f(x) }", "10");
  }
  @Test
  public void micro2002() {
    assertIdentical("{ f <- function(...) { g <- function() { ..1 } ; g() } ; f(a=2) }", "2");
  }
  @Test
  public void micro2003() {
    assertIdentical("{ f <- function(...) { ..1 <- 2 ; ..1 } ; f(z = 1) }", "1");
  }
  @Test
  public void micro2004() {
    assertIdentical("{ g <- function(a,b) { a + b } ; f <- function(...) { g(...) }  ; f(1,2) }", "3");
  }
  @Test
  public void micro2005() {
    assertIdentical("{ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(...,x=4) }  ; f(b=1,a=2) }", "6");
  }
  @Test
  public void micro2006() {
    assertIdentical("{ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(x=4, ...) }  ; f(b=1,a=2) }", "6");
  }
  @Test
  public void micro2007() {
    assertIdentical("{ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(x=4, ..., 10) }  ; f(b=1) }", "14");
  }
  @Test
  public void micro2008() {
    assertIdentical("{ g <- function(...) { 0 } ; f <- function(...) { g(...) ; x <<- 10 ; ..1 } ; x <- 1 ; f(x) }", "10");
  }
  @Test
  public void micro2009() {
    assertIdentical("{ g <- function(a,b,aa,bb) { a ; x <<- 10 ; aa ; c(a, aa) } ; f <- function(...) {  g(..., ...) } ; x <- 1; y <- 2; f(x, y) }", "c(1, 1)");
  }
  @Test
  public void micro2010() {
    assertIdentical("{ f <- function(a, b) { a - b } ; g <- function(...) { f(1, ...) } ; g(b = 2) }", "-1");
  }
  @Test
  public void micro2011() {
    assertIdentical("{ f <- function(a, b) { a - b } ; g <- function(...) { f(1, ...) } ; g(a = 2) }", "1");
  }
  @Test
  public void micro2012() {
    assertIdentical("{ f <- function(...) { g(...) } ;  g <- function(b=2) { b } ; f() }", "2");
  }
  @Test
  public void micro2013() {
    assertIdentical("{ f <- function(a, barg) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(b=2) }", "3");
  }
  @Test
  public void micro2014() {
    assertIdentical("{ f <- function(a, barg, ...) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(b=2,3) }", "3");
  }
  @Test
  public void micro2015() {
    assertIdentical("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(be=2,du=3, 3) }", "4");
  }
  @Test
  public void micro2016() {
    assertIdentical("{ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(1,2,3) }", "2");
  }
  @Test
  public void micro2017() {
    assertIdentical("{ f <- function(a, b) { a * b } ; g <- function(...) { f(...,...) } ; g(3) }", "9");
  }
  @Test
  public void micro2018() {
    assertIdentical("{ g <- function(...) { c(...,...) } ; g(3) }", "c(3, 3)");
  }
  @Test
  public void micro2019() {
    assertIdentical("{ f <- function(...,d) { ..1 + ..2 } ; f(1,d=4,2) }", "3");
  }
  @Test
  public void micro2020() {
    assertIdentical("{ f <- function(...,d) { ..1 + ..2 } ; f(1,2,d=4) }", "3");
  }
  @Test
  public void micro2021() {
    assertIdentical("{ if(TRUE) 1 else 2 }", "1");
  }
  @Test
  public void micro2022() {
    assertIdentical("{ if(FALSE) 1 else 2 }", "2");
  }
  @Test
  public void micro2023() {
    assertIdentical("{ if(!FALSE) 1 else 2 }", "1");
  }
  @Test
  public void micro2024() {
    assertIdentical("{ if(!TRUE) 1 else 2 }", "2");
  }
  @Test
  public void micro2025() {
    assertIdentical("{ x <- 2 ; if (1==x) TRUE else 2 }", "2");
  }
  @Test
  public void micro2026() {
    assertIdentical("{ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(TRUE) }", "1");
  }
  @Test
  public void micro2027() {
    assertIdentical("{ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(FALSE) }", "2");
  }
  @Test
  public void micro2028() {
    assertIdentical("{ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(1:3) }", "1");
  }
  @Test
  public void micro2029() {
    assertIdentical("{ if (TRUE==FALSE) TRUE else FALSE }", "FALSE");
  }
  @Test
  public void micro2030() {
    assertIdentical("{ if (FALSE==TRUE) TRUE else FALSE }", "FALSE");
  }
  @Test
  public void micro2031() {
    assertIdentical("{ if (FALSE==1) TRUE else FALSE }", "FALSE");
  }
  @Test
  public void micro2032() {
    assertIdentical("{ f <- function(v) { if (FALSE==v) TRUE else FALSE } ; f(TRUE) ; f(1) }", "FALSE");
  }
  @Test
  public void micro2033() {
    assertIdentical("{ f <- function(a) { if (is.na(a)) { 1 } else { 2 } } ; f(5) ; f(1:3)}", "2");
  }
  @Test
  public void micro2034() {
    assertIdentical("{ if (1:3) { TRUE } }", "TRUE");
  }
  @Test
  public void micro2035() {
    assertIdentical("{ if (c(0,0,0)) { TRUE } else { 2 } }", "2");
  }
  @Test
  public void micro2036() {
    assertIdentical("{ if (c(1L,0L,0L)) { TRUE } else { 2 } }", "TRUE");
  }
  @Test
  public void micro2037() {
    assertIdentical("{ if (c(0L,0L,0L)) { TRUE } else { 2 } }", "2");
  }
  @Test
  public void micro2038() {
    assertIdentical("{ f <- function(cond) { if (cond) { TRUE } else { 2 } } ; f(1:3) ; f(2) }", "TRUE");
  }
  @Test
  public void micro2039() {
    assertIdentical("{ f <- function(cond) { if (cond) { TRUE } else { 2 }  } ; f(c(TRUE,FALSE)) ; f(FALSE) }", "2");
  }
  @Test
  public void micro2040() {
    assertIdentical("{ f <- function(cond) { if (cond) { TRUE } else { 2 }  } ; f(c(TRUE,FALSE)) ; f(1) }", "TRUE");
  }
  @Test
  public void micro2041() {
    assertIdentical("{ l <- quote({x <- 0 ; for(i in 1:10) { x <- x + i } ; x}) ; f <- function() { eval(l) } ; x <<- 10 ; f() }", "55");
  }
  @Test
  public void micro2042() {
    assertIdentical("{ x<-210 ; repeat { x <- x + 1 ; break } ; x }", "211");
  }
  @Test
  public void micro2043() {
    assertIdentical("{ x<-1 ; repeat { x <- x + 1 ; if (x > 11) { break } } ; x }", "12");
  }
  @Test
  public void micro2044() {
    assertIdentical("{ x<-1 ; repeat { x <- x + 1 ; if (x <= 11) { next } else { break } ; x <- 1024 } ; x }", "12");
  }
  @Test
  public void micro2045() {
    assertIdentical("{ x<-1 ; while(TRUE) { x <- x + 1 ; if (x > 11) { break } } ; x }", "12");
  }
  @Test
  public void micro2046() {
    assertIdentical("{ x<-1 ; while(x <= 10) { x<-x+1 } ; x }", "11");
  }
  @Test
  public void micro2047() {
    assertIdentical("{ x<-1 ; for(i in 1:10) { x<-x+1 } ; x }", "11");
  }
  @Test
  public void micro2048() {
    assertIdentical("{ for(i in c(1,2)) { x <- i } ; x }", "2");
  }
  @Test
  public void micro2049() {
    assertIdentical("{ f<-function(i) { if (i<=1) {1} else {r<-i; for(j in 2:(i-1)) {r=r*j}; r} }; f(10) }", "3628800");
  }
  @Test
  public void micro2050() {
    assertIdentical("{ f<-function(i) { x<-integer(i); x[1]<-1; x[2]<-1; if (i>2) { for(j in 3:i) { x[j]<-x[j-1]+x[j-2] } }; x[i] } ; f(32) }", "2178309");
  }
  @Test
  public void micro2051() {
    assertIdentical("{ f<-function(r) { x<-0 ; for(i in r) { x<-x+i } ; x } ; f(1:10) ; f(c(1,2,3,4,5)) }", "15");
  }
  @Test
  public void micro2052() {
    assertIdentical("{ f<-function(r) { x<-0 ; for(i in r) { x<-x+i } ; x } ; f(c(1,2,3,4,5)) ; f(1:10) }", "55");
  }
  @Test
  public void micro2053() {
    assertIdentical("{ l <- quote({for(i in c(1,2)) { x <- i } ; x }) ; f <- function() { eval(l) } ; f() }", "2");
  }
  @Test
  public void micro2054() {
    assertIdentical("{ l <- quote(for(i in s) { x <- i }) ; s <- 1:3 ; eval(l) ; s <- 2:1 ; eval(l) ; x }", "1L");
  }
  @Test
  public void micro2055() {
    assertIdentical("{ l <- quote({for(i in c(2,1)) { x <- i } ; x }) ; f <- function() { if (FALSE) i <- 2 ; eval(l) } ; f() }", "1");
  }
  @Test
  public void micro2056() {
    assertIdentical("{ l <- quote(for(i in s) { x <- i }) ; s <- 1:3 ; eval(l) ; s <- NULL ; eval(l) ; x }", "3L");
  }
  @Test
  public void micro2057() {
    assertIdentical("{ for(i in c(1,2,3,4)) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }", "2");
  }
  @Test
  public void micro2058() {
    assertIdentical("{ f <- function() { for(i in c(1,2,3,4)) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x } ; f()  }", "2");
  }
  @Test
  public void micro2059() {
    assertIdentical("{ l <- quote({ for(i in c(1,2,3,4)) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }) ; f <- function() { eval(l) } ; f()  }", "2");
  }
  @Test
  public void micro2060() {
    assertIdentical("{ l <- quote({ for(i in 1:4) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }) ; f <- function() { eval(l) } ; f()  }", "2L");
  }
  @Test
  public void micro2061() {
    assertIdentical("{ f <- function(s) { for(i in s) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x } ; f(2:1) ; f(c(1,2,3,4)) }", "2");
  }
  @Test
  public void micro2062() {
    assertIdentical("{ f <- function() { for(i in 1:4) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x } ; f() }", "2L");
  }
  @Test
  public void micro2063() {
    assertIdentical("{ for(i in 1:4) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }", "2L");
  }
  @Test
  public void micro2064() {
    assertIdentical("{ i <- 0L ; while(i < 3L) { i <- i + 1 ; if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }", "2");
  }
  @Test
  public void micro2065() {
    assertIdentical("{ i <- 1 ; r <- NULL ; for(v in list(NA,1)) { r[i] <- typeof(v) ; i <- i + 1 } ; r }", "c(\"logical\", \"double\")");
  }
  @Test
  public void micro2066() {
    assertIdentical("{ l <- quote(x[1] <- 1) ; f <- function() { eval(l) } ; x <- 10 ; f() ; x }", "10");
  }
  @Test
  public void micro2067() {
    assertIdentical("{ l <- quote(x[1] <- 1) ; f <- function() { eval(l) ; x <<- 10 ; get(\"x\") } ; x <- 20 ; f() }", "1");
  }
  @Test
  public void micro2068() {
    assertIdentical("{ 1:3 %in% 1:10 }", "c(TRUE, TRUE, TRUE)");
  }
  @Test
  public void micro2069() {
    assertIdentical("{ 1 %in% 1:10 }", "TRUE");
  }
  @Test
  public void micro2071() {
    assertIdentical("{ (1 + 2i) %in% c(1+10i, 1+4i, 2+2i, 1+2i) }", "TRUE");
  }
  @Test
  public void micro2073() {
    assertIdentical("{ x<-1:10; x[3] }", "3L");
  }
  @Test
  public void micro2074() {
    assertIdentical("{ x<-1:10; x[3L] }", "3L");
  }
  @Test
  public void micro2075() {
    assertIdentical("{ x<-c(1,2,3); x[3] }", "3");
  }
  @Test
  public void micro2076() {
    assertIdentical("{ x<-c(1,2,3); x[3L] }", "3");
  }
  @Test
  public void micro2077() {
    assertIdentical("{ x<-1:3; x[0-2] }", "c(1L, 3L)");
  }
  @Test
  public void micro2078() {
    assertIdentical("{ x<-1:3; x[FALSE] }", "integer(0)");
  }
  @Test
  public void micro2079() {
    assertIdentical("{ x<-1:3; x[TRUE] }", "1:3");
  }
  @Test
  public void micro2080() {
    assertIdentical("{ x<-c(TRUE,TRUE,FALSE); x[0-2] }", "c(TRUE, FALSE)");
  }
  @Test
  public void micro2081() {
    assertIdentical("{ x<-c(1,2);x[[0-1]] }", "2");
  }
  @Test
  public void micro2082() {
    assertIdentical("{ x<-c(1,2);x[0-3] }", "c(1, 2)");
  }
  @Test
  public void micro2083() {
    assertIdentical("{ x<-10; x[0-1] }", "numeric(0)");
  }
  @Test
  public void micro2084() {
    assertIdentical("{ x<-10; x[NA] }", "NA_real_");
  }
  @Test
  public void micro2085() {
    assertIdentical("{ x <- c(a=1, b=2, c=3) ; x[2] }", "structure(2, .Names = \"b\")");
  }
  @Test
  public void micro2086() {
    assertIdentical("{ x <- c(a=1, b=2, c=3) ; x[[2]] }", "2");
  }
  @Test
  public void micro2087() {
    assertIdentical("{ x <- c(a=\"A\", b=\"B\", c=\"C\") ; x[-2] }", "structure(c(\"A\", \"C\"), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro2088() {
    assertIdentical("{ x <- c(a=1+2i, b=2+3i, c=3) ; x[-2] }", "structure(c(1+2i, 3+0i), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro2089() {
    assertIdentical("{ x <- c(a=1, b=2, c=3) ; x[-2] }", "structure(c(1, 3), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro2090() {
    assertIdentical("{ x <- c(a=1L, b=2L, c=3L) ; x[-2] }", "structure(c(1L, 3L), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro2091() {
    assertIdentical("{ x <- c(a=TRUE, b=FALSE, c=NA) ; x[-2] }", "structure(c(TRUE, NA), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro2092() {
    assertIdentical("{ x <- c(a=as.raw(10), b=as.raw(11), c=as.raw(12)) ; x[-2] }", "structure(as.raw(c(0x0a, 0x0c)), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro2093() {
    assertIdentical("{ x <- c(a=1L, b=2L, c=3L) ; x[0] }", "structure(integer(0), .Names = character(0))");
  }
  @Test
  public void micro2094() {
    assertIdentical("{ x <- c(a=1L, b=2L, c=3L) ; x[10] }", "structure(NA_integer_, .Names = NA_character_)");
  }
  @Test
  public void micro2095() {
    assertIdentical("{ x <- c(a=TRUE, b=FALSE, c=NA) ; x[0] }", "structure(logical(0), .Names = character(0))");
  }
  @Test
  public void micro2096() {
    assertIdentical("{ x <- c(TRUE, FALSE, NA) ; x[0] }", "logical(0)");
  }
  @Test
  public void micro2097() {
    assertIdentical("{ x <- list(1L, 2L, 3L) ; x[10] }", "list(NULL)");
  }
  @Test
  public void micro2098() {
    assertIdentical("{ x <- list(a=1L, b=2L, c=3L) ; x[0] }", "structure(list(), .Names = character(0))");
  }
  @Test
  public void micro2099() {
    assertIdentical("{ x <- c(a=\"A\", b=\"B\", c=\"C\") ; x[10] }", "structure(NA_character_, .Names = NA_character_)");
  }
  @Test
  public void micro2100() {
    assertIdentical("{ x <- c(a=\"A\", b=\"B\", c=\"C\") ; x[0] }", "structure(character(0), .Names = character(0))");
  }
  @Test
  public void micro2101() {
    assertIdentical("{ x <- c(a=1+1i, b=2+2i, c=3+3i) ; x[10] }", "structure(NA_complex_, .Names = NA_character_)");
  }
  @Test
  public void micro2102() {
    assertIdentical("{ x <- c(a=1+1i, b=2+2i, c=3+3i) ; x[0] }", "structure(complex(0), .Names = character(0))");
  }
  @Test
  public void micro2103() {
    assertIdentical("{ x <- c(a=as.raw(10), b=as.raw(11), c=as.raw(12)) ; x[10] }", "structure(as.raw(0x00), .Names = NA_character_)");
  }
  @Test
  public void micro2104() {
    assertIdentical("{ x <- c(a=as.raw(10), b=as.raw(11), c=as.raw(12)) ; x[0] }", "structure(raw(0), .Names = character(0))");
  }
  @Test
  public void micro2105() {
    assertIdentical("{ x <- c(a=1, b=2, c=3) ; x[10] }", "structure(NA_real_, .Names = NA_character_)");
  }
  @Test
  public void micro2106() {
    assertIdentical("{ x <- c(a=1, b=2, c=3) ; x[0] }", "structure(numeric(0), .Names = character(0))");
  }
  @Test
  public void micro2107() {
    assertIdentical("{ x <- c(a=1,b=2,c=3,d=4) ; x[\"b\"] }", "structure(2, .Names = \"b\")");
  }
  @Test
  public void micro2108() {
    assertIdentical("{ x <- c(a=1,b=2,c=3,d=4) ; x[\"d\"] }", "structure(4, .Names = \"d\")");
  }
  @Test
  public void micro2109() {
    assertIdentical("{ x <- 1 ; attr(x, \"hi\") <- 2; x[2] <- 2; attr(x, \"hi\") }", "2");
  }
  @Test
  public void micro2110() {
    assertIdentical("{ x<-5:1 ; y <- -1L;  x[y] }", "c(4L, 3L, 2L, 1L)");
  }
  @Test
  public void micro2111() {
    assertIdentical("{ x<-5:1 ; y <- 6L;  x[y] }", "NA_integer_");
  }
  @Test
  public void micro2112() {
    assertIdentical("{ x<-5:1 ; y <- 2L;  x[[y]] }", "4L");
  }
  @Test
  public void micro2113() {
    assertIdentical("{ x<-as.list(5:1) ; y <- 2L;  x[[y]] }", "4L");
  }
  @Test
  public void micro2114() {
    assertIdentical("{ x <- c(1,4) ; y <- -1L ; x[y] }", "4");
  }
  @Test
  public void micro2115() {
    assertIdentical("{ x <- c(1,4) ; y <- 10L ; x[y] }", "NA_real_");
  }
  @Test
  public void micro2116() {
    assertIdentical("{ x <- c(1,4) ; y <- -1 ; x[y] }", "4");
  }
  @Test
  public void micro2117() {
    assertIdentical("{ x <- c(1,4) ; y <- 10 ; x[y] }", "NA_real_");
  }
  @Test
  public void micro2118() {
    assertIdentical("{ x <- c(a=1,b=2) ; y <- 2L ; x[y] }", "structure(2, .Names = \"b\")");
  }
  @Test
  public void micro2119() {
    assertIdentical("{ x <- 1:4 ; y <- -1 ; x[y] }", "2:4");
  }
  @Test
  public void micro2120() {
    assertIdentical("{ x <- 1:4 ; y <- 10 ; x[y] }", "NA_integer_");
  }
  @Test
  public void micro2121() {
    assertIdentical("{ x <- c(a=1,b=2) ; y <- 2 ; x[y] }", "structure(2, .Names = \"b\")");
  }
  @Test
  public void micro2122() {
    assertIdentical("{ x <- list(1,2,3,4) ; y <- 3 ; x[y] }", "list(3)");
  }
  @Test
  public void micro2123() {
    assertIdentical("{ x <- list(1,2,3,4) ; y <- 3 ; x[[y]] }", "3");
  }
  @Test
  public void micro2124() {
    assertIdentical("{ x <- list(1,4) ; y <- -1 ; x[y] }", "list(4)");
  }
  @Test
  public void micro2125() {
    assertIdentical("{ x <- list(1,4) ; y <- 4 ; x[y] }", "list(NULL)");
  }
  @Test
  public void micro2126() {
    assertIdentical("{ x <- list(a=1,b=4) ; y <- 2 ; x[y] }", "structure(list(b = 4), .Names = \"b\")");
  }
  @Test
  public void micro2127() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; x <- c(a=1,b=2) ; f(x,\"a\") }", "structure(1, .Names = \"a\")");
  }
  @Test
  public void micro2128() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; x <- c(a=1,b=2) ; f(x,\"a\") ; f(x,2) }", "structure(2, .Names = \"b\")");
  }
  @Test
  public void micro2129() {
    assertIdentical("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(1,2),TRUE) }", "1");
  }
  @Test
  public void micro2130() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(), NA) }", "list(NULL)");
  }
  @Test
  public void micro2131() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(integer(), NA) }", "NA_integer_");
  }
  @Test
  public void micro2132() {
    assertIdentical("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,-1) }", "2L");
  }
  @Test
  public void micro2133() {
    assertIdentical("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,-2) }", "1L");
  }
  @Test
  public void micro2134() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,NA) }", "c(NA_integer_, NA_integer_)");
  }
  @Test
  public void micro2135() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,-4) }", "1:2");
  }
  @Test
  public void micro2136() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=1L,b=2L),0) }", "structure(integer(0), .Names = character(0))");
  }
  @Test
  public void micro2137() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,0) }", "integer(0)");
  }
  @Test
  public void micro2138() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,-2) }", "1L");
  }
  @Test
  public void micro2139() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),NA) }", "c(NA, NA)");
  }
  @Test
  public void micro2140() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),-4) }", "c(TRUE, FALSE)");
  }
  @Test
  public void micro2141() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),0) }", "logical(0)");
  }
  @Test
  public void micro2142() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=TRUE,b=FALSE),0) }", "structure(logical(0), .Names = character(0))");
  }
  @Test
  public void micro2143() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),-2) }", "TRUE");
  }
  @Test
  public void micro2144() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),4) }", "NA");
  }
  @Test
  public void micro2145() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=TRUE,b=FALSE),4) }", "structure(NA, .Names = NA_character_)");
  }
  @Test
  public void micro2146() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(1,2),-4) }", "list(1, 2)");
  }
  @Test
  public void micro2147() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(1,2),4) }", "list(NULL)");
  }
  @Test
  public void micro2148() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(a=1,b=2),4) }", "structure(list(\"NA\" = NULL), .Names = NA_character_)");
  }
  @Test
  public void micro2149() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(\"a\",\"b\"),4) }", "NA_character_");
  }
  @Test
  public void micro2150() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(\"a\",\"b\"),NA) }", "c(NA_character_, NA_character_)");
  }
  @Test
  public void micro2151() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(\"a\",\"b\"),-4) }", "c(\"a\", \"b\")");
  }
  @Test
  public void micro2152() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(\"a\",\"b\"),0) }", "character(0)");
  }
  @Test
  public void micro2153() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=\"a\",b=\"b\"),0) }", "structure(character(0), .Names = character(0))");
  }
  @Test
  public void micro2154() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(1+2i,3+4i),NA) }", "c(NA_complex_, NA_complex_)");
  }
  @Test
  public void micro2155() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(1+2i,3+4i),-4) }", "c(1+2i, 3+4i)");
  }
  @Test
  public void micro2156() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(1+2i,3+4i),4) }", "NA_complex_");
  }
  @Test
  public void micro2157() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=1+2i,b=3+4i),4) }", "structure(NA_complex_, .Names = NA_character_)");
  }
  @Test
  public void micro2158() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(as.raw(c(10,11)),-4) }", "as.raw(c(0x0a, 0x0b))");
  }
  @Test
  public void micro2159() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(as.raw(c(10,11)),0) }", "raw(0)");
  }
  @Test
  public void micro2160() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(as.raw(c(10,11)),4) }", "as.raw(0x00)");
  }
  @Test
  public void micro2161() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(1+2i,3+4i) ; attr(z, \"my\") <- 1 ; f(z,-10) }", "c(1+2i, 3+4i)");
  }
  @Test
  public void micro2162() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(1,3) ; attr(z, \"my\") <- 1 ; f(z,-10) }", "c(1, 3)");
  }
  @Test
  public void micro2163() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(1L,3L) ; attr(z, \"my\") <- 1 ; f(z,-10) }", "c(1L, 3L)");
  }
  @Test
  public void micro2164() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(TRUE,FALSE) ; attr(z, \"my\") <- 1 ; f(z,-10) }", "c(TRUE, FALSE)");
  }
  @Test
  public void micro2165() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(a=\"a\",b=\"b\") ; attr(z, \"my\") <- 1 ; f(z,-10) }", "structure(c(\"a\", \"b\"), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2166() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(a=as.raw(10),b=as.raw(11)) ; attr(z, \"my\") <- 1 ; f(z,-10) }", "structure(as.raw(c(0x0a, 0x0b)), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2167() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:3,c(TRUE,FALSE)) }", "c(1L, 3L)");
  }
  @Test
  public void micro2168() {
    assertIdentical("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:3,c(1,2)) }", "1:2");
  }
  @Test
  public void micro2169() {
    assertIdentical("{ x <- as.list(1:2) ; f <- function(i) { x[i] <- NULL ; x } ; f(1) ; f(NULL) }", "list(1L, 2L)");
  }
  @Test
  public void micro2170() {
    assertIdentical("{ x <- 1:3 ; x[TRUE] <- 10 ; x }", "c(10, 10, 10)");
  }
  @Test
  public void micro2171() {
    assertIdentical("{ x <- 1:3 ; x[[TRUE]] <- 10 ; x }", "c(10, 2, 3)");
  }
  @Test
  public void micro2173() {
    assertIdentical("{ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[\"hello\"] <- NULL ; b }", "list(1+2i, 3+4i)");
  }
  @Test
  public void micro2174() {
    assertIdentical("{ x<-1:5 ; x[3:4] }", "3:4");
  }
  @Test
  public void micro2175() {
    assertIdentical("{ x<-1:5 ; x[4:3] }", "c(4L, 3L)");
  }
  @Test
  public void micro2176() {
    assertIdentical("{ x<-c(1,2,3,4,5) ; x[4:3] }", "c(4, 3)");
  }
  @Test
  public void micro2177() {
    assertIdentical("{ (1:5)[3:4] }", "3:4");
  }
  @Test
  public void micro2178() {
    assertIdentical("{ x<-(1:5)[2:4] ; x[2:1] }", "c(3L, 2L)");
  }
  @Test
  public void micro2179() {
    assertIdentical("{ x<-1:5;x[c(0-2,0-3)] }", "c(1L, 4L, 5L)");
  }
  @Test
  public void micro2180() {
    assertIdentical("{ x<-1:5;x[c(0-2,0-3,0,0,0)] }", "c(1L, 4L, 5L)");
  }
  @Test
  public void micro2181() {
    assertIdentical("{ x<-1:5;x[c(2,5,4,3,3,3,0)] }", "c(2L, 5L, 4L, 3L, 3L, 3L)");
  }
  @Test
  public void micro2182() {
    assertIdentical("{ x<-1:5;x[c(2L,5L,4L,3L,3L,3L,0L)] }", "c(2L, 5L, 4L, 3L, 3L, 3L)");
  }
  @Test
  public void micro2183() {
    assertIdentical("{ f<-function(x, i) { x[i] } ; f(1:3,3:1) ; f(1:5,c(0,0,0,0-2)) }", "c(1L, 3L, 4L, 5L)");
  }
  @Test
  public void micro2184() {
    assertIdentical("{ f<-function(x, i) { x[i] } ; f(1:3,0-3) ; f(1:5,c(0,0,0,0-2)) }", "c(1L, 3L, 4L, 5L)");
  }
  @Test
  public void micro2185() {
    assertIdentical("{ f<-function(x, i) { x[i] } ; f(1:3,0L-3L) ; f(1:5,c(0,0,0,0-2)) }", "c(1L, 3L, 4L, 5L)");
  }
  @Test
  public void micro2186() {
    assertIdentical("{ x<-1:5 ; x[c(TRUE,FALSE)] }", "c(1L, 3L, 5L)");
  }
  @Test
  public void micro2187() {
    assertIdentical("{ x<-1:5 ; x[c(TRUE,TRUE,TRUE,NA)] }", "c(1L, 2L, 3L, NA, 5L)");
  }
  @Test
  public void micro2188() {
    assertIdentical("{ x<-1:5 ; x[c(TRUE,TRUE,TRUE,FALSE,FALSE,FALSE,FALSE,TRUE,NA)] }", "c(1L, 2L, 3L, NA, NA)");
  }
  @Test
  public void micro2189() {
    assertIdentical("{ f<-function(i) { x<-1:5 ; x[i] } ; f(1) ; f(1L) ; f(TRUE) }", "1:5");
  }
  @Test
  public void micro2190() {
    assertIdentical("{ f<-function(i) { x<-1:5 ; x[i] } ; f(1) ; f(TRUE) ; f(1L)  }", "1L");
  }
  @Test
  public void micro2191() {
    assertIdentical("{ f<-function(i) { x<-1:5 ; x[i] } ; f(1) ; f(TRUE) ; f(c(3,2))  }", "c(3L, 2L)");
  }
  @Test
  public void micro2192() {
    assertIdentical("{ f<-function(i) { x<-1:5 ; x[i] } ; f(1)  ; f(3:4) }", "3:4");
  }
  @Test
  public void micro2193() {
    assertIdentical("{ f<-function(i) { x<-1:5 ; x[i] } ; f(c(TRUE,FALSE))  ; f(3:4) }", "3:4");
  }
  @Test
  public void micro2194() {
    assertIdentical("{ x<-as.complex(c(1,2,3,4)) ; x[2:4] }", "c(2+0i, 3+0i, 4+0i)");
  }
  @Test
  public void micro2195() {
    assertIdentical("{ x<-as.raw(c(1,2,3,4)) ; x[2:4] }", "as.raw(c(0x02, 0x03, 0x04))");
  }
  @Test
  public void micro2196() {
    assertIdentical("{ x<-c(1,2,3,4) ; names(x) <- c(\"a\",\"b\",\"c\",\"d\") ; x[c(10,2,3,0)] }", "structure(c(NA, 2, 3), .Names = c(NA, \"b\", \"c\"))");
  }
  @Test
  public void micro2197() {
    assertIdentical("{ x<-c(1,2,3,4) ; names(x) <- c(\"a\",\"b\",\"c\",\"d\") ; x[c(10,2,3)] }", "structure(c(NA, 2, 3), .Names = c(NA, \"b\", \"c\"))");
  }
  @Test
  public void micro2198() {
    assertIdentical("{ x<-c(1,2,3,4) ; names(x) <- c(\"a\",\"b\",\"c\",\"d\") ; x[c(-2,-4,0)] }", "structure(c(1, 3), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro2199() {
    assertIdentical("{ x<-c(1,2) ; names(x) <- c(\"a\",\"b\") ; x[c(FALSE,TRUE,NA,FALSE)] }", "structure(c(2, NA), .Names = c(\"b\", NA))");
  }
  @Test
  public void micro2200() {
    assertIdentical("{ x<-c(1,2) ; names(x) <- c(\"a\",\"b\") ; x[c(FALSE,TRUE)] }", "structure(2, .Names = \"b\")");
  }
  @Test
  public void micro2201() {
    assertIdentical("{ x <- c(a=1,b=2,c=3,d=4) ; x[character()] }", "structure(numeric(0), .Names = character(0))");
  }
  @Test
  public void micro2202() {
    assertIdentical("{ x <- c(a=1,b=2,c=3,d=4) ; x[c(\"b\",\"b\",\"d\",\"a\",\"a\")] }", "structure(c(2, 2, 4, 1, 1), .Names = c(\"b\", \"b\", \"d\", \"a\", \"a\"))");
  }
  @Test
  public void micro2203() {
    assertIdentical("{ x <- c(a=as.raw(10),b=as.raw(11),c=as.raw(12),d=as.raw(13)) ; f <- function(s) { x[s] } ; f(TRUE) ; f(1L) ; f(as.character(NA)) }", "structure(as.raw(0x00), .Names = NA_character_)");
  }
  @Test
  public void micro2204() {
    assertIdentical("{ x <- c(a=1,b=2,c=3,d=4) ; f <- function(s) { x[s] } ; f(TRUE) ; f(1L) ; f(\"b\") }", "structure(2, .Names = \"b\")");
  }
  @Test
  public void micro2205() {
    assertIdentical("{ x <- c(a=as.raw(10),b=as.raw(11),c=as.raw(12),d=as.raw(13)) ; f <- function(s) { x[c(s,s)] } ; f(TRUE) ; f(1L) ; f(as.character(NA)) }", "structure(as.raw(c(0x00, 0x00)), .Names = c(NA_character_, NA_character_))");
  }
  @Test
  public void micro2206() {
    assertIdentical("{ x <- c(a=1,b=2,c=3,d=4) ; f <- function(s) { x[c(s,s)] } ; f(TRUE) ; f(1L) ; f(\"b\") }", "structure(c(2, 2), .Names = c(\"b\", \"b\"))");
  }
  @Test
  public void micro2207() {
    assertIdentical("{ x <- 1;  y<-c(1,1) ; x[y] }", "c(1, 1)");
  }
  @Test
  public void micro2208() {
    assertIdentical("{ x <- 1L;  y<-c(1,1) ; x[y] }", "c(1L, 1L)");
  }
  @Test
  public void micro2209() {
    assertIdentical("{ x <- TRUE;  y<-c(1,1) ; x[y] }", "c(TRUE, TRUE)");
  }
  @Test
  public void micro2210() {
    assertIdentical("{ x <- \"hi\";  y<-c(1,1) ; x[y] }", "c(\"hi\", \"hi\")");
  }
  @Test
  public void micro2211() {
    assertIdentical("{ x <- 1+2i;  y<-c(1,2) ; x[y] }", "c(1+2i, complex(real=NA, i=NA))");
  }
  @Test
  public void micro2212() {
    assertIdentical("{ f<-function(x,l) { x[l == 3] } ; f(c(1,2,3), c(1,2,3)) ; f(c(1,2,3), 1:3) ; f(1:3, c(3,3,2)) }", "1:2");
  }
  @Test
  public void micro2213() {
    assertIdentical("{ f<-function(x,l) { x[l == 3] <- 4 } ; f(c(1,2,3), c(1,2,3)) ; f(c(1,2,3), 1:3) ; f(1:3, c(3,3,2)) }", "4");
  }
  @Test
  public void micro2214() {
    assertIdentical("{ x <- c(TRUE,FALSE,TRUE) ; x[2:3] }", "c(FALSE, TRUE)");
  }
  @Test
  public void micro2215() {
    assertIdentical("{ x <- c(1+2i,3+4i,5+6i) ; x[2:3] }", "c(3+4i, 5+6i)");
  }
  @Test
  public void micro2216() {
    assertIdentical("{ x <- c(1+2i,3+4i,5+6i) ; x[c(2,3,NA)] }", "c(3+4i, 5+6i, complex(real=NA, i=NA))");
  }
  @Test
  public void micro2217() {
    assertIdentical("{ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,-4,-5)] }", "1+2i");
  }
  @Test
  public void micro2218() {
    assertIdentical("{ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,-4,-5,-5)] }", "1+2i");
  }
  @Test
  public void micro2219() {
    assertIdentical("{ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,-4,-5,-2)] }", "1+2i");
  }
  @Test
  public void micro2220() {
    assertIdentical("{ x <- c(TRUE,FALSE,TRUE) ; x[integer()] }", "logical(0)");
  }
  @Test
  public void micro2221() {
    assertIdentical("{ x <- c(1,2,3,2) ; x[x==2] }", "c(2, 2)");
  }
  @Test
  public void micro2222() {
    assertIdentical("{ x <- c(1,2,3,2) ; x[c(3,4,2)==2] }", "3");
  }
  @Test
  public void micro2223() {
    assertIdentical("{ x <- c(a=1,x=2,b=3,y=2) ; x[c(3,4,2)==2] }", "structure(3, .Names = \"b\")");
  }
  @Test
  public void micro2224() {
    assertIdentical("{ x <- c(a=1,x=2,b=3,y=2) ; x[c(3,4,2,1)==2] }", "structure(3, .Names = \"b\")");
  }
  @Test
  public void micro2225() {
    assertIdentical("{ x <- c(as.double(1:2000)) ; x[c(1,3,3,3,1:1996)==3] }", "c(2, 3, 4, 7)");
  }
  @Test
  public void micro2226() {
    assertIdentical("{ x <- c(as.double(1:2000)) ; x[c(NA,3,3,NA,1:1996)==3] }", "c(NA, 2, 3, NA, 7)");
  }
  @Test
  public void micro2227() {
    assertIdentical("{ x <- c(as.double(1:2000)) ; sum(x[rep(3, 2000)==3]) }", "2001000");
  }
  @Test
  public void micro2228() {
    assertIdentical("{ x <- c(1,2,3,2) ; x[c(3,4,2,NA)==2] }", "c(3, NA)");
  }
  @Test
  public void micro2229() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,TRUE)) ; f(1:3,3:1) }", "c(3L, 2L, 1L)");
  }
  @Test
  public void micro2230() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,TRUE)) ; f(c(a=1,b=2,c=3),3:1) }", "structure(c(3, 2, 1), .Names = c(\"c\", \"b\", \"a\"))");
  }
  @Test
  public void micro2231() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,NA)) }", "c(1L, NA)");
  }
  @Test
  public void micro2232() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,NA,NA,NA)) }", "c(1L, NA, NA, NA)");
  }
  @Test
  public void micro2233() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(c(a=1,b=2,c=3), c(TRUE,NA,FALSE,FALSE,TRUE)) }", "structure(c(1, NA, NA), .Names = c(\"a\", NA, NA))");
  }
  @Test
  public void micro2234() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(c(a=1,b=2,c=3), c(TRUE,NA)) }", "structure(c(1, NA, 3), .Names = c(\"a\", NA, \"c\"))");
  }
  @Test
  public void micro2235() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(1:3, logical()) }", "integer(0)");
  }
  @Test
  public void micro2236() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(c(a=1L,b=2L,c=3L), logical()) }", "structure(integer(0), .Names = character(0))");
  }
  @Test
  public void micro2237() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(c(a=1,b=2,c=3), character()) }", "structure(numeric(0), .Names = character(0))");
  }
  @Test
  public void micro2238() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(c(1,2,3), character()) }", "numeric(0)");
  }
  @Test
  public void micro2239() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(c(1,2,3), c(\"hello\",\"hi\")) }", "c(NA_real_, NA_real_)");
  }
  @Test
  public void micro2240() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(1:3, c(\"h\",\"hi\")) ; f(1:3,TRUE) }", "1:3");
  }
  @Test
  public void micro2241() {
    assertIdentical("{ x <- list(1,2,list(3)) ; x[[c(3,1)]] }", "3");
  }
  @Test
  public void micro2243() {
    assertIdentical("{ x <- list(1,list(3)) ; x[[c(-1,1)]] }", "3");
  }
  @Test
  public void micro2244() {
    assertIdentical("{ l <- list(1,list(2)) ; f <- function(i) { l[[i]] } ; f(c(2,1)) ; f(1) }", "1");
  }
  @Test
  public void micro2247() {
    assertIdentical("{ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,-1)) }", "3");
  }
  @Test
  public void micro2248() {
    assertIdentical("{ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,-2)) }", "2");
  }
  @Test
  public void micro2249() {
    assertIdentical("{ x <- list(a=1,b=2,d=list(x=3)) ; x[[c(\"d\",\"x\")]] }", "3");
  }
  @Test
  public void micro2251() {
    assertIdentical("{ x <- list(a=1,b=2,d=list(x=3)) ; f <- function(i) { x[[i]] } ; f(c(\"d\",\"x\")) ; f(\"b\") }", "2");
  }
  @Test
  public void micro2252() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(1:3,c(2,1)) ; f(1:3,c(TRUE,FALSE)) }", "c(1L, 3L)");
  }
  @Test
  public void micro2253() {
    assertIdentical("{ f <- function(b,i) { b[i] } ; f(1:3,c(2,1)) ; f(1:3,NULL) }", "integer(0)");
  }
  @Test
  public void micro2254() {
    assertIdentical("{ x<-1:3; x[1]<-100L; x }", "c(100L, 2L, 3L)");
  }
  @Test
  public void micro2255() {
    assertIdentical("{ x<-c(1,2,3); x[2L]<-100L; x }", "c(1, 100, 3)");
  }
  @Test
  public void micro2256() {
    assertIdentical("{ x<-c(1,2,3); x[2L]<-100; x }", "c(1, 100, 3)");
  }
  @Test
  public void micro2257() {
    assertIdentical("{ x<-c(1,2,3); x[2]<-FALSE; x }", "c(1, 0, 3)");
  }
  @Test
  public void micro2258() {
    assertIdentical("{ x<-1:5; x[2]<-1000; x[3] <- TRUE; x[8]<-3L; x }", "c(1, 1000, 1, 4, 5, NA, NA, 3)");
  }
  @Test
  public void micro2259() {
    assertIdentical("{ x<-5:1; x[0-2]<-1000; x }", "c(1000, 4, 1000, 1000, 1000)");
  }
  @Test
  public void micro2260() {
    assertIdentical("{ x<-c(); x[[TRUE]] <- 2; x }", "2");
  }
  @Test
  public void micro2261() {
    assertIdentical("{ x<-1:2; x[[0-2]]<-100; x }", "c(100, 2)");
  }
  @Test
  public void micro2262() {
    assertIdentical("{ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,3L) ; f(c(1L,2L),2,3) }", "c(1, 3, 3, 4, 5)");
  }
  @Test
  public void micro2263() {
    assertIdentical("{ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,3L) ; f(c(1L,2L),8,3L) }", "c(1L, 2L, 3L, 4L, 5L, NA, NA, 3L)");
  }
  @Test
  public void micro2264() {
    assertIdentical("{ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,FALSE) ; f(c(1L,2L),2,3) }", "c(1, 3, 3, 4, 5)");
  }
  @Test
  public void micro2265() {
    assertIdentical("{ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,FALSE) ; f(c(1L,2L),8,TRUE) }", "c(1L, 2L, 3L, 4L, 5L, NA, NA, 1L)");
  }
  @Test
  public void micro2266() {
    assertIdentical("{ a <- c(1L,2L,3L); a <- 1:5; a[3] <- TRUE; a }", "c(1L, 2L, 1L, 4L, 5L)");
  }
  @Test
  public void micro2267() {
    assertIdentical("{ x <- 1:3 ; x[2] <- \"hi\"; x }", "c(\"1\", \"hi\", \"3\")");
  }
  @Test
  public void micro2268() {
    assertIdentical("{ x <- c(1,2,3) ; x[2] <- \"hi\"; x }", "c(\"1\", \"hi\", \"3\")");
  }
  @Test
  public void micro2269() {
    assertIdentical("{ x <- c(TRUE,FALSE,FALSE) ; x[2] <- \"hi\"; x }", "c(\"TRUE\", \"hi\", \"FALSE\")");
  }
  @Test
  public void micro2270() {
    assertIdentical("{ x <- c(2,3,4) ; x[1] <- 3+4i ; x  }", "c(3+4i, 3+0i, 4+0i)");
  }
  @Test
  public void micro2271() {
    assertIdentical("{ b <- c(1,2) ; x <- b ; b[2L] <- 3 ; b }", "c(1, 3)");
  }
  @Test
  public void micro2272() {
    assertIdentical("{ b <- c(1,2) ; b[0L] <- 3 ; b }", "c(1, 2)");
  }
  @Test
  public void micro2273() {
    assertIdentical("{ b <- c(1,2) ; b[0] <- 1+2i ; b }", "c(1+0i, 2+0i)");
  }
  @Test
  public void micro2274() {
    assertIdentical("{ b <- c(1,2) ; b[5L] <- 3 ; b }", "c(1, 2, NA, NA, 3)");
  }
  @Test
  public void micro2275() {
    assertIdentical("{ b <- c(1,2) ; z <- c(10,11) ; attr(z,\"my\") <- 4 ; b[2] <- z ; b }", "c(1, 10)");
  }
  @Test
  public void micro2276() {
    assertIdentical("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),10L) ; f(1,3) }", "c(1, 3)");
  }
  @Test
  public void micro2277() {
    assertIdentical("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),10L) ; f(1L,3) }", "c(1, 3)");
  }
  @Test
  public void micro2278() {
    assertIdentical("{ b <- c(1L,2L) ; b[3] <- 13L ; b }", "c(1L, 2L, 13L)");
  }
  @Test
  public void micro2279() {
    assertIdentical("{ b <- c(1L,2L) ; b[0] <- 13L ; b }", "1:2");
  }
  @Test
  public void micro2280() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; b <- c(10L,2L) ; b[0] <- TRUE ; b }", "c(10L, 2L)");
  }
  @Test
  public void micro2281() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; b <- c(10L,2L) ; b[3] <- TRUE ; b }", "c(10L, 2L, 1L)");
  }
  @Test
  public void micro2282() {
    assertIdentical("{ b <- c(1L,2L) ; b[2] <- FALSE ; b }", "c(1L, 0L)");
  }
  @Test
  public void micro2283() {
    assertIdentical("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),TRUE) ; f(1L,3) }", "c(1, 3)");
  }
  @Test
  public void micro2284() {
    assertIdentical("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),TRUE) ; f(10,3) }", "c(10, 3)");
  }
  @Test
  public void micro2285() {
    assertIdentical("{ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10) ; f(b,13L) }", "c(1, 13)");
  }
  @Test
  public void micro2286() {
    assertIdentical("{ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10) ; f(1:3,13L) }", "c(1L, 13L, 3L)");
  }
  @Test
  public void micro2287() {
    assertIdentical("{ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10) ; f(c(1,2),10) }", "c(1, 10)");
  }
  @Test
  public void micro2288() {
    assertIdentical("{ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10L) ; f(1:3,13L) }", "c(1L, 13L, 3L)");
  }
  @Test
  public void micro2289() {
    assertIdentical("{ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10L) ; f(b,13) }", "c(1, 13)");
  }
  @Test
  public void micro2290() {
    assertIdentical("{ b <- c(1,2) ; z <- b ; b[3L] <- 3L ; b }", "c(1, 2, 3)");
  }
  @Test
  public void micro2291() {
    assertIdentical("{ b <- c(1,2) ; z <- b ; b[-2] <- 3L ; b }", "c(3, 2)");
  }
  @Test
  public void micro2292() {
    assertIdentical("{ b <- c(1,2) ; z <- b ; b[3L] <- FALSE ; b }", "c(1, 2, 0)");
  }
  @Test
  public void micro2293() {
    assertIdentical("{ b <- c(1,2) ; z <- b ; b[-10L] <- FALSE ; b }", "c(0, 0)");
  }
  @Test
  public void micro2294() {
    assertIdentical("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1,2),FALSE) ; f(10L,3) }", "c(10, 3)");
  }
  @Test
  public void micro2295() {
    assertIdentical("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1,2),FALSE) ; f(10,3) }", "c(10, 3)");
  }
  @Test
  public void micro2296() {
    assertIdentical("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(TRUE,NA),FALSE) ; f(c(FALSE,TRUE),3) }", "c(0, 3)");
  }
  @Test
  public void micro2297() {
    assertIdentical("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(TRUE,NA),FALSE) ; f(3,3) }", "c(3, 3)");
  }
  @Test
  public void micro2298() {
    assertIdentical("{ b <- c(TRUE,NA) ; z <- b ; b[-10L] <- FALSE ; b }", "c(FALSE, FALSE)");
  }
  @Test
  public void micro2299() {
    assertIdentical("{ b <- c(TRUE,NA) ; z <- b ; b[4L] <- FALSE ; b }", "c(TRUE, NA, NA, FALSE)");
  }
  @Test
  public void micro2300() {
    assertIdentical("{ b <- list(TRUE,NA) ; z <- b ; b[[4L]] <- FALSE ; b }", "list(TRUE, NA, NULL, FALSE)");
  }
  @Test
  public void micro2301() {
    assertIdentical("{ b <- list(TRUE,NA) ; z <- b ; b[[-1L]] <- FALSE ; b }", "list(TRUE, FALSE)");
  }
  @Test
  public void micro2302() {
    assertIdentical("{ f <- function(b,v) { b[[2]] <- v ; b } ; f(list(TRUE,NA),FALSE) ; f(3,3) }", "c(3, 3)");
  }
  @Test
  public void micro2303() {
    assertIdentical("{ f <- function(b,v) { b[[2]] <- v ; b } ; f(list(TRUE,NA),FALSE) ; f(list(3),NULL) }", "list(3)");
  }
  @Test
  public void micro2304() {
    assertIdentical("{ f <- function(b,v) { b[[2]] <- v ; b } ; f(list(TRUE,NA),FALSE) ; f(list(),NULL) }", "list()");
  }
  @Test
  public void micro2305() {
    assertIdentical("{ f <- function(b,v) { b[[2]] <- v ; b } ; f(c(\"a\",\"b\"),\"d\") ; f(1:3,\"x\") }", "c(\"1\", \"x\", \"3\")");
  }
  @Test
  public void micro2306() {
    assertIdentical("{ b <- c(\"a\",\"b\") ; z <- b ; b[[-1L]] <- \"xx\" ; b }", "c(\"a\", \"xx\")");
  }
  @Test
  public void micro2307() {
    assertIdentical("{ b <- c(\"a\",\"b\") ; z <- b ; b[[3L]] <- \"xx\" ; b }", "c(\"a\", \"b\", \"xx\")");
  }
  @Test
  public void micro2308() {
    assertIdentical("{ b <- c(1,2) ; b[3] <- 2+3i ; b }", "c(1+0i, 2+0i, 2+3i)");
  }
  @Test
  public void micro2309() {
    assertIdentical("{ b <- c(1+2i,3+4i) ; b[3] <- 2 ; b }", "c(1+2i, 3+4i, 2+0i)");
  }
  @Test
  public void micro2310() {
    assertIdentical("{ b <- c(TRUE,NA) ; b[3] <- FALSE ; b }", "c(TRUE, NA, FALSE)");
  }
  @Test
  public void micro2311() {
    assertIdentical("{ b <- as.raw(c(1,2)) ; b[3] <- as.raw(13) ; b }", "as.raw(c(0x01, 0x02, 0x0d))");
  }
  @Test
  public void micro2313() {
    assertIdentical("{ b <- as.raw(c(1,2)) ; b[[-2]] <- as.raw(13) ; b }", "as.raw(c(0x0d, 0x02))");
  }
  @Test
  public void micro2314() {
    assertIdentical("{ b <- as.raw(c(1,2)) ; b[[-1]] <- as.raw(13) ; b }", "as.raw(c(0x01, 0x0d))");
  }
  @Test
  public void micro2315() {
    assertIdentical("{ x <- c(a=1+2i, b=3+4i) ; x[\"a\"] <- 10 ; x }", "structure(c(10+0i, 3+4i), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2316() {
    assertIdentical("{ x <- as.raw(c(10,11)) ; x[\"a\"] <- as.raw(13) ; x }", "structure(as.raw(c(0x0a, 0x0b, 0x0d)), .Names = c(\"\", \"\", \"a\"))");
  }
  @Test
  public void micro2317() {
    assertIdentical("{ x <- 1:2 ; x[\"a\"] <- 10+3i ; x }", "structure(c(1+0i, 2+0i, 10+3i), .Names = c(\"\", \"\", \"a\"))");
  }
  @Test
  public void micro2318() {
    assertIdentical("{ x <- c(a=1+2i, b=3+4i) ; x[\"a\"] <- \"hi\" ; x }", "structure(c(\"hi\", \"3+4i\"), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2319() {
    assertIdentical("{ x <- 1:2 ; x[\"a\"] <- 10 ; x }", "structure(c(1, 2, 10), .Names = c(\"\", \"\", \"a\"))");
  }
  @Test
  public void micro2320() {
    assertIdentical("{ x <- c(a=1,a=2) ; x[\"a\"] <- 10L ; x }", "structure(c(10, 2), .Names = c(\"a\", \"a\"))");
  }
  @Test
  public void micro2321() {
    assertIdentical("{ x <- 1:2 ; x[\"a\"] <- FALSE ; x }", "structure(c(1L, 2L, 0L), .Names = c(\"\", \"\", \"a\"))");
  }
  @Test
  public void micro2322() {
    assertIdentical("{ x <- c(aa=TRUE,b=FALSE) ; x[\"a\"] <- 2L ; x }", "structure(c(1L, 0L, 2L), .Names = c(\"aa\", \"b\", \"a\"))");
  }
  @Test
  public void micro2324() {
    assertIdentical("{ x <- c(aa=TRUE) ; x[\"a\"] <- list(2L) ; x }", "structure(list(aa = TRUE, a = 2L), .Names = c(\"aa\", \"a\"))");
  }
  @Test
  public void micro2325() {
    assertIdentical("{ x <- c(b=2,a=3) ; z <- x ; x[\"a\"] <- 1 ; x }", "structure(c(2, 1), .Names = c(\"b\", \"a\"))");
  }
  @Test
  public void micro2328() {
    assertIdentical("{ x <- list(1,2) ; dim(x) <- c(2,1) ; x[2] <- NULL ; x }", "list(1)");
  }
  @Test
  public void micro2329() {
    assertIdentical("{ x <- list(1,2) ; dim(x) <- c(2,1) ; x[[2]] <- NULL ; x }", "list(1)");
  }
  @Test
  public void micro2330() {
    assertIdentical("{ x <- list(1,2) ; x[0] <- NULL ; x }", "list(1, 2)");
  }
  @Test
  public void micro2331() {
    assertIdentical("{ x <- list(1,2) ; x[NA] <- NULL ; x }", "list(1, 2)");
  }
  @Test
  public void micro2332() {
    assertIdentical("{ x <- list(1,2) ; x[as.integer(NA)] <- NULL ; x }", "list(1, 2)");
  }
  @Test
  public void micro2334() {
    assertIdentical("{ x <- list(3,4) ; x[[-1]] <- NULL ; x }", "list(3)");
  }
  @Test
  public void micro2335() {
    assertIdentical("{ x <- list(3,4) ; x[[-2]] <- NULL ; x }", "list(4)");
  }
  @Test
  public void micro2336() {
    assertIdentical("{ x <- list(a=3,b=4) ; x[[\"a\"]] <- NULL ; x }", "structure(list(b = 4), .Names = \"b\")");
  }
  @Test
  public void micro2337() {
    assertIdentical("{ x <- list(a=3,b=4) ; x[\"z\"] <- NULL ; x }", "structure(list(a = 3, b = 4), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2338() {
    assertIdentical("{ x <- as.list(1:2) ; x[[\"z\"]] <- NULL ; x }", "list(1L, 2L)");
  }
  @Test
  public void micro2339() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,-2,10) }", "c(10, 2)");
  }
  @Test
  public void micro2341() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,c(2),10) ; f(1:2,2, 10) }", "c(1, 10)");
  }
  @Test
  public void micro2343() {
    assertIdentical("{ l <- list(1,2) ; l[[2]] <- as.raw(13) ; l }", "list(1, as.raw(0x0d))");
  }
  @Test
  public void micro2344() {
    assertIdentical("{ a <- c(1,2,3) ; b <- a; a[1] <- 4L; a }", "c(4, 2, 3)");
  }
  @Test
  public void micro2345() {
    assertIdentical("{ a <- c(1,2,3) ; b <- a; a[2] <- 4L; a }", "c(1, 4, 3)");
  }
  @Test
  public void micro2346() {
    assertIdentical("{ a <- c(1,2,3) ; b <- a; a[3] <- 4L; a }", "c(1, 2, 4)");
  }
  @Test
  public void micro2347() {
    assertIdentical("{ a <- c(2.1,2.2,2.3); b <- a; a[[1]] <- TRUE; a }", "c(1, 2.2, 2.3)");
  }
  @Test
  public void micro2348() {
    assertIdentical("{ a <- c(2.1,2.2,2.3); b <- a; a[[2]] <- TRUE; a }", "c(2.1, 1, 2.3)");
  }
  @Test
  public void micro2349() {
    assertIdentical("{ a <- c(2.1,2.2,2.3); b <- a; a[[3]] <- TRUE; a }", "c(2.1, 2.2, 1)");
  }
  @Test
  public void micro2350() {
    assertIdentical("{ a <- c(TRUE,TRUE,TRUE); b <- a; a[[1]] <- FALSE; a }", "c(FALSE, TRUE, TRUE)");
  }
  @Test
  public void micro2351() {
    assertIdentical("{ a <- c(TRUE,TRUE,TRUE); b <- a; a[[2]] <- FALSE; a }", "c(TRUE, FALSE, TRUE)");
  }
  @Test
  public void micro2352() {
    assertIdentical("{ a <- c(TRUE,TRUE,TRUE); b <- a; a[[3]] <- FALSE; a }", "c(TRUE, TRUE, FALSE)");
  }
  @Test
  public void micro2353() {
    assertIdentical("{ x<-c(1,2,3,4,5); x[3:4]<-c(300L,400L); x }", "c(1, 2, 300, 400, 5)");
  }
  @Test
  public void micro2354() {
    assertIdentical("{ x<-c(1,2,3,4,5); x[4:3]<-c(300L,400L); x }", "c(1, 2, 400, 300, 5)");
  }
  @Test
  public void micro2355() {
    assertIdentical("{ x<-1:5; x[4:3]<-c(300L,400L); x }", "c(1L, 2L, 400L, 300L, 5L)");
  }
  @Test
  public void micro2356() {
    assertIdentical("{ x<-5:1; x[3:4]<-c(300L,400L); x }", "c(5L, 4L, 300L, 400L, 1L)");
  }
  @Test
  public void micro2357() {
    assertIdentical("{ x<-5:1; x[3:4]<-c(300,400); x }", "c(5, 4, 300, 400, 1)");
  }
  @Test
  public void micro2358() {
    assertIdentical("{ x<-1:5; x[c(0-2,0-3,0-3,0-100,0)]<-256; x }", "c(256, 2, 3, 256, 256)");
  }
  @Test
  public void micro2359() {
    assertIdentical("{ x<-1:5; x[c(4,2,3)]<-c(256L,257L,258L); x }", "c(1L, 257L, 258L, 256L, 5L)");
  }
  @Test
  public void micro2360() {
    assertIdentical("{ x<-c(1,2,3,4,5); x[c(TRUE,FALSE)] <- 1000; x }", "c(1000, 2, 1000, 4, 1000)");
  }
  @Test
  public void micro2361() {
    assertIdentical("{ x<-c(1,2,3,4,5,6); x[c(TRUE,TRUE,FALSE)] <- c(1000L,2000L) ; x }", "c(1000, 2000, 3, 1000, 2000, 6)");
  }
  @Test
  public void micro2362() {
    assertIdentical("{ x<-c(1,2,3,4,5); x[c(TRUE,FALSE,TRUE,TRUE,FALSE)] <- c(1000,2000,3000); x }", "c(1000, 2, 2000, 3000, 5)");
  }
  @Test
  public void micro2363() {
    assertIdentical("{ x<-c(1,2,3,4,5); x[c(TRUE,FALSE,TRUE,TRUE,0)] <- c(1000,2000,3000); x }", "c(3000, 2, 3, 4, 5)");
  }
  @Test
  public void micro2364() {
    assertIdentical("{ x<-1:3; x[c(TRUE, FALSE, TRUE)] <- c(TRUE,FALSE); x }", "c(1L, 2L, 0L)");
  }
  @Test
  public void micro2365() {
    assertIdentical("{ x<-c(TRUE,TRUE,FALSE); x[c(TRUE, FALSE, TRUE)] <- c(FALSE,TRUE); x }", "c(FALSE, TRUE, TRUE)");
  }
  @Test
  public void micro2366() {
    assertIdentical("{ x<-c(TRUE,TRUE,FALSE); x[c(TRUE, FALSE, TRUE)] <- c(1000,2000); x }", "c(1000, 1, 2000)");
  }
  @Test
  public void micro2367() {
    assertIdentical("{ x<-11:9 ; x[c(TRUE, FALSE, TRUE)] <- c(1000,2000); x }", "c(1000, 10, 2000)");
  }
  @Test
  public void micro2368() {
    assertIdentical("{ l <- double() ; l[c(TRUE,TRUE)] <-2 ; l}", "c(2, 2)");
  }
  @Test
  public void micro2369() {
    assertIdentical("{ l <- double() ; l[c(FALSE,TRUE)] <-2 ; l}", "c(NA, 2)");
  }
  @Test
  public void micro2370() {
    assertIdentical("{ a<- c('a','b','c','d'); a[3:4] <- c(4,5); a}", "c(\"a\", \"b\", \"4\", \"5\")");
  }
  @Test
  public void micro2371() {
    assertIdentical("{ a<- c('a','b','c','d'); a[3:4] <- c(4L,5L); a}", "c(\"a\", \"b\", \"4\", \"5\")");
  }
  @Test
  public void micro2372() {
    assertIdentical("{ a<- c('a','b','c','d'); a[3:4] <- c(TRUE,FALSE); a}", "c(\"a\", \"b\", \"TRUE\", \"FALSE\")");
  }
  @Test
  public void micro2373() {
    assertIdentical("{ f<-function(i,v) { x<-1:5 ; x[i]<-v ; x } ; f(1,1) ; f(1L,TRUE) ; f(2,TRUE) }", "c(1L, 1L, 3L, 4L, 5L)");
  }
  @Test
  public void micro2374() {
    assertIdentical("{ f<-function(i,v) { x<-1:5 ; x[[i]]<-v ; x } ; f(1,1) ; f(1L,TRUE) ; f(2,TRUE) }", "c(1L, 1L, 3L, 4L, 5L)");
  }
  @Test
  public void micro2375() {
    assertIdentical("{ f<-function(i,v) { x<-1:5 ; x[i]<-v ; x } ; f(3:2,1) ; f(1L,TRUE) ; f(2:4,4:2) }", "c(1L, 4L, 3L, 2L, 5L)");
  }
  @Test
  public void micro2376() {
    assertIdentical("{ f<-function(i,v) { x<-1:5 ; x[i]<-v ; x } ; f(c(3,2),1) ; f(1L,TRUE) ; f(2:4,c(4,3,2)) }", "c(1, 4, 3, 2, 5)");
  }
  @Test
  public void micro2377() {
    assertIdentical("{ f<-function(b,i,v) { b[i]<-v ; b } ; f(1:4,4:1,TRUE) ; f(c(3,2,1),8,10) }", "c(3, 2, 1, NA, NA, NA, NA, 10)");
  }
  @Test
  public void micro2378() {
    assertIdentical("{ f<-function(b,i,v) { b[i]<-v ; b } ; f(1:4,4:1,TRUE) ; f(c(3,2,1),8,10) ; f(c(TRUE,FALSE),TRUE,FALSE) }", "c(FALSE, FALSE)");
  }
  @Test
  public void micro2379() {
    assertIdentical("{ x<-c(TRUE,TRUE,FALSE,TRUE) ; x[3:2] <- TRUE; x }", "c(TRUE, TRUE, TRUE, TRUE)");
  }
  @Test
  public void micro2380() {
    assertIdentical("{ x<-1:3 ; y<-(x[2]<-100) ; y }", "100");
  }
  @Test
  public void micro2382() {
    assertIdentical("{ x<-1:5 ; x[3] <- (x[4]<-100) ; x }", "c(1, 2, 100, 100, 5)");
  }
  @Test
  public void micro2383() {
    assertIdentical("{ x<-5:1 ; x[x[2]<-2] }", "4L");
  }
  @Test
  public void micro2384() {
    assertIdentical("{ x<-5:1 ; x[x[2]<-2] <- (x[3]<-50) ; x }", "c(5, 50, 50, 2, 1)");
  }
  @Test
  public void micro2385() {
    assertIdentical("{ v<-1:3 ; v[TRUE] <- 100 ; v }", "c(100, 100, 100)");
  }
  @Test
  public void micro2386() {
    assertIdentical("{ v<-1:3 ; v[-1] <- c(100,101) ; v }", "c(1, 100, 101)");
  }
  @Test
  public void micro2387() {
    assertIdentical("{ v<-1:3 ; v[TRUE] <- c(100,101,102) ; v }", "c(100, 101, 102)");
  }
  @Test
  public void micro2388() {
    assertIdentical("{ x <- c(a=1,b=2,c=3) ; x[2]<-10; x }", "structure(c(1, 10, 3), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2389() {
    assertIdentical("{ x <- c(a=1,b=2,c=3) ; x[2:3]<-10; x }", "structure(c(1, 10, 10), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2390() {
    assertIdentical("{ x <- c(a=1,b=2,c=3) ; x[c(2,3)]<-10; x }", "structure(c(1, 10, 10), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2391() {
    assertIdentical("{ x <- c(a=1,b=2,c=3) ; x[c(TRUE,TRUE,FALSE)]<-10; x }", "structure(c(10, 10, 3), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2392() {
    assertIdentical("{ x <- c(a=1,b=2) ; x[2:3]<-10; x }", "structure(c(1, 10, 10), .Names = c(\"a\", \"b\", \"\"))");
  }
  @Test
  public void micro2393() {
    assertIdentical("{ x <- c(a=1,b=2) ; x[c(2,3)]<-10; x }", "structure(c(1, 10, 10), .Names = c(\"a\", \"b\", \"\"))");
  }
  @Test
  public void micro2394() {
    assertIdentical("{ x <- c(a=1,b=2) ; x[3]<-10; x }", "structure(c(1, 2, 10), .Names = c(\"a\", \"b\", \"\"))");
  }
  @Test
  public void micro2395() {
    assertIdentical("{ x <- matrix(1:2) ; x[c(FALSE,FALSE,TRUE)]<-10; x }", "c(1, 2, 10)");
  }
  @Test
  public void micro2396() {
    assertIdentical("{ x <- 1:2 ; x[c(FALSE,FALSE,TRUE)]<-10; x }", "c(1, 2, 10)");
  }
  @Test
  public void micro2397() {
    assertIdentical("{ x <- c(a=1,b=2) ; x[c(FALSE,FALSE,TRUE)]<-10; x }", "structure(c(1, 2, 10), .Names = c(\"a\", \"b\", \"\"))");
  }
  @Test
  public void micro2398() {
    assertIdentical("{ x<-c(a=1,b=2,c=3) ; x[[\"b\"]]<-200; x }", "structure(c(1, 200, 3), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2399() {
    assertIdentical("{ x<-c(a=1,b=2,c=3) ; x[[\"d\"]]<-200; x }", "structure(c(1, 2, 3, 200), .Names = c(\"a\", \"b\", \"c\", \"d\"))");
  }
  @Test
  public void micro2400() {
    assertIdentical("{ x<-c() ; x[c(\"a\",\"b\",\"c\",\"d\")]<-c(1,2); x }", "structure(c(1, 2, 1, 2), .Names = c(\"a\", \"b\", \"c\", \"d\"))");
  }
  @Test
  public void micro2401() {
    assertIdentical("{ x<-c(a=1,b=2,c=3) ; x[\"d\"]<-4 ; x }", "structure(c(1, 2, 3, 4), .Names = c(\"a\", \"b\", \"c\", \"d\"))");
  }
  @Test
  public void micro2402() {
    assertIdentical("{ x<-c(a=1,b=2,c=3) ; x[c(\"d\",\"e\")]<-c(4,5) ; x }", "structure(c(1, 2, 3, 4, 5), .Names = c(\"a\", \"b\", \"c\", \"d\", \"e\"))");
  }
  @Test
  public void micro2403() {
    assertIdentical("{ x<-c(a=1,b=2,c=3) ; x[c(\"d\",\"a\",\"d\",\"a\")]<-c(4,5) ; x }", "structure(c(5, 2, 3, 4), .Names = c(\"a\", \"b\", \"c\", \"d\"))");
  }
  @Test
  public void micro2404() {
    assertIdentical("{ a = c(1, 2); a[['a']] = 67; a; }", "structure(c(1, 2, 67), .Names = c(\"\", \"\", \"a\"))");
  }
  @Test
  public void micro2405() {
    assertIdentical("{ a = c(a=1,2,3); a[['x']] = 67; a; }", "structure(c(1, 2, 3, 67), .Names = c(\"a\", \"\", \"\", \"x\"))");
  }
  @Test
  public void micro2406() {
    assertIdentical("{ x <- c(TRUE,TRUE,TRUE,TRUE); x[2:3] <- c(FALSE,FALSE); x }", "c(TRUE, FALSE, FALSE, TRUE)");
  }
  @Test
  public void micro2407() {
    assertIdentical("{ x <- c(TRUE,TRUE,TRUE,TRUE); x[3:2] <- c(FALSE,TRUE); x }", "c(TRUE, TRUE, FALSE, TRUE)");
  }
  @Test
  public void micro2408() {
    assertIdentical("{ x <- c('a','b','c','d'); x[2:3] <- 'x'; x}", "c(\"a\", \"x\", \"x\", \"d\")");
  }
  @Test
  public void micro2409() {
    assertIdentical("{ x <- c('a','b','c','d'); x[2:3] <- c('x','y'); x}", "c(\"a\", \"x\", \"y\", \"d\")");
  }
  @Test
  public void micro2410() {
    assertIdentical("{ x <- c('a','b','c','d'); x[3:2] <- c('x','y'); x}", "c(\"a\", \"y\", \"x\", \"d\")");
  }
  @Test
  public void micro2411() {
    assertIdentical("{ x <- c('a','b','c','d'); x[c(TRUE,FALSE,TRUE)] <- c('x','y','z'); x }", "c(\"x\", \"b\", \"y\", \"z\")");
  }
  @Test
  public void micro2412() {
    assertIdentical("{ x <- c(TRUE,TRUE,TRUE,TRUE); x[c(TRUE,TRUE,FALSE)] <- c(10L,20L,30L); x }", "c(10L, 20L, 1L, 30L)");
  }
  @Test
  public void micro2413() {
    assertIdentical("{ x <- c(1L,1L,1L,1L); x[c(TRUE,TRUE,FALSE)] <- c('a','b','c'); x}", "c(\"a\", \"b\", \"1\", \"c\")");
  }
  @Test
  public void micro2414() {
    assertIdentical("{ x <- c(TRUE,TRUE,TRUE,TRUE); x[c(TRUE,TRUE,FALSE)] <- list(10L,20L,30L); x }", "list(10L, 20L, TRUE, 30L)");
  }
  @Test
  public void micro2415() {
    assertIdentical("{ x <- c(); x[c('a','b')] <- c(1L,2L); x }", "structure(1:2, .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2416() {
    assertIdentical("{ x <- c(); x[c('a','b')] <- c(TRUE,FALSE); x }", "structure(c(TRUE, FALSE), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2417() {
    assertIdentical("{ x <- c(); x[c('a','b')] <- c('a','b'); x }", "structure(c(\"a\", \"b\"), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2418() {
    assertIdentical("{ x <- list(); x[c('a','b')] <- c('a','b'); x }", "structure(list(a = \"a\", b = \"b\"), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2419() {
    assertIdentical("{ x <- list(); x[c('a','b')] <- list('a','b'); x }", "structure(list(a = \"a\", b = \"b\"), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2420() {
    assertIdentical("{ x = c(1,2,3,4); x[x %% 2 == 0] <- c(1,2,3,4); }", "c(1, 2, 3, 4)");
  }
  @Test
  public void micro2421() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, 10) ; f(1:2, 1:2, 11) }", "c(11, 11)");
  }
  @Test
  public void micro2422() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, TRUE) }", "list(TRUE, TRUE)");
  }
  @Test
  public void micro2423() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, 11L) }", "list(11L, 11L)");
  }
  @Test
  public void micro2424() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, TRUE) ;  f(list(1,2), 1:2, as.raw(10))}", "list(as.raw(0x0a), as.raw(0x0a))");
  }
  @Test
  public void micro2425() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(list(1,2), 1:2, c(1+2i,3+4i))}", "list(1+2i, 3+4i)");
  }
  @Test
  public void micro2426() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(1:2, 1:2, c(10,5))}", "c(10, 5)");
  }
  @Test
  public void micro2427() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(list(1,2), 1:3, c(2,10,5)) }", "list(2, 10, 5)");
  }
  @Test
  public void micro2428() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2,3,4,5), 4:3, c(TRUE,NA)) }", "list(1, 2, NA, TRUE, 5)");
  }
  @Test
  public void micro2429() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2,3,4), seq(1L,4L,2L), c(TRUE,NA)) }", "list(TRUE, 2, NA, 4)");
  }
  @Test
  public void micro2430() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2),1:2,3:4) }", "list(3L, 4L)");
  }
  @Test
  public void micro2431() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2),1:2,c(4,3)) }", "list(4, 3)");
  }
  @Test
  public void micro2432() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2),1:2,c(1+2i,3+2i)) }", "list(1+2i, 3+2i)");
  }
  @Test
  public void micro2433() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,1+2i) }", "c(1+2i, 1+2i, 10+0i)");
  }
  @Test
  public void micro2434() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(3,NA)) }", "c(3, NA, 10)");
  }
  @Test
  public void micro2435() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(3L,NA)) }", "c(3, NA, 10)");
  }
  @Test
  public void micro2436() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(TRUE,FALSE)) }", "c(1, 0, 10)");
  }
  @Test
  public void micro2437() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(TRUE,FALSE)) ; f(c(10L,4L), 2:1, 1+2i) }", "c(1+2i, 1+2i)");
  }
  @Test
  public void micro2438() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),-1:0,c(TRUE,FALSE)) }", "c(1, 1, 0)");
  }
  @Test
  public void micro2439() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10), seq(2L,4L,2L) ,c(TRUE,FALSE)) }", "c(1, 1, 10, 0)");
  }
  @Test
  public void micro2440() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.double(1:5), seq(1L,6L,2L) ,c(TRUE,FALSE,NA)) }", "c(1, 2, 0, 4, NA)");
  }
  @Test
  public void micro2441() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.double(1:5), seq(7L,1L,-3L) ,c(TRUE,FALSE,NA)) }", "c(NA, 2, 3, 0, 5, NA, 1)");
  }
  @Test
  public void micro2442() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),2:1,1+2i) }", "c(1+2i, 1+2i, 10+0i)");
  }
  @Test
  public void micro2443() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),2:1,c(3,NA)) }", "c(NA, 3, 10)");
  }
  @Test
  public void micro2444() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),2:1,c(3L,NA)) }", "c(NA, 3L, 10L)");
  }
  @Test
  public void micro2445() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),1:2,c(TRUE,FALSE)) }", "c(1L, 0L, 10L)");
  }
  @Test
  public void micro2446() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),1:2,c(TRUE,FALSE)) ; f(c(10,4), 2:1, 1+2i) }", "c(1+2i, 1+2i)");
  }
  @Test
  public void micro2447() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:5, seq(1L,6L,2L) ,c(TRUE,FALSE,NA)) }", "c(1L, 2L, 0L, 4L, NA)");
  }
  @Test
  public void micro2448() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2, seq(1L,6L,2L) ,c(TRUE,FALSE,NA)) }", "c(1L, 2L, 0L, NA, NA)");
  }
  @Test
  public void micro2449() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,FALSE,NA),2:1,1+2i) }", "c(1+2i, 1+2i, complex(real=NA, i=NA))");
  }
  @Test
  public void micro2450() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,NA,FALSE),2:1,c(TRUE,NA)) }", "c(NA, TRUE, FALSE)");
  }
  @Test
  public void micro2451() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,NA,FALSE),2:0,c(TRUE,NA)) }", "c(NA, TRUE, FALSE)");
  }
  @Test
  public void micro2452() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,NA,FALSE),3:4,c(TRUE,NA)) }", "c(TRUE, NA, TRUE, NA)");
  }
  @Test
  public void micro2453() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.logical(-3:3),seq(1L,7L,3L),c(TRUE,NA,FALSE)) }", "c(TRUE, TRUE, TRUE, NA, TRUE, TRUE, FALSE)");
  }
  @Test
  public void micro2454() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,FALSE),2:1,c(NA,NA)) ; f(c(TRUE,FALSE),1:2,3:4) }", "3:4");
  }
  @Test
  public void micro2455() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,FALSE),2:1,c(NA,NA)) ; f(10:11,1:2,c(NA,FALSE)) }", "c(NA, 0L)");
  }
  @Test
  public void micro2456() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"a\",\"b\"),2:1,1+2i) }", "c(\"1+2i\", \"1+2i\")");
  }
  @Test
  public void micro2457() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.character(-3:3),seq(1L,7L,3L),c(\"A\",\"a\",\"XX\")) }", "c(\"A\", \"-2\", \"-1\", \"a\", \"1\", \"2\", \"XX\")");
  }
  @Test
  public void micro2458() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"hello\",\"hi\",\"X\"), -1:-2, \"ZZ\") }", "c(\"hello\", \"hi\", \"ZZ\")");
  }
  @Test
  public void micro2459() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"hello\",\"hi\",\"X\"), 3:4, \"ZZ\") }", "c(\"hello\", \"hi\", \"ZZ\", \"ZZ\")");
  }
  @Test
  public void micro2460() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"hello\",\"hi\",\"X\"), 1:2, c(\"ZZ\",\"xx\")) ; f(1:4,1:2,NA) }", "c(NA, NA, 3L, 4L)");
  }
  @Test
  public void micro2461() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"hello\",\"hi\",\"X\"), 1:2, c(\"ZZ\",\"xx\")) ; f(as.character(1:2),1:2,NA) }", "c(NA_character_, NA_character_)");
  }
  @Test
  public void micro2462() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1+2i,2+3i), 1:2, c(10+1i,2+4i)) }", "c(10+1i, 2+4i)");
  }
  @Test
  public void micro2463() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.raw(1:3), 1:2, as.raw(40:41)) }", "as.raw(c(0x28, 0x29, 0x03))");
  }
  @Test
  public void micro2464() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(1:2, c(0,0), c(1+2i,3+4i))}", "c(1+0i, 2+0i)");
  }
  @Test
  public void micro2465() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1:2, 3:4); f(c(TRUE,FALSE), 2:1, 1:2) }", "c(2L, 1L)");
  }
  @Test
  public void micro2466() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1:2, 3:4); f(3:4, 2:1, c(NA,FALSE)) }", "c(0L, NA)");
  }
  @Test
  public void micro2467() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(TRUE,FALSE,NA), 1:2, c(FALSE,TRUE)) }", "c(FALSE, TRUE, NA)");
  }
  @Test
  public void micro2468() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4), 1:2, c(NA,NA)) }", "c(NA_real_, NA_real_)");
  }
  @Test
  public void micro2469() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4), 1:2, c(\"hello\",\"hi\")) }", "c(\"hello\", \"hi\")");
  }
  @Test
  public void micro2470() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4,8), 1:2, list(3,TRUE)) }", "list(3, TRUE, 8)");
  }
  @Test
  public void micro2472() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); l <- list(3,5L) ; dim(l) <- c(2,1) ; f(5:6,1:2,c(3,4)) ; f(list(3,TRUE), 1:2, l) }", "list(3, 5L)");
  }
  @Test
  public void micro2475() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4,8), -1:-2, 10) }", "c(3, 4, 10)");
  }
  @Test
  public void micro2476() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4,8), 3:4, 10) }", "c(3, 4, 10, 10)");
  }
  @Test
  public void micro2477() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(1:8, seq(1L,7L,3L), c(10,100,1000)) }", "c(10, 2, 3, 100, 5, 6, 1000, 8)");
  }
  @Test
  public void micro2478() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; z <- f(1:8, seq(1L,7L,3L), list(10,100,1000)) ; sum(as.double(z)) }", "1134");
  }
  @Test
  public void micro2480() {
    assertIdentical("{ b <- 1:3 ; b[c(3,2)] <- list(TRUE,10) ; b }", "list(1L, 10, TRUE)");
  }
  @Test
  public void micro2481() {
    assertIdentical("{ b <- as.raw(11:13) ; b[c(3,2)] <- list(2) ; b }", "list(as.raw(0x0b), 2, 2)");
  }
  @Test
  public void micro2482() {
    assertIdentical("{ b <- as.raw(11:13) ; b[c(3,2)] <- as.raw(2) ; b }", "as.raw(c(0x0b, 0x02, 0x02))");
  }
  @Test
  public void micro2483() {
    assertIdentical("{ b <- c(TRUE,NA,FALSE) ; b[c(3,2)] <- FALSE ; b }", "c(TRUE, FALSE, FALSE)");
  }
  @Test
  public void micro2484() {
    assertIdentical("{ b <- 1:4 ; b[c(3,2)] <- c(NA,NA) ; b }", "c(1L, NA, NA, 4L)");
  }
  @Test
  public void micro2485() {
    assertIdentical("{ b <- c(TRUE,FALSE) ; b[c(3,2)] <- 5:6 ; b }", "c(1L, 6L, 5L)");
  }
  @Test
  public void micro2486() {
    assertIdentical("{ b <- c(1+2i,3+4i) ; b[c(3,2)] <- 5:6 ; b }", "c(1+2i, 6+0i, 5+0i)");
  }
  @Test
  public void micro2487() {
    assertIdentical("{ b <- 3:4 ; b[c(3,2)] <- c(1+2i,3+4i) ; b }", "c(3+0i, 3+4i, 1+2i)");
  }
  @Test
  public void micro2488() {
    assertIdentical("{ b <- c(\"hello\",\"hi\") ; b[c(3,2)] <- c(2,3) ; b }", "c(\"hello\", \"3\", \"2\")");
  }
  @Test
  public void micro2489() {
    assertIdentical("{ b <- 3:4 ; b[c(3,2)] <- c(\"X\",\"xx\") ; b }", "c(\"3\", \"xx\", \"X\")");
  }
  @Test
  public void micro2490() {
    assertIdentical("{ b <- 3:4 ; b[c(0,1)] <- c(2,10,11) ; b }", "c(2, 4)");
  }
  @Test
  public void micro2492() {
    assertIdentical("{ b <- c(1,4,5) ; x <- c(2,8,2) ; b[x==2] <- c(10,11) ; b }", "c(10, 4, 11)");
  }
  @Test
  public void micro2493() {
    assertIdentical("{ b <- c(1,4,5) ; z <- b ; x <- c(2,8,2) ; b[x==2] <- c(10,11) ; b }", "c(10, 4, 11)");
  }
  @Test
  public void micro2494() {
    assertIdentical("{ b <- c(1,4,5) ;  x <- c(2,2) ; b[x==2] <- c(10,11) ; b }", "c(10, 11, 10)");
  }
  @Test
  public void micro2495() {
    assertIdentical("{ b <- c(1,2,5) ;  x <- as.double(NA) ; attr(x,\"my\") <- 2 ; b[c(1,NA,2)==2] <- x ; b }", "c(1, 2, NA)");
  }
  @Test
  public void micro2496() {
    assertIdentical("{ b <- c(1,2,5) ;  x <- c(2,2,-1) ; b[x==2] <- c(10,11,5) ; b }", "c(10, 11, 5)");
  }
  @Test
  public void micro2497() {
    assertIdentical("{ b <- c(1,2,5) ; b[integer()] <- NULL ; b }", "c(1, 2, 5)");
  }
  @Test
  public void micro2503() {
    assertIdentical("{ b <- list(1,2,5) ; b[c(1,NA)] <- NULL ; b }", "list(2, 5)");
  }
  @Test
  public void micro2510() {
    assertIdentical("{ b <- c(1,2,5) ; b[logical()] <- NULL ; b }", "c(1, 2, 5)");
  }
  @Test
  public void micro2511() {
    assertIdentical("{ b <- c(1,2,5) ; b[c(TRUE,FALSE,TRUE)] <- list(TRUE,1+2i) ; b }", "list(TRUE, 2, 1+2i)");
  }
  @Test
  public void micro2515() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(\"a\",\"XX\",\"b\"), c(FALSE,TRUE,TRUE), 21:22) }", "c(\"a\", \"21\", \"22\")");
  }
  @Test
  public void micro2516() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(10,12,3), c(FALSE,TRUE,TRUE), c(\"hi\",NA)) }", "c(\"10\", \"hi\", NA)");
  }
  @Test
  public void micro2517() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(10,12,3), c(FALSE,TRUE,TRUE), c(1+2i,10)) }", "c(10+0i, 1+2i, 10+0i)");
  }
  @Test
  public void micro2518() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(3+4i,5+6i), c(FALSE,TRUE,TRUE), c(\"hi\",NA)) }", "c(\"3+4i\", \"hi\", NA)");
  }
  @Test
  public void micro2519() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(3+4i,5+6i), c(FALSE,TRUE,TRUE), c(NA,1+10i)) }", "c(3+4i, complex(real=NA, i=NA), 1+10i)");
  }
  @Test
  public void micro2520() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(TRUE,FALSE), c(FALSE,TRUE,TRUE), c(NA,2L)) }", "c(1L, NA, 2L)");
  }
  @Test
  public void micro2521() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), c(NA,FALSE)) }", "c(3L, NA, 0L)");
  }
  @Test
  public void micro2522() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), 4:6) }", "3:5");
  }
  @Test
  public void micro2523() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(TRUE,TRUE,FALSE), c(FALSE,TRUE,TRUE), c(TRUE,NA)) }", "c(TRUE, TRUE, NA)");
  }
  @Test
  public void micro2524() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), c(NA,FALSE)) }", "c(3L, NA, 0L)");
  }
  @Test
  public void micro2525() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), 4:6) }", "3:5");
  }
  @Test
  public void micro2526() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,NA), 4) }", "c(3, 4, 5)");
  }
  @Test
  public void micro2527() {
    assertIdentical("{ b <- as.list(3:6) ; dim(b) <- c(4,1) ; b[c(TRUE,FALSE)] <- NULL ; b }", "list(4L, 6L)");
  }
  @Test
  public void micro2528() {
    assertIdentical("{ b <- as.list(3:6) ; names(b) <- c(\"X\",\"Y\",\"Z\",\"Q\") ; b[c(TRUE,FALSE)] <- NULL ; b }", "structure(list(Y = 4L, Q = 6L), .Names = c(\"Y\", \"Q\"))");
  }
  @Test
  public void micro2529() {
    assertIdentical("{ b <- as.list(3:6) ; names(b) <- c(\"X\",\"Y\",\"Z\",\"Q\") ; b[c(FALSE,FALSE)] <- NULL ; b }", "structure(list(X = 3L, Y = 4L, Z = 5L, Q = 6L), .Names = c(\"X\", \"Y\", \"Z\", \"Q\"))");
  }
  @Test
  public void micro2531() {
    assertIdentical("{ b <- as.list(3:6) ; dim(b) <- c(1,4) ; b[c(FALSE,FALSE,TRUE)] <- NULL ; b }", "list(3L, 4L, 6L)");
  }
  @Test
  public void micro2533() {
    assertIdentical("{ b <- as.list(3:5) ; dim(b) <- c(1,3) ; b[c(FALSE,TRUE,NA)] <- NULL ; b }", "list(3L, 5L)");
  }
  @Test
  public void micro2534() {
    assertIdentical("{ b <- 1:3 ; b[integer()] <- 3:5 ; b }", "1:3");
  }
  @Test
  public void micro2535() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) }", "list(1+2i, 2)");
  }
  @Test
  public void micro2536() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(1:2, c(TRUE,FALSE), list(TRUE)) }", "list(TRUE, 2L)");
  }
  @Test
  public void micro2537() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), TRUE) }", "list(TRUE, 2L)");
  }
  @Test
  public void micro2538() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), 1+2i) }", "list(1+2i, 2L)");
  }
  @Test
  public void micro2539() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), 10) }", "list(10, 2L)");
  }
  @Test
  public void micro2540() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), 10L) }", "list(10L, 2L)");
  }
  @Test
  public void micro2541() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,NA), list(1+2i)) }", "list(1+2i, 2)");
  }
  @Test
  public void micro2542() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,NA), 10) }", "list(10, 2)");
  }
  @Test
  public void micro2544() {
    assertIdentical("{ x <- list(1,0) ; x[as.logical(x)] <- c(10,11); x }", "list(10, 0)");
  }
  @Test
  public void micro2545() {
    assertIdentical("{ x <- list(1,0) ; x[is.na(x)] <- c(10,11); x }", "list(1, 0)");
  }
  @Test
  public void micro2546() {
    assertIdentical("{ x <- list(1,0) ; x[c(TRUE,FALSE)] <- x[2:1] ; x }", "list(0, 0)");
  }
  @Test
  public void micro2548() {
    assertIdentical("{ x <- list(1,0) ; x[is.na(x)] <- c(10L,11L); x }", "list(1, 0)");
  }
  @Test
  public void micro2549() {
    assertIdentical("{ x <- list(1,0) ; x[c(TRUE,TRUE)] <- c(TRUE,NA); x }", "list(TRUE, NA)");
  }
  @Test
  public void micro2550() {
    assertIdentical("{ x <- list(1,0) ; x[logical()] <- c(TRUE,NA); x }", "list(1, 0)");
  }
  @Test
  public void micro2551() {
    assertIdentical("{ x <- c(1,0) ; x[c(TRUE,TRUE)] <- c(TRUE,NA); x }", "c(1, NA)");
  }
  @Test
  public void micro2552() {
    assertIdentical("{ x <- c(1,0) ; x[c(TRUE,TRUE)] <- 3:4; x }", "c(3, 4)");
  }
  @Test
  public void micro2553() {
    assertIdentical("{ x <- c(1,0) ; x[logical()] <- 3:4; x }", "c(1, 0)");
  }
  @Test
  public void micro2555() {
    assertIdentical("{ x <- c(1,0) ; z <- x ; x[c(NA,TRUE)] <- TRUE; x }", "c(1, 1)");
  }
  @Test
  public void micro2556() {
    assertIdentical("{ x <- c(1,0)  ; x[is.na(x)] <- TRUE; x }", "c(1, 0)");
  }
  @Test
  public void micro2557() {
    assertIdentical("{ x <- c(1,0)  ; x[c(TRUE,TRUE)] <- rev(x) ; x }", "c(0, 1)");
  }
  @Test
  public void micro2558() {
    assertIdentical("{ x <- c(1,0) ; f <- function(v) { x[c(TRUE,TRUE)] <- v ; x } ; f(1:2) ; f(c(1,2)) }", "c(1, 2)");
  }
  @Test
  public void micro2559() {
    assertIdentical("{ x <- c(1,0) ; f <- function(v) { x[c(TRUE,TRUE)] <- v ; x } ; f(1:2) ; f(1+2i) }", "c(1+2i, 1+2i)");
  }
  @Test
  public void micro2562() {
    assertIdentical("{ x <- 1:2 ; x[c(TRUE,FALSE,FALSE,TRUE)] <- 3:4 ; x }", "c(3L, 2L, NA, 4L)");
  }
  @Test
  public void micro2563() {
    assertIdentical("{ x <- 1:2 ; x[c(TRUE,FALSE,FALSE,NA)] <- 3L ; x }", "c(3L, 2L, NA, NA)");
  }
  @Test
  public void micro2564() {
    assertIdentical("{ x <- 1:2 ; x[c(TRUE,NA)] <- 3L ; x }", "c(3L, 2L)");
  }
  @Test
  public void micro2565() {
    assertIdentical("{ x <- c(1L,2L) ; x[c(TRUE,FALSE)] <- 3L ; x }", "c(3L, 2L)");
  }
  @Test
  public void micro2566() {
    assertIdentical("{ x <- c(1L,2L) ; x[c(TRUE,NA)] <- 3L ; x }", "c(3L, 2L)");
  }
  @Test
  public void micro2567() {
    assertIdentical("{ x <- c(1L,2L) ; x[TRUE] <- 3L ; x }", "c(3L, 3L)");
  }
  @Test
  public void micro2568() {
    assertIdentical("{ x <- c(1L,2L,3L,4L) ; x[c(TRUE,FALSE)] <- 5:6 ; x }", "c(5L, 2L, 6L, 4L)");
  }
  @Test
  public void micro2570() {
    assertIdentical("{ x <- c(1L,2L,3L,4L) ;  x[is.na(x)] <- 5:6 ; x }", "1:4");
  }
  @Test
  public void micro2571() {
    assertIdentical("{ x <- c(1L,2L,3L,4L) ; x[c(TRUE,FALSE)] <- rev(x) ; x }", "c(4L, 2L, 3L, 4L)");
  }
  @Test
  public void micro2572() {
    assertIdentical("{ x <- c(1L,2L) ; x[logical()] <- 3L ; x }", "1:2");
  }
  @Test
  public void micro2573() {
    assertIdentical("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE)] <- c(FALSE,NA) ; b }", "c(FALSE, NA, NA, TRUE)");
  }
  @Test
  public void micro2574() {
    assertIdentical("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,FALSE)] <- c(FALSE,NA) ; b }", "c(FALSE, NA, FALSE, NA)");
  }
  @Test
  public void micro2575() {
    assertIdentical("{ b <- c(TRUE,NA,FALSE) ; b[c(TRUE,TRUE)] <- c(FALSE,NA) ; b }", "c(FALSE, NA, FALSE)");
  }
  @Test
  public void micro2576() {
    assertIdentical("{ b <- c(TRUE,NA,FALSE) ; b[c(TRUE,FALSE,TRUE,TRUE)] <- c(FALSE,NA,NA) ; b }", "c(FALSE, NA, NA, NA)");
  }
  @Test
  public void micro2577() {
    assertIdentical("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,TRUE,NA)] <- FALSE ; b }", "c(FALSE, NA, FALSE, TRUE)");
  }
  @Test
  public void micro2578() {
    assertIdentical("{ b <- c(TRUE,NA,FALSE,TRUE) ; z <- b ; b[c(TRUE,FALSE,TRUE,NA)] <- FALSE ; b }", "c(FALSE, NA, FALSE, TRUE)");
  }
  @Test
  public void micro2580() {
    assertIdentical("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,TRUE,FALSE)] <- b ; b }", "c(TRUE, NA, NA, TRUE)");
  }
  @Test
  public void micro2581() {
    assertIdentical("{ b <- c(TRUE,FALSE,FALSE,TRUE) ; b[b] <- c(TRUE,FALSE) ; b }", "c(TRUE, FALSE, FALSE, FALSE)");
  }
  @Test
  public void micro2582() {
    assertIdentical("{ f <- function(b,i,v) { b[b] <- b ; b } ; f(c(TRUE,FALSE,FALSE,TRUE)) ; f(1:3) }", "1:3");
  }
  @Test
  public void micro2583() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(TRUE,FALSE,FALSE,TRUE),c(TRUE,FALSE), NA) ; f(1:4, c(TRUE,TRUE), NA) }", "c(NA_integer_, NA_integer_, NA_integer_, NA_integer_)");
  }
  @Test
  public void micro2584() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(TRUE,FALSE,FALSE,TRUE),c(TRUE,FALSE), NA) ; f(c(FALSE,FALSE,TRUE), c(TRUE,TRUE), c(1,2,3)) }", "c(1, 2, 3)");
  }
  @Test
  public void micro2585() {
    assertIdentical("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[logical()] <- c(FALSE,NA) ; b }", "c(TRUE, NA, FALSE, TRUE)");
  }
  @Test
  public void micro2586() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE)] <- \"X\" ; b }", "c(\"X\", \"b\", \"X\")");
  }
  @Test
  public void micro2587() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE,TRUE,TRUE)] <- \"X\" ; b }", "c(\"X\", \"b\", \"X\", \"X\")");
  }
  @Test
  public void micro2588() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE,TRUE,NA)] <- \"X\" ; b }", "c(\"X\", \"b\", \"X\", NA)");
  }
  @Test
  public void micro2589() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE,NA)] <- \"X\" ; b }", "c(\"X\", \"b\", \"c\")");
  }
  @Test
  public void micro2590() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[logical()] <- \"X\" ; b }", "c(\"a\", \"b\", \"c\")");
  }
  @Test
  public void micro2592() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[logical()] <- \"X\" ; b }", "c(\"a\", \"b\", \"c\")");
  }
  @Test
  public void micro2593() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[c(FALSE,TRUE,TRUE)] <- c(\"X\",\"y\",\"z\") ; b }", "c(\"a\", \"X\", \"y\")");
  }
  @Test
  public void micro2594() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; x <- b ; b[c(FALSE,TRUE,TRUE)] <- c(\"X\",\"z\") ; b }", "c(\"a\", \"X\", \"z\")");
  }
  @Test
  public void micro2595() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[is.na(b)] <- c(\"X\",\"z\") ; b }", "c(\"a\", \"b\", \"c\")");
  }
  @Test
  public void micro2597() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,TRUE,TRUE)] <- rev(as.character(b)) ; b }", "c(\"c\", \"b\", \"a\")");
  }
  @Test
  public void micro2598() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(\"a\",\"b\",\"c\"),c(TRUE,FALSE),c(\"A\",\"X\")) ; f(1:3,c(TRUE,FALSE),4) }", "c(4, 2, 4)");
  }
  @Test
  public void micro2599() {
    assertIdentical("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(\"a\",\"b\",\"c\"),c(TRUE,FALSE),c(\"A\",\"X\")) ; f(c(\"A\",\"X\"),c(TRUE,FALSE),4) }", "c(\"4\", \"X\")");
  }
  @Test
  public void micro2600() {
    assertIdentical("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE,TRUE)] <- c(1+2i,3+4i) ; b }", "c(\"1+2i\", \"b\", \"3+4i\")");
  }
  @Test
  public void micro2601() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,c(2),10) ; f(1:2, -1, 10) }", "c(1, 10)");
  }
  @Test
  public void micro2602() {
    assertIdentical("{ x <- c(); f <- function(i, v) { x[i] <- v ; x } ; f(1:2,3:4); f(c(1,2),c(TRUE,FALSE)) }", "c(TRUE, FALSE)");
  }
  @Test
  public void micro2603() {
    assertIdentical("{ x <- c(); f <- function(i, v) { x[i] <- v ; x } ; f(1:2,3:4); f(c(\"a\",\"b\"),c(TRUE,FALSE)) }", "structure(c(TRUE, FALSE), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2604() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), 2, NULL) }", "list(1, 3)");
  }
  @Test
  public void micro2605() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), 3L, NULL) }", "list(1, 2)");
  }
  @Test
  public void micro2606() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), 3:2, NULL) }", "list(1)");
  }
  @Test
  public void micro2607() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), c(2,3), NULL) }", "list(1)");
  }
  @Test
  public void micro2608() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), NULL, NULL) }", "list(1, 2, 3)");
  }
  @Test
  public void micro2609() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), c(TRUE,TRUE,FALSE), NULL) }", "list(3)");
  }
  @Test
  public void micro2610() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; l <- list(1,2,3) ; dim(l) <- c(1,3) ; z <- f(l, c(TRUE,TRUE,FALSE), NULL) ; z }", "list(3)");
  }
  @Test
  public void micro2612() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), 3:1, 10) }", "list(10, 10, 10)");
  }
  @Test
  public void micro2613() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), c(3,3,2), 10) }", "list(1, 10, 10)");
  }
  @Test
  public void micro2614() {
    assertIdentical("{ list(1:4) }", "list(1:4)");
  }
  @Test
  public void micro2615() {
    assertIdentical("{ list(1,list(2,list(3,4))) }", "list(1, list(2, list(3, 4)))");
  }
  @Test
  public void micro2616() {
    assertIdentical("{ list(1,b=list(2,3)) }", "structure(list(1, b = list(2, 3)), .Names = c(\"\", \"b\"))");
  }
  @Test
  public void micro2617() {
    assertIdentical("{ list(1,b=list(c=2,3)) }", "structure(list(1, b = structure(list(c = 2, 3), .Names = c(\"c\", \"\"))), .Names = c(\"\", \"b\"))");
  }
  @Test
  public void micro2618() {
    assertIdentical("{ list(list(c=2)) }", "list(structure(list(c = 2), .Names = \"c\"))");
  }
  @Test
  public void micro2619() {
    assertIdentical("{ l<-list(1,2L,TRUE) ; l[[2]] }", "2L");
  }
  @Test
  public void micro2620() {
    assertIdentical("{ l<-list(1,2L,TRUE) ; l[c(FALSE,FALSE,TRUE)] }", "list(TRUE)");
  }
  @Test
  public void micro2621() {
    assertIdentical("{ l<-list(1,2L,TRUE) ; l[FALSE] }", "list()");
  }
  @Test
  public void micro2622() {
    assertIdentical("{ l<-list(1,2L,TRUE) ; l[-2] }", "list(1, TRUE)");
  }
  @Test
  public void micro2623() {
    assertIdentical("{ l<-list(1,2L,TRUE) ; l[NA] }", "list(NULL, NULL, NULL)");
  }
  @Test
  public void micro2624() {
    assertIdentical("{ l<-list(1,2,3) ; l[c(1,2)] }", "list(1, 2)");
  }
  @Test
  public void micro2625() {
    assertIdentical("{ l<-list(1,2,3) ; l[c(2)] }", "list(2)");
  }
  @Test
  public void micro2626() {
    assertIdentical("{ x<-list(1,2L,TRUE,FALSE,5) ; x[2:4] }", "list(2L, TRUE, FALSE)");
  }
  @Test
  public void micro2627() {
    assertIdentical("{ x<-list(1,2L,TRUE,FALSE,5) ; x[4:2] }", "list(FALSE, TRUE, 2L)");
  }
  @Test
  public void micro2628() {
    assertIdentical("{ x<-list(1,2L,TRUE,FALSE,5) ; x[c(-2,-3)] }", "list(1, FALSE, 5)");
  }
  @Test
  public void micro2629() {
    assertIdentical("{ x<-list(1,2L,TRUE,FALSE,5) ; x[c(-2,-3,-4,0,0,0)] }", "list(1, 5)");
  }
  @Test
  public void micro2630() {
    assertIdentical("{ x<-list(1,2L,TRUE,FALSE,5) ; x[c(2,5,4,3,3,3,0)] }", "list(2L, 5, FALSE, TRUE, TRUE, TRUE)");
  }
  @Test
  public void micro2631() {
    assertIdentical("{ x<-list(1,2L,TRUE,FALSE,5) ; x[c(2L,5L,4L,3L,3L,3L,0L)] }", "list(2L, 5, FALSE, TRUE, TRUE, TRUE)");
  }
  @Test
  public void micro2632() {
    assertIdentical("{ m<-list(1,2) ; m[NULL] }", "list()");
  }
  @Test
  public void micro2633() {
    assertIdentical("{ f<-function(x, i) { x[i] } ; f(list(1,2,3),3:1) ; f(list(1L,2L,3L,4L,5L),c(0,0,0,0-2)) }", "list(1L, 3L, 4L, 5L)");
  }
  @Test
  public void micro2634() {
    assertIdentical("{ x<-list(1,2,3,4,5) ; x[c(TRUE,TRUE,TRUE,FALSE,FALSE,FALSE,FALSE,TRUE,NA)] }", "list(1, 2, 3, NULL, NULL)");
  }
  @Test
  public void micro2635() {
    assertIdentical("{ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(1) ; f(1L) ; f(TRUE) }", "list(1, 2, 3, 4, 5)");
  }
  @Test
  public void micro2636() {
    assertIdentical("{ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(1) ; f(TRUE) ; f(1L)  }", "list(1)");
  }
  @Test
  public void micro2637() {
    assertIdentical("{ f<-function(i) { x<-list(1L,2L,3L,4L,5L) ; x[i] } ; f(1) ; f(TRUE) ; f(c(3,2))  }", "list(3L, 2L)");
  }
  @Test
  public void micro2638() {
    assertIdentical("{ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(1)  ; f(3:4) }", "list(3, 4)");
  }
  @Test
  public void micro2639() {
    assertIdentical("{ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(c(TRUE,FALSE))  ; f(3:4) }", "list(3, 4)");
  }
  @Test
  public void micro2640() {
    assertIdentical("{ l<-(list(list(1,2),list(3,4))); l[[c(1,2)]] }", "2");
  }
  @Test
  public void micro2641() {
    assertIdentical("{ l<-(list(list(1,2),list(3,4))); l[[c(1,-2)]] }", "1");
  }
  @Test
  public void micro2642() {
    assertIdentical("{ l<-(list(list(1,2),list(3,4))); l[[c(1,-1)]] }", "2");
  }
  @Test
  public void micro2643() {
    assertIdentical("{ l<-(list(list(1,2),list(3,4))); l[[c(1,TRUE)]] }", "1");
  }
  @Test
  public void micro2644() {
    assertIdentical("{ l<-(list(list(1,2),c(3,4))); l[[c(2,1)]] }", "3");
  }
  @Test
  public void micro2645() {
    assertIdentical("{ l <- list(a=1,b=2,c=list(d=3,e=list(f=4))) ; l[[c(3,2)]] }", "structure(list(f = 4), .Names = \"f\")");
  }
  @Test
  public void micro2646() {
    assertIdentical("{ l <- list(a=1,b=2,c=list(d=3,e=list(f=4))) ; l[[c(3,1)]] }", "3");
  }
  @Test
  public void micro2647() {
    assertIdentical("{ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c(\"c\",\"e\")]] }", "structure(4, .Names = \"f\")");
  }
  @Test
  public void micro2648() {
    assertIdentical("{ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c(\"c\",\"e\", \"f\")]] }", "4");
  }
  @Test
  public void micro2649() {
    assertIdentical("{ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c(\"c\")]] }", "structure(list(d = 3, e = structure(4, .Names = \"f\")), .Names = c(\"d\", \"e\"))");
  }
  @Test
  public void micro2650() {
    assertIdentical("{ f <- function(b, i, v) { b[[i]] <- v ; b } ; f(1:3,2,2) ; f(1:3,\"X\",2) ; f(list(1,list(2)),c(2,1),4) }", "list(1, list(4))");
  }
  @Test
  public void micro2651() {
    assertIdentical("{ l<-list(1,2L,TRUE) ; l[[2]]<-100 ; l }", "list(1, 100, TRUE)");
  }
  @Test
  public void micro2652() {
    assertIdentical("{ l<-list(1,2L,TRUE) ; l[[5]]<-100 ; l }", "list(1, 2L, TRUE, NULL, 100)");
  }
  @Test
  public void micro2653() {
    assertIdentical("{ l<-list(1,2L,TRUE) ; l[[3]]<-list(100) ; l }", "list(1, 2L, list(100))");
  }
  @Test
  public void micro2654() {
    assertIdentical("{ v<-1:3 ; v[2] <- list(100) ; v }", "list(1L, 100, 3L)");
  }
  @Test
  public void micro2656() {
    assertIdentical("{ l <- list() ; l[[1]] <-2 ; l}", "list(2)");
  }
  @Test
  public void micro2657() {
    assertIdentical("{ l<-list() ; x <- 1:3 ; l[[1]] <- x  ; l }", "list(1:3)");
  }
  @Test
  public void micro2658() {
    assertIdentical("{ l <- list(1,2,3) ; l[2] <- list(100) ; l[2] }", "list(100)");
  }
  @Test
  public void micro2659() {
    assertIdentical("{ l <- list(1,2,3) ; l[[2]] <- list(100) ; l[2] }", "list(list(100))");
  }
  @Test
  public void micro2660() {
    assertIdentical("{ m<-list(1,2) ; m[TRUE] <- NULL ; m }", "list()");
  }
  @Test
  public void micro2661() {
    assertIdentical("{ m<-list(1,2) ; m[[TRUE]] <- NULL ; m }", "list(2)");
  }
  @Test
  public void micro2662() {
    assertIdentical("{ m<-list(1,2) ; m[[1]] <- NULL ; m }", "list(2)");
  }
  @Test
  public void micro2663() {
    assertIdentical("{ m<-list(1,2) ; m[[-1]] <- NULL ; m }", "list(1)");
  }
  @Test
  public void micro2664() {
    assertIdentical("{ m<-list(1,2) ; m[[-2]] <- NULL ; m }", "list(2)");
  }
  @Test
  public void micro2669() {
    assertIdentical("{ l <- list(a=1,b=2,c=3) ; l[1] <- NULL ; l }", "structure(list(b = 2, c = 3), .Names = c(\"b\", \"c\"))");
  }
  @Test
  public void micro2670() {
    assertIdentical("{ l <- list(a=1,b=2,c=3) ; l[3] <- NULL ; l }", "structure(list(a = 1, b = 2), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2672() {
    assertIdentical("{ l <- list(a=1,b=2,c=3) ; l[4] <- NULL ; l}", "structure(list(a = 1, b = 2, c = 3), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2673() {
    assertIdentical("{ l <- list(a=1,b=2,c=3) ; l[[5]] <- NULL ; l}", "structure(list(a = 1, b = 2, c = 3), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2674() {
    assertIdentical("{ l <- list(a=1,b=2,c=3) ; l[[4]] <- NULL ; l}", "structure(list(a = 1, b = 2, c = 3), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2675() {
    assertIdentical("{ l <- list(1,2); l[0] <- NULL; l}", "list(1, 2)");
  }
  @Test
  public void micro2676() {
    assertIdentical("{ l <- list(1,2,3) ; l[c(2,3)] <- c(20,30) ; l }", "list(1, 20, 30)");
  }
  @Test
  public void micro2677() {
    assertIdentical("{ l <- list(1,2,3) ; l[c(2:3)] <- c(20,30) ; l }", "list(1, 20, 30)");
  }
  @Test
  public void micro2678() {
    assertIdentical("{ l <- list(1,2,3) ; l[-1] <- c(20,30) ; l }", "list(1, 20, 30)");
  }
  @Test
  public void micro2679() {
    assertIdentical("{ l <- list(1,2,3) ; l[-1L] <- c(20,30) ; l }", "list(1, 20, 30)");
  }
  @Test
  public void micro2680() {
    assertIdentical("{ l <- list(1,2,3) ; l[c(FALSE,TRUE,TRUE)] <- c(20,30) ; l }", "list(1, 20, 30)");
  }
  @Test
  public void micro2681() {
    assertIdentical("{ l <- list() ; l[c(TRUE,TRUE)] <-2 ; l }", "list(2, 2)");
  }
  @Test
  public void micro2682() {
    assertIdentical("{ x <- 1:3 ; l <- list(1) ; l[[TRUE]] <- x ; l[[1]] }", "1:3");
  }
  @Test
  public void micro2683() {
    assertIdentical("{ x<-list(1,2,3,4,5); x[3:4]<-c(300L,400L); x }", "list(1, 2, 300L, 400L, 5)");
  }
  @Test
  public void micro2684() {
    assertIdentical("{ x<-list(1,2,3,4,5); x[4:3]<-c(300L,400L); x }", "list(1, 2, 400L, 300L, 5)");
  }
  @Test
  public void micro2685() {
    assertIdentical("{ x<-list(1,2L,TRUE,TRUE,FALSE); x[c(-2,-3,-3,-100,0)]<-256; x }", "list(256, 2L, TRUE, 256, 256)");
  }
  @Test
  public void micro2686() {
    assertIdentical("{ x<-list(1,2L,list(3,list(4)),list(5)) ; x[c(4,2,3)]<-list(256L,257L,258L); x }", "list(1, 257L, 258L, 256L)");
  }
  @Test
  public void micro2687() {
    assertIdentical("{ x<-list(FALSE,NULL,3L,4L,5.5); x[c(TRUE,FALSE)] <- 1000; x }", "list(1000, NULL, 1000, 4L, 1000)");
  }
  @Test
  public void micro2688() {
    assertIdentical("{ x<-list(11,10,9) ; x[c(TRUE, FALSE, TRUE)] <- c(1000,2000); x }", "list(1000, 10, 2000)");
  }
  @Test
  public void micro2689() {
    assertIdentical("{ l <- list(1,2,3) ; x <- list(100) ; y <- x; l[1:1] <- x ; l[[1]] }", "100");
  }
  @Test
  public void micro2690() {
    assertIdentical("{ l <- list(1,2,3) ; x <- list(100) ; y <- x; l[[1:1]] <- x ; l[[1]] }", "list(100)");
  }
  @Test
  public void micro2692() {
    assertIdentical("{ v<-list(1,2,3) ; v[c(2,3,4)] <- NULL ; v }", "list(1)");
  }
  @Test
  public void micro2694() {
    assertIdentical("{ v<-list(1,2,3) ; v[c(TRUE,FALSE,TRUE)] <- NULL ; v }", "list(2)");
  }
  @Test
  public void micro2695() {
    assertIdentical("{ v<-list(1,2,3) ; v[c()] <- NULL ; v }", "list(1, 2, 3)");
  }
  @Test
  public void micro2696() {
    assertIdentical("{ v<-list(1,2,3) ; v[integer()] <- NULL ; v }", "list(1, 2, 3)");
  }
  @Test
  public void micro2697() {
    assertIdentical("{ v<-list(1,2,3) ; v[double()] <- NULL ; v }", "list(1, 2, 3)");
  }
  @Test
  public void micro2698() {
    assertIdentical("{ v<-list(1,2,3) ; v[logical()] <- NULL ; v }", "list(1, 2, 3)");
  }
  @Test
  public void micro2699() {
    assertIdentical("{ v<-list(1,2,3) ; v[c(TRUE,FALSE)] <- NULL ; v }", "list(2)");
  }
  @Test
  public void micro2703() {
    assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(2,3)] <- NULL ; l}", "structure(list(a = 1, d = 4), .Names = c(\"a\", \"d\"))");
  }
  @Test
  public void micro2704() {
    assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(2,3,5)] <- NULL ; l}", "structure(list(a = 1, d = 4), .Names = c(\"a\", \"d\"))");
  }
  @Test
  public void micro2706() {
    assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,TRUE,FALSE,TRUE)] <- NULL ; l}", "structure(list(c = 3), .Names = \"c\")");
  }
  @Test
  public void micro2707() {
    assertIdentical("{ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,FALSE)] <- NULL ; l}", "structure(list(b = 2, d = 4), .Names = c(\"b\", \"d\"))");
  }
  @Test
  public void micro2709() {
    assertIdentical("{ l <- list(a=1,b=2,c=3) ; l[[\"b\"]] <- NULL ; l }", "structure(list(a = 1, c = 3), .Names = c(\"a\", \"c\"))");
  }
  @Test
  public void micro2710() {
    assertIdentical("{ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- NULL ; l }", "list(1, list(2))");
  }
  @Test
  public void micro2711() {
    assertIdentical("{ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- 4 ; l }", "list(1, list(2, 4))");
  }
  @Test
  public void micro2712() {
    assertIdentical("{ l <- list(1,list(2,list(3))) ; l[[1]] <- NULL ; l }", "list(list(2, list(3)))");
  }
  @Test
  public void micro2713() {
    assertIdentical("{ l <- list(1,list(2,list(3))) ; l[[1]] <- 5 ; l }", "list(5, list(2, list(3)))");
  }
  @Test
  public void micro2714() {
    assertIdentical("{ l<-list(a=1,b=2,list(c=3,d=4,list(e=5:6,f=100))) ; l[[c(3,3,1)]] <- NULL ; l }", "structure(list(a = 1, b = 2, structure(list(c = 3, d = 4, structure(list(    f = 100), .Names = \"f\")), .Names = c(\"c\", \"d\", \"\"))), .Names = c(\"a\", \"b\", \"\"))");
  }
  @Test
  public void micro2715() {
    assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"zz\")]] <- 100 ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(c(1, 2, 3, 100), .Names = c(\"x\", \"y\", \"z\",     \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2716() {
    assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"z\")]] <- 100 ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(c(1, 2, 100), .Names = c(\"x\", \"y\", \"z\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2717() {
    assertIdentical("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\")]] <- NULL ; l }", "structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2), .Names = c(\"d\", \"e\"))), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2718() {
    assertIdentical("{ l<-list(a=1,b=2,c=3) ; l[c(\"a\",\"a\",\"a\",\"c\")] <- NULL ; l }", "structure(list(b = 2), .Names = \"b\")");
  }
  @Test
  public void micro2719() {
    assertIdentical("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\",\"zz\")]] <- 100L ; l }", "structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = structure(c(1L, 2L, 3L, 100L), .Names = c(\"x\", \"y\", \"z\",     \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2720() {
    assertIdentical("{ l<-list(a=TRUE,b=FALSE,c=list(d=TRUE,e=FALSE,f=c(x=TRUE,y=FALSE,z=TRUE))) ; l[[c(\"c\",\"f\",\"zz\")]] <- TRUE ; l }", "structure(list(a = TRUE, b = FALSE, c = structure(list(d = TRUE,     e = FALSE, f = structure(c(TRUE, FALSE, TRUE, TRUE), .Names = c(\"x\",     \"y\", \"z\", \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2721() {
    assertIdentical("{ l<-list(a=\"a\",b=\"b\",c=list(d=\"cd\",e=\"ce\",f=c(x=\"cfx\",y=\"cfy\",z=\"cfz\"))) ; l[[c(\"c\",\"f\",\"zz\")]] <- \"cfzz\" ; l }", "structure(list(a = \"a\", b = \"b\", c = structure(list(d = \"cd\",     e = \"ce\", f = structure(c(\"cfx\", \"cfy\", \"cfz\", \"cfzz\"), .Names = c(\"x\",     \"y\", \"z\", \"zz\"))), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2723() {
    assertIdentical("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\")]] <- 100L ; l }", "structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = 100L), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2724() {
    assertIdentical("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\")]] <- list(haha=\"gaga\") ; l }", "structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = structure(list(haha = \"gaga\"), .Names = \"haha\")), .Names = c(\"d\", \"e\", \"f\"))), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2725() {
    assertIdentical("{ x<-c(1,2,3) ; y<-x ; x[2]<-100 ; y }", "c(1, 2, 3)");
  }
  @Test
  public void micro2726() {
    assertIdentical("{ l<-list() ; x <- 1:3 ; l[[1]] <- x; x[2] <- 100L; l[[1]] }", "1:3");
  }
  @Test
  public void micro2727() {
    assertIdentical("{ l <- list(1, list(2)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }", "2");
  }
  @Test
  public void micro2728() {
    assertIdentical("{ l <- list(1, list(2,3,4)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }", "2");
  }
  @Test
  public void micro2729() {
    assertIdentical("{ x <- c(1L,2L,3L) ; l <- list(1) ; l[[1]] <- x ; x[2] <- 100L ; l[[1]] }", "1:3");
  }
  @Test
  public void micro2730() {
    assertIdentical("{ l <- list(100) ; f <- function() { l[[1]] <- 2 } ; f() ; l }", "list(100)");
  }
  @Test
  public void micro2731() {
    assertIdentical("{ l <- list(100,200,300,400,500) ; f <- function() { l[[3]] <- 2 } ; f() ; l }", "list(100, 200, 300, 400, 500)");
  }
  @Test
  public void micro2732() {
    assertIdentical("{ x <-2L ; y <- x; x[1] <- 211L ; y }", "2L");
  }
  @Test
  public void micro2733() {
    assertIdentical("{ f <- function() { l[1:2] <- x ; x[1] <- 211L  ; l[1] } ; l <- 1:3 ; x <- 10L ; f() }", "10L");
  }
  @Test
  public void micro2734() {
    assertIdentical("{ x <- list(1,list(2,3),4) ; x[[c(2,3)]] <- 3 ; x }", "list(1, list(2, 3, 3), 4)");
  }
  @Test
  public void micro2735() {
    assertIdentical("{ x <- list(1,list(2,3),4) ; z <- x[[2]] ; x[[c(2,3)]] <- 3 ; z }", "list(2, 3)");
  }
  @Test
  public void micro2736() {
    assertIdentical("{ x <- list(1,list(2,3),4) ; z <- list(x,x) ; u <- list(z,z) ; u[[c(2,2,3)]] <- 6 ; unlist(u) }", "c(1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 6)");
  }
  @Test
  public void micro2737() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, 3) }", "list(1, 3, 3)");
  }
  @Test
  public void micro2738() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, NULL) }", "list(1, 3)");
  }
  @Test
  public void micro2739() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(c(1,2,3), \"hello\", 2) }", "structure(c(1, 2, 3, 2), .Names = c(\"\", \"\", \"\", \"hello\"))");
  }
  @Test
  public void micro2740() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(x=3)),c(\"b\",\"x\"),10) }", "structure(list(1, 2, b = structure(list(x = 10), .Names = \"x\")), .Names = c(\"\", \"\", \"b\"))");
  }
  @Test
  public void micro2741() {
    assertIdentical("list(1,2,b=c(x=3))", "structure(list(1, 2, b = structure(3, .Names = \"x\")), .Names = c(\"\", \"\", \"b\"))");
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=c(x=3)),c(\"b\",\"x\"),10) }", "structure(list(1, 2, b = structure(10, .Names = \"x\")), .Names = c(\"\", \"\", \"b\"))");
  }
  @Test
  public void micro2742() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(c(1,2,b=c(x=3)),c(\"b\"),10) }", "structure(c(1, 2, 3, 10), .Names = c(\"\", \"\", \"b.x\", \"b\"))");
  }
  @Test
  public void micro2743() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(a=list(x=1,y=2),3),4),c(\"b\",\"a\",\"x\"),10) }", "structure(list(1, 2, b = structure(list(a = structure(list(x = 10,     y = 2), .Names = c(\"x\", \"y\")), 3), .Names = c(\"a\", \"\")),     4), .Names = c(\"\", \"\", \"b\", \"\"))");
  }
  @Test
  public void micro2744() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=2),\"b\",NULL) }", "structure(list(a = 1), .Names = \"a\")");
  }
  @Test
  public void micro2745() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=list(2)),\"b\",double()) }", "structure(list(a = 1, b = numeric(0)), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2746() {
    assertIdentical("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=c(a=2)),c(TRUE,TRUE),3) }", "structure(list(a = 3, b = structure(2, .Names = \"a\")), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2747() {
    assertIdentical("{ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c(2,2,3,2)]] <- 10 ; l }", "structure(list(a = 1, b = 2, cd = structure(list(c = 3, d = 4), .Names = c(\"c\", \"d\"))), .Names = c(\"a\", \"b\", \"cd\"))");
  }
  @Test //@Ignore("Recursive setting")
  public void micro2748() {
    assertIdentical("{ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c(\"xy\",\"y\",\"cd\",\"d\")]] <- 10 ; l }", "structure(list(a = 1, b = 2, cd = structure(list(c = 3, d = 4), .Names = c(\"c\", \"d\"))), .Names = c(\"a\", \"b\", \"cd\"))");
  }
  @Test
  public void micro2749() {
    assertIdentical("{ a <- 'hello'; a[[5]] <- 'done'; a[[3]] <- 'muhuhu'; a; }", "c(\"hello\", NA, \"muhuhu\", NA, \"done\")");
  }
  @Test
  public void micro2750() {
    assertIdentical("{ a <- 'hello'; a[[5]] <- 'done'; b <- a; b[[3]] <- 'muhuhu'; b; }", "c(\"hello\", NA, \"muhuhu\", NA, \"done\")");
  }
  @Test
  public void micro2751() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(1:3,\"a\",4) }", "structure(c(1, 2, 3, 4), .Names = c(\"\", \"\", \"\", \"a\"))");
  }
  @Test
  public void micro2752() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(NULL,\"a\",4) }", "structure(4, .Names = \"a\")");
  }
  @Test
  public void micro2753() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(NULL,c(\"a\",\"X\"),4:5) }", "structure(4:5, .Names = c(\"a\", \"X\"))");
  }
  @Test
  public void micro2754() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(double(),c(\"a\",\"X\"),4:5) }", "structure(c(4, 5), .Names = c(\"a\", \"X\"))");
  }
  @Test
  public void micro2755() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(double(),c(\"a\",\"X\"),list(3,TRUE)) }", "structure(list(a = 3, X = TRUE), .Names = c(\"a\", \"X\"))");
  }
  @Test
  public void micro2756() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.raw(11:13),c(\"a\",\"X\"),list(3,TRUE)) }", "structure(list(as.raw(0x0b), as.raw(0x0c), as.raw(0x0d), a = 3,     X = TRUE), .Names = c(\"\", \"\", \"\", \"a\", \"X\"))");
  }
  @Test
  public void micro2757() {
    assertIdentical("{ b <- c(11,12) ; b[\"\"] <- 100 ; b }", "structure(c(11, 12, 100), .Names = c(\"\", \"\", \"\"))");
  }
  @Test
  public void micro2758() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(1,a=2),c(\"a\",\"X\",\"a\"),list(3,TRUE,FALSE)) }", "structure(list(1, a = FALSE, X = TRUE), .Names = c(\"\", \"a\", \"X\"))");
  }
  @Test
  public void micro2759() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"a\",\"X\",\"a\"),list(3,TRUE,FALSE)) }", "structure(list(X = TRUE, a = FALSE), .Names = c(\"X\", \"a\"))");
  }
  @Test
  public void micro2760() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),as.character(NA),as.complex(23)) }", "structure(c(13+0i, 14+0i, 23+0i), .Names = c(\"\", \"\", NA))");
  }
  @Test
  public void micro2761() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; " +
        "f(1:3,3:1,4:6) ; " +
        "f(as.complex(c(13,14)),character(),as.complex(23)) }", "c(13+0i, 14+0i)");
  }
  @Test
  public void micro2762() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),c(\"\",\"\",\"\"),as.complex(23)) }", "structure(c(13+0i, 14+0i, 23+0i, 23+0i, 23+0i), .Names = c(\"\", \"\", \"\", \"\", \"\"))");
  }
  @Test
  public void micro2763() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),c(\"\",\"\",NA),as.complex(23)) }", "structure(c(13+0i, 14+0i, 23+0i, 23+0i, 23+0i), .Names = c(\"\", \"\", \"\", \"\", NA))");
  }
  @Test
  public void micro2764() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.raw(c(13,14)),c(\"a\",\"X\",\"a\"),as.raw(23)) }", "structure(as.raw(c(0x0d, 0x0e, 0x17, 0x17)), .Names = c(\"\", \"\", \"a\", \"X\"))");
  }
  @Test
  public void micro2765() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"a\",\"X\",\"a\",\"b\"),list(3,TRUE,FALSE)) }", "structure(list(X = TRUE, a = FALSE, b = 3), .Names = c(\"X\", \"a\", \"b\"))");
  }
  @Test
  public void micro2766() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),list(3,TRUE,FALSE)) }", "structure(list(X = 3, a = 2, b = TRUE, \"NA\" = FALSE), .Names = c(\"X\", \"a\", \"b\", NA))");
  }
  @Test
  public void micro2767() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),as.complex(10)) }", "structure(c(10+0i, 2+0i, 10+0i, 10+0i), .Names = c(\"X\", \"a\", \"b\", NA))");
  }
  @Test
  public void micro2768() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),1:3) }", "structure(c(1, 2, 2, 3), .Names = c(\"X\", \"a\", \"b\", NA))");
  }
  @Test
  public void micro2769() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),c(TRUE,NA)) }", "structure(c(1, 2, NA, 1), .Names = c(\"X\", \"a\", \"b\", NA))");
  }
  @Test
  public void micro2770() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(c(X=1L,a=2L),c(\"X\",\"b\",NA),c(TRUE,NA,FALSE)) }", "structure(c(1L, 2L, NA, 0L), .Names = c(\"X\", \"a\", \"b\", NA))");
  }
  @Test
  public void micro2771() {
    assertIdentical("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(list(X=1L,a=2L),c(\"X\",\"b\",NA),NULL) }", "structure(list(a = 2L), .Names = \"a\")");
  }
  @Test
  public void micro2772() {
    assertIdentical("{ b <- c(a=1+2i,b=3+4i) ; dim(b) <- c(2,1) ; b[c(\"a\",\"b\")] <- 3+1i ; b }", "structure(c(1+2i, 3+4i, 3+1i, 3+1i), .Names = c(\"\", \"\", \"a\", \"b\"))");
  }
  @Test
  public void micro2776() {
    assertIdentical("{ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[c(\"hello\",\"hi\")] <- NULL ; b }", "list(1+2i, 3+4i)");
  }
  @Test
  public void micro2777() {
    assertIdentical("{ a <- TRUE; a[[2]] <- FALSE; a; }", "c(TRUE, FALSE)");
  }
  @Test
  public void micro2778() {
    assertIdentical("{ x <- 1:3 ; f <- function() { x[2] <<- 100 } ; f() ; x }", "c(1, 100, 3)");
  }
  @Test
  public void micro2779() {
    assertIdentical("{ x <- 1:3 ; f <- function() { x[2] <- 10 ; x[2] <<- 100 ; x[2] <- 1000 } ; f() ; x }", "c(1, 100, 3)");
  }
  @Test
  public void micro2780() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; m[1,2] }", "3L");
  }
  @Test
  public void micro2781() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; m[1,] }", "c(1L, 3L, 5L)");
  }
  @Test
  public void micro2783() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; m[,1] }", "1:2");
  }
  @Test
  public void micro2793() {
    assertIdentical("{ m <- matrix(1:16, nrow=8) ; m[c(TRUE,FALSE),c(FALSE,TRUE), drop=TRUE]}", "c(9L, 11L, 13L, 15L)");
  }
  @Test
  public void micro2794() {
    assertIdentical("{ m <- matrix(1:16, nrow=8) ; m[c(TRUE,FALSE,FALSE),c(FALSE,TRUE), drop=TRUE]}", "c(9L, 12L, 15L)");
  }
  @Test
  public void micro2795() {
    assertIdentical("{ m <- matrix(1:6, nrow=3) ; f <- function(i,j) { m[i,j] } ; f(1,c(1,2)) ; f(1,c(-1,0,-1,-10)) }", "4L");
  }
  @Test
  public void micro2796() {
    assertIdentical("{ m <- matrix(1:6, nrow=3) ; f <- function(i,j) { m[i,j] } ; f(1,c(1,2)) ; f(c(TRUE),c(FALSE,TRUE)) }", "4:6");
  }
  @Test
  public void micro2797() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; x<-2 ; m[[1,x]] }", "3L");
  }
  @Test
  public void micro2798() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; m[[1,2]] }", "3L");
  }
  @Test
  public void micro2799() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; f <- function(i,j) { m[i,j] } ;  f(1,1); f(1,1:3) }", "c(1L, 3L, 5L)");
  }
  @Test
  public void micro2800() {
    assertIdentical("{ m <- matrix(1:4, nrow=2) ; m[[2,1,drop=FALSE]] }", "2L");
  }
  @Test
  public void micro2801() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; m[1:2,0:1] }", "1:2");
  }
  @Test
  public void micro2802() {
    assertIdentical("{ m <- matrix(1:6, nrow=2) ; m[1:2,0:1] ; m[1:2,1:1] }", "1:2");
  }
  @Test
  public void micro2803() {
    assertIdentical("{ a <- list(); a$a = 6; a; }", "structure(list(a = 6), .Names = \"a\")");
  }
  @Test
  public void micro2804() {
    assertIdentical("{ a <- list(); a[['b']] = 6; a; }", "structure(list(b = 6), .Names = \"b\")");
  }
  @Test
  public void micro2805() {
    assertIdentical("{ a <- list(a = 1, b = 2); a$a; }", "1");
  }
  @Test
  public void micro2806() {
    assertIdentical("{ a <- list(a = 1, b = 2); a$b; }", "2");
  }
  @Test
  public void micro2808() {
    assertIdentical("{ a <- list(a = 1, b = 2); a$a <- 67; a; }", "structure(list(a = 67, b = 2), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2809() {
    assertIdentical("{ a <- list(a = 1, b = 2); a$b <- 67; a; }", "structure(list(a = 1, b = 67), .Names = c(\"a\", \"b\"))");
  }
  @Test
  public void micro2810() {
    assertIdentical("{ a <- list(a = 1, b = 2); a$c <- 67; a; }", "structure(list(a = 1, b = 2, c = 67), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2811() {
    assertIdentical("{ v <- list(xb=1, b=2, aa=3, aa=4) ; v$aa }", "3");
  }
  @Test
  public void micro2813() {
    assertIdentical("{ x <- list(a=1, b=2) ; f <- function(x) { x$b } ; f(x) ; f(x) }", "2");
  }
  @Test
  public void micro2814() {
    assertIdentical("{ x <- list(a=1, b=2) ; f <- function(x) { x$b } ; f(x) ; x <- list(c=2,b=10) ; f(x) }", "10");
  }
  @Test
  public void micro2815() {
    assertIdentical("{ v <- list(xb=1, b=2, aa=3, aa=4) ; v$x }", "1");
  }
  @Test
  public void micro2817() {
    assertIdentical("{ f <- function(v) { v$x } ; f(list(xa=1, xb=2, hello=3)) ; f(list(y=2,x=3)) }", "3");
  }
  @Test
  public void micro2818() {
    assertIdentical("{ f <- function(v) { v$x } ; f(list(xa=1, xb=2, hello=3)) ; l <- list(y=2,x=3) ; f(l) ; l[[2]] <- 4 ; f(l) }", "4");
  }
  @Test
  public void micro2819() {
    assertIdentical("{ a <- c(1,2); a$a = 3; a; }", "structure(list(1, 2, a = 3), .Names = c(\"\", \"\", \"a\"))");
  }
  @Test
  public void micro2820() {
    assertIdentical("{ l <- list(a=1,b=2,c=3) ; z <- l ; l$b <- 10 ; z }", "structure(list(a = 1, b = 2, c = 3), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2821() {
    assertIdentical("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1),11) }", "structure(list(a = 1, z = 11), .Names = c(\"a\", \"z\"))");
  }
  @Test
  public void micro2822() {
    assertIdentical("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2,z=3),10) }", "structure(list(a = 1, b = 2, z = 10), .Names = c(\"a\", \"b\", \"z\"))");
  }
  @Test
  public void micro2823() {
    assertIdentical("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(c(a=1,b=2,z=3),10) }", "structure(list(a = 1, b = 2, z = 10), .Names = c(\"a\", \"b\", \"z\"))");
  }
  @Test
  public void micro2824() {
    assertIdentical("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(list(a=1,z=2),10) }", "structure(list(a = 1, z = 10), .Names = c(\"a\", \"z\"))");
  }
  @Test
  public void micro2825() {
    assertIdentical("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(c(a=1,z=2),10) }", "structure(list(a = 1, z = 10), .Names = c(\"a\", \"z\"))");
  }
  @Test
  public void micro2826() {
    assertIdentical("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(l <- list(a=1,z=2),10) }", "structure(list(a = 1, z = 10), .Names = c(\"a\", \"z\"))");
  }
  @Test
  public void micro2827() {
    assertIdentical("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(l <- list(a=1,z=2),10) ; l }", "structure(list(a = 1, z = 2), .Names = c(\"a\", \"z\"))");
  }
  @Test
  public void micro2828() {
    assertIdentical("{ x <- list(a=1,b=2,c=3) ; x$z <- NULL ; x }", "structure(list(a = 1, b = 2, c = 3), .Names = c(\"a\", \"b\", \"c\"))");
  }
  @Test
  public void micro2829() {
    assertIdentical("{ x <- list(a=1,b=2,c=3) ; x$a <- NULL ; x }", "structure(list(b = 2, c = 3), .Names = c(\"b\", \"c\"))");
  }
  @Test
  public void micro2831() {
    assertIdentical("{ f <- function(x, v) { x$a <- v ; x } ; x <- list(a=1,b=2,c=3) ; z <- x ; f(x, 10) ; f(x,NULL) }", "structure(list(b = 2, c = 3), .Names = c(\"b\", \"c\"))");
  }
  @Test
  public void micro2832() {
    assertIdentical("{ x <- list(a = 1, b = list(x = 2, y = 3)); x[[c('b', 'x')]] <- 42; x[[c('b', 'x')]] }", "42");
  }
  @Test
  public void micro2833() {
    assertIdentical("{ x <- list(a = 1, b = list(x = 2, y = 3, z = list(q = 9))); x[[c('b', 'z', 'q')]] <- 42; x[[c('b', 'z', 'q')]] }", "42");
  }
  @Test
  public void micro2834() {
    assertIdentical("{ x <- list(a = 1, b = list(x = 2, y = 3)); x[[c('b','x')]] <- 42; x[['b']][['x']] }", "42");
  }
  @Test
  public void micro2835() {
    assertIdentical("{ x <- list(a = 1, s = 67, b = list(x = 2, y = 3), c = list(k = 8, m = list(w = list(s = 89), d = 12 ), g = 7) ); x[[c('c','m','d')]] <- 42; x[[c('c','m','d')]] }", "42");
  }
  @Test
  public void micro2836() {
    assertIdentical("{ x <- list(a = 1, s = 67, b = list(x = 2, y = 3), c = list(k = 8, m = list(w = list(s = 89), d = 12 ), g = 7) ); x[[c('c','m','w','s')]] <- 42; x[[c('c','m','w','s')]] }", "42");
  }
  @Test
  public void micro2837() {
    assertIdentical("{ x <- list(a = 1, s = 67 ); x[['s']] <- 42; x[['s']] }", "42");
  }
  @Test
  public void grepNAtest() {
    assertIdentical("x <- grep(c(\"a\",\"b\"), pattern=NA); x", "c(NA_character_,NA_character_)");
  }
  @Test
  public void microSTRSPLIT0() {
    assertIdentical("{ a <- strsplit(NA, \"d\"); a[[1]] }", "NULL");
  }
  @Test
  public void microSTRSPLIT1() {
    assertIdentical("{ a <- strsplit(\"abc\", \"d\"); a[[1]] }", "c(\"abc\")");
  }
  @Test
  public void microGREPL1() {
    assertIdentical("{ a <- grepl(\"a\",c(\"a\",\"b\")); a }", "c(TRUE,FALSE)");
  }
  @Test
  public void microGREPL2() {
    assertIdentical("{ a <- grepl(NA,c(\"a\",\"b\")); a }", "c(NA_character_, NA_character_)");
  }
  @Test
  public void microGREP0() {
    assertIdentical("{ a <- grep(\"a\", c(\"a\",\"b\",NA,\"a\")); a }", "c(1L, 4L)");
  }
  @Test
  public void microGREP1() {
    assertIdentical("{ a <- grep(\"a\", NA); a }", "integer(0)");
  }
  @Test
  public void microGREP2() {
    assertIdentical("{ a <- grep(NA, \"abc\"); a }", "NA_character_");
  }
  @Test
  public void microENC2UTF8_1() {
    assertIdentical("{ a <- enc2utf8(\"abc\"); a }", "c(\"abc\")");
  }
  @Test
  public void microENC2UTF8_2() {
    assertIdentical("{ a <- enc2utf8(c(\"a\",\"b\")); a }", "c(\"a\",\"b\")");
  }

}