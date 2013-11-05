package org.renjin.simple;

import org.junit.Test;

public class ArithmeticTest extends SimpleTestBase {

  @Test
  public void testScalars() {
    assertEval("1L+1", "2.0");
    assertEval("1L+1L", "2L");
    assertEval("(1+1)*(3+2)", "10.0");
    assertEval("1000000000*100000000000", "1.0E20");
    assertEval("1000000000L*1000000000L", "NA_integer_");
    assertEval("1000000000L*1000000000", "1.0E18");
    assertEval("1+TRUE", "2.0");
    assertEval("1L+TRUE", "2L");
    assertEval("1+FALSE<=0", "FALSE");
    assertEval("1L+FALSE<=0", "FALSE");
    assertEval("TRUE+TRUE+TRUE*TRUE+FALSE+4", "7.0");
    assertEval("1L*NA", "NA_integer_");
    assertEval("1+NA", "NA_real_");
    assertEval("2L^10L", "1024.0");

    assertEval("3 %/% 2", "1.0");
    assertEval("3L %/% 2L", "1L");
    assertEval("3L %/% -2L", "-2L");
    assertEval("3 %/% -2", "-2.0");
    assertEval("3 %/% 0", "Inf");
    assertEval("3L %/% 0L", "NA_integer_"); // note this would return 0L in earlier versions of R

    assertEval("3 %% 2", "1.0");
    assertEval("3L %% 2L", "1L");
    assertEval("3L %% -2L", "-1L");
    assertEval("3 %% -2", "-1.0");
    assertEval("is.nan(3 %% 0)", "TRUE");
    assertEval("3L %% 0L", "NA_integer_");

    assertEval("0x10 + 0x10L + 1.28", "33.28");

    assertEval("1/0", "Inf");

    assertEval("(1+2i)*(3+4i)", "-5.0+10.0i");
    assertEval("x <- 1+2i; y <- 3+4i; x*y", "-5.0+10.0i");
    assertEval("x <- 1+2i; y <- 3+4i; x/y", "0.44+0.08i");
    assertEval("x <- 1+2i; y <- 3+4i; x-y", "-2.0-2.0i");
    assertEval("x <- 1+2i; y <- 3+4i; identical(round(x*x*y/(x+y), digits=5), -1.92308+2.88462i)", "TRUE");
    assertEval("x <- c(-1.5-1i,-1.3-1i) ; y <- c(0+0i, 0+0i) ; y*y+x", "c(-1.5-1.0i, -1.3-1.0i)");
    assertEval("x <- c(-1.5-1i,-1.3-1i) ; y <- c(0+0i, 0+0i) ; y-x", "c(1.5+1.0i, 1.3+1.0i)");
    assertEval("x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; y-x", "c(4.0+3.0i, -7.0-5.0i)");
    assertEval("x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; y+x", "c(2.0-1.0i, -1.0+15.0i)");
    assertEval("x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; y*x", "c(-1.0-7.0i, -62.0-25.0i)");
    assertEval("x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; round(y/x, digits=5)", "c(-1.0+1.0i, 0.34862+0.50459i)");

    assertEval("round( (1+2i)^(3+4i), digits=5 )", "0.12901+0.03392i");
    assertEval("(1+2i)^2", "-3.0+4.0i");
    assertEval("(1+2i)^(-2)", "-0.12-0.16i");
    assertEval("(1+2i)^0", "1.0+0.0i");
    assertEval("0^(-1+1i)", "complex(real=NaN,imaginary=NaN)");

    assertEval("(0+0i)/(0+0i)", "complex(real=NaN,imaginary=NaN)");
    assertEval("(1+0i)/(0+0i)", "complex(real=Inf,imaginary=NaN)");
    assertEval("(0+1i)/(0+0i)", "complex(real=NaN,imaginary=Inf)");
    assertEval("(1+1i)/(0+0i)", "complex(real=Inf, imaginary=Inf)");
    assertEval("(-1+0i)/(0+0i)", "complex(real=-Inf, imaginary=NaN)");
    assertEval("(-1-1i)/(0+0i)", "complex(real=-Inf, imaginary=-Inf)");

    assertEval("((0+1i)/0) * ((0+1i)/0)", "complex(real=-Inf, imaginary=NaN)");
    assertEval("((0-1i)/0) * ((0+1i)/0)", "complex(real=Inf, imaginary=NaN)");
    assertEval("((0-1i)/0) * ((0-1i)/0)", "complex(real=-Inf, imaginary=+NaN)");
    assertEval("((0-1i)/0) * ((1-1i)/0)", "complex(real=-Inf, imaginary=-Inf)");
    assertEval("((0-1i)/0) * ((-1-1i)/0)", "complex(real=-Inf, imaginary=Inf)");

    //assertEval("identical( (1+2i) / ((0-1i)/(0+0i)), -0.0+0.0i, num.eq=FALSE)", "TRUE"); fails, fails in GNU R too

    assertEval("1/((1+0i)/(0+0i))", "0.0+0.0i");
    assertEval("(1+2i) / ((0-0i)/(0+0i))", "complex(real=NaN, imaginary=NaN)");


    assertEval("1^(1/0)", "1.0"); // FDLIBM (Math.pow) fails on this
    assertEval("(-2)^(1/0)", "NaN");
    assertEval("(-2)^(-1/0)", "NaN");
    assertEval("(1)^(-1/0)", "1.0");
    assertEval("0^(-1/0)", "Inf");
    assertEval("0^(1/0)", "0.0");
    assertEval("0^(0/0)", "NaN");
    assertEval("1^(0/0)", "1.0");
    assertEval("(-1)^(0/0)", "NaN");
    assertEval("(-1/0)^(0/0)", "NaN");
    assertEval("(1/0)^(0/0)", "NaN");
    assertEval("(0/0)^(1/0)", "NaN");
    assertEval("(-1/0)^3", "-Inf");
    assertEval("(1/0)^(-4)", "0.0");
    assertEval("(-1/0)^(-4)", "0.0");

    assertEval("f <- function(a, b) { a + b } ; f(1+2i, 3+4i) ; f(1, 2)", "3.0");
    assertEval("f <- function(a, b) { a + b } ; f(2, 3+4i) ; f(1, 2)", "3.0");
    assertEval("f <- function(a, b) { a + b } ; f(1+2i, 3) ; f(1, 2)", "3.0");
    assertEval("f <- function(a, b) { a + b } ; f(2, 3+4i) ; f(1, 2)", "3.0");
    assertEval("f <- function(a, b) { a + b } ; f(1+2i, 3) ; f(1, 2)", "3.0");
    assertEval("1L / 2L", "0.5");
    assertEval("f <- function(a, b) { a / b } ; f(1L, 2L) ; f(1, 2)", "0.5");
    assertEval("(1:2)[3] / 2L", "NA_real_");
    assertEval("2L / (1:2)[3]", "NA_real_");
    assertEval("a <- (1:2)[3] ; b <- 2L ; a / b", "NA_real_");
    assertEval("a <- 2L ; b <- (1:2)[3] ; a / b", "NA_real_");
    assertEval("(1:2)[3] + 2L", "NA_integer_");
    assertEval("2L + (1:2)[3]", "NA_integer_");
    assertEval("a <- (1:2)[3] ; b <- 2L ; a + b", "NA_integer_");
    assertEval("a <- 2L ; b <- (1:2)[3] ; a + b", "NA_integer_");
    assertEval("a <- (1:2)[3] ; b <- 2 ; a + b", "NA_real_");
    assertEval("a <- 2 ; b <- (1:2)[3] ; a + b", "NA_real_");

    assertEval("f <- function(a, b) { a + b } ; f(c(1,2), c(3,4)) ; f(c(1,2), 3:4)", "c(4.0, 6.0)");
    assertEval("f <- function(a, b) { a + b } ; f(1:2, c(3,4)) ; f(c(1,2), 3:4)", "c(4.0, 6.0)");
    assertEval("f <- function(a, b) { a + b } ; f(1:2, 3:4) ; f(c(1,2), 3:4)", "c(4.0, 6.0)");

    assertEval("f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2L,4)", "0.5");
    assertEval("f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2L,4L)", "0.5");
    assertEval("f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2L,(1:2)[3])", "NA_real_");
    assertEval("f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f((1:2)[3], 2L)", "NA_real_");
    assertEval("f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f(2L,4)", "6.0");
    assertEval("f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f(2L,4L)", "6L");
    assertEval("f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f(2L,(1:2)[3])", "NA_integer_");
    assertEval("f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f((1:2)[3], 2L)", "NA_integer_");
    assertEval("f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2,(1:2)[3])", "NA_real_");
    assertEval("f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f((1:2)[3],2)", "NA");
    assertEval("f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2+1i,(1:2)[3])", "NA");
    assertEval("f <- function(a, b) { a + b } ; f(1,1) ; f(1,1+2i) ; f(TRUE, 2)", "3.0");

    assertEval("f <- function(b) { 1 / b } ; f(1) ; f(1L) ; f(4)", "0.25");
    assertEval("f <- function(b) { 1 / b } ; f(1+1i) ; f(1L)", "1.0");
    assertEval("f <- function(b) { 1 / b } ; f(1) ; f(1L)", "1.0");
    assertEval("f <- function(b) { 1 / b } ; f(1L) ; f(1)", "1.0");
    assertEval("f <- function(b) { 1 / b } ; f(TRUE) ; f(1L)", "1.0");
    assertEval("f <- function(b) { 1i / b } ; f(1) ; f(1L) ; f(4)", "0.0+0.25i");
    assertEval("f <- function(b) { 1i / b } ; f(1+1i) ; f(1L)", "0.0+1.0i");
    assertEval("f <- function(b) { 1i / b } ; f(1) ; f(1L)", "0.0+1.0i");
    assertEval("f <- function(b) { 1i / b } ; f(TRUE) ; f(1L)", "0.0+1.0i");
    assertEval("f <- function(b) { b / 1 } ; f(1) ; f(1L) ; f(4)", "4.0");
    assertEval("f <- function(b) { b / 2 } ; f(1+1i) ; f(1L)", "0.5");
    assertEval("f <- function(b) { b / 2 } ; f(1) ; f(1L)", "0.5");
    assertEval("f <- function(b) { b / 4 } ; f(1L) ; f(1)", "0.25");
    assertEval("f <- function(b) { b / 4i } ; f(1) ; f(1L)", "0.0-0.25i");
    assertEval("f <- function(b) { 4L / b } ; f(1L) ; f(2)", "2.0");
    assertEval("f <- function(b) { 4L + b } ; f(1L) ; f(2)", "6.0");
    assertEval("f <- function(b) { b / 2L } ; f(1L) ; f(2)", "1.0");
    assertEval("f <- function(b) { 4L / b } ; f(1L) ; f(2) ; f(TRUE)", "4.0");
    assertEval("f <- function(b) { 4L + b } ; f(1L) ; f(2) ; f(TRUE)", "5L");
    assertEval("f <- function(b) { 4L + b } ; f(1L) ; f(2) ; f((1:2)[3])", "NA");
    assertEval("f <- function(b) { 4L / b } ; f(1L) ; f(2) ; f((1:2)[3])", "NA");
    assertEval("f <- function(b) { (1:2)[3] + b } ; f(1L) ; f(2)", "NA");
    assertEval("f <- function(b) { (1:2)[3] + b } ; f(1) ; f(2L)", "NA");
    assertEval("f <- function(b) { b + 4L } ; f(1L) ; f(2) ; f(TRUE)", "5L");
    assertEval("f <- function(b) { b + 4L } ; f(1L) ; f(2) ; f((1:2)[3])", "NA");
    assertEval("f <- function(b) { b / 4L } ; f(1L) ; f(2) ; f(TRUE)", "0.25");
    assertEval("f <- function(b) { b / 4L } ; f(1L) ; f(2) ; f((1:2)[3])", "NA");
    assertEval("f <- function(b) { 1 + b } ; f(1L) ; f(TRUE)", "2.0");
    assertEval("f <- function(b) { FALSE + b } ; f(1L) ; f(2)", "2.0");
    assertEval("f <- function(b) { b + 1 } ; f(1L) ; f(TRUE)", "2.0");
    assertEval("f <- function(b) { b + FALSE } ; f(1L) ; f(2)", "2.0");
    assertEval("(0+2i)^0", "1.0+2.0i");
  }

  @Test
  public void complexDivisionByZero() {
    assertEval("((1+0i)/(0+0i)) ^ (-3)", "0.0+0.0i");
    assertEval("((1+1i)/(0+0i))", "complex(real=Inf,imaginary=NaN)");

    assertEval("((1+1i)/(0+0i)) ^ (-3)", "-0.0+-0.0i"); // NOTE: GNU-R prints negative zero as zero
    assertEval("round( ((1+1i)/(0+1i)) ^ (-3.54), digits=5)", "-0.27428+0.10364i");
  }

  @Test
  public void zeroDividedByZero() {
    assertEval("0/0 - 4i", "NA");
    assertEval("4i + 0/0", "NA");
    assertEval("a <- 1 + 2i; b <- 0/0 - 4i; a + b", "NA");
  }

  @Test
  public void testVectors()  {
    assertEval("x<-c(1,2,3);x", "1.0, 2.0, 3.0");
    assertEval("x<-c(1,2,3);x*2", "2.0, 4.0, 6.0");
    assertEval("x<-c(1,2,3);x+2", "3.0, 4.0, 5.0");
    assertEval("x<-c(1,2,3);x+FALSE", "1.0, 2.0, 3.0");
    assertEval("x<-c(1,2,3);x+TRUE", "2.0, 3.0, 4.0");
    assertEval("x<-c(1,2,3);x*x+x", "2.0, 6.0, 12.0");
    assertEval("x<-c(1,2);y<-c(3,4,5,6);x+y", "4.0, 6.0, 6.0, 8.0");
    assertEval("x<-c(1,2);y<-c(3,4,5,6);x*y", "3.0, 8.0, 5.0, 12.0");
    assertEval("x<-c(1,2);z<-c();x==z", "logical(0)");
    assertEval("x<-1+NA; c(1,2,3,4)+c(x,10)", "NA, 12.0, NA, 14.0");
    assertEval("c(1L,2L,3L)+TRUE", "2L, 3L, 4L");
    assertEval("c(1L,2L,3L)*c(10L)", "10L, 20L, 30L");
    assertEval("c(1L,2L,3L)*c(10,11,12)", "10.0, 22.0, 36.0");
    assertEval("c(1L,2L,3L,4L)-c(TRUE,FALSE)", "0L, 2L, 2L, 4L");
    assertEval("ia<-c(1L,2L);ib<-c(3L,4L);d<-c(5,6);ia+ib+d", "9.0, 12.0");
    assertEval("z <- c(-1.5-1i,10) ; (z * z)[1]", "1.25+3.0i");

    assertEval("c(1,2,3+1i)^3", "1.0+0.0i, 8.0+0.0i, 18.0+26.0i");
    assertEval("round( 3^c(1,2,3+1i), digits=5 )", "3.0+0.0i, 9.0+0.0i, 12.28048+24.04558i");

    assertEval("1L + 1:2", "2L, 3L");
    assertEval("4:3 + 2L", "6L, 5L");
    assertEval("1:2 + 3:4", "4L, 6L");
    assertEval("1:2 + c(1L, 2L)", "2L, 4L");
    assertEval("c(1L, 2L) + 1:4", "2L, 4L, 4L, 6L");
    assertEval("1:4 + c(1L, 2L)", "2L, 4L, 4L, 6L");
    assertEval("2L + 1:2", "3L, 4L");
    assertEval("1:2 + 2L", "3L, 4L");
    assertEval("c(1L, 2L) + 2L", "3L, 4L");
    assertEval("2L + c(1L, 2L)", "3L, 4L");
    assertEval("1 + 1:2", "2.0, 3.0");
    assertEval("c(1,2) + 1:2", "2.0, 4.0");
    assertEval("c(1,2,3,4) + 1:2", "2.0, 4.0, 4.0, 6.0");
    assertEval("c(1,2,3,4) + c(1L,2L)", "2.0, 4.0, 4.0, 6.0");
    assertEval("1:2 + 1", "2.0, 3.0");
    assertEval("1:2 + c(1,2)", "2.0, 4.0");
    assertEval("1:2 + c(1,2,3,4)", "2.0, 4.0, 4.0, 6.0");
    assertEval("c(1L,2L) + c(1,2,3,4)", "2.0, 4.0, 4.0, 6.0");
    assertEval("1L + c(1,2)", "2.0, 3.0");

    assertEval("a <- c(1,3) ; b <- c(2,4) ; a ^ b", "1.0, 81.0");
    assertEval("a <- c(1,3) ; a ^ 3", "1.0, 27.0");
    assertEval("a <- c(1+1i,3+2i) ; a - (4+3i)", "-3.0-2.0i, -1.0-1.0i");
    assertEval("c(1,3) - 4", "-3.0, -1.0");
    assertEval("c(1+1i,3+2i) * c(1,2)", "1.0+1.0i, 6.0+4.0i");
    assertEval("z <- c(1+1i,3+2i) ; z * c(1,2)", "1.0+1.0i, 6.0+4.0i");
    assertEval("round(c(1+1i,2+3i)^c(1+1i,3+4i), digits = 5)", "0.27396+0.5837i, -0.20455+0.89662i");
    assertEval("c(1+1i,3+2i) / 2", "0.5+0.5i, 1.5+1.0i");
    assertEval("c(1,3) / c(2,4)", "0.5, 0.75");
    assertEval("c(1,3) %/% c(2,4)", "0.0, 0.0");

    assertEval("integer()+1", "numeric(0)");
    assertEval("1+integer()", "numeric(0)");
    assertEvalWarning("1:2+1:3", "2L, 4L, 4L", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("1:3*1:2", "1L, 4L, 3L", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("1:3+c(1,2+2i)", "2.0+0.0i, 4.0+2.0i, 4.0+0.0i", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("c(1,2+2i)+1:3", "2.0+0.0i, 4.0+2.0i, 4.0+0.0i", "longer object length is not a multiple of shorter object length");

    assertEvalError("x <- 1:2 ; dim(x) <- 1:2 ; y <- 2:3 ; dim(y) <- 2:1 ; x + y", "non-conformable arrays");
    assertEvalError("x <- 1:2 ; dim(x) <- 1:2 ; y <- 2:3 ; dim(y) <- c(1,1,2) ; x + y", "non-conformable arrays");

    assertEval("NA+1:3", "NA, NA, NA");
    assertEval("1:3+NA", "NA, NA, NA");
    assertEval("NA+c(1L, 2L, 3L)", "NA, NA, NA");
    assertEval("c(1L, 2L, 3L)+NA", "NA, NA, NA");
    assertEval("c(NA,NA,NA)+1:3", "NA, NA, NA");
    assertEval("1:3+c(NA, NA, NA)", "NA, NA, NA");
    assertEval("c(NA,NA,NA)+c(1L,2L,3L)", "NA, NA, NA");
    assertEval("c(1L,2L,3L)+c(NA, NA, NA)", "NA, NA, NA");
    assertEval("c(NA,NA)+1:4", "NA, NA, NA, NA");
    assertEval("1:4+c(NA, NA)", "NA, NA, NA, NA");
    assertEval("c(NA,NA,NA,NA)+1:2", "NA, NA, NA, NA");
    assertEval("1:2+c(NA,NA,NA,NA)", "NA, NA, NA, NA");
    assertEval("c(NA,NA)+c(1L,2L,3L,4L)", "NA, NA, NA, NA");
    assertEval("c(1L,2L,3L,4L)+c(NA, NA)", "NA, NA, NA, NA");
    assertEval("c(NA,NA,NA,NA)+c(1L,2L)", "NA, NA, NA, NA");
    assertEval("c(1L,2L)+c(NA,NA,NA,NA)", "NA, NA, NA, NA");
    assertEval("c(1L,NA)+1", "2.0, NA");
    assertEval("c(1L,NA) + c(2,3)", "3.0, NA");
    assertEval("c(2,3) + c(1L,NA)", "3.0, NA");
    assertEval("1:4+c(1,2)", "2.0, 4.0, 4.0, 6.0");
    assertEval("c(1,2)+1:4", "2.0, 4.0, 4.0, 6.0");
    assertEval("1:4+c(1,2+2i)", "2.0+0.0i, 4.0+2.0i, 4.0+0.0i, 6.0+2.0i");
    assertEval("c(1,2+2i)+1:4", "2.0+0.0i, 4.0+2.0i, 4.0+0.0i, 6.0+2.0i");

    assertEval("c(3,4) %% 2", "1.0, 0.0");
    assertEval("c(3,4) %% c(2,5)", "1.0, 4.0");
    assertEval("c(3,4) %/% 2", "1.0, 2.0");
    assertEval("3L %/% 2L", "1L");
    assertEval("3L %/% 0L", "NA");

    assertEvalError("m <- matrix(nrow=2, ncol=2, 1:4) ; m + 1:16", "dims [product 4] do not match the length of object [16]");
  }

  @Test
  public void testUnary()  {
    assertEval("!TRUE", "FALSE");
    assertEval("!FALSE", "TRUE");
    assertEval("!NA", "NA");
    assertEval("!c(TRUE,TRUE,FALSE,NA)", "FALSE, FALSE, TRUE, NA");
    assertEval("!c(1,2,3,4,0,0,NA)", "FALSE, FALSE, FALSE, FALSE, TRUE, TRUE, NA");
    assertEval("!((0-3):3)", "FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE");
    assertEval("f <- function(arg) { !arg } ; f(as.raw(10)) ; f(as.raw(1:3))", "fe, fd, fc");
    assertEval("a <- as.raw(201) ; !a", "36");
    assertEval("a <- as.raw(12) ; !a", "f3");
    assertEval("l <- list(); !l", "logical(0)");
    assertEvalError("l <- c(\"hello\", \"hi\") ; !l", "invalid argument type");
    assertEvalError("l <- function(){1} ; !l", "invalid argument type");
    assertEval("f <- function(arg) { !arg } ; f(as.raw(10)) ; f(as.raw(c(a=1,b=2)))", "fe, fd");
    assertEval("f <- function(arg) { !arg } ; f(as.raw(10)) ; f(matrix(as.raw(1:4),nrow=2 ))", "     [,1] [,2]\n[1,]   fe   fc\n[2,]   fd   fb");
    assertEval("f <- function(arg) { !arg } ; f(as.raw(10)) ; x <- as.raw(10:11) ; attr(x, \"my\") <- 1 ; f(x)", "f5, f4");

    assertEval("-(0/0)", "NaN");
    assertEval("-(1/0)", "-Infinity");
    assertEval("-(1[2])", "NA");
    assertEval("-(2+1i)", "-2.0-1.0i");
    assertEval("-((0+1i)/0)", "NaN-Infinityi"); // not the same formatting as GNU-R
    assertEval("-((1+0i)/0)", "-Infinity+NaNi");
    assertEval("-c((1+0i)/0,2)", "-Infinity+NaNi, -2.0+-0.0i"); // not the same formatting as GNU-R, which would print negative zero as zero

    assertEval("f <- function(z) { -z } ; f(1+1i) ; f(1L)", "-1L");
    assertEval("f <- function(z) { -z } ; f(TRUE) ; f(1L)", "-1L");
    assertEval("f <- function(z) { -z } ; f(1L) ; f(1)", "-1.0");
    assertEval("f <- function(z) { -z } ; f(1) ; f(1L)", "-1L");
    assertEval("f <- function(z) { -z } ; f(1L) ; f(1+1i)", "-1.0-1.0i");
    assertEval("f <- function(z) { -z } ; f(1L) ; f(TRUE)", "-1L");
    assertEval("f <- function(z) { -z } ; f(1:3) ; f(1L)", "-1L");
    assertEval("f <- function(z) { -z } ; f(1:3) ; f(TRUE)", "-1L");
    assertEval("f <- function(z) { -z } ; f(1:3) ; f(c((0+0i)/0,1+1i))", "NaN+NaNi, -1.0-1.0i");

//        assertEval("z <- logical() ; -z", "integer(0)");
//        assertEval("z <- integer() ; -z", "integer(0)");
//        assertEval("z <- double() ; -z", "numeric(0)");
//        assertEval("z <- (1+1i)[0] ; -z", "complex(0)");

    assertEvalError("z <- \"hello\" ; -z", "invalid argument to unary operator");
    assertEvalError("z <- c(\"hello\",\"hi\") ; -z", "invalid argument to unary operator");
    assertEvalError("f <- function(z) { -z } ; f(1:3) ; f(\"hello\")", "invalid argument to unary operator");
  }

  @Test
  public void testMatrices()  {
    assertEval("m <- matrix(1:6, nrow=2, ncol=3, byrow=TRUE) ; m+1L", "     [,1] [,2] [,3]\n[1,]   2L   3L   4L\n[2,]   5L   6L   7L");
    assertEval("m <- matrix(1:6, nrow=2, ncol=3, byrow=TRUE) ; m-1",  "     [,1] [,2] [,3]\n[1,]  0.0  1.0  2.0\n[2,]  3.0  4.0  5.0");
    assertEval("m <- matrix(1:6, nrow=2, ncol=3, byrow=TRUE) ; m+m", "     [,1] [,2] [,3]\n[1,]   2L   4L   6L\n[2,]   8L  10L  12L");
    assertEval("z<-matrix(12)+1 ; z", "     [,1]\n[1,] 13.0");

    // matrix product
    assertEval("x <- 1:3 %*% 9:11 ; x[1]", "62.0");
    assertEval("m<-matrix(1:3, nrow=1) ; 1:2 %*% m", "     [,1] [,2] [,3]\n[1,]  1.0  2.0  3.0\n[2,]  2.0  4.0  6.0");
    assertEval("m<-matrix(1:6, nrow=2) ; 1:2 %*% m", "     [,1] [,2] [,3]\n[1,]  5.0 11.0 17.0");
    assertEval("m<-matrix(1:6, nrow=2) ; m %*% 1:3", "     [,1]\n[1,] 22.0\n[2,] 28.0");
    assertEval("m<-matrix(1:3, ncol=1) ; m %*% 1:2", "     [,1] [,2]\n[1,]  1.0  2.0\n[2,]  2.0  4.0\n[3,]  3.0  6.0");
    assertEval("a<-matrix(1:6, ncol=2) ; b<-matrix(11:16, nrow=2) ; a %*% b", "      [,1]  [,2]  [,3]\n[1,]  59.0  69.0  79.0\n[2,]  82.0  96.0 110.0\n[3,] 105.0 123.0 141.0");
    assertEval("a <- array(1:9, dim=c(3,1,3)) ;  a %*% 1:9", "      [,1]\n[1,] 285.0");
    assertEvalError("matrix(2,nrow=2,ncol=3) %*% matrix(4,nrow=1,ncol=5)", "non-conformable arguments");
    assertEvalError("1:3 %*% matrix(4,nrow=2,ncol=5)", "non-conformable arguments");
    assertEvalError("matrix(4,nrow=2,ncol=5) %*% 1:4", "non-conformable arguments");
    assertEval("double() %*% double()", "     [,1]\n[1,]  0.0");
    assertEval("m <- double() ; dim(m) <- c(0,4) ; m %*% t(m)", "<0 x 0 matrix>");
    assertEval("m <- double() ; dim(m) <- c(0,4) ; t(m) %*% m", "     [,1] [,2] [,3] [,4]\n[1,]  0.0  0.0  0.0  0.0\n[2,]  0.0  0.0  0.0  0.0\n[3,]  0.0  0.0  0.0  0.0\n[4,]  0.0  0.0  0.0  0.0");
    assertEval("m <- matrix(c(1,2,3,0/0), nrow=4) ; m %*% 1:4", "     [,1] [,2] [,3] [,4]\n[1,]  1.0  2.0  3.0  4.0\n[2,]  2.0  4.0  6.0  8.0\n[3,]  3.0  6.0  9.0 12.0\n[4,]  NaN  NaN  NaN  NaN");
    assertEval("m <- matrix(c(NA,1,0/0,2), nrow=2) ; 1:2 %*% m", "     [,1] [,2]\n[1,]   NA  NaN");
    assertEval("m <- double() ; dim(m) <- c(0,0) ; m %*% m", "<0 x 0 matrix>");
    assertEval("m <- matrix(c(NA,1,4,2), nrow=2) ; t(m) %*% m", "     [,1] [,2]\n[1,]   NA   NA\n[2,]   NA 20.0");
    assertEval("matrix(c(3,1,0/0,2), nrow=2) %*% matrix(1:6,nrow=2)", "     [,1] [,2] [,3]\n[1,]  NaN  NaN  NaN\n[2,]  5.0 11.0 17.0");
    assertEvalError("as.raw(1:3) %*% 1:3", "requires numeric/complex matrix/vector arguments");

    // outer product
    assertEval("1:3 %o% 1:2", "     [,1] [,2]\n[1,]  1.0  2.0\n[2,]  2.0  4.0\n[3,]  3.0  6.0");
    assertEvalError("1:4 %*% 1:3", "non-conformable arguments");
    assertEvalError("1:3 %*% as.raw(c(1,2,3))", "requires numeric/complex matrix/vector arguments");
    assertEval("1:3 %*% c(TRUE,FALSE,TRUE)", "     [,1]\n[1,]  4.0");
    assertEvalError("as.raw(1:3) %o% 1:3", "requires numeric/complex matrix/vector arguments");

    // precedence
    assertEval("10 / 1:3 %*% 3:1", "     [,1]\n[1,]  1.0");

    assertEval("x <- 1:2 ; dim(x) <- c(1,1,2) ; y <- 2:3 ; dim(y) <- c(1,1,2) ; x + y", ", , 1\n\n     [,1]\n[1,]   3L\n\n, , 2\n\n     [,1]\n[1,]   5L");
  }

  @Test
  public void testNonvectorizedLogical() {
    assertEval("1.1 || 3.15", "TRUE");
    assertEval("0 || 0", "FALSE");
    assertEval("1 || 0", "TRUE");
    assertEval("NA || 1", "TRUE");
    assertEval("NA || 0", "FALSE");
    assertEval("0 || NA", "NA");
    assertEval("x <- 1 ; f <- function(r) { x <<- 2; r } ; NA || f(NA) ; x", "2.0");
    assertEval("x <- 1 ; f <- function(r) { x <<- 2; r } ; TRUE || f(FALSE) ; x } ", "1.0");

    assertEval("TRUE && FALSE", "FALSE");
    assertEval("FALSE && FALSE", "FALSE");
    assertEval("FALSE && TRUE", "FALSE");
    assertEval("TRUE && TRUE", "TRUE");
    assertEval("TRUE && NA", "NA");
    assertEval("FALSE && NA", "FALSE");
    assertEval("NA && TRUE", "NA");
    assertEval("NA && FALSE", "FALSE");
    assertEval("NA && NA", "NA");
    assertEval("x <- 1 ; f <- function(r) { x <<- 2; r } ; NA && f(NA) ; x } ", "2.0");
    assertEval("x <- 1 ; f <- function(r) { x <<- 2; r } ; FALSE && f(FALSE) ; x } ", "1.0");

    assertEval("f <- function(a,b) { a || b } ; f(1,2) ; f(1,2) ; f(1L,2L)", "TRUE");
    assertEval("f <- function(a,b) { a || b } ; f(1L,2L) ; f(1L,2L) ; f(0,FALSE)", "FALSE");
    assertEval("f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), TRUE)", "TRUE");
    assertEval("f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical())", "NA");
    assertEval("f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(1,2)", "TRUE");
    assertEval("f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(double(),2)", "NA");
    assertEval("f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(integer(),2)", "NA");
    assertEval("f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(2+3i,1/0)", "TRUE");
    assertEval("f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(2+3i,logical())", "NA");
    assertEval("f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(1,2) ; f(logical(),4)", "NA");
    assertEval("f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(TRUE, c(TRUE,TRUE,FALSE)) ; f(1,2)", "TRUE");

    assertEvalError("\"hello\" || TRUE", "invalid 'x' type in 'x || y'");
    assertEval("FALSE && \"hello\"", "FALSE");
    assertEval("TRUE || \"hello\"", "TRUE");
    assertEvalError("FALSE || \"hello\"", "invalid 'y' type in 'x || y'");
    assertEvalError("as.raw(10) && \"hi\"", "invalid 'x' type in 'x && y'");
    assertEval("c(TRUE,FALSE) | logical()", "logical(0)");
    assertEval("logical() | c(TRUE,FALSE)", "logical(0)");
    assertEval("as.raw(c(1,4)) | raw()", "raw(0)");
    assertEval("raw() | as.raw(c(1,4)", "raw(0)");
    assertEvalWarning("as.raw(c(1,4)) | as.raw(c(1,5,4))", "01, 05, 05", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("as.raw(c(1,5,4)) | as.raw(c(1,4))", "01, 05, 05", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("c(TRUE, FALSE, FALSE) & c(TRUE,TRUE)", "TRUE, FALSE, FALSE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("c(TRUE, TRUE) & c(TRUE, FALSE, FALSE)", "TRUE, FALSE, FALSE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("c(a=TRUE, TRUE) | c(TRUE, b=FALSE, FALSE)", "        b     \nTRUE TRUE TRUE", "longer object length is not a multiple of shorter object length");
  }

  @Test
  public void testVectorizedLogical() {
    assertEval("1.1 | 3.15", "TRUE");
    assertEval("0 | 0", "FALSE");
    assertEval("1 | 0", "TRUE");
    assertEval("NA | 1", "TRUE");
    assertEval("NA | 0", "NA");
    assertEval("0 | NA", "NA");
    assertEval("x <- 1 ; f <- function(r) { x <<- 2; r } ; NA | f(NA) ; x", "2.0");
    assertEval("x <- 1 ; f <- function(r) { x <<- 2; r } ; TRUE | f(FALSE) ; x", "2.0");

    assertEval("TRUE & FALSE", "FALSE");
    assertEval("FALSE & FALSE", "FALSE");
    assertEval("FALSE & TRUE", "FALSE");
    assertEval("TRUE & TRUE", "TRUE");
    assertEval("TRUE & NA", "NA");
    assertEval("FALSE & NA", "FALSE");
    assertEval("NA & TRUE", "NA");
    assertEval("NA & FALSE", "FALSE");
    assertEval("NA & NA", "NA");
    assertEval("x <- 1 ; f <- function(r) { x <<- 2; r } ; NA & f(NA) ; x", "2.0");
    assertEval("x <- 1 ; f <- function(r) { x <<- 2; r } ; FALSE & f(FALSE) ; x", "2.0");

    assertEval("1:4 & c(FALSE,TRUE)", "FALSE, TRUE, FALSE, TRUE");

    assertEval("a <- as.raw(200) ; b <- as.raw(255) ; a | b", "ff");
    assertEval("a <- as.raw(200) ; b <- as.raw(1) ; a | b", "c9");
    assertEval("a <- as.raw(201) ; b <- as.raw(1) ; a & b", "01");

    assertEval("1+2i | 0", "TRUE");
    assertEval("1+2i & 0", "FALSE");
    assertEvalError("TRUE | \"hello\"", "operations are possible only for numeric, logical or complex types");
    assertEval("f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(FALSE, FALSE)", "FALSE");
    assertEval("f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(as.raw(10), as.raw(11))", "0a");
    assertEvalError("f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(as.raw(10), 12)", "operations are possible only for numeric, logical or complex types");
    assertEvalError("f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(FALSE, as.raw(10))", "operations are possible only for numeric, logical or complex types");
    assertEval("f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, 0L)", "FALSE");
    assertEval("f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, 0)", "FALSE");
    assertEval("f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, TRUE)", "TRUE");
    assertEval("f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, 3+4i)", "TRUE");
    assertEval("f <- function(a,b) { a & b } ; f(TRUE, FALSE) ; f(1L, 3+4i)", "TRUE");
    assertEval("f <- function(a,b) { a & b } ; f(TRUE, FALSE) ; f(TRUE, 3+4i)", "TRUE");
    assertEval("f <- function(a,b) { a | b } ; f(c(TRUE, FALSE), FALSE) ; f(1L, 3+4i)", "TRUE");
    assertEval("f <- function(a,b) { a | b } ; f(c(TRUE, FALSE), FALSE) ; f(c(FALSE,FALSE), 3+4i)", "TRUE, TRUE");
    assertEval("f <- function(a,b) { a | b } ; f(as.raw(c(1,4)), as.raw(3)) ; f(4, FALSE)", "TRUE");
    assertEvalError("f <- function(a,b) { a | b } ; f(as.raw(c(1,4)), as.raw(3)) ; f(as.raw(4), FALSE)", "operations are possible only for numeric, logical or complex types");
    assertEvalError("f <- function(a,b) { a | b } ; f(as.raw(c(1,4)), as.raw(3)) ; f(FALSE, as.raw(4))", "operations are possible only for numeric, logical or complex types");
    assertEvalError("f <- function(a,b) { a | b } ; f(as.raw(c(1,4)), 3)", "operations are possible only for numeric, logical or complex types");
    assertEvalError("f <- function(a,b) { a | b } ; f(3, as.raw(c(1,4)))", "operations are possible only for numeric, logical or complex types");

  }

  @Test
  public void testIntegerOverflow()  {
    assertEvalWarning("x <- 2147483647L ; x + 1L", "NA", "NAs produced by integer overflow");
    assertEvalWarning("x <- 2147483647L ; x * x", "NA", "NAs produced by integer overflow");
    assertEvalWarning("x <- -2147483647L ; x - 2L", "NA", "NAs produced by integer overflow");
    assertEvalWarning("x <- -2147483647L ; x - 1L", "NA", "NAs produced by integer overflow");
    assertEvalNoWarnings("3L %/% 0L", "NA");
    assertEvalNoWarnings("3L %% 0L", "NA");
    assertEvalNoWarnings("c(3L,3L) %/% 0L", "NA, NA");
    assertEvalNoWarnings("c(3L,3L) %% 0L", "NA, NA");
    assertEvalWarning("2147483647L + 1:3", "NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("2147483647L + c(1L,2L,3L)", "NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("1:3 + 2147483647L", "NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("c(1L,2L,3L) + 2147483647L", "NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("1:3 + c(2147483647L,2147483647L,2147483647L)", "NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("c(2147483647L,2147483647L,2147483647L) + 1:3", "NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("c(1L,2L,3L) + c(2147483647L,2147483647L,2147483647L)", "NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("c(2147483647L,2147483647L,2147483647L) + c(1L,2L,3L)", "NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("1:4 + c(2147483647L,2147483647L)", "NA, NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("c(2147483647L,2147483647L) + 1:4", "NA, NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("c(1L,2L,3L,4L) + c(2147483647L,2147483647L)", "NA, NA, NA, NA", "NAs produced by integer overflow");
    assertEvalWarning("c(2147483647L,2147483647L) + c(1L,2L,3L,4L)", "NA, NA, NA, NA", "NAs produced by integer overflow");

  }

  @Test
  public void testArithmeticUpdate()  {
    assertEval("x <- 3 ; f <- function(z) { if (z) { x <- 1 } ; x <- x + 1L ; x } ; f(FALSE)", "4.0");
    assertEval("x <- 3 ; f <- function(z) { if (z) { x <- 1 } ; x <- 1L + x ; x } ; f(FALSE)", "4.0");
    assertEval("x <- 3 ; f <- function(z) { if (z) { x <- 1 } ; x <- x - 1L ; x } ; f(FALSE)", "2.0");
  }
}
