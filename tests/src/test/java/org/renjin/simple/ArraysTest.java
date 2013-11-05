package org.renjin.simple;

import org.junit.Test;


public class ArraysTest extends SimpleTestBase {

  @Test
  public void testArrayBuiltin() {
    // array with no arguments produces array of length 1
    assertTrue("{ a = array(); length(a) == 1; }");

    // empty arg has first element NA
    assertTrue("{ a = array(); is.na(a[1]); }");

    // dimnames not implemented yet
    // dimension names of empty array are null
    // assertTrue("{ a = array(); is.null(dimnames(a)); }");

    // empty array has single dimension that is 1
    assertTrue("{ a <- array(); dim(a) == 1; }");

    // wrapping in arrays work even when prohibited by help
    assertTrue("{ a = array(1:10, dim = c(2,6)); length(a) == 12; }");

    // negative length vectors are not allowed is the error reported by gnu-r
    // negative dims not allowed by R, special GNU message
    assertEvalError("{ array(dim=c(-2,2)); }", "RError.DIMS_CONTAIN_NEGATIVE_VALUES");

    // negative dims not allowed
    assertEvalError("{ array(dim=c(-2,-2)); }", "RError.DIMS_CONTAIN_NEGATIVE_VALUES");

    // zero dimension array has length 0
    assertTrue("{ length(array(dim=c(1,0,2,3))) == 0; }");

    // double dimensions work and are rounded down always
    assertTrue("{ a = dim(array(dim=c(2.1,2.9,3.1,4.7))); a[1] == 2 && a[2] == 2 && a[3] == 3 && a[4] == 4; }");
  }

  @Test
  public void testMatrixBuiltin() {
    // empty matrix length is 1
    assertTrue("{ length(matrix()) == 1; }");
  }


  @Test
  public void testArraySimpleRead() {
    // simple read
    assertTrue("{ a = array(1:27,c(3,3,3)); a[1,1,1] == 1 && a[3,3,3] == 27 && a[1,2,3] == 22 && a[3,2,1] == 6; }");

    // empty selectors reads the whole array
    assertTrue("{ a = array(1:27, c(3,3,3)); b = a[,,]; d = dim(b); d[1] == 3 && d[2] == 3 && d[3] == 3; }");

    // dimensions of 1 are dropped
    assertTrue("{ a = array(1,c(3,3,3)); a = dim(a[,1,]); length(a) == 2 && a[1] == 3 && a[2] == 3; }");

    // when all dimensions are dropped, dim is null
    assertTrue("{ a = array(1,c(3,3,3)); is.null(dim(a[1,1,1])); }");

    // last dimension is dropped
    assertTrue("{ a = array(1,c(3,3,3)); is.null(dim(a[1,1,])); } ");

    // dimensions of 1 are not dropped when requested (with subset)
    assertTrue("{ a = array(1,c(3,3,3)); a = dim(a[1,1,1, drop = FALSE]); length(a) == 3 && a[1] == 1 && a[2] == 1 && a[3] == 1; }");

    // with subscript, dimensions are always dropped
    assertTrue("{ m <- array(1:4, dim=c(4,1,1)) ; x <- m[[2,1,1,drop=FALSE]] ; is.null(dim(x)) }");

    // fallback to one dimensional read
    assertTrue("{ a = array(1:27, c(3,3,3)); a[1] == 1 && a[27] == 27 && a[22] == 22 && a[6] == 6; }");

    // error when different dimensions given
    assertEvalError("{ a = array(1,c(3,3,3)); a[2,2]; }", "RError.INCORRECT_DIMENSIONS");

    // calculating result dimensions
    assertTrue("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,1] ; x[1] == 1 && x[2] == 2 }");
    assertTrue("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- dim(m[1:2,1,1]) ; is.null(x) }");
    assertTrue("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- dim(m[1:2,1,1,drop=FALSE]) ; x[1] == 2 && x[2] == 1 && x[3] == 1 }");
    assertTrue("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,integer()] ; d <- dim(x) ; length(x) == 0 }");
    assertTrue("{ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,integer()] ; d <- dim(x) ; d[1] == 2 && d[2] == 0 }");
  }

  @Test
  public void testArraySubsetAndSelection()  {
    // subset operator works for arrays
    assertTrue("{ array(1,c(3,3,3))[1,1,1] == 1; }");

    // selection operator works for arrays
    assertTrue("{ array(1,c(3,3,3))[[1,1,1]] == 1; }");

    // selection on multiple elements fails in arrays
    assertEvalError("{ array(1,c(3,3,3))[[,,]]; }", "String.format(RError.INVALID_SUBSCRIPT_TYPE, \"symbol\")");

    // selection on multiple elements fails in arrays
    assertEvalError("{ array(1,c(3,3,3))[[c(1,2),1,1]]; }", "RError.SELECT_MORE_1");

    // last column
    assertEval("{ m <- array(1:24, dim=c(2,3,4)) ; m[,,2] }", "     [,1] [,2] [,3]\n[1,]   7L   9L  11L\n[2,]   8L  10L  12L");
    assertEval("{ m <- array(1:24, dim=c(2,3,4)) ; m[,,2,drop=FALSE] }", ", , 1\n\n     [,1] [,2] [,3]\n[1,]   7L   9L  11L\n[2,]   8L  10L  12L");
    assertEval("{ m <- array(1:24, dim=c(2,3,4)) ; f <- function(i) { m[,,i] } ; f(1) ; f(2) ; dim(f(1:2)) }", "2L, 3L, 2L");
    assertEval("{ m <- array(1:24, dim=c(2,3,4)) ; f <- function(i) { m[,,i] } ; f(1[2]) ; f(3) }", "     [,1] [,2] [,3]\n[1,]  13L  15L  17L\n[2,]  14L  16L  18L");
  }

  @Test
  public void testMatrixSubsetAndSelection()  {
    // subset operator works for matrices
    assertTrue("{ matrix(1,3,3)[1,1] == 1; }");

    // selection operator works for arrays
    assertTrue("{ matrix(1,3,3)[[1,1]] == 1; }");

    // selection on multiple elements fails in matrices with empty selector
    assertEvalError("{ matrix(1,3,3)[[,]]; }", "String.format(RError.INVALID_SUBSCRIPT_TYPE, \"symbol\")");

    // selection on multiple elements fails in matrices
    assertEvalError("{ matrix(1,3,3)[[c(1,2),1]]; }", "RError.SELECT_MORE_1");

    assertEval("{  m <- matrix(1:6, nrow=2) ;  m[1,NULL] }", "integer(0)");
  }

  @Test
  public void testArrayUpdate() {
    // update to matrix works
    assertTrue("{ a = matrix(1,2,2); a[1,2] = 3; a[1,2] == 3; }");

    // update to an array works
    assertTrue("{ a = array(1,c(3,3,3)); a[1,2,3] = 3; a[1,2,3] == 3; }");

    // update returns the rhs
    assertTrue("{ a = array(1,c(3,3,3)); (a[1,2,3] = 3) == 3; }");

    // update of shared object does the copy
    assertTrue("{ a = array(1,c(3,3,3)); b = a; b[1,2,3] = 3; a[1,2,3] == 1 && b[1,2,3] == 3; }");

    // update where rhs depends on the lhs
    assertTrue("{ x <- array(c(1,2,3), dim=c(3,1,1)) ; x[1:2,1,1] <- sqrt(x[2:1]) ; x[1] == sqrt(2) && x[2] == 1 && x[3] == 3 }");

  }

  @Test
  public void testLhsCopy() {
    // lhs gets upgraded to int
    assertTrue("{ a = array(TRUE,c(3,3,3)); a[1,2,3] = 8L; a[1,2,3] == 8L; }");

    // lhs logical gets upgraded to double
    assertTrue("{ a = array(TRUE,c(3,3,3)); a[1,2,3] = 8.1; a[1,2,3] == 8.1; }");

    // lhs integer gets upgraded to double
    assertTrue("{ a = array(1L,c(3,3,3)); a[1,2,3] = 8.1; a[1,2,3] == 8.1; }");

    // lhs logical gets upgraded to complex
    assertTrue("{ a = array(TRUE,c(3,3,3)); a[1,2,3] = 2+3i; a[1,2,3] == 2+3i; }");

    // lhs integer gets upgraded to complex
    assertTrue("{ a = array(1L,c(3,3,3)); a[1,2,3] = 2+3i; a[1,2,3] == 2+3i; }");

    // lhs double gets upgraded to complex
    assertTrue("{ a = array(1.3,c(3,3,3)); a[1,2,3] = 2+3i; a[1,2,3] == 2+3i; }");

    // lhs logical gets upgraded to string
    assertTrue("{ a = array(TRUE,c(3,3,3)); a[1,2,3] = \"2+3i\"; a[1,2,3] == \"2+3i\" && a[1,1,1] == \"TRUE\"; }");

    // lhs integer gets upgraded to string
    assertTrue("{ a = array(1L,c(3,3,3)); a[1,2,3] = \"2+3i\"; a[1,2,3] == \"2+3i\" && a[1,1,1] == \"1L\"; }");

    // lhs double gets upgraded to string
    assertTrue("{ a = array(1.5,c(3,3,3)); a[1,2,3] = \"2+3i\"; a[1,2,3] == \"2+3i\" && a[1,1,1] == \"1.5\"; }");
  }

  @Test
  public void testRhsCopy() {
    // rhs logical gets upgraded to int
    assertTrue("{ a = array(7L,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == 1L && a[1,1,1] == 7L; }");

    // rhs logical gets upgraded to double
    assertTrue("{ a = array(1.7,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == 1 && a[1,1,1] == 1.7; }");

    // rhs logical gets upgraded to complex
    assertTrue("{ a = array(3+2i,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == 1 && a[1,1,1] == 3+2i; } ");

    // rhs logical gets upgraded to string
    assertTrue("{ a = array(\"3+2i\",c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == \"TRUE\" && a[1,1,1] == \"3+2i\"; }");

    // rhs int gets upgraded to double
    assertTrue("{ a = array(1.7,c(3,3,3)); b = 3L; a[1,2,3] = b; a[1,2,3] == 3 && a[1,1,1] == 1.7; }");

    // rhs int gets upgraded to complex
    assertTrue("{ a = array(3+2i,c(3,3,3)); b = 4L; a[1,2,3] = b; a[1,2,3] == 4 && a[1,1,1] == 3+2i; }");
    assertTrue("{ m <- array(c(1+1i,2+2i,3+3i), dim=c(3,1,1)) ; m[1:2,1,1] <- c(100L,101L) ; m ; m[1,1,1] == 100 && m[2,1,1] == 101 }");

    // rhs logical gets upgraded to string
    assertTrue("{ a = array(\"3+2i\",c(3,3,3)); b = 7L; a[1,2,3] = b; a[1,2,3] == \"7L\" && a[1,1,1] == \"3+2i\"; }");

    // rhs double gets upgraded to complex
    assertTrue("{ a = array(3+2i,c(3,3,3)); b = 4.2; a[1,2,3] = b; a[1,2,3] == 4.2 && a[1,1,1] == 3+2i; }");

    // rhs complex gets upgraded to string
    assertTrue("{ a = array(\"3+2i\",c(3,3,3)); b = 2+3i; a[1,2,3] = b; a[1,2,3] == \"2.0+3.0i\" && a[1,1,1] == \"3+2i\"; }");
  }

  @Test
  public void testMultiDimensionalUpdate() {
    // update matrix by vector, rows
    assertTrue("{ a = matrix(1,3,3); a[1,] = c(3,4,5); a[1,1] == 3 && a[1,2] == 4 && a[1,3] == 5; }");

    // update matrix by vector, cols
    assertTrue("{ a = matrix(1,3,3); a[,1] = c(3,4,5); a[1,1] == 3 && a[2,1] == 4 && a[3,1] == 5; }");

    // update array by vector, dim 3
    assertTrue("{ a = array(1,c(3,3,3)); a[1,1,] = c(3,4,5); a[1,1,1] == 3 && a[1,1,2] == 4 && a[1,1,3] == 5; }");

    // update array by vector, dim 2
    assertTrue("{ a = array(1,c(3,3,3)); a[1,,1] = c(3,4,5); a[1,1,1] == 3 && a[1,2,1] == 4 && a[1,3,1] == 5; }");

    // update array by vector, dim 1
    assertTrue("{ a = array(1,c(3,3,3)); a[,1,1] = c(3,4,5); a[1,1,1] == 3 && a[2,1,1] == 4 && a[3,1,1] == 5; }");

    // update array by matrix
    assertTrue("{ a = array(1,c(3,3,3)); a[1,,] = matrix(1:9,3,3); a[1,1,1] == 1 && a[1,3,1] == 3 && a[1,3,3] == 9; }");

  }

  @Test
  public void testBugIfiniteLoopInGeneralizedRewriting() {
    assertTrue("{ m <- array(1:3, dim=c(3,1,1)) ; f <- function(x,v) { x[1:2,1,1] <- v ; x } ; f(m,10L) ; f(m,10) ; f(m,c(11L,12L)); m[1,1,1] == 1L && m[2,1,1] == 2L && m[3,1,1] == 3L }");
  }


  @Test
  public void testMatrixSimpleRead() {
    // last dimension is dropped
    assertTrue("{ a = matrix(1,3,3); is.null(dim(a[1,])); }");
  }


  @Test
  public void testDefinitions()  {
    assertEval("{ m <- matrix(1:6, nrow=2, ncol=3, byrow=TRUE) \nm }", "     [,1] [,2] [,3]\n[1,]   1L   2L   3L\n[2,]   4L   5L   6L");
    assertEval("{ m <- matrix(1:6, ncol=3, byrow=TRUE) \nm }", "     [,1] [,2] [,3]\n[1,]   1L   2L   3L\n[2,]   4L   5L   6L");
    assertEval("{ m <- matrix(1:6, nrow=2, byrow=TRUE) \nm }", "     [,1] [,2] [,3]\n[1,]   1L   2L   3L\n[2,]   4L   5L   6L");
    assertEval("{ m <- matrix() \nm }", "     [,1]\n[1,]   NA");
    assertEval("{ matrix( (1:6) * (1+3i), nrow=2 ) }", "         [,1]      [,2]      [,3]\n[1,] 1.0+3.0i  3.0+9.0i 5.0+15.0i\n[2,] 2.0+6.0i 4.0+12.0i 6.0+18.0i");
    assertEval("{ matrix( as.raw(101:106), nrow=2 ) }", "     [,1] [,2] [,3]\n[1,]   65   67   69\n[2,]   66   68   6a");
    assertEval("{ x <- list(\"a\",1,TRUE,NULL) ; dim(x) <- c(2,1,2) ; x }", ", , 1\n\n     [,1]\n[1,]  \"a\"\n[2,]  1.0\n\n, , 2\n\n     [,1]\n[1,] TRUE\n[2,] NULL");
  }

  @Test
  public void testSelection()  {
    assertEval("{ m <- matrix(c(1,2,3,4,5,6), nrow=3) \nm[0] }", "numeric(0)");
    assertEval("{ m <- matrix(list(1,2,3,4,5,6), nrow=3) \nm[0] }", "list()");
    assertEval("{ m <- matrix(1:6, nrow=2) \nm[upper.tri(m)] }", "3L, 5L, 6L");
    assertEvalError("{ a <- 1:3; a[1,2,3] <- 10 }", "incorrect number of subscripts");
    assertEvalError("{ f <- function() {3} ; f[1,1,2] }", "object of type 'closure' is not subsettable");
    assertEvalError("{ x <- list(a=1,b=2,c=3) ; x[1,1,2] }", "incorrect number of dimensions");
    assertEvalError("{ x <- 1:3 ; x[1,1,2] }", "incorrect number of dimensions");
    assertEvalError("{ x <- 1:3 ; dim(x) <- c(3,1) ; x[1,1,2] }", "incorrect number of dimensions");

    // matrix
    assertEvalError("{ f <- function(b) { b[,2] } ; f(matrix(1:4,nrow=2)) ; f(f) }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(b) { b[,2] } ; f(matrix(1:4,nrow=2)) ; f(1:3) }", "incorrect number of dimensions");
    assertEvalError("{ f <- function(b) { b[,2] } ; f(matrix(1:4,nrow=2)) ; z <- 1:3; dim(z) <- c(3,1,1,1) ; f(z) }", "incorrect number of dimensions");
    assertEvalError("{ f <- function(b) { b[,4] } ; f(matrix(1:4,nrow=2)) }", "subscript out of bounds");
    assertEval("{ f <- function(b) { b[,0] } ; f(matrix(1:4,nrow=2)) }", "    \n[1,]\n[2,]");
    assertEval("{ m <- matrix(1:4,nrow=2) ; m[2,2,drop=TRUE] }", "4L");
    assertEval("{ m <- matrix(1:4,nrow=2) ; m[2,2,drop=FALSE] }", "     [,1]\n[1,]   4L");
    assertEval("{ m <- matrix(1:4,nrow=2) ; m[,2,drop=FALSE] }", "     [,1]\n[1,]   3L\n[2,]   4L");
    assertEval("{ m <- matrix(1:4,nrow=2) ; m[,2,drop=TRUE] }", "3L, 4L");
    assertEvalError("{ f <- function(b) { b[2,] } ; f(matrix(1:4,nrow=2)) ; f(f) }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(b) { b[2,] } ; f(matrix(1:4,nrow=2)) ; f(1:3) }", "incorrect number of dimensions");
    assertEvalError("{ f <- function(b) { b[2,] } ; f(matrix(1:4,nrow=2)) ; z <- 1:3; dim(z) <- c(3,1,1,1) ; f(z) }", "incorrect number of dimensions");
    assertEvalError("{ f <- function(b) { b[4,] } ; f(matrix(1:4,nrow=2)) }", "subscript out of bounds");
    assertEval("{ f <- function(b) { b[0,] } ; f(matrix(1:4,nrow=2)) }", "     [,1] [,2]");
    assertEvalError("{ f <- function(b) { b[1+0i,] } ; f(matrix(1:4,nrow=2)) }", "invalid subscript type 'complex'");
    assertEvalError("{ f <- function(b,x,y) { b[1:2,2:1] } ; f(matrix(1:4,nrow=2)) ; f(f) }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(b,x,y) { b[1:2,2:1] } ; f(1:4) }", "incorrect number of dimensions");
    assertEvalError("{ f <- function(b,x,y) { b[1:2,2:1] } ; m <- matrix(1:4,nrow=2) ; dim(m) <- c(2,2,1,1) ; f(m) }", "incorrect number of dimensions");
    assertEval("{ f <- function(b,x,y) { b[2:1,2:1] } ; f(matrix(1:4,nrow=2)) }", "     [,1] [,2]\n[1,]   4L   2L\n[2,]   3L   1L");
    assertEval("{ f <- function(b,x,y) { b[2:1,1:2] } ; f(matrix(1:4,nrow=2)) }", "     [,1] [,2]\n[1,]   2L   4L\n[2,]   1L   3L");
    assertEval("{ f <- function(b,x,y) { b[1:2,2:1] } ; f(matrix(1:4,nrow=2)) }", "     [,1] [,2]\n[1,]   3L   1L\n[2,]   4L   2L");
    assertEvalError("{ f <- function(b,x,y) { b[-1:1,2:1] } ; f(matrix(1:4,nrow=2)) }", "only 0's may be mixed with negative subscripts");
    assertEval("{ f <- function(b,x,y) { b[0:2,2:1] } ; f(matrix(1:4,nrow=2)) }", "     [,1] [,2]\n[1,]   3L   1L\n[2,]   4L   2L");
    assertEval("{ f <- function(b,x,y) { b[1:2,2:2,drop=TRUE] } ; f(matrix(1:4,nrow=2)) }", "3L, 4L");
    assertEval("{ f <- function(b,x,y) { b[1:1,2:1,drop=TRUE] } ; f(matrix(1:4,nrow=2)) }", "3L, 1L");
    assertEval("{ f <- function(b,x,y) { b[1:1,2:2,drop=FALSE] } ; f(matrix(1:4,nrow=2)) }", "     [,1]\n[1,]   3L");
    assertEval("{ f <- function(b,x,y) { b[-1:-2,2:2,drop=FALSE] } ; f(matrix(1:4,nrow=2)) }", "     [,1]");
    assertEvalWarning("{ f <- function(b,x,y) { b[1e100:1e100,2:2] } ; f(matrix(1:4,nrow=2)) }", "NA", "NAs introduced by coercion");
    assertEval("{ f <- function(b,x,y) { b[-2L:-2L,2:2] } ; f(matrix(1:4,nrow=2)) }", "3L");
    assertEval("{ f <- function(b,x,y) { b[TRUE:FALSE,2:2] } ; f(matrix(1:4,nrow=2)) }", "3L");
    assertEvalError("{ f <- function(b,x,y) { b[1:2,2:4] } ; f(matrix(1:4,nrow=2)) }", "subscript out of bounds");
    assertEvalError("{ f <- function(b,x,y) { b[1:4,2:1] } ; f(matrix(1:4,nrow=2)) }", "subscript out of bounds");
    assertEvalError("{ f <- function(b,x,y) { b[4:1,2:1] } ; f(matrix(1:4,nrow=2)) }", "subscript out of bounds");
    assertEvalError("{ f <- function(b,x,y) { b[1:2,4:2] } ; f(matrix(1:4,nrow=2)) }", "subscript out of bounds");
    assertEval("{ f <- function(b,x,y) { b[[2,1]] } ; f(matrix(1:4,nrow=2)) }", "2L");
    assertEval("{ f <- function(b,x,y) { b[[2,1]] } ; f(matrix(as.list(1:4),nrow=2)) }", "2L");
    assertEvalError("{ f <- function(b,x,y) { b[c(2,3),1] } ; f(f) }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(b,x,y) { b[c(2,3),1] } ; f(1:4) }", "incorrect number of dimensions");
    assertEvalError("{ f <- function(b,x,y) { b[c(2,3),1] } ; b <- 1:4 ; dim(b) <- c(4,1,1); f(b) }", "incorrect number of dimensions");
    assertEval("{ b <- 1:4 ; dim(b) <- c(1,4); x <- b[drop=FALSE,,1:1] ; x }", "     [,1]\n[1,]   1L");
    assertEval("{ f <- function(d) { b <- matrix(1:4,nrow=2,ncol=2) ; b[,drop=d,2] } ; f(0) ; f(1L) }", "3L, 4L");
    assertEval("{ z <- 1 ; f <- function(d) { b <- matrix(1:4,nrow=2,ncol=2) ; b[{z<<-z+1;1},drop=z<<-z*10,{z<<-z*2;2}] } ; f(0) ; f(1L) ; z }", "820.0");

    // arrays
    assertEvalError("{ b <- 1:4 ; dim(b) <- c(1,0,4,1); x <- b[,,,1] ; dim(x) }", "dims [product 0] do not match the length of object [4]");
    assertEval("{ b <- 1:4 ; dim(b) <- c(1,1,4,1); x <- b[,,,1] ; x }", "1L, 2L, 3L, 4L");
    assertEval("{ b <- 1:4 ; dim(b) <- c(1,1,1,4); x <- b[,,,1,drop=FALSE] ; x }", ", , 1, 1\n\n     [,1]\n[1,]   1L");
    assertEval("{ b <- 1:4 ; dim(b) <- c(1,1,1,4); x <- b[,,,1,drop=FALSE] ; x }", ", , 1, 1\n\n     [,1]\n[1,]   1L");
    assertEval("{ b <- 1:4 ; dim(b) <- c(1,1,1,4); x <- b[,,drop=FALSE,,1] ; x }", ", , 1, 1\n\n     [,1]\n[1,]   1L");
    assertEval("{ b <- 1:4 ; dim(b) <- c(1,1,1,4); x <- b[drop=FALSE,,,,1] ; x }", ", , 1, 1\n\n     [,1]\n[1,]   1L");
    assertEval("{ b <- 1:4 ; dim(b) <- c(1,1,1,4); x <- b[drop=FALSE,,,,-1] ; x }", ", , 1, 1\n\n     [,1]\n[1,]   2L\n\n, , 1, 2\n\n     [,1]\n[1,]   3L\n\n, , 1, 3\n\n     [,1]\n[1,]   4L");
    assertEvalError("{ b <- 1:4 ; dim(b) <- c(1,1,1,4); x <- b[drop=TRUE,,,,10] ; x }", "subscript out of bounds");
    assertEvalError("{ b <- 1:4 ; x <- b[drop=TRUE,,,,10] ; x }", "incorrect number of dimensions");
    assertEvalError("{ b <- 1:4 ; dim(b) <- c(4,1) ; b[drop=TRUE,,,,10]  }", "incorrect number of dimensions");
    assertEvalError("{ b <- function(){3}  ; b[drop=TRUE,,,,10]  }", "object of type 'closure' is not subsettable");
    assertEvalError("{ x <- 1:2 ; dim(x) <- c(2,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,1) ; f(x,f) }", "invalid subscript type 'closure'");
    assertEvalError("{ x <- list(1,2) ; dim(x) <- c(2,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,-2) }", "attempt to select more than one element");
    assertEvalError("{ x <- list(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,integer()) }", "attempt to select less than one element");
    assertEvalError("{ x <- list(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,4) }", "subscript out of bounds");
    assertEvalError("{ x <- list(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,FALSE) }", "attempt to select less than one element");
    assertEvalError("{ x <- list(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,NA) }", "subscript out of bounds");
    assertEvalError("{ x <- list(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,-4) }", "attempt to select more than one element");
    assertEvalError("{ x <- list(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,NULL) }", "attempt to select less than one element");
    assertEval("{ x <- list(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,1L) ; f(x,2) } ", "[[1]]\n2.0");
    assertEval("{ x <- c(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,TRUE) }", "1.0");
    assertEvalError("{ x <- c(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,TRUE) ; f(x,4) }", "subscript out of bounds");
    assertEvalError("{ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,1:3,1:5) }", "subscript out of bounds");
    assertEval("{ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4,-6), 1) }", "2.0, 3.0");
    assertEval("{ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4,-6), 1) ; f(x,c(-2,-4,-4,-6), 1) }", "1.0, 3.0");
    assertEval("{ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4,-6), 1) ; y <- 1:8 ; dim(y) <- c(1,8,1) ; f(y,c(-2,-4,-4,-6,-8), 1) }", "1L, 3L, 5L, 7L");
    assertEvalError("{ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4, NA), 1) }", "only 0's may be mixed with negative subscripts");
    assertEval("{ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(1,1),c(2,1)) }", "     [,1] [,2]\n[1,]  3.0  1.0\n[2,]  3.0  1.0");
    assertEval("{ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-1),c(-3,-3,-3,-4)) }", "2.0, 4.0");
    assertEvalError("{ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(TRUE,TRUE,FALSE,NA),c(TRUE,TRUE)) }", "(subscript) logical subscript too long");
    assertEval("{ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(TRUE,FALSE),c(NA)) }", "NA, NA");
    assertEvalError("{ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[[1,i,j]] } ; f(x,TRUE,1) ; f(x,2,TRUE) ; f(x,TRUE,NULL) }", "attempt to select less than one element");
    assertEval("{ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,c(1,1,1,1),c(2,1)) }", "     [,1] [,2]\n[1,]  3.0  1.0\n[2,]  3.0  1.0\n[3,]  3.0  1.0\n[4,]  3.0  1.0");
    assertEvalWarning("{ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,c(1,1e100,2,2e100),c(2,1)) }", "     [,1] [,2]\n[1,]  3.0  1.0\n[2,]   NA   NA\n[3,]  4.0  2.0\n[4,]   NA   NA", "NAs introduced by coercion");
    assertEval("{ x <- c(1,2,3,4) ; dim(x) <- c(2,1,2) ; f <- function(b,i,j) { b[j,1,i] } ; f(x,c(2,1,2,2),c(NA,2)) }", "     [,1] [,2] [,3] [,4]\n[1,]   NA   NA   NA   NA\n[2,]  4.0  2.0  4.0  4.0");
  }

  @Test
  public void testUpdate()  {
    assertEval("{ m <- matrix(list(1,2,3,4,5,6), nrow=3) \nm[[2]] <- list(100) \nm }", "       [,1] [,2]\n[1,]    1.0  4.0\n[2,] List,1  5.0\n[3,]    3.0  6.0");
    assertEval("{ m <- matrix(list(1,2,3,4,5,6), nrow=3) \nm[2] <- list(100) \nm }", "      [,1] [,2]\n[1,]   1.0  4.0\n[2,] 100.0  5.0\n[3,]   3.0  6.0");
    assertEval("{ m <- matrix(1:6, nrow=3) \nm[2] <- list(100) \nm }", "[[1]]\n1L\n\n[[2]]\n100.0\n\n[[3]]\n3L\n\n[[4]]\n4L\n\n[[5]]\n5L\n\n[[6]]\n6L");

    // element deletion
    assertEval("{ m <- matrix(list(1,2,3,4,5,6), nrow=3) \nm[c(2,3,4,6)] <- NULL \nm }", "[[1]]\n1.0\n\n[[2]]\n5.0");

    // proper update in place
    assertEval("{ m <- matrix(1,2,2)\nm[1,1] = 6\nm }", "     [,1] [,2]\n[1,]  6.0  1.0\n[2,]  1.0  1.0");
    assertEval("{ m <- matrix(1,2,2)\nm[,1] = 7\nm }", "     [,1] [,2]\n[1,]  7.0  1.0\n[2,]  7.0  1.0");
    assertEval("{ m <- matrix(1,2,2)\nm[1,] = 7\nm }", "     [,1] [,2]\n[1,]  7.0  7.0\n[2,]  1.0  1.0");
    assertEval("{ m <- matrix(1,2,2)\nm[,1] = c(10,11)\nm }", "     [,1] [,2]\n[1,] 10.0  1.0\n[2,] 11.0  1.0");

    // error in lengths
    assertEvalError("{ m <- matrix(1,2,2)\nm[,1] = c(1,2,3,4)\nm }", "RError.NOT_MULTIPLE_REPLACEMENT");

    // column update
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,2] <- 10:11 ; m }", "     [,1] [,2] [,3]\n[1,]   1L  10L   5L\n[2,]   2L  11L   6L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,2:3] <- 10:11 ; m }", "     [,1] [,2] [,3]\n[1,]   1L  10L  10L\n[2,]   2L  11L  11L");
    assertEval("{ m <- array(1:24, dim=c(2,3,4)) ; m[,,4] <- 10:15 ; m[,,4] }", "     [,1] [,2] [,3]\n[1,]  10L  12L  14L\n[2,]  11L  13L  15L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,integer()] <- integer() ; m }", "     [,1] [,2] [,3]\n[1,]   1L   3L   5L\n[2,]   2L   4L   6L");
    assertEvalError("{ m <- matrix(1:6, nrow=2) ; m[,2] <- integer() }", "replacement has length zero");

    // subscript with rewriting
    assertTrue("{  m <- array(1:3, dim=c(3,1,1)) ; f <- function(x,v) { x[[2,1,1]] <- v ; x } ; f(m,10L) ; f(m,10) ; x <- f(m,11L) ; x[1] == 1 && x[2] == 11 && x[3] == 3 }");

    // error reporting
    assertEvalError("{ a <- 1:9 ; a[,,1] <- 10L }", "incorrect number of subscripts");
    assertEvalError("{ a <- 1:9 ; a[,1] <- 10L }", "incorrect number of subscripts on a matrix");
    assertEvalError("{ a <- 1:9 ; a[1,1] <- 10L }", "incorrect number of subscripts on a matrix");
    assertEvalError("{ a <- 1:9 ; a[1,1,1] <- 10L }", "incorrect number of subscripts");
    assertEvalError("{ m <- matrix(1:6, nrow=2) ; m[[1:2,1]] <- 1 }", "attempt to select more than one element");
    assertEvalError("{ m <- matrix(1:6, nrow=2) ; m[[integer(),1]] <- 1 }", "attempt to select less than one element");
    assertEvalError("{ m <- matrix(1:6, nrow=2) ; m[[1,1]] <- integer() }", "replacement has length zero");
    assertEvalError("{ m <- matrix(1:6, nrow=2) ; m[[1:2,1]] <- integer() }", "replacement has length zero");
    assertEvalError("{ m <- matrix(1:6, nrow=2) ; m[1,2] <- integer() }", "replacement has length zero");
    assertEvalError("{ m <- matrix(1:6, nrow=2) ; m[1,2] <- 1:3 }", "number of items to replace is not a multiple of replacement length");

    // pushback child of a selector node
    assertEval("{ m <- matrix(1:100, nrow=10) ; z <- 1; s <- 0 ; for(i in 1:3) { m[z <- z + 1,z <- z + 1] <- z * z * 1000 } ; sum(m) }", "39918.0");

    // recovery from scalar selection update
    assertEval("{ m <- matrix(as.double(1:6), nrow=2) ; mi <- matrix(1:6, nrow=2) ; f <- function(v,i,j) { v[i,j] <- 100 ; v[i,j] * i * j } ; f(m, 1L, 2L) ; f(m,1L,TRUE)  }", "100.0, 100.0, 100.0");
    assertEval("{ m <- matrix(as.double(1:6), nrow=2) ; mi <- matrix(1:6, nrow=2) ; f <- function(v,i,j) { v[i,j] <- 100 ; v[i,j] * i * j } ; f(m, 1L, 2L) ; f(m,1L,-1)  }", "-100.0, -100.0");

    assertEval("{ m <- matrix(1:6, nrow=2) ; f <- function(i,j) { m[i,j] <- 10 ; m } ; m <- f(1,-1) ; m }", "     [,1] [,2] [,3]\n[1,]  1.0 10.0 10.0\n[2,]  2.0  4.0  6.0");
    assertEval("{ m <- matrix(1:6, nrow=2) ; f <- function(i,j) { m[i,j] <- 10 ; m } ; m <- f(1, c(-1,-10)) ; m }", "     [,1] [,2] [,3]\n[1,]  1.0 10.0 10.0\n[2,]  2.0  4.0  6.0");
    assertEval("{ m <- matrix(1:6, nrow=2) ; f <- function(i,j) { m[i,j] <- 10 ; m } ; m <- f(1,c(-1,-10)) ; m <- f(1,-1) ; m }", "     [,1] [,2] [,3]\n[1,]  1.0 10.0 10.0\n[2,]  2.0  4.0  6.0");
    assertEval("{ m <- matrix(1:6, nrow=2) ; f <- function(i,j) { m[i,j] <- 10 ; m } ; m <- f(1,c(-1,-10)) ; m <- f(-1,2) ; m }", "     [,1] [,2] [,3]\n[1,]  1.0 10.0 10.0\n[2,]  2.0 10.0  6.0");
    assertEval("{ m <- matrix(1:6, nrow=2) ; f <- function(i,j) { m[i,j] <- 10 ; m } ; m <- f(2,1:3) ; m <- f(1,-2) ; m }", "     [,1] [,2] [,3]\n[1,] 10.0  3.0 10.0\n[2,] 10.0 10.0 10.0");
    assertEval("{ x <- array(c(1,2,3), dim=c(3,1)) ; x[1:2,1] <- 2:1 ; x }", "     [,1]\n[1,]  2.0\n[2,]  1.0\n[3,]  3.0");

    // more tests
    assertEval("{ x <- c(1+2i,2,3,4) ; dim(x) <- c(2,1,2) ; x[2,1,2] <- TRUE; x }", ", , 1\n\n         [,1]\n[1,] 1.0+2.0i\n[2,] 2.0+0.0i\n\n, , 2\n\n         [,1]\n[1,] 3.0+0.0i\n[2,] 1.0+0.0i");
    assertEval("{ x <- c(1+2i,2,3,4) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- 1:2; x }", ", , 1\n\n         [,1]\n[1,] 1.0+2.0i\n[2,] 1.0+0.0i\n\n, , 2\n\n         [,1]\n[1,] 3.0+0.0i\n[2,] 2.0+0.0i");
    assertEval("{ x <- c(1+2i,2,3,4) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- 10+2i; x }", ", , 1\n\n          [,1]\n[1,]  1.0+2.0i\n[2,] 10.0+2.0i\n\n, , 2\n\n          [,1]\n[1,]  3.0+0.0i\n[2,] 10.0+2.0i");
    assertEval("{ x <- c(1+2i,2,3,4) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(12,10); x }", ", , 1\n\n          [,1]\n[1,]  1.0+2.0i\n[2,] 12.0+0.0i\n\n, , 2\n\n          [,1]\n[1,]  3.0+0.0i\n[2,] 10.0+0.0i");
    assertEval("{ x <- c(1+2i,2,3,4) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- \"hello\"; x }", ", , 1\n\n           [,1]\n[1,] \"1.0+2.0i\"\n[2,]    \"hello\"\n\n, , 2\n\n           [,1]\n[1,] \"3.0+0.0i\"\n[2,]    \"hello\"");
    assertEval("{ x <- c(TRUE,FALSE,NA,FALSE) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- FALSE; x }", ", , 1\n\n      [,1]\n[1,]  TRUE\n[2,] FALSE\n\n, , 2\n\n      [,1]\n[1,]    NA\n[2,] FALSE");
    assertEval("{ x <- c(1L,3L,5L,4L) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(10L,11L) ; x }", ", , 1\n\n     [,1]\n[1,]   1L\n[2,]  10L\n\n, , 2\n\n     [,1]\n[1,]   5L\n[2,]  11L");
    assertEval("{ x <- c(1+2i,3+4i,5+6i,4+5i) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(10+11i,12+13i) ; x }", ", , 1\n\n           [,1]\n[1,]   1.0+2.0i\n[2,] 10.0+11.0i\n\n, , 2\n\n           [,1]\n[1,]   5.0+6.0i\n[2,] 12.0+13.0i");
    assertEvalError("{ x <- c(1+2i,3+4i,5+6i,4+5i) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- as.raw(1:2) ; x }", "incompatible types (from raw to complex) in subassignment type fix");
    assertEvalError("{ x <- function(){3} ; x[2,1,2] <- x }", "object of type 'closure' is not subsettable");
    assertEvalError("{ x <- c(1+2i,3+4i,5+6i,4+5i) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- sum ; x }", "number of items to replace is not a multiple of replacement length");
    assertEvalError("{ x <- as.raw(1:4) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- 3:4 ; x }", "incompatible types (from integer to raw) in subassignment type fix");
    assertEval("{ x <- as.raw(11:14) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- as.raw(13:14) ; x }", ", , 1\n\n     [,1]\n[1,]   0b\n[2,]   0d\n\n, , 2\n\n     [,1]\n[1,]   0d\n[2,]   0e");
    assertEval("{ x <- list(1+2i,3+4i,5+6i,4+5i) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- as.raw(c(1,2)) ; unlist(x) }", "1.0+2.0i, 1.0+0.0i, 5.0+6.0i, 2.0+0.0i");
    assertEval("{ for (i in 1:3) { x <- as.raw(11:14) ; dim(x) <- c(2,1,2) ; if (i==2) { z <- x } ; x[2,1,1:2] <- as.raw(13) ; r <- x } ; z }", ", , 1\n\n     [,1]\n[1,]   0b\n[2,]   0c\n\n, , 2\n\n     [,1]\n[1,]   0d\n[2,]   0e");
    assertEvalError("{ x <- as.raw(11:14) ; dim(x) <- c(2,1,2) ; x[2,1,c(NA,2)] <- as.raw(13:14) ; x }", "NAs are not allowed in subscripted assignments");
    assertEvalError("{ x <- as.raw(11:14) ; dim(x) <- c(2,1,2) ; x[2,1,c(1,NA)] <- as.raw(13:14) ; x }", "NAs are not allowed in subscripted assignments");
    assertEvalError("{ x <- as.raw(11:14) ; dim(x) <- c(2,1,2) ; ina <- c(1,2) ; inb <- c(1,NA) ; x[ina, 1, inb] <- as.raw(13:14) ; x }", "NAs are not allowed in subscripted assignments");
    assertEval("{ x <- as.raw(11:14) ; dim(x) <- c(2,1,2) ; ina <- c(TRUE,FALSE) ; inb <- c(FALSE,TRUE) ; x[ina, 1, inb] <- as.raw(13) ; x }", ", , 1\n\n     [,1]\n[1,]   0b\n[2,]   0c\n\n, , 2\n\n     [,1]\n[1,]   0d\n[2,]   0e");
    assertEval("{ x <- as.raw(11:14) ; dim(x) <- c(2,1,2) ; ina <- c(TRUE,FALSE) ; inb <- logical() ; x[ina, 1, inb] <- as.raw(13) ; x }", ", , 1\n\n     [,1]\n[1,]   0b\n[2,]   0c\n\n, , 2\n\n     [,1]\n[1,]   0d\n[2,]   0e");
    assertEvalError("{ f <- function(b, v) { b[2,1,1:2] <- v ; b } ; x <- 1:4 ; dim(x) <- c(2,1,2) ; f(x, c(10L,11L)) ; f(x, c(TRUE,FALSE)) ; f(f,f) }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(b, v) { b[2,1,1:2] <- v ; b } ; x <- 1:4 ; dim(x) <- c(2,1,2) ; f(x, c(10L,11L)) ; f(x, c(TRUE,FALSE)) ; f(x,f) }", "number of items to replace is not a multiple of replacement length");
    assertEvalError("{ f <- function(b, v) { b[2,1,1:2] <- v ; b } ; x <- 1:4 ; dim(x) <- c(2,1,2) ; f(x, c(10L,11L)) ; f(x, c(TRUE,FALSE)) ; f(1:10,f) }", "incorrect number of subscripts");
    assertEval("{ x <- NULL ; x[2,1] <- NULL ; x }", "NULL");
    assertEvalError("{ x <- matrix(2,nrow=2,ncol=2) ; x[2,1] <- NULL ; x }", "number of items to replace is not a multiple of replacement length");
    assertEvalError("{ x <- matrix(2,nrow=2,ncol=2) ; x[[2,1]] <- NULL ; x }", "more elements supplied than there are to replace");

    assertEval("{ x <- 1:4 ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- 11:12 ; x }", ", , 1\n\n     [,1]\n[1,]   1L\n[2,]  11L\n\n, , 2\n\n     [,1]\n[1,]   3L\n[2,]  12L");
    assertEval("{ x <- c(1,2,3,4) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(11,12) ; x }", ", , 1\n\n     [,1]\n[1,]  1.0\n[2,] 11.0\n\n, , 2\n\n     [,1]\n[1,]  3.0\n[2,] 12.0");
    assertEval("{ x <- c(1+1i,2+2i,3+3i,4+4i) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(11+1i,12+2i) ; x }", ", , 1\n\n          [,1]\n[1,]  1.0+1.0i\n[2,] 11.0+1.0i\n\n, , 2\n\n          [,1]\n[1,]  3.0+3.0i\n[2,] 12.0+2.0i");
    assertEval("{ x <- c(TRUE,FALSE,NA,TRUE) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(FALSE,NA) ; x }", ", , 1\n\n      [,1]\n[1,]  TRUE\n[2,] FALSE\n\n, , 2\n\n     [,1]\n[1,]   NA\n[2,]   NA");
    assertEval("{ x <- c(\"a\",\"A\",\"XX\",\"Y\") ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(\"b\",\"BB\") ; x }", ", , 1\n\n     [,1]\n[1,]  \"a\"\n[2,]  \"b\"\n\n, , 2\n\n     [,1]\n[1,] \"XX\"\n[2,] \"BB\"");
    assertEval("{ x <- list(\"a\",1,TRUE,NULL) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- list(FALSE,NULL) ; x }", ", , 1\n\n      [,1]\n[1,]   \"a\"\n[2,] FALSE\n\n, , 2\n\n     [,1]\n[1,] TRUE\n[2,] NULL");
    assertEval("{ x <- list(\"a\",1,TRUE,NULL) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(FALSE,TRUE) ; x }", ", , 1\n\n      [,1]\n[1,]   \"a\"\n[2,] FALSE\n\n, , 2\n\n     [,1]\n[1,] TRUE\n[2,] TRUE");
    assertEval("{ x <- as.raw(11:14) ; dim(x) <- c(2,1,2) ; x[2,1,2] <- as.raw(21)[1:1] ; x }", ", , 1\n\n     [,1]\n[1,]   0b\n[2,]   0c\n\n, , 2\n\n     [,1]\n[1,]   0d\n[2,]   15");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(FALSE,TRUE) ; x }", ", , 1\n\n     [,1]\n[1,]   1L\n[2,]   0L\n\n, , 2\n\n     [,1]\n[1,]   3L\n[2,]   1L");

    assertEval("{ for (i in 1:3 ) { x <- 1:4 ; if (i>=2) { x <- c(10,1,3,4) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(14*i,12+i)  } ; x }", ", , 1\n\n     [,1]\n[1,] 10.0\n[2,] 42.0\n\n, , 2\n\n     [,1]\n[1,]  3.0\n[2,] 15.0");
    assertEval("{ for (i in 1:3 ) { x <- c(10,11,13,14) ; if (i>=2) { x <- c(1L,2L,10L,100L) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(14L*i,12L+i)  } ; x }", ", , 1\n\n     [,1]\n[1,]   1L\n[2,]  42L\n\n, , 2\n\n     [,1]\n[1,]  10L\n[2,]  15L");
    assertEval("{ for (i in 1:3 ) { x <- c(11:14) ; if (i>=2) { x <- c(1,2,10,100) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(14L*i,12L+i)  } ; x }", ", , 1\n\n     [,1]\n[1,]  1.0\n[2,] 42.0\n\n, , 2\n\n     [,1]\n[1,] 10.0\n[2,] 15.0");
    assertEval("{ for (i in 1:3 ) { x <- c(11:14) ; if (i>=2) { x <- c(1,2,10+1i,100) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(14L*i,12L+i)  } ; x }", ", , 1\n\n          [,1]\n[1,]  1.0+0.0i\n[2,] 42.0+0.0i\n\n, , 2\n\n          [,1]\n[1,] 10.0+1.0i\n[2,] 15.0+0.0i");
    assertEval("{ for (i in 1:3 ) { x <- c(11:14) ; if (i>=2) { x <- c(1,2,10+1i,100) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(15*i,-12+i)  } ; x }", ", , 1\n\n          [,1]\n[1,]  1.0+0.0i\n[2,] 45.0+0.0i\n\n, , 2\n\n          [,1]\n[1,] 10.0+1.0i\n[2,] -9.0+0.0i");
    assertEval("{ for (i in 1:3 ) { x <- c(11:14) ; if (i>=2) { x <- c(1,2,10+1i,100) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(TRUE,NA)  } ; x }", ", , 1\n\n         [,1]\n[1,] 1.0+0.0i\n[2,] 1.0+0.0i\n\n, , 2\n\n          [,1]\n[1,] 10.0+1.0i\n[2,]        NA");
    assertEval("{ r <- 0 ; for (i in 1:5 ) { x <- c(11:14) ; if (i==2 || i==3) { x <- c(1,2,10+1i,100) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(15L*i,-12L+i) ; r <- r + sum(x) } ; r }", "274.0+2.0i");
    assertEval("{ r <- 0 ; for (i in 1:5 ) { x <- c(11:14) ; if (i==2 || i==3) { x <- c(1,2,10+1i,100) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(15L*i,NA) } ; x }", ", , 1\n\n     [,1]\n[1,]  11L\n[2,]  75L\n\n, , 2\n\n     [,1]\n[1,]  13L\n[2,]   NA");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- x ; x }", ", , 1\n\n     [,1]\n[1,]   4L\n[2,]   3L\n\n, , 2\n\n     [,1]\n[1,]   2L\n[2,]   1L");
    assertEval("{ y <- as.double(1:4) ; dim(y) <- c(2,1,2) ; for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- c(1,2,3,4) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { x } else { y }  } ; x }", ", , 1\n\n     [,1]\n[1,]  4.0\n[2,]  3.0\n\n, , 2\n\n     [,1]\n[1,]  2.0\n[2,]  1.0");
    assertEval("{ y <- as.double(1:4) ; dim(y) <- c(2,1,2) ; for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- c(1+2i,2,3,4) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { x } else { y }  } ; x }", ", , 1\n\n         [,1]\n[1,] 4.0+0.0i\n[2,] 3.0+0.0i\n\n, , 2\n\n         [,1]\n[1,] 2.0+0.0i\n[2,] 1.0+2.0i");
    assertEval("{ y <- 1:4 ; dim(y) <- c(2,1,2) ; for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- c(1+2i,2,3,4) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { x } else { y }  } ; x }", ", , 1\n\n         [,1]\n[1,] 4.0+0.0i\n[2,] 3.0+0.0i\n\n, , 2\n\n         [,1]\n[1,] 2.0+0.0i\n[2,] 1.0+2.0i");
    assertEval("{ y <- 1:4 ; dim(y) <- c(2,1,2) ; for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- c(1,2,3,4) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { x } else { y }  } ; x }", ", , 1\n\n     [,1]\n[1,]  4.0\n[2,]  3.0\n\n, , 2\n\n     [,1]\n[1,]  2.0\n[2,]  1.0");
    assertEval("{ y <- 1:4 ; dim(y) <- c(2,1,2) ; for(i in 1:6) { x <- as.double(1:4) ; if (i>=2) { x <- 11:14 } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { x } else { y }  } ; x }", ", , 1\n\n     [,1]\n[1,]  14L\n[2,]  13L\n\n, , 2\n\n     [,1]\n[1,]  12L\n[2,]  11L");
    assertEval("{ y <- 1:4+1+2i ; dim(y) <- c(2,1,2) ; for(i in 1:6) { x <- as.double(1:4) ; if (i>=2) { x <- c(1+2i,3+4i,5,NA) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { x } else { y }  } ; x }", ", , 1\n\n         [,1]\n[1,]       NA\n[2,] 5.0+0.0i\n\n, , 2\n\n         [,1]\n[1,] 3.0+4.0i\n[2,] 1.0+2.0i");
    assertEval("{ for(i in 1:6) { x <- as.double(1:4) ; if (i>=2) { x <- 1:4 } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { 11:14+1+2i } else { c(1L,10L,NA,3L) }  } ; x }", ", , 1\n\n          [,1]\n[1,] 15.0+2.0i\n[2,] 14.0+2.0i\n\n, , 2\n\n          [,1]\n[1,] 13.0+2.0i\n[2,] 12.0+2.0i");
    assertEval("{ for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- as.double(1:4) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { 11:14+1+2i } else { c(1L,10L,NA,3L) }  } ; x }", ", , 1\n\n          [,1]\n[1,] 15.0+2.0i\n[2,] 14.0+2.0i\n\n, , 2\n\n          [,1]\n[1,] 13.0+2.0i\n[2,] 12.0+2.0i");
    assertEval("{ for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- (1:4)+1+2i } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { 11:14 } else { c(1L,10L,NA,3L)+2+3i }  } ; x }", ", , 1\n\n          [,1]\n[1,] 14.0+0.0i\n[2,] 13.0+0.0i\n\n, , 2\n\n          [,1]\n[1,] 12.0+0.0i\n[2,] 11.0+0.0i");
    assertEval("{ for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- (1:4)+1+2i } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { 11:14 } else { c(1,10,NA,3) }  } ; x }", ", , 1\n\n          [,1]\n[1,] 14.0+0.0i\n[2,] 13.0+0.0i\n\n, , 2\n\n          [,1]\n[1,] 12.0+0.0i\n[2,] 11.0+0.0i");
    assertEval("{ for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- (1:4)+1 } ; dim(x) <- c(2,1,2) ; x[2:1,1,2:1] <- if (i >= 4) { 11:14 } else { c(1,10,NA,3) }  } ; x }", ", , 1\n\n     [,1]\n[1,] 14.0\n[2,] 13.0\n\n, , 2\n\n     [,1]\n[1,] 12.0\n[2,] 11.0");
    assertEval("{ for(i in 1:6) { x <- 1:4+1 ; if (i>=2) { x <- 1:4 } ; if (i>=4) { x <- c(TRUE,FALSE,FALSE,NA) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2] <- c(11L,12L)  } ; x }", ", , 1\n\n     [,1]\n[1,]   1L\n[2,]   0L\n\n, , 2\n\n     [,1]\n[1,]  12L\n[2,]  11L");
    assertEval("{ for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- 1:4+1 } ; if (i>=4) { x <- c(TRUE,FALSE,FALSE,NA) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2] <- c(11L,12L)  } ; x }", ", , 1\n\n     [,1]\n[1,]   1L\n[2,]   0L\n\n, , 2\n\n     [,1]\n[1,]  12L\n[2,]  11L");
    assertEval("{ for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- 1:4+1+1i } ; if (i>=4) { x <- c(TRUE,FALSE,FALSE,NA) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2] <- c(11,12)  } ; x }", ", , 1\n\n     [,1]\n[1,]  1.0\n[2,]  0.0\n\n, , 2\n\n     [,1]\n[1,] 12.0\n[2,] 11.0");
    assertEval("{ for(i in 1:6) { x <- 1:4 ; if (i>=2) { x <- 1:4+1+1i } ; if (i>=4) { x <- c(TRUE,FALSE,FALSE,NA) } ; dim(x) <- c(2,1,2) ; x[2:1,1,2] <- c(11+1i,12+2i)  } ; x }", ", , 1\n\n         [,1]\n[1,] 1.0+0.0i\n[2,] 0.0+0.0i\n\n, , 2\n\n          [,1]\n[1,] 12.0+2.0i\n[2,] 11.0+1.0i");
    assertEval("{ x <- list(1,2,3,4); dim(x) <- c(2,1,2); x[2,1,1] <- TRUE;  x }", ", , 1\n\n     [,1]\n[1,]  1.0\n[2,] TRUE\n\n, , 2\n\n     [,1]\n[1,]  3.0\n[2,]  4.0");
    assertEval("{ x <- as.raw(c(1,2,3,4)); dim(x) <- c(2,1,2); x[2,1,1] <- as.raw(TRUE);  x }", ", , 1\n\n     [,1]\n[1,]   01\n[2,]   01\n\n, , 2\n\n     [,1]\n[1,]   03\n[2,]   04");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,1,2); x[[2,1,1]] <- 10+1i; x }", ", , 1\n\n          [,1]\n[1,]  1.0+0.0i\n[2,] 10.0+1.0i\n\n, , 2\n\n         [,1]\n[1,] 3.0+0.0i\n[2,] 4.0+0.0i");
    assertEval("{ x <- c(1L,3L,4L,NA) ; dim(x) <- c(2,1,2); x[[2,1,1]] <- 10+1i;  x }", ", , 1\n\n          [,1]\n[1,]  1.0+0.0i\n[2,] 10.0+1.0i\n\n, , 2\n\n         [,1]\n[1,] 4.0+0.0i\n[2,]       NA");
    assertEval("{ x <- c(1L,3L,4L,NA) ; dim(x) <- c(2,1,2); x[[2,1,1]] <- list(10+1i); x[2] }", "[[1]]\n[[1]][[1]]\n10.0+1.0i");

    assertEval("{ x <- 1:4 ; dim(x) <- c(2,1,2); z <- x ; f <- function(b) { b[2,1,1] <- 10L; b } ; f(x) ; y <- as.double(x); dim(y) <- c(2,1,2) ; f(y) }", ", , 1\n\n     [,1]\n[1,]  1.0\n[2,] 10.0\n\n, , 2\n\n     [,1]\n[1,]  3.0\n[2,]  4.0");
    assertEval("{ x <- c(\"a\",\"b\",\"c\",\"d\") ; dim(x) <- c(2,1,2); x[2,1,1] <- 2; x }", ", , 1\n\n      [,1]\n[1,]   \"a\"\n[2,] \"2.0\"\n\n, , 2\n\n     [,1]\n[1,]  \"c\"\n[2,]  \"d\"");
    assertEval("{ x <- c(\"a\",\"b\",\"c\",\"d\") ; dim(x) <- c(2,1,2); x[2,1,1] <- list(1); dim(x) <- NULL; x }", "[[1]]\n\"a\"\n\n[[2]]\n1.0\n\n[[3]]\n\"c\"\n\n[[4]]\n\"d\"");
    assertEval("{ x <- c(\"a\",\"b\",\"c\",\"d\") ; dim(x) <- c(2,1,2); f <- function(v) { x[2,1,1] <- v ; x } ; f(1+2i) ; f(\"XX\") }", ", , 1\n\n     [,1]\n[1,]  \"a\"\n[2,] \"XX\"\n\n, , 2\n\n     [,1]\n[1,]  \"c\"\n[2,]  \"d\"");
    assertEval("{ x <- list(\"a\",\"b\",\"c\",\"d\") ; dim(x) <- c(2,1,2); f <- function(v) { x[2,1,1] <- v ; x } ; f(1+2i) ; f(list(\"XX\")) }", ", , 1\n\n     [,1]\n[1,]  \"a\"\n[2,] \"XX\"\n\n, , 2\n\n     [,1]\n[1,]  \"c\"\n[2,]  \"d\"");

    assertEval("{ x <- c(\"a\",\"b\",\"c\",\"d\") ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(\"AA\"); f(10) }", ", , 1\n\n       [,1]\n[1,]    \"a\"\n[2,] \"10.0\"\n\n, , 2\n\n     [,1]\n[1,]  \"c\"\n[2,]  \"d\"");
    assertEval("{ x <- c(1+1i,2+2i,3+3i,4+4i) ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(1+12i) ; f(10) }", ", , 1\n\n          [,1]\n[1,]  1.0+1.0i\n[2,] 10.0+0.0i\n\n, , 2\n\n         [,1]\n[1,] 3.0+3.0i\n[2,] 4.0+4.0i");
    assertEval("{ x <- c(1,10,0/0,1/0) ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(12) ; f(10L) }", ", , 1\n\n     [,1]\n[1,]  1.0\n[2,] 10.0\n\n, , 2\n\n         [,1]\n[1,]      NaN\n[2,] Infinity");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(12L) ; f(10/0) }", ", , 1\n\n         [,1]\n[1,]      1.0\n[2,] Infinity\n\n, , 2\n\n     [,1]\n[1,]  3.0\n[2,]  4.0");
    assertEval("{ x <- c(TRUE,FALSE,NA,FALSE) ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(FALSE) ; f(10/0) }", ", , 1\n\n         [,1]\n[1,]      1.0\n[2,] Infinity\n\n, , 2\n\n     [,1]\n[1,]   NA\n[2,]  0.0");
    assertEvalError("{ x <- as.raw(11:14) ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(as.raw(10)) ; f(10/0) }", "incompatible types (from double to raw) in subassignment type fix");
    assertEval("{ x <- list(1,10,-1/0,0/0) ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(list(TRUE)) ; z <- f(NA) ; unlist(z) }", "1.0, NA, -Infinity, NaN");
    assertEval("{ for(i in 1:2) { if (i==1) { b <- c(\"a\",\"b\",\"c\",\"d\") } else { b <- 1:4 };  dim(b) <- c(2,1,2); b[[2,1,1]] <- \"AA\" } ; b }", ", , 1\n\n     [,1]\n[1,] \"1L\"\n[2,] \"AA\"\n\n, , 2\n\n     [,1]\n[1,] \"3L\"\n[2,] \"4L\"");
    assertEval("{ for(i in 1:2) { if (i==1) { b <- c(1+2i/0,-3/0,0/0,4) } else { b <- 1:4 };  dim(b) <- c(2,1,2); b[[2,1,1]] <- 1+5i } ; b }", ", , 1\n\n         [,1]\n[1,] 1.0+0.0i\n[2,] 1.0+5.0i\n\n, , 2\n\n         [,1]\n[1,] 3.0+0.0i\n[2,] 4.0+0.0i");
    assertEval("{ for(i in 1:2) { if (i==1) { b <- c(1/0,-3/0,0/0,4) } else { b <- 1:4 };  dim(b) <- c(2,1,2); b[[2,1,1]] <- 10 } ; b }", ", , 1\n\n     [,1]\n[1,]  1.0\n[2,] 10.0\n\n, , 2\n\n     [,1]\n[1,]  3.0\n[2,]  4.0");
    assertEval("{ for(i in 1:2) { if (i==1) { b <- 1:4 } else { b <- c(1/0,-3/0,0/0,4) };  dim(b) <- c(2,1,2); b[[2,1,1]] <- 10L } ; b }", ", , 1\n\n         [,1]\n[1,] Infinity\n[2,]     10.0\n\n, , 2\n\n     [,1]\n[1,]  NaN\n[2,]  4.0");
    assertEval("{ for(i in 1:2) { if (i==1) { b <- c(TRUE,FALSE,FALSE,NA) } else { b <- c(1/0,-3/0,0/0,4) }; dim(b) <- c(2,1,2); b[[2,1,1]] <- TRUE } ; b }", ", , 1\n\n         [,1]\n[1,] Infinity\n[2,]      1.0\n\n, , 2\n\n     [,1]\n[1,]  NaN\n[2,]  4.0");
    assertEvalError("{ for(i in 1:2) { if (i==1) { b <- as.raw(11:14) } else { b <- c(1/0,-3/0,0/0,4) }; dim(b) <- c(2,1,2); b[[2,1,1]] <- as.raw(111) } ; b }", "incompatible types (from raw to double) in subassignment type fix");
    assertEval("{ for(i in 1:2) { if (i==1) { b <- as.list(11:14) } else { b <- c(1/0,-3/0,0/0,4) }; dim(b) <- c(2,1,2); b[[2,1,1]] <- list(111) } ; dim(b) <- NULL ; b }", "[[1]]\nInfinity\n\n[[2]]\n[[2]][[1]]\n111.0\n\n[[3]]\nNaN\n\n[[4]]\n4.0");

    // column selection
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,1,2); x[,,1] <- 11:12 ; x }", ", , 1\n\n     [,1]\n[1,]  11L\n[2,]  12L\n\n, , 2\n\n     [,1]\n[1,]   3L\n[2,]   4L");
    assertEval("{ for(i in 1:2) { x <- 1:4 ; dim(x) <- c(1,1,4); if (i==2) { z <- x } ; x[,,1] <- 12L } ; as.integer(z) }", "1L, 2L, 3L, 4L");
    assertEval("{ x <- 1:4 ; dim(x) <- c(1,1,4); x[,,NA] <- 12L ; as.integer(x) }", "1L, 2L, 3L, 4L");
    assertEval("{ for (i in 1:3) { if (i==1) { z <- 1 } ; if (i==2) { z <- c(-1,-1) } ; x <- 1:4 ; dim(x) <- c(1,1,4)  ; x[,,z] <- 12L } ; as.integer(x) }", "1L, 12L, 12L, 12L");

    // matrix
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,2); x[,] <- 12L ; x }", "     [,1] [,2]\n[1,]  12L  12L\n[2,]  12L  12L");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,2); x[2,] <- 12L ; x }", "     [,1] [,2]\n[1,]   1L   3L\n[2,]  12L  12L");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,2); x[2,1] <- 12L ; x }", "     [,1] [,2]\n[1,]   1L   3L\n[2,]  12L   4L");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,2); x[2,NA] <- 12L ; x }", "     [,1] [,2]\n[1,]   1L   3L\n[2,]   2L   4L");
    assertEvalError("{ x <- 1:4 ; dim(x) <- c(2,2,1); x[2,NA,1] <- 12:13 ; x }", "NAs are not allowed in subscripted assignments");
    assertEvalWarning("{ x <- 1:4 ; dim(x) <- c(2,2); x[2,1e100] <- 12L ; x }", "     [,1] [,2]\n[1,]   1L   3L\n[2,]   2L   4L", "NAs introduced by coercion");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,2); x[2,-1L] <- 12L ; x }", "     [,1] [,2]\n[1,]   1L   3L\n[2,]   2L  12L");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,2); x[2,-1] <- 12L ; x }", "     [,1] [,2]\n[1,]   1L   3L\n[2,]   2L  12L");
    assertEval("{ for(i in 1:2) { x <- 1:4 ; dim(x) <- c(2,2) ; if (i==2) { z <- x } ; x[1,2] <- 12L } ; z }", "     [,1] [,2]\n[1,]   1L   3L\n[2,]   2L   4L");
    assertEvalError("{ for(i in 1:2) { x <- 1:4 ; dim(x) <- c(2,2) ; if (i==2) { dim(x) <- NULL } ; x[1,2] <- 12L } ; z }", "incorrect number of subscripts on a matrix");
    assertEvalError("{ for(i in 1:2) { x <- 1:4 ; dim(x) <- c(2,2) ; if (i==2) { dim(x) <- c(2,1,1,2) } ; x[1,2] <- 12L } ; z }", "incorrect number of subscripts on a matrix");
    assertEvalError("{ x <- 1:4 ; dim(x) <- c(2,2) ; x[3,2] <- 12L ; x }", "subscript out of bounds");
    assertEvalError("{ x <- 1:4 ; dim(x) <- c(2,2) ; x[2,3] <- 12L ; x }", "subscript out of bounds");
    assertEvalError("{ x <- 1:4 ; dim(x) <- c(2,2) ; x[2,2] <- 12:13 ; x }", "number of items to replace is not a multiple of replacement length");
    assertEval("{ for (i in c(1,-1)) { x <- as.raw(1:4) ; dim(x) <- c(2,2) ; x[i,2] <- as.raw(12) } ; x }", "     [,1] [,2]\n[1,]   01   03\n[2,]   02   0c");
    assertEval("{ for (i in c(1,-1)) { x <- 1:4 ; dim(x) <- c(2,2) ; x[i,2] <- list(12) } ; unlist(x) }", "1.0, 2.0, 3.0, 12.0");

    // scalar values
    assertEval("{ x <- c(TRUE,FALSE,NA,TRUE) ; dim(x) <- c(2,2,1) ; x[2,1,1] <- NA ; x }", ", , 1\n\n     [,1] [,2]\n[1,] TRUE   NA\n[2,]   NA TRUE");
    assertEval("{ x <- c(TRUE,FALSE,NA,TRUE) ; dim(x) <- c(2,2,1) ; x[2,1,1] <- 1 ; x }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0   NA\n[2,]  1.0  1.0");
    assertEval("{ for(v in list(NA,1)) { x <- c(TRUE,FALSE,NA,TRUE) ; dim(x) <- c(2,2,1) ; x[2,1,1] <- v } ; x }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0   NA\n[2,]  1.0  1.0");
    assertEval("{ for(v in list(2L,1)) { x <- 1:4 ; dim(x) <- c(2,2,1) ; x[2,1,1] <- v } ; x }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  3.0\n[2,]  1.0  4.0");
    assertEval("{ for(v in list(2,NA)) { x <- as.double(1:4) ; dim(x) <- c(2,2,1) ; x[2,1,1] <- v } ; x }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  3.0\n[2,]   NA  4.0");
    assertEval("{ for(v in list(2+1i,NA)) { x <- c(1+2i,3+4i,5+6i,7+8i) ; dim(x) <- c(2,2,1) ; x[2,1,1] <- v } ; x }", ", , 1\n\n         [,1]     [,2]\n[1,] 1.0+2.0i 5.0+6.0i\n[2,]       NA 7.0+8.0i");
    assertEval("{ for(v in list(\"z\",-3/0)) { x <- c(\"a\",\"aa\",\"b\",\"X\") ; dim(x) <- c(2,2,1) ; x[2,1,1] <- v } ; x }", ", , 1\n\n            [,1] [,2]\n[1,]         \"a\"  \"b\"\n[2,] \"-Infinity\"  \"X\"");

    assertEval("{ for(b in list(1:4,as.double(1:4))) { dim(b) <- c(2,2,1) ; b[2,1,1] <- NA } ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  3.0\n[2,]   NA  4.0");
    assertEval("{ for(b in list(c(TRUE,FALSE,FALSE,NA),1:4)) { dim(b) <- c(2,2,1) ; b[2,1,1] <- NA } ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   3L\n[2,]   NA   4L");
    assertEval("{ for(b in list(c(0/0,1/0,-1/0,1e100),1:4)) { dim(b) <- c(2,2,1) ; b[2,1,1] <- NA } ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   3L\n[2,]   NA   4L");
    assertEval("{ for(b in list(c(1+2i,3+4i,5+6i,3),1:4)) { dim(b) <- c(2,2,1) ; b[2,1,1] <- 2+2i } ; b }", ", , 1\n\n         [,1]     [,2]\n[1,] 1.0+0.0i 3.0+0.0i\n[2,] 2.0+2.0i 4.0+0.0i");
    assertEval("{ for(b in list(c(1,3,5,10),1:4)) { dim(b) <- c(2,2,1) ; b[2,1,1] <- 12 } ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  3.0\n[2,] 12.0  4.0");
    assertEval("{ for(b in list(1:4,c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2,1,1] <- 12L } ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   NA\n[2,]  12L   NA");
    assertEval("{ for(b in list(c(\"a\",\"aa\",\"b\",\"X\"),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2,1,1] <- \"YYY\" } ; b }", ", , 1\n\n       [,1] [,2]\n[1,] \"TRUE\"   NA\n[2,]  \"YYY\"   NA");

    assertEval("{ x <- c(TRUE,FALSE,NA,FALSE) ; dim(x) <- c(2,1,2) ; x[2,1,c(1,NA)] <- FALSE; x }", ", , 1\n\n      [,1]\n[1,]  TRUE\n[2,] FALSE\n\n, , 2\n\n      [,1]\n[1,]    NA\n[2,] FALSE");
    assertEval("{ x <- c(11:14) ; dim(x) <- c(2,1,2) ; x[2,1,c(1,NA)] <- 2L; x }", ", , 1\n\n     [,1]\n[1,]  11L\n[2,]   2L\n\n, , 2\n\n     [,1]\n[1,]  13L\n[2,]  14L");
    assertEval("{ x <- c(11,0/0,2,3) ; dim(x) <- c(2,1,2) ; x[TRUE,TRUE,c(NA,0)] <- 100; x }", ", , 1\n\n     [,1]\n[1,] 11.0\n[2,]  NaN\n\n, , 2\n\n     [,1]\n[1,]  2.0\n[2,]  3.0");
    assertEval("{ x <- c(11,0/0,2,3) ; dim(x) <- c(2,1,2) ; x[2,1,c(NA,0)] <- 100; x }", ", , 1\n\n     [,1]\n[1,] 11.0\n[2,]  NaN\n\n, , 2\n\n     [,1]\n[1,]  2.0\n[2,]  3.0");
    assertEval("{ x <- c(11+2i,0/0,2+1i,3) ; dim(x) <- c(2,1,2) ; x[c(NA,1),1,c(NA,0)] <- 100+1i; x }", ", , 1\n\n          [,1]\n[1,] 11.0+2.0i\n[2,]  NaN+0.0i\n\n, , 2\n\n         [,1]\n[1,] 2.0+1.0i\n[2,] 3.0+0.0i");
    assertEval("{ x <- c(11+2i,0/0,2+1i,3) ; dim(x) <- c(2,1,2) ; x[2:1,1,c(NA,NA)] <- 100+1i; x }", ", , 1\n\n          [,1]\n[1,] 11.0+2.0i\n[2,]  NaN+0.0i\n\n, , 2\n\n         [,1]\n[1,] 2.0+1.0i\n[2,] 3.0+0.0i");
    assertEval("{ x <- c(11+2i,0/0,2+1i,3) ; dim(x) <- c(2,1,2) ; x[2:1,1,c(NA,1)] <- 100+1i; x }", ", , 1\n\n           [,1]\n[1,] 100.0+1.0i\n[2,] 100.0+1.0i\n\n, , 2\n\n         [,1]\n[1,] 2.0+1.0i\n[2,] 3.0+0.0i");
    assertEval("{ x <- c(\"A\",\"a\",\"m\",\"MM\") ; dim(x) <- c(2,1,2) ; x[2:1,1,c(NA,1)] <- \"xxx\"; x }", ", , 1\n\n      [,1]\n[1,] \"xxx\"\n[2,] \"xxx\"\n\n, , 2\n\n     [,1]\n[1,]  \"m\"\n[2,] \"MM\"");
    assertEval("{ x <- c(TRUE,FALSE,NA,FALSE) ; dim(x) <- c(2,1,2) ; x[2,1,logical()] <- FALSE; x }", ", , 1\n\n      [,1]\n[1,]  TRUE\n[2,] FALSE\n\n, , 2\n\n      [,1]\n[1,]    NA\n[2,] FALSE");
    assertEval("{ x <- c(11:14) ; dim(x) <- c(2,1,2) ; x[2,1,double()] <- 2L; x }", ", , 1\n\n     [,1]\n[1,]  11L\n[2,]  12L\n\n, , 2\n\n     [,1]\n[1,]  13L\n[2,]  14L");
    assertEval("{ x <- c(11,0/0,2,3) ; dim(x) <- c(2,1,2) ; x[TRUE,TRUE,double()] <- 100; x }", ", , 1\n\n     [,1]\n[1,] 11.0\n[2,]  NaN\n\n, , 2\n\n     [,1]\n[1,]  2.0\n[2,]  3.0");
    assertEval("{ x <- c(11+2i,0/0,2+1i,3) ; dim(x) <- c(2,1,2) ; x[2:1,1,integer()] <- 100+1i; x }", ", , 1\n\n          [,1]\n[1,] 11.0+2.0i\n[2,]  NaN+0.0i\n\n, , 2\n\n         [,1]\n[1,] 2.0+1.0i\n[2,] 3.0+0.0i");
    assertEval("{ x <- c(\"A\",\"a\",\"m\",\"MM\") ; dim(x) <- c(2,1,2) ; x[2:1,1,double()] <- \"xxx\"; x }", ", , 1\n\n     [,1]\n[1,]  \"A\"\n[2,]  \"a\"\n\n, , 2\n\n     [,1]\n[1,]  \"m\"\n[2,] \"MM\"");

    // non-scalar values
    assertEval("{ for(b in list(c(TRUE,FALSE,FALSE,NA),1:4)) { dim(b) <- c(2,2,1) ; b[2,1:2,1] <- c(NA,FALSE) } ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   3L\n[2,]   NA   0L");
    assertEval("{ for(b in list(1:4,c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2,2:1,1] <- 12L:13L } ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   NA\n[2,]  13L  12L");
    assertEval("{ for(b in list(c(0/0,1/0,-1/0,1e100),1:4)) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- as.double(c(NA,11L)) } ; b }", ", , 1\n\n     [,1] [,2]\n[1,] 11.0  3.0\n[2,]   NA  4.0");
    assertEval("{ for(b in list(c(1+2i,3+4i,5+6i,3),1:4)) { dim(b) <- c(2,2,1) ; b[2,2:1,1] <- as.complex(c(2,3)) } ; b }", ", , 1\n\n         [,1]     [,2]\n[1,] 1.0+0.0i 3.0+0.0i\n[2,] 3.0+0.0i 2.0+0.0i");
    assertEval("{ for(b in list(c(\"a\",\"aa\",\"b\",\"X\"),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- as.character(3:4) } ; b }", ", , 1\n\n     [,1] [,2]\n[1,] \"4L\"   NA\n[2,] \"3L\"   NA");
    assertEvalError("{ for(b in list(as.raw(11:14),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- as.raw(3:4)[2:1] } ; b }", "incompatible types (from raw to logical) in subassignment type fix");
    assertEval("{ for(b in list(as.raw(11:14),list(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- as.raw(3:4)[2:1] } ; dim(b) <- NULL ; b }", "[[1]]\n03\n\n[[2]]\n04\n\n[[3]]\nNA\n\n[[4]]\nNA");
    assertEval("{ for(b in list(as.list(11:14),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- list(1,2) } ; dim(b) <- NULL ; b }", "[[1]]\n2.0\n\n[[2]]\n1.0\n\n[[3]]\nNA\n\n[[4]]\nNA");

    assertEval("{ for(v in list(c(NA,FALSE),3:4)) { x <- c(TRUE,FALSE,NA,TRUE) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   NA\n[2,]   3L   4L");
    assertEval("{ for(v in list(as.double(13:14),c(TRUE,NA))) { x <- c(0/0,2,3,10) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n     [,1] [,2]\n[1,]  NaN  3.0\n[2,]  1.0   NA");
    assertEval("{ for(v in list(as.complex(13:14),c(TRUE,NA))) { x <- c(0/0,2+1i,3,10) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n         [,1]     [,2]\n[1,] NaN+0.0i 3.0+0.0i\n[2,] 1.0+0.0i       NA");
    assertEval("{ for(v in list(as.character(13:14),c(TRUE,NA))) { x <- c(0/0,2+1i,3,10) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n         [,1]     [,2]\n[1,] NaN+0.0i 3.0+0.0i\n[2,] 1.0+0.0i       NA");
    assertEval("{ for(v in list(as.character(13:14),c(TRUE,NA))) { x <- c(\"a\",\"A\",\"XX\",\"B\") ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n       [,1] [,2]\n[1,]    \"a\" \"XX\"\n[2,] \"TRUE\"   NA");
    assertEvalError("{ for(v in list(as.raw(13:14),c(TRUE,NA))) { x <- as.raw(111:114) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", "incompatible types (from logical to raw) in subassignment type fix");
    assertEval("typeof({ for(v in list(as.list(13:14),c(TRUE,NA))) { x <- list(1,1L,TRUE,NA) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x })", "\"list\"");
    assertEval("{ for(v in list(as.list(13:14),c(TRUE,NA))) { x <- list(1,1L,TRUE,NA) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0 TRUE\n[2,] TRUE   NA");

    assertEvalError("{ x <- 1:4 ; dim(x) <- c(2,2,1) ; x[[1:2,1,1]] <- 100:104 }", "more elements supplied than there are to replace");

    // int -> int
    assertEval("{ for(v in list(c(2L,NA),3:4)) { x <- c(1L,10L,100L,1000L) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n     [,1] [,2]\n[1,]   1L 100L\n[2,]   3L   4L");
    assertEval("{ for(b in list(c(1L,3L,5L,7L),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- c(10L,20L) };  b }", ", , 1\n\n     [,1] [,2]\n[1,]  20L   NA\n[2,]  10L   NA");
    assertEval("{ for(b in list(c(1L,3L,5L,7L),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ;  if (typeof(b)==\"logical\") { s <- b } ; b[2:1,1,1] <- c(10L,20L) } ; s }", ", , 1\n\n      [,1] [,2]\n[1,]  TRUE   NA\n[2,] FALSE   NA");
    assertEval("{ for(b in list(c(1L,3L,5L,7L),c(10L,12L,13L,5L))) { dim(b) <- c(2,2,1) ;  if (b[1]==10) { s <- b } ; b[2:1,1,1] <- c(10L,20L) } ; s }", ", , 1\n\n     [,1] [,2]\n[1,]  10L  13L\n[2,]  12L   5L");
    assertEval("{ b <- c(1L,3L,5L,NA,4L,10L,8L,15L) ; dim(b) <- c(2,2,2) ; b[2:1,2:1,1:2] <- b ; b}", ", , 1\n\n     [,1] [,2]\n[1,]   NA   3L\n[2,]   5L   1L\n\n, , 2\n\n     [,1] [,2]\n[1,]  15L  10L\n[2,]   8L   4L");
    assertEvalError("{ b <- c(1L,3L,5L,6L) ; dim(b) <- c(2,2,1) ; b[c(NA,0,1),2,1] <- c(3L,10L) ; b }", "NAs are not allowed in subscripted assignments");
    assertEval("{ b <- c(1L,3L,5L,6L) ; dim(b) <- c(2,2,1) ; b[c(0,1,2),2,1] <- c(3L,10L) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   3L\n[2,]   3L  10L");
    assertEval("{ b <- c(1L,3L,5L,6L) ; dim(b) <- c(2,2,1) ; b[c(0,1,2,0,1,2),2,1] <- c(3L,10L) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   3L\n[2,]   3L  10L");
    assertEval("{ b <- c(1L,3L,5L,6L) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(3L,10L) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   3L  10L\n[2,]   3L   6L");
    assertEval("{ b <- c(1L,3L,5L,6L) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(my=3L) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   3L   3L\n[2,]   3L   6L");
    assertEval("{ b <- c(1L,3L,5L,6L) ; dim(b) <- c(2,2,1) ; b[logical(),1:2,1] <- c(my=3L) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   5L\n[2,]   3L   6L");

    // int -> double
    assertEval("{ for(v in list(c(2L,NA),c(3,4))) { x <- c(1,10,100,1000) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n     [,1]  [,2]\n[1,]  1.0 100.0\n[2,]  3.0   4.0");
    assertEval("{ for(b in list(c(1,3,5,7),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- c(10L,20L) };  b }", ", , 1\n\n     [,1] [,2]\n[1,]  20L   NA\n[2,]  10L   NA");
    assertEval("{ for(i in 1:2) { b <- c(1,3,5,7); dim(b) <- c(2,2,1) ; if (i==2) { s <- b } ; b[2:1,1,1] <- c(10L,20L) };  s }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  5.0\n[2,]  3.0  7.0");
    assertEvalError("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[c(NA,0,1),2,1] <- c(3L,10L) ; b }", "NAs are not allowed in subscripted assignments");
    assertEval("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[c(0,1,2,0,1,2),2,1] <- c(3L,10L) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  3.0\n[2,]  3.0 10.0");
    assertEval("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(3L,10L) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  3.0 10.0\n[2,]  3.0  6.0");
    assertEval("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(my=3L) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  3.0  3.0\n[2,]  3.0  6.0");
    assertEval("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[logical(),1:2,1] <- c(my=3L) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  5.0\n[2,]  3.0  6.0");

    // double -> double
    assertEval("{ for(v in list(c(3,4),c(NA,TRUE))) { x <- c(1,10,100,1000) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n     [,1]  [,2]\n[1,]  1.0 100.0\n[2,]   NA   1.0");
    assertEval("{ for(b in list(c(1,3,5,7),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- c(10,20) }; b }", ", , 1\n\n     [,1] [,2]\n[1,] 20.0   NA\n[2,] 10.0   NA");
    assertEval("{ for(i in 1:2) { b <- c(1,3,5,7); dim(b) <- c(2,2,1) ; if (i==2) { s <- b } ; b[2:1,1,1] <- c(10,20) };  s }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  5.0\n[2,]  3.0  7.0");
    assertEval("{ b <- c(1,3,5,NA,4,10,8,15) ; dim(b) <- c(2,2,2) ; b[2:1,2:1,1:2] <- b ; b }", ", , 1\n\n     [,1] [,2]\n[1,]   NA  3.0\n[2,]  5.0  1.0\n\n, , 2\n\n     [,1] [,2]\n[1,] 15.0 10.0\n[2,]  8.0  4.0");
    assertEvalError("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[c(NA,0,1),2,1] <- c(3,10) ; b }", "NAs are not allowed in subscripted assignments");
    assertEval("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[c(0,1,2,0,1,2),2,1] <- c(3,10) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  3.0\n[2,]  3.0 10.0");
    assertEval("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(3,11) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  3.0 11.0\n[2,]  3.0  6.0");
    assertEval("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(my=3) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  3.0  3.0\n[2,]  3.0  6.0");
    assertEval("{ b <- c(1,3,5,6) ; dim(b) <- c(2,2,1) ; b[logical(),1:2,1] <- c(my=3) ; b }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  5.0\n[2,]  3.0  6.0");

    // int -> complex
    assertEval("{ for(v in list(c(2L,NA),c(3,4))) { x <- c(1+1i,10+2i,100+3i,1000+4i) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n         [,1]       [,2]\n[1,] 1.0+1.0i 100.0+3.0i\n[2,] 3.0+0.0i   4.0+0.0i");
    assertEval("{ for(b in list(c(1+1i,3+2i,5,7),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- c(10L,20L) };  b }", ", , 1\n\n     [,1] [,2]\n[1,]  20L   NA\n[2,]  10L   NA");
    assertEval("{ for(i in 1:2) { b <- c(1+1i,3+2i,5+3i,7+4i); dim(b) <- c(2,2,1) ; if (i==2) { s <- b } ; b[2:1,1,1] <- c(10L,20L) }; s }", ", , 1\n\n         [,1]     [,2]\n[1,] 1.0+1.0i 5.0+3.0i\n[2,] 3.0+2.0i 7.0+4.0i");
    assertEvalError("{ b <- c(1+2i,3+3i,5+1i,6+2i) ; dim(b) <- c(2,2,1) ; b[c(NA,0,1),2,1] <- c(3L,10L) ; b }", "NAs are not allowed in subscripted assignments");
    assertEval("{ b <- c(1+1i,3+10i,5+2i,6) ; dim(b) <- c(2,2,1) ; b[c(0,1,2,0,1,2),2,1] <- c(3L,10L) ; b }", ", , 1\n\n          [,1]      [,2]\n[1,]  1.0+1.0i  3.0+0.0i\n[2,] 3.0+10.0i 10.0+0.0i");
    assertEval("{ b <- c(1+2i,3+3i,5+4i,6+1i) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(3L,10L) ; b }", ", , 1\n\n         [,1]      [,2]\n[1,] 3.0+0.0i 10.0+0.0i\n[2,] 3.0+3.0i  6.0+1.0i");
    assertEval("{ b <- c(1+1i,3+5i,5+6i,6+7i) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(my=3L) ; b }", ", , 1\n\n         [,1]     [,2]\n[1,] 3.0+0.0i 3.0+0.0i\n[2,] 3.0+5.0i 6.0+7.0i");
    assertEval("{ b <- c(1+5i,3+1i,5+2i,6+3i) ; dim(b) <- c(2,2,1) ; b[logical(),1:2,1] <- c(my=3L) ; b }", ", , 1\n\n         [,1]     [,2]\n[1,] 1.0+5.0i 5.0+2.0i\n[2,] 3.0+1.0i 6.0+3.0i");

    // double -> complex
    assertEval("{ for(v in list(c(2,NA),c(3L,4L))) { x <- c(1+1i,10+2i,100+3i,1000+4i) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n         [,1]       [,2]\n[1,] 1.0+1.0i 100.0+3.0i\n[2,] 3.0+0.0i   4.0+0.0i");
    assertEval("{ for(i in 1:2) { b <- c(1+1i,3+2i,5+3i,7+4i); dim(b) <- c(2,2,1) ; if (i==2) { s <- b } ; b[2:1,1,1] <- c(10,20) }; s }", ", , 1\n\n         [,1]     [,2]\n[1,] 1.0+1.0i 5.0+3.0i\n[2,] 3.0+2.0i 7.0+4.0i");
    assertEvalError("{ b <- c(1+2i,3+3i,5+1i,6+2i) ; dim(b) <- c(2,2,1) ; b[c(NA,0,1),2,1] <- c(3,10) ; b }", "NAs are not allowed in subscripted assignments");
    assertEval("{ b <- c(1+1i,3+10i,5+2i,6) ; dim(b) <- c(2,2,1) ; b[c(0,1,2,0,1,2),2,1] <- c(3,10) ; b }", ", , 1\n\n          [,1]      [,2]\n[1,]  1.0+1.0i  3.0+0.0i\n[2,] 3.0+10.0i 10.0+0.0i");
    assertEval("{ b <- c(1+2i,3+3i,5+4i,6+1i) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(3,10) ; b }", ", , 1\n\n         [,1]      [,2]\n[1,] 3.0+0.0i 10.0+0.0i\n[2,] 3.0+3.0i  6.0+1.0i");
    assertEval("{ b <- c(1+1i,3+5i,5+6i,6+7i) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(my=3) ; b }", ", , 1\n\n         [,1]     [,2]\n[1,] 3.0+0.0i 3.0+0.0i\n[2,] 3.0+5.0i 6.0+7.0i");
    assertEval("{ b <- c(1+5i,3+1i,5+2i,6+3i) ; dim(b) <- c(2,2,1) ; b[logical(),1:2,1] <- c(my=3) ; b }", ", , 1\n\n         [,1]     [,2]\n[1,] 1.0+5.0i 5.0+2.0i\n[2,] 3.0+1.0i 6.0+3.0i");
    assertEval("{ for(b in list(c(1+1i,3+2i,5,7),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- c(10,20) };  b }", ", , 1\n\n     [,1] [,2]\n[1,] 20.0   NA\n[2,] 10.0   NA");

    // complex -> complex
    assertEval("{ for(v in list(c(2+1i,NA),c(3L,4L))) { x <- c(1+1i,10+2i,100+3i,1000+4i) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }", ", , 1\n\n         [,1]       [,2]\n[1,] 1.0+1.0i 100.0+3.0i\n[2,] 3.0+0.0i   4.0+0.0i");
    assertEval("{ for(i in 1:2) { b <- c(1+1i,3+2i,5+3i,7+4i); dim(b) <- c(2,2,1) ; if (i==2) { s <- b } ; b[2:1,1,1] <- c(10+1i,20+2i) }; s }", ", , 1\n\n         [,1]     [,2]\n[1,] 1.0+1.0i 5.0+3.0i\n[2,] 3.0+2.0i 7.0+4.0i");
    assertEvalError("{ b <- c(1+2i,3+3i,5+1i,6+2i) ; dim(b) <- c(2,2,1) ; b[c(NA,0,1),2,1] <- c(3+1i,10+2i) ; b }", "NAs are not allowed in subscripted assignments");
    assertEval("{ b <- c(1+1i,3+10i,5+2i,6) ; dim(b) <- c(2,2,1) ; b[c(0,1,2,0,1,2),2,1] <- c(3+1i,10+2i) ; b }", ", , 1\n\n          [,1]      [,2]\n[1,]  1.0+1.0i  3.0+1.0i\n[2,] 3.0+10.0i 10.0+2.0i");
    assertEval("{ b <- c(1+2i,3+3i,5+4i,6+1i) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(3+1i,10+5i) ; b }", ", , 1\n\n         [,1]      [,2]\n[1,] 3.0+1.0i 10.0+5.0i\n[2,] 3.0+3.0i  6.0+1.0i");
    assertEval("{ b <- c(1+1i,3+5i,5+6i,6+7i) ; dim(b) <- c(2,2,1) ; b[c(TRUE,FALSE),1:2,1] <- c(my=3+5i) ; b }", ", , 1\n\n         [,1]     [,2]\n[1,] 3.0+5.0i 3.0+5.0i\n[2,] 3.0+5.0i 6.0+7.0i");
    assertEval("{ b <- c(1+5i,3+1i,5+2i,6+3i) ; dim(b) <- c(2,2,1) ; b[logical(),1:2,1] <- c(my=3+100i) ; b }", ", , 1\n\n         [,1]     [,2]\n[1,] 1.0+5.0i 5.0+2.0i\n[2,] 3.0+1.0i 6.0+3.0i");
    assertEval("{ for(b in list(c(1+1i,3+2i,5,7),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- c(10+5i,20+6i) };  b }", ", , 1\n\n          [,1] [,2]\n[1,] 20.0+6.0i   NA\n[2,] 10.0+5.0i   NA");
    assertEval("{ b <- c(1+2i,3+5i,5,NA,4,10+1i,8,15) ; dim(b) <- c(2,2,2) ; b[2:1,2:1,1:2] <- b ; b }", ", , 1\n\n         [,1]     [,2]\n[1,]       NA 3.0+5.0i\n[2,] 5.0+0.0i 1.0+2.0i\n\n, , 2\n\n          [,1]      [,2]\n[1,] 15.0+0.0i 10.0+1.0i\n[2,]  8.0+0.0i  4.0+0.0i");

    // guards
    assertEval("{ for(b in list(c(1+1i,3+2i,5,7),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- c(TRUE,FALSE) };  b }", ", , 1\n\n      [,1] [,2]\n[1,] FALSE   NA\n[2,]  TRUE   NA");
    assertEval("{ for(b in list(list(1+1i,3+2i,5,7),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- c(TRUE,FALSE) };  b }", ", , 1\n\n      [,1] [,2]\n[1,] FALSE   NA\n[2,]  TRUE   NA");

    // copiers
    assertEval("{ for(b in list(c(TRUE,FALSE,FALSE,NA),1:4)) { dim(b) <- c(2,2,1); s <- b; b[2:1,1,1] <- c(TRUE,FALSE) };  b }", ", , 1\n\n     [,1] [,2]\n[1,]   0L   3L\n[2,]   1L   4L");
    assertEval("{ for(b in list(c(TRUE,FALSE,FALSE,NA),1:4)) { dim(b) <- c(2,2,1); s <- b; b[2:1,1,1] <- 2:3 };  b }", ", , 1\n\n     [,1] [,2]\n[1,]   3L   3L\n[2,]   2L   4L");
    assertEval("{ b <- 11:14 ; dim(b) <- c(2,2,1) ; iv <- b + b ; lv <- c(TRUE,FALSE,FALSE,TRUE) ; dim(lv) <- c(2,2,1) ; for(b in list(iv,lv)) { s <- b; b[2:1,1,1] <- 2:3 };  b }", ", , 1\n\n     [,1] [,2]\n[1,]   3L   0L\n[2,]   2L   1L");
    assertEval("{ b <- 11:14 ; dim(b) <- c(2,2,1) ; iv <- b + b ; lv <- c(TRUE,FALSE,FALSE,TRUE) ; dim(lv) <- c(2,2,1) ; for(b in list(iv,lv)) { s <- b; b[2:1,1,1] <- c(2,3) };  b }", ", , 1\n\n     [,1] [,2]\n[1,]  3.0  0.0\n[2,]  2.0  1.0");
    assertEval("{ b <- as.double(11:14) ; dim(b) <- c(2,2,1) ; dv <- b + b ; lv <- c(TRUE,FALSE,FALSE,TRUE) ; dim(lv) <- c(2,2,1) ; for(b in list(dv,lv)) { s <- b; b[2:1,1,1] <- c(0/0,-1/0) };  b }", ", , 1\n\n          [,1] [,2]\n[1,] -Infinity  0.0\n[2,]       NaN  1.0");
    assertEval("{ b <- as.double(11:14) ; dim(b) <- c(2,2,1) ; dv <- b ; lv <- c(TRUE,FALSE,FALSE,TRUE) ; dim(lv) <- c(2,2,1) ; for(b in list(dv,lv)) { s <- b; b[2:1,1,1] <- c(0/0,-1/0) };  b }", ", , 1\n\n          [,1] [,2]\n[1,] -Infinity  0.0\n[2,]       NaN  1.0");
    assertEval("{ for(b in list(c(TRUE,FALSE,FALSE,NA),1:4)) { dim(b) <- c(2,2,1); b[2:1,1,1] <- c(2+3i,3+4i) };  b }", ", , 1\n\n         [,1]     [,2]\n[1,] 3.0+4.0i 3.0+0.0i\n[2,] 2.0+3.0i 4.0+0.0i");
    assertEval("{ b <- c(11L,NA,12L,4L) ; dim(b) <- c(2,2,1) ; iv <- b + b ; lv <- c(TRUE,FALSE,FALSE,TRUE) ; dim(lv) <- c(2,2,1) ; for(b in list(iv,lv)) { s <- b; b[2:1,1,1] <- c(2+3i,3+4i) };  b }", ", , 1\n\n         [,1]     [,2]\n[1,] 3.0+4.0i 0.0+0.0i\n[2,] 2.0+3.0i 1.0+0.0i");
    assertEval("{ b <- c(11,NA,-12/0,4/0) ; dim(b) <- c(2,2,1) ; iv <- b + b ; lv <- c(TRUE,FALSE,FALSE,TRUE) ; dim(lv) <- c(2,2,1) ; for(b in list(iv,lv)) { s <- b; b[2:1,1,1] <- c(2+3i,3+4i) };  b }", ", , 1\n\n         [,1]     [,2]\n[1,] 3.0+4.0i 0.0+0.0i\n[2,] 2.0+3.0i 1.0+0.0i");
    assertEval("{ b <- c(11,NA,-12/0,4/0) ; dim(b) <- c(2,2,1) ; iv <- b; lv <- c(TRUE,FALSE,FALSE,TRUE) ; dim(lv) <- c(2,2,1) ; for(b in list(iv,lv)) { s <- b; b[2:1,1,1] <- c(2+3i,3+4i) };  b }", ", , 1\n\n         [,1]     [,2]\n[1,] 3.0+4.0i 0.0+0.0i\n[2,] 2.0+3.0i 1.0+0.0i");
    assertEval("{ b <- c(11+1i,NA,-12/0,4/0) ; dim(b) <- c(2,2,1) ; iv <- b + b ; lv <- c(TRUE,FALSE,FALSE,TRUE) ; dim(lv) <- c(2,2,1) ; for(b in list(iv,lv)) { s <- b; b[2:1,1,1] <- c(2+3i,3+4i) };  b }", ", , 1\n\n         [,1]     [,2]\n[1,] 3.0+4.0i 0.0+0.0i\n[2,] 2.0+3.0i 1.0+0.0i");
    assertEval("{ b <- c(11+1i,NA,-12/0,4/0) ; dim(b) <- c(2,2,1) ; iv <- b + c(1+2i,3+4i) ; lv <- c(TRUE,FALSE,FALSE,TRUE) ; dim(lv) <- c(2,2,1) ; for(b in list(iv,lv)) { s <- b; b[2:1,1,1] <- c(2+3i,3+4i) };  b }", ", , 1\n\n         [,1]     [,2]\n[1,] 3.0+4.0i 0.0+0.0i\n[2,] 2.0+3.0i 1.0+0.0i");
    assertEval("{ for(b in list(c(TRUE,FALSE,FALSE,NA),1:4)) { dim(b) <- c(2,2,1); b[2:1,1,1] <- c(\"X\",\"Y\") };  b }", ", , 1\n\n     [,1] [,2]\n[1,]  \"Y\" \"3L\"\n[2,]  \"X\" \"4L\"");
    assertEval("{ for(b in list(1:4, c(TRUE,FALSE,FALSE,NA))) { dim(b) <- c(2,2,1); b[2:1,1,1] <- c(\"X\",\"Y\") };  b }", ", , 1\n\n     [,1]    [,2]\n[1,]  \"Y\" \"FALSE\"\n[2,]  \"X\"      NA");
    assertEval("{ for(b in list(c(1,2,3,8), c(TRUE,FALSE,FALSE,NA))) { dim(b) <- c(2,2,1); b[2:1,1,1] <- c(\"X\",\"Y\") };  b }", ", , 1\n\n     [,1]    [,2]\n[1,]  \"Y\" \"FALSE\"\n[2,]  \"X\"      NA");
    assertEval("{ for(b in list(c(\"a\",\"b\",\"C\",\"DD\"), c(TRUE,FALSE,FALSE,NA))) { dim(b) <- c(2,2,1); s <- b ; b[2:1,1,1] <- c(\"X\",\"Y\") };  b }", ", , 1\n\n     [,1]    [,2]\n[1,]  \"Y\" \"FALSE\"\n[2,]  \"X\"      NA");
    assertEval("{ typeof({ for(b in list(1:4, list(TRUE,1, 3+4i, 0/0))) { dim(b) <- c(2,2,1); b[2:1,1,1] <- list(1L,TRUE) };  b }) }", "\"list\"");
    assertEval("{ for(b in list(1:4, list(TRUE,1, 3+4i, 0/0))) { dim(b) <- c(2,2,1); b[2:1,1,1] <- list(1L,TRUE) };  b }", ", , 1\n\n     [,1]     [,2]\n[1,] TRUE 3.0+4.0i\n[2,]   1L      NaN");
    assertEvalError("{ for(b in list(1:4, function(){3})) { if (typeof(b)==\"integer\") { dim(b) <- c(2,2,1) }; b[2:1,1,1] <- list(1L,TRUE) };  b }", "object of type 'closure' is not subsettable");
    assertEval("{ for(b in list(list(TRUE,1, 3+4i, 0/0), 1:4)) { dim(b) <- c(2,2,1); s <- b ; b[2:1,1,1] <- list(1L,TRUE) };  dim(b) <- NULL ; b }", "[[1]]\nTRUE\n\n[[2]]\n1L\n\n[[3]]\n3L\n\n[[4]]\n4L");
    assertEvalError("{ for(b in list(as.raw(11:14), 1:4)) { dim(b) <- c(2,2,1); s <- b ; b[2:1,1,1] <- as.raw(111:112) }; b }", "incompatible types (from raw to integer) in subassignment type fix");

    assertEval("{ x <- NULL; x[1,2,2] <- x ; x }", "NULL");
    assertEval("{ x <- NULL; s <- x ; x[1,2,2] <- NULL ; x }", "NULL");
    assertEval("{ x <- NULL; x[1,2,2] <- NULL ; x }", "NULL");

  }

  @Test
  public void testAssignment()  {
    assertEvalError("{ f <- function() { x[1,1,2] <- 3 } ; f() }", "object 'x' not found");
    assertEvalError("{ g <- function() { if (FALSE) { x <- 1 } ; f <- function() { x[1,1,2] <- 3 } ; f() } ; g() }", "object 'x' not found");
    assertEvalError("{ f <- function(i) { if (i==1) { x <- 1:4 ; dim(x) <- c(1,2,2) } ; x[1,1,2] <- 13 } ; f(1) ; f(2) }", "object 'x' not found");
    assertEval("{ g <- function() { x <- 11:14 ; dim(x) <- c(1,2,2) ; f <- function(i) { if (i==1) { x <- 1:4 ; dim(x) <- c(1,2,2) } ; x[1,1,2] <- 13 ; x } ; f(1) ; f(2) } ; g() }", ", , 1\n\n     [,1] [,2]\n[1,] 11.0 12.0\n\n, , 2\n\n     [,1] [,2]\n[1,] 13.0 14.0");
    assertEval("{ g <- function() { x <- 11:14 ; dim(x) <- c(1,2,2) ; f <- function(i) { if (i==1) { x <- 1:4 ; dim(x) <- c(1,2,2) } ; x[1,1,2] <- 13 ; x } ; f(2) ; f(2) ; f(1) } ; g() }", ", , 1\n\n     [,1] [,2]\n[1,]  1.0  2.0\n\n, , 2\n\n     [,1] [,2]\n[1,] 13.0  4.0");
    assertEval("{ g <- function() { x <- 11:14 ; dim(x) <- c(1,2,2) ; f <- function(i) { if (i==1) { x <- 1:4 ; dim(x) <- c(1,2,2) } ; x[1,1,2] <- 13L ; x } ; f(2) ; f(2) ; f(1) } ; g() }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   2L\n\n, , 2\n\n     [,1] [,2]\n[1,]  13L   4L");
    assertEvalError("{ x[1,1,2] <- 3 }", "object 'x' not found");
    assertEvalError("{ z <- 1 ; x[1,1,2] <- z }", "object 'x' not found");
    assertEvalError("{ f <- function() { quote({ x[1,1,2] <- 2 }) } ; eval(f()) }", "object 'x' not found");
    assertEvalError("{ f <- function() { quote({ x[1,2,1] <- 2 }) } ; g <- function() { eval(f()) } ; g() }", "object 'x' not found");

    assertEvalError("{ x <- 1:4 ; dim(x) <- c(2,2,1); x[2,1,1] <<- 100 ; x }", "object 'x' not found");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,2,1); f <- function() { x[2,1,1] <<- 100 } ; f() ; x }", ", , 1\n\n      [,1] [,2]\n[1,]   1.0  3.0\n[2,] 100.0  4.0");
    assertEval("{ x <- 1:4 ; dim(x) <- c(2,2,1); f <- function() { x[2,1,1] <<- 100L ; x } ; f() }", ", , 1\n\n     [,1] [,2]\n[1,]   1L   3L\n[2,] 100L   4L");
  }


  @Test
  public void testDynamic()  {
    assertEval("{ l <- quote(x[1,1] <- 10) ; f <- function() { eval(l) } ; x <- matrix(1:4,nrow=2) ; f() ; x }", "     [,1] [,2]\n[1,]   1L   3L\n[2,]   2L   4L");
  }
}
