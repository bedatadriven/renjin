package org.renjin.simple;

import org.junit.Test;

public class ComparisonTest extends SimpleTestBase {

  @Test
  public void testScalars()  {
    assertEval("{ 1==1 }", "TRUE");
    assertEval("{ 2==1 }", "FALSE");
    assertEval("{ 1L<=1 }", "TRUE");
    assertEval("{ 1<=0L }", "FALSE");
    assertEval("{ x<-2; f<-function(z=x) { if (z<=x) {z} else {x} } ; f(1.4)}", "1.4");
    assertEval("{ 1==NULL }", "logical(0)");
    assertEval("{ 1L==1 }", "TRUE");
    assertEval("{ TRUE==1 }", "TRUE");
    assertEval("{ TRUE==1L }", "TRUE");
    assertEval("{ 2L==TRUE }", "FALSE");
    assertEval("{ TRUE==FALSE }", "FALSE");
    assertEval("{ FALSE<=TRUE }", "TRUE");
    assertEval("{ FALSE<TRUE }", "TRUE");
    assertEval("{ TRUE>FALSE }", "TRUE");
    assertEval("{ TRUE>=FALSE }", "TRUE");
    assertEval("{ TRUE!=FALSE }", "TRUE");
    assertEval("{ 2L==NA }", "NA");
    assertEval("{ NA==2L }", "NA");
    assertEval("{ 2L==as.double(NA) }", "NA");
    assertEval("{ as.double(NA)==2L }", "NA");

    assertEval("{ 1+1i == 1-1i }", "FALSE");
    assertEval("{ 1+1i == 1+1i }", "TRUE");
    assertEval("{ 1+1i == 2+1i }", "FALSE");
    assertEval("{ 1+1i != 1+1i }", "FALSE");
    assertEval("{ 1+1i != 1-1i }", "TRUE");
    assertEval("{ 1+1i != 2+1i }", "TRUE");

    assertEval("'hello' < 'hi'", "TRUE");
    assertEval("'hello' > 'hi'", "FALSE");
    assertEval("'hi' <= 'hello'", "FALSE");
    assertEval("'hi' >= 'hello'", "TRUE");
    assertEval("'hi' < 'hello'", "FALSE");
    assertEval("'hi' > 'hello'", "TRUE");
    assertEval("'hi' == 'hello'", "FALSE");
    assertEval("'hi' != 'hello'", "TRUE");
    assertEval("'hello' <= 'hi'", "TRUE");
    assertEval("'hello' >= 'hi'", "FALSE");
    assertEval("'hello' < 'hi'", "TRUE");
    assertEval("'hello' > 'hi'", "FALSE");
    assertEval("'hello' == 'hello'", "TRUE");
    assertEval("'hello' != 'hello'", "FALSE");
    assertEval("{ 'a' <= 'b' }", "TRUE");
    assertEval("{ 'a' > 'b' }", "FALSE");
    assertEval("{ '2.0' == 2 }", "FALSE");

    assertEval("{ as.raw(15) > as.raw(10) }", "TRUE");
    assertEval("{ as.raw(15) < as.raw(10) }", "FALSE");
    assertEval("{ as.raw(15) >= as.raw(10) }", "TRUE");
    assertEval("{ as.raw(15) <= as.raw(10) }", "FALSE");
    assertEval("{ as.raw(10) >= as.raw(15) }", "FALSE");
    assertEval("{ as.raw(10) <= as.raw(15) }", "TRUE");
    assertEval("{ as.raw(15) == as.raw(10) }", "FALSE");
    assertEval("{ as.raw(15) != as.raw(10) }", "TRUE");
    assertEval("{ as.raw(15) == as.raw(15) }", "TRUE");
    assertEval("{ as.raw(15) != as.raw(15) }", "FALSE");
    assertEval("{ a <- as.raw(1) ; b <- as.raw(2) ; a < b }", "TRUE");
    assertEval("{ a <- as.raw(1) ; b <- as.raw(2) ; a > b }", "FALSE");
    assertEval("{ a <- as.raw(1) ; b <- as.raw(2) ; a == b }", "FALSE");
    assertEval("{ a <- as.raw(1) ; b <- as.raw(200) ; a < b }", "TRUE");
    assertEval("{ a <- as.raw(200) ; b <- as.raw(255) ; a < b }", "TRUE");

    assertEval("{ a <- 1 ; b <- a[2] ; a == b }", "NA");
    assertEval("{ a <- 1 ; b <- a[2] ; b > a }", "NA");
    assertEval("{ a <- 1L ; b <- a[2] ; a == b }", "NA");
    assertEval("{ a <- 1L ; b <- a[2] ; b > a }", "NA");
    assertEval("{ a <- 1L ; b <- 1[2] ; a == b }", "NA");
    assertEval("{ a <- 1L[2] ; b <- 1 ; a == b }", "NA");
    assertEval("{ a <- 1L[2] ; b <- 1 ; b > a }", "NA");
    assertEval("{ a <- 1 ; b <- 1L[2] ; a == b }", "NA");
    assertEval("{ a <- 1[2] ; b <- 1L ; b > a }", "NA");
    assertEval("{ a <- 1L ; b <- TRUE[2] ; a == b }", "NA");
    assertEval("{ a <- 1L[2] ; b <- TRUE ; a != b }", "NA");
    assertEval("{ a <- TRUE ; b <- 1L[2] ; a > b }", "NA");
    assertEval("{ a <- TRUE[2] ; b <- 1L ; a == b }", "NA");

    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1,2L) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(1L,2L) ; f(1,2) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(1L,2L) ; f(1L,2) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(1L,2) ; f(1,2) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(1L,2) ; f(1L,2L) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2L) ; f(1,2) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2L) ; f(1L,2L) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(TRUE,FALSE) ; f(TRUE,2) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(TRUE,FALSE) ; f(1L,2L) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(0L,TRUE) ; f(FALSE,2) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(0L,TRUE) ; f(0L,2L) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(0L,TRUE) ; f(2L,TRUE) }", "TRUE");
    assertEval("{ f <- function(a,b) { a > b } ; f(TRUE,2L) ; f(FALSE,2) }", "FALSE");
    assertEval("{ f <- function(a,b) { a > b } ; f(TRUE,2L) ; f(0L,2L) }", "FALSE");

    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f('hello', 'hi'[2]) }", "NA");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f('hello'[2], 'hi') }", "NA");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2, 1L[2]) }", "NA");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2[2], 1L) }", "NA");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2, 1[2]) }", "NA");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2[2], 1) }", "NA");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L, 1[2]) }", "NA");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L[2], 1) }", "NA");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L, 1L[2]) }", "NA");
    assertEval("{ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L[2], 1L) }", "NA");

    assertEval("{ z <- TRUE; dim(z) <- c(1) ; dim(z == TRUE) }", "1L");
    assertEvalError("{ z <- TRUE; dim(z) <- c(1) ; u <- 1:3 ; dim(u) <- 3 ; u == z }", "non-conformable arrays");

  }

  @Test
  public void testVectors()  {
    assertEval("{ x<-c(1,2,3,4);y<-c(10,2); x<=y }", "c(TRUE, TRUE, TRUE, FALSE)");
    assertEval("{ x<-c(1,2,3,4);y<-2.5; x<=y }", "c(TRUE, TRUE, FALSE, FALSE)");
    assertEval("{ x<-c(1,2,3,4);y<-c(2.5+NA,2.5); x<=y }", "c(NA, TRUE, NA, FALSE)");
    assertEval("{ x<-c(1L,2L,3L,4L);y<-c(2.5+NA,2.5); x<=y }", "c(NA, TRUE, NA, FALSE)");
    assertEval("{ x<-c(1L,2L,3L,4L);y<-c(TRUE,FALSE); x<=y }", "c(TRUE, FALSE, FALSE, FALSE)");
    assertEval("{ x<-c(1L,2L,3L,4L);y<-1.5; x<=y }", "c(TRUE, FALSE, FALSE, FALSE)");
    assertEval("{ c(1:3,4,5)==1:5 }", "c(TRUE, TRUE, TRUE, TRUE, TRUE)");
    assertEval("{ 3 != 1:2 }", "c(TRUE, TRUE)");
    assertEval("{ b <- 1:3 ; z <- FALSE ; b[2==2] }", "c(1L, 2L, 3L)");

    assertEval("{ 1:3 == TRUE }", "c(TRUE, FALSE, FALSE)");
    assertEval("{ TRUE == 1:3 }", "c(TRUE, FALSE, FALSE)");

    assertEvalError("{ m <- matrix(nrow=2, ncol=2, 1:4) ; m == 1:16 }", "dims [product 4] do not match the length of object [16]");

    assertEvalWarning("{ c(1,2) < c(2,1,4) }", "TRUE, FALSE, TRUE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ c(2,1,4) < c(1,2) }", "FALSE, TRUE, FALSE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ c(1L,2L) < c(2L,1L,4L) }", "TRUE, FALSE, TRUE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ c(2L,1L,4L) < c(1L,2L) }", "FALSE, TRUE, FALSE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ c(TRUE,FALSE,FALSE) < c(TRUE,TRUE) }", "FALSE, TRUE, TRUE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ c(TRUE,TRUE) == c(TRUE,FALSE,FALSE) }", "TRUE, FALSE, FALSE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ as.raw(c(1,2)) < as.raw(c(2,1,4)) }", "TRUE, FALSE, TRUE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ as.raw(c(2,1,4)) < as.raw(c(1,2)) }", "FALSE, TRUE, FALSE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ c('hi','hello','bye') > c('cau', 'ahoj') }", "TRUE, TRUE, FALSE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ c('cau', 'ahoj') != c('hi','hello','bye') }", "TRUE, TRUE, TRUE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ c(1+1i,2+2i) == c(2+1i,1+2i,1+1i) }", "FALSE, FALSE, TRUE", "longer object length is not a multiple of shorter object length");
    assertEvalWarning("{ c(2+1i,1+2i,1+1i) == c(1+1i, 2+2i) }", "FALSE, FALSE, TRUE", "longer object length is not a multiple of shorter object length");

    assertEval("{ as.raw(c(2,1,4)) < raw() }", "logical(0)");
    assertEval("{ raw() < as.raw(c(2,1,4)) }", "logical(0)");
    assertEval("{ 1:3 < integer() }", "logical(0)");
    assertEval("{ integer() < 1:3 }", "logical(0)");
    assertEval("{ c(1,2,3) < double() }", "logical(0)");
    assertEval("{ double() == c(1,2,3) }", "logical(0)");
    assertEval("{ c(TRUE,FALSE) < logical() }", "logical(0)");
    assertEval("{ logical() == c(FALSE, FALSE) }", "logical(0)");
    assertEval("{ c(1+2i, 3+4i) == (1+2i)[0] }", "logical(0)");
    assertEval("{ (1+2i)[0] == c(2+3i, 4+1i) }", "logical(0)");
    assertEval("{ c('hello', 'hi') == character() }", "logical(0)");
    assertEval("{ character() > c('hello', 'hi') }", "logical(0)");

    assertEval("{ c(1,2,3,4) != c(1,NA) }", "FALSE, NA, TRUE, NA");
    assertEval("{ c(1,2,NA,4) != 2 }", "TRUE, FALSE, NA, TRUE");
    assertEval("{ 2 != c(1,2,NA,4) }", "TRUE, FALSE, NA, TRUE");
    assertEval("{ c(1,2,NA,4) == 2 }", "FALSE, TRUE, NA, FALSE");
    assertEval("{ 2 == c(1,2,NA,4) }", "FALSE, TRUE, NA, FALSE");
    assertEval("{ c('hello', NA) < c('hi', NA) }", "TRUE, NA");
    assertEval("{ c('hello', NA) >= 'hi' }", "FALSE, NA");
    assertEval("{ 'hi' > c('hello', NA)  }", "TRUE, NA");
    assertEval("{ c('hello', NA) > c(NA, 'hi') }", "NA, NA");
    assertEval("{ c(1L, NA) > c(NA, 2L) }", "NA, NA");
    assertEval("{ c(TRUE, NA) > c(NA, FALSE) }", "NA, NA");
    assertEval("{ 'hi' > c('hello', 'hi')  }", "TRUE, FALSE");
    assertEval("{ NA > c('hello', 'hi') }", "NA, NA");
    assertEval("{ c('hello', 'hi') < NA }", "NA, NA");
    assertEval("{ 1:3 < NA }", "NA, NA, NA");
    assertEval("{ NA > 1:3 }", "NA, NA, NA");
    assertEval("{ 2L > c(1L,NA,2L) }", "TRUE, NA, FALSE");
    assertEval("{ c(1L,NA,2L) < 2L }", "TRUE, NA, FALSE");
    assertEval("{ c(0/0+1i,2+1i) == c(1+1i,2+1i) }", "NA, TRUE");
    assertEval("{ c(1+1i,2+1i) == c(0/0+1i,2+1i) }", "NA, TRUE");

    assertEval("{ integer() == 2L }", "logical(0)");
  }

  @Test
  public void complexInequalities() {
    assertEvalError("{ 1+1i > 2+2i }", "invalid comparison with complex values");
    assertEvalError("{ 1+1i < 2+2i }", "invalid comparison with complex values");
    assertEvalError("{ 1+1i >= 2+2i }", "invalid comparison with complex values");
    assertEvalError("{ 1+1i <= 2+2i }", "invalid comparison with complex values");
  }

  @Test
  public void nanInEquality() {
    assertEval("{ 0/0 < c(1,2,3,4) }", "c(NA, NA, NA, NA)");
    assertEval("{ 0/0 == c(1,2,3,4) }", "c(NA, NA, NA, NA)");
  }

  @Test
  public void testMatrices()  {
    assertEval("{ matrix(1) > matrix(2) }", "      [,1]\n[1,] FALSE");
    assertEval("{ matrix(1) > NA }", "     [,1]\n[1,]   NA");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m > c(1,2,3) }", "      [,1]  [,2] [,3]\n[1,] FALSE FALSE TRUE\n[2,] FALSE  TRUE TRUE");
  }

}
