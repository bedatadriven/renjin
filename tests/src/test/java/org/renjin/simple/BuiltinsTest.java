package org.renjin.simple;

import org.junit.Test;

public class BuiltinsTest extends SimpleTestBase {

  @Test
  public void testSequence()  {
    assertEval("5L:10L ", "c(5L, 6L, 7L, 8L, 9L, 10L)");
    assertEval("5L:(0L-5L) ", "c(5L, 4L, 3L, 2L, 1L, 0L, -1L, -2L, -3L, -4L, -5L)");
    assertEval("1:10 ", "c(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)"); // note: yes, GNU R will convert to integers
    assertEval("1:(0-10) ", "c(1L, 0L, -1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L)");
    assertEval("1L:(0-10) ", "c(1L, 0L, -1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L)");
    assertEval("1:(0L-10L) ", "c(1L, 0L, -1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L)");
    assertEval("(0-12):1.5 ", "c(-12L, -11L, -10L, -9L, -8L, -7L, -6L, -5L, -4L, -3L, -2L, -1L, 0L, 1L)");
    assertEval("1.5:(0-12) ", "c(1.5, 0.5, -0.5, -1.5, -2.5, -3.5, -4.5, -5.5, -6.5, -7.5, -8.5, -9.5, -10.5, -11.5)");
    assertEval("(0-1.5):(0-12) ", "c(-1.5, -2.5, -3.5, -4.5, -5.5, -6.5, -7.5, -8.5, -9.5, -10.5, -11.5)");
    assertEval("10:1 ", "c(10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L)");
    assertEval("(0-5):(0-9) ", "c(-5L, -6L, -7L, -8L, -9L)");
    assertEvalError("1:as.integer(NA)", "NA/NaN argument");

    assertEval("seq(1,10) ", "c(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)");
    assertEval("seq(10,1) ", "c(10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L)");
    assertEval("seq(from=1,to=3) ", "c(1L, 2L, 3L)");
    assertEval("seq(to=-1,from=-10) ", "c(-10L, -9L, -8L, -7L, -6L, -5L, -4L, -3L, -2L, -1L)");
    assertEval("seq(length.out=13.4) ", "c(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L)");
    assertEval("seq(length.out=0) ", "integer(0)");
    assertEval("seq(length.out=1) ", "1L");
    assertEval("seq(along.with=10) ", "1L");
    assertEval("seq(along.with=NA) ", "1L");
    assertEval("seq(along.with=1:10) ", "c(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)");
    assertEval("seq(along.with=-3:-5) ", "c(1L, 2L, 3L)");
    assertEval("seq(from=1.4) ", "1L");
    assertEval("seq(from=1.7) ", "1L");
    assertEval("seq(from=10:12) ", "c(1L, 2L, 3L)");
    assertEval("seq(from=c(TRUE, FALSE)) ", "c(1L, 2L)");
    assertEval("seq(from=TRUE, to=TRUE, length.out=0) ", "integer(0)");
    assertEval("round(seq(from=10.5, to=15.4, length.out=4), digits=5) ", "c(10.5, 12.13333, 13.76667, 15.4)");
    assertEval("seq(from=11, to=12, length.out=2) ", "c(11.0, 12.0)");
    assertEval("seq(from=1,to=3,by=1) ", "c(1.0, 2.0, 3.0)");
    assertEval("seq(from=-10,to=-5,by=2) ", "c(-10.0, -8.0, -6.0)");
    assertEval("seq(from=-10.4,to=-5.8,by=2.1) ", "c(-10.4, -8.3, -6.2)");
    assertEval("round(seq(from=3L,to=-2L,by=-4.2), digits=5) ", "c(3.0, -1.2)");
    assertEval("seq(along=c(10,11,12)) ", "c(1L, 2L, 3L)"); // test partial name match
    assertEval("seq(1L,4L,2L) ", "c(1L, 3L)");
    assertEval("seq(1,-4,-2) ", "c(1.0, -1.0, -3.0)");
  }

  @Test
  public void testArrayConstructors()  {
    assertEval("integer() ", "integer(0)");
    assertEval("double() ", "numeric(0)");
    assertEval("logical() ", "logical(0)");
    assertEval("double(3) ", "c(0.0, 0.0, 0.0)");
    assertEval("logical(3L) ", "c(FALSE, FALSE, FALSE)");
    assertEval("character(1L) ", "''");
  }

  @Test
  public void testMaxWithInteger() {
    assertEval("max(1:10, 100:200, c(4.0, 5.0)) ", "200.0");
  }

  @Test
  public void testMaximum()  {
    assertEval("max((-1):100) ", "100L");
    assertEval("max(1:10, 100:200, c(4.0, 5.0), c(TRUE,FALSE,NA)) ", "NA");
    assertEval("max(2L, 4L) ", "4L");
    assertEval("max() ", "-Infinity");
    assertEval("max(c('hi','abbey','hello')) ", "'hi'");
    assertEval("max('hi','abbey','hello') ", "'hi'");
  }

  @Test
  public void testMinimum()  {
    assertEval("min((-1):100) ", "-1L");
    assertEval("min(1:10, 100:200, c(4.0, -5.0)) ", "-5.0");
    assertEval("min(1:10, 100:200, c(4.0, 5.0), c(TRUE,FALSE,NA)) ", "NA");
    assertEval("min(2L, 4L) ", "2L");
    assertEval("min() ", "Infinity");
    assertEval("min(c('hi','abbey','hello')) ", "'abbey'");
    assertEval("min('hi','abbey','hello') ", "'abbey'");
    assertEval("min('hi',100) ", "'100.0'");
    assertEval("min(c(1,2,0/0)) ", "NaN");
    assertEval("max(c(1,2,0/0)) ", "NaN");
  }

  @Test
  public void testRep()  {
    assertEval("rep(1,3) ", "1.0, 1.0, 1.0");
    assertEval("rep(1:3,2) ", "1L, 2L, 3L, 1L, 2L, 3L");
    assertEval("rep(c(1,2),0) ", "numeric(0)");
    assertEval("rep(1:3, length.out=4) ", "1L, 2L, 3L, 1L");
    assertEval("rep(1:3, length.out=NA) ", "1L, 2L, 3L");
    assertEval("rep(as.raw(14), 4) ", "0e, 0e, 0e, 0e");

    assertEval("x <- as.raw(11) ; names(x) <- c('X') ; rep(x, 3) ", " X  X  X\n0b 0b 0b");
    assertEval("x <- as.raw(c(11,12)) ; names(x) <- c('X','Y') ; rep(x, 2) ", " X  Y  X  Y\n0b 0c 0b 0c");
    assertEval("x <- c(TRUE,NA) ; names(x) <- c('X',NA) ; rep(x, length.out=3) ", "   X <NA>    X\nTRUE   NA TRUE");
    assertEval("x <- 1L ; names(x) <- c('X') ; rep(x, times=2) } ", " X  X\n1L 1L");
    assertEval("x <- 1 ; names(x) <- c('X') ; rep(x, times=0) ", "named numeric(0)");
    assertEval("x <- 1+1i ; names(x) <- c('X') ; rep(x, times=2) ", "       X        X\n1.0+1.0i 1.0+1.0i");
    assertEval("x <- c(1+1i,1+2i) ; names(x) <- c('X') ; rep(x, times=2) ", "       X     <NA>        X     <NA>\n1.0+1.0i 1.0+2.0i 1.0+1.0i 1.0+2.0i");
    assertEval("x <- c('A','B') ; names(x) <- c('X') ; rep(x, length.out=3) ", "  X <NA>   X\n'A'  'B' 'A'");
  }

  @Test
  public void testCombine()  {
    assertEval("c(1.0,1L) ", "1.0, 1.0");
    assertEval("c(1L,1.0) ", "1.0, 1.0");
    assertEval("c(TRUE,1L,1.0,list(3,4)) ", "[[1]]\nTRUE\n\n[[2]]\n1L\n\n[[3]]\n1.0\n\n[[4]]\n3.0\n\n[[5]]\n4.0");
    assertEval("c(TRUE,1L,1.0,list(3,list(4,5))) ", "[[1]]\nTRUE\n\n[[2]]\n1L\n\n[[3]]\n1.0\n\n[[4]]\n3.0\n\n[[5]]\n[[5]][[1]]\n4.0\n\n[[5]][[2]]\n5.0");
    assertEval("c() ", "NULL");
    assertEval("c(NULL,NULL) ", "NULL");
    assertEval("c(NULL,1,2,3) ", "1.0, 2.0, 3.0");
    assertEval("f <- function(x,y) { c(x,y) } ; f(1,1) ; f(1, TRUE) ", "1.0, 1.0");
    assertEval("f <- function(x,y) { c(x,y) } ; f(1,1) ; f(1, TRUE) ; f(NULL, NULL) ", "NULL");
    assertEval("c('hello', 'hi') ", "'hello', 'hi'");
    assertEval("c(1+1i, as.raw(10)) ", "1.0+1.0i, 10.0+0.0i");
    assertEval("c(as.raw(10), as.raw(20)) ", "0a, 14");

    assertEval("c(x=1,y=2) ", "  x   y\n1.0 2.0");
    assertEval("c(x=1,2) ", "  x    \n1.0 2.0");
    assertEval("x <- 1:2 ; names(x) <- c('A',NA) ; c(x,test=x) ", " A <NA> test.A test.NA\n1L   2L     1L      2L");
    assertEval("c(a=1,b=2:3,list(x=FALSE))  ", "$a\n1.0\n\n$b1\n2L\n\n$b2\n3L\n\n$x\nFALSE");
    assertEval("c(1,z=list(1,b=22,3)) ", "[[1]]\n1.0\n\n$z1\n1.0\n\n$z.b\n22.0\n\n$z3\n3.0");

    assertEval("c(1i,0/0) ", "0.0+1.0i, NaN+0.0i"); // yes, this is done by GNU-R, note inconsistency with as.complex(0/0)
  }

  @Test
  public void testIsNA()  {
    assertEval("is.na(c(1,2,3,4)) ", "FALSE, FALSE, FALSE, FALSE");
    assertEval("is.na(1[10]) ", "TRUE");
    assertEval("is.na(c(1[10],2[10],3)) ", "TRUE, TRUE, FALSE");
    assertEval("is.na(list(1[10],1L[10],list(),integer())) ", "TRUE, TRUE, FALSE, FALSE");
  }

  @Test
  public void testCasts()  {
    assertEval("as.integer(c(1,2,3)) ", "1L, 2L, 3L");
    assertEval("as.integer(list(c(1),2,3)) ", "1L, 2L, 3L");
    assertEval("as.integer(list(integer(),2,3)) ", "NA, 2L, 3L");
    assertEval("as.integer(list(list(1),2,3)) ", "NA, 2L, 3L");
    assertEval("as.integer(list(1,2,3,list())) ", "1L, 2L, 3L, NA");

    assertEval("m<-matrix(1:6, nrow=3) ; as.integer(m) ", "1L, 2L, 3L, 4L, 5L, 6L");
    assertEval("m<-matrix(1:6, nrow=3) ; as.vector(m, 'any') ", "1L, 2L, 3L, 4L, 5L, 6L");
    assertEval("m<-matrix(1:6, nrow=3) ; as.vector(mode = 'integer', x=m) ", "1L, 2L, 3L, 4L, 5L, 6L");
    assertEval("as.vector(list(1,2,3), mode='integer') ", "1L, 2L, 3L");

    assertEval("as.double('1.27') ", "1.27");
    assertEval("as.double(1L) ", "1.0");
    assertEval("as.double('TRUE') ", "NA");
    assertEval("as.double(c('1','hello')) ", "1.0, NA");

    assertEval("as.character(1L) ", "'1L'");
    assertEval("as.character(TRUE) ", "'TRUE'");
    assertEval("as.character(1:3) ", "'1L', '2L', '3L'");
    assertEval("as.character(NULL) ", "character(0)");
    assertEval("as.character(list(c('hello', 'hi'))) ", "'c(\\'hello\\', \\'hi\\')'");
    assertEval("as.character(list(list(c('hello', 'hi')))) ", "'list(c(\\'hello\\', \\'hi\\'))'");
    assertEval("as.character(list(1,2,3)) ", "'1.0', '2.0', '3.0'");
    assertEval("as.character(list(c(2L, 3L))) ", "'2:3'");
    assertEval("as.character(list(c(2L, 3L, 5L))) ", "'c(2L, 3L, 5L)'");

    assertEval("as.raw(list(1,2,3)) ", "01, 02, 03");
    assertEval("as.raw(list('1', 2L, 3.4)) ", "01, 02, 03");
    assertEval("as.raw(c(1,1000,NA)) ", "01, 00, 00");

    assertEval("as.logical(1) ", "TRUE");
    assertEval("as.logical('false') ", "FALSE");
    assertEval("as.logical('dummy') ", "NA"); // no warning produced

    assertEval("as.complex(0) ", "0.0+0.0i");
    assertEval("as.complex(TRUE) ", "1.0+0.0i");
    assertEval("as.complex('1+5i') ", "1.0+5.0i");
    assertEval("as.complex('1e10+5i') ", "1.0E10+5.0i");
    assertEval("as.complex('-1+5i') ", "-1.0+5.0i");
    assertEval("as.complex('-1-5i') ", "-1.0-5.0i");
    assertEval("as.complex('-.1e10+5i') ", "-1.0E9+5.0i");
    assertEval("as.complex('1e-2+3i') ", "0.01+3.0i");
    assertEval("as.complex('+.1e+2-3i') ", "10.0-3.0i");
    assertEval("as.complex(0/0) ", "NA");
    assertEval("as.complex(c(0/0, 0/0)) ", "NA, NA");

    assertEval("l <- list(1) ; attr(l, 'my') <- 1; as.list(l) ", "[[1]]\n1.0\nattr(,'my')\n1.0");
    assertEval("l <- 1 ; attr(l, 'my') <- 1; as.list(l) ", "[[1]]\n1.0");
    assertEval("l <- c(x=1) ; as.list(l) ", "$x\n1.0");

    // shortcuts in views (only some combinations)
    assertEval("as.complex(as.character(c(1+1i,1+1i))) ", "1.0+1.0i, 1.0+1.0i");
    assertEval("as.complex(as.double(c(1+1i,1+1i))) ", "1.0+0.0i, 1.0+0.0i");
    assertEval("as.complex(as.integer(c(1+1i,1+1i))) ", "1.0+0.0i, 1.0+0.0i");
    assertEval("as.complex(as.logical(c(1+1i,1+1i))) ", "1.0+0.0i, 1.0+0.0i");
    assertEval("as.complex(as.raw(c(1+1i,1+1i))) ", "1.0+0.0i, 1.0+0.0i");

    assertEval("as.double(as.logical(c(10,10))) ", "1.0, 1.0");
    assertEval("as.integer(as.logical(-1:1)) ", "1L, 0L, 1L");
    assertEval("as.raw(as.logical(as.raw(c(1,2)))) ", "01, 01");
    assertEval("as.character(as.double(1:5)) ", "'1.0', '2.0', '3.0', '4.0', '5.0'");
    assertEval("as.character(as.complex(1:2)) ", "'1.0+0.0i', '2.0+0.0i'");

    // dropping dimensions
    assertEval("m <- matrix(1:6, nrow=2) ; as.double(m) ", "1.0, 2.0, 3.0, 4.0, 5.0, 6.0");
    assertEval("m <- matrix(c(1,2,3,4,5,6), nrow=2) ; as.integer(m) ", "1L, 2L, 3L, 4L, 5L, 6L");
    assertEval("m <- matrix(c(1,2,3,4,5,6), nrow=2) ; as.logical(m) ", "TRUE, TRUE, TRUE, TRUE, TRUE, TRUE");

    // dropping names
    assertEval("x <- 1:2; names(x) <- c('hello','hi') ; as.double(x) ", "1.0, 2.0");
    assertEval("x <- c(1,2); names(x) <- c('hello','hi') ; as.integer(x) ", "1L, 2L");
    assertEval("x <- c(0,2); names(x) <- c('hello','hi') ; as.logical(x) ", "FALSE, TRUE");

    assertEval("as.matrix(1) ", "     [,1]\n[1,]  1.0");
    assertEval("as.matrix(1:3) ", "     [,1]\n[1,]   1L\n[2,]   2L\n[3,]   3L");
    assertEval("x <- 1:3; z <- as.matrix(x); x ", "1L, 2L, 3L");
    assertEval("x <- 1:3 ; attr(x,'my') <- 10 ; attributes(as.matrix(x)) ", "$dim\n3L, 1L");

    assertEval("as.raw('09') ", "09");
    assertEval("as.raw('077') ", "4d");
    assertEval("as.raw('0004') ", "04");
  }

  @Test
  public void testSum()  {
    assertEval("sum(1:6, 3, 4) ", "28.0");
    assertEval("sum(1:6, 3L, TRUE) ", "25L");
    assertEval("sum() ", "0L");
    assertEval("sum(0, 1[3]) ", "NA");
    assertEval("sum(na.rm=FALSE, 0, 1[3]) ", "NA");
    assertEval("sum(0, na.rm=FALSE, 1[3]) ", "NA");
    assertEval("sum(0, 1[3], na.rm=FALSE) ", "NA");
    assertEval("sum(0, 1[3], na.rm=TRUE) ", "0.0");
    assertEval("`sum`(1:10) ", "55L");
    assertEval("sum(1+1i,2,NA, na.rm=TRUE) ", "3.0+1.0i");
  }

  @Test
  public void testApply()  {
    assertEval("lapply(1:3, function(x) { 2*x }) ", "[[1]]\n2.0\n\n[[2]]\n4.0\n\n[[3]]\n6.0");
    assertEval("lapply(1:3, function(x,y) { x*y }, 2) ", "[[1]]\n2.0\n\n[[2]]\n4.0\n\n[[3]]\n6.0");

    assertEval("sapply(1:3,function(x){x*2}) ", "2.0, 4.0, 6.0");
    assertEval("sapply(c(1,2,3),function(x){x*2}) ", "2.0, 4.0, 6.0");
    assertEval("sapply(list(1,2,3),function(x){x*2}) ", "2.0, 4.0, 6.0");
    assertEval("sapply(1:3, function(x) { if (x==1) { 1 } else if (x==2) { integer() } else { TRUE } }) ", "[[1]]\n1.0\n\n[[2]]\ninteger(0)\n\n[[3]]\nTRUE");
    assertEval("f<-function(g) { sapply(1:3, g) } ; f(function(x) { x*2 }) ", "2.0, 4.0, 6.0");
    assertEval("f<-function(g) { sapply(1:3, g) } ; f(function(x) { x*2 }) ; f(function(x) { TRUE }) ", "TRUE, TRUE, TRUE");
    assertEval("sapply(1:3, function(x) { if (x==1) { list(1) } else if (x==2) { list(NULL) } else { list(2) } }) ", "[[1]]\n1.0\n\n[[2]]\nNULL\n\n[[3]]\n2.0");
    assertEval("sapply(1:3, function(x) { if (x==1) { list(1) } else if (x==2) { list(NULL) } else { list() } }) ", "[[1]]\n[[1]][[1]]\n1.0\n\n[[2]]\n[[2]][[1]]\nNULL\n\n[[3]]\nlist()");
    assertEval("f<-function() { x<-2 ; sapply(1, function(i) { x }) } ; f() ", "2.0");

    assertEval("sapply(1:3, length) ", "1L, 1L, 1L");
    assertEval("f<-length; sapply(1:3, f) ", "1L, 1L, 1L");
    assertEval("sapply(1:3, `-`, 2) ", "-1.0, 0.0, 1.0");
    assertEval("sapply(1:3, '-', 2) ", "-1.0, 0.0, 1.0");

    assertEval("sapply(1:2, function(i) { if (i==1) { as.raw(0) } else { as.raw(10) } }) ", "00, 0a");
    assertEval("sapply(1:2, function(i) { if (i==1) { as.raw(0) } else { 'hello' }} ) } ", "'00', 'hello'");
    assertEval("sapply(1:2, function(i) { if (i==1) { as.raw(0) } else { 5+10i } }) ", "0.0+0.0i, 5.0+10.0i");

    // matrix support
    assertEval("sapply(1:3, function(i) { list(1,2) }) ", "     [,1] [,2] [,3]\n[1,]  1.0  1.0  1.0\n[2,]  2.0  2.0  2.0");
    assertEval("sapply(1:3, function(i) { if (i < 3) { list(1,2) } else { c(11,12) } }) ", "     [,1] [,2] [,3]\n[1,]  1.0  1.0 11.0\n[2,]  2.0  2.0 12.0");
    assertEval("sapply(1:3, function(i) { if (i < 3) { c(1+1i,2) } else { c(11,12) } }) ", "         [,1]     [,2]      [,3]\n[1,] 1.0+1.0i 1.0+1.0i 11.0+0.0i\n[2,] 2.0+0.0i 2.0+0.0i 12.0+0.0i");

    // names
    assertEval("( sapply(1:3, function(i) { if (i < 3) { list(xxx=1) } else {list(zzz=2)} })) ", "$xxx\n1.0\n\n$xxx\n1.0\n\n$zzz\n2.0");
    assertEval("( sapply(1:3, function(i) { list(xxx=1:i) } )) ", "$xxx\n1L\n\n$xxx\n1L, 2L\n\n$xxx\n1L, 2L, 3L");
    assertEval("sapply(1:3, function(i) { if (i < 3) { list(xxx=1) } else {list(2)} }) ", "$xxx\n1.0\n\n$xxx\n1.0\n\n[[3]]\n2.0");
    assertEval("( sapply(1:3, function(i) { if (i < 3) { c(xxx=1) } else {c(2)} })) ", "xxx xxx    \n1.0 1.0 2.0");
    assertEval("f <- function() { lapply(c(X='a',Y='b'), function(x) { c(a=x) })  } ; f() ", "$X\n  a\n'a'\n\n$Y\n  a\n'b'");
    assertEval("f <- function() { sapply(c(1,2), function(x) { c(a=x) })  } ; f() ", "  a   a\n1.0 2.0");
    assertEval("f <- function() { sapply(c(X=1,Y=2), function(x) { c(a=x) })  } ; f() ", "X.a Y.a\n1.0 2.0");
    assertEval("f <- function() { sapply(c('a','b'), function(x) { c(a=x) })  } ; f() ", "a.a b.a\n'a' 'b'");
    assertEval("f <- function() { sapply(c(X='a',Y='b'), function(x) { c(a=x) })  } ; f() ", "X.a Y.a\n'a' 'b'");
    assertEval("sapply(c('a','b','c'), function(x) { x }) ", "  a   b   c\n'a' 'b' 'c'");

    assertEval("sapply(c(a=1,b=2,`c+`=3), function(x) { c(x=x*x) }) ", "a.x b.x c+.x\n1.0 4.0  9.0");
    assertEval("sapply(c(a=1,2,3,`c+`=3), function(x) { c(x=x*x) }) ", "a.x   x   x c+.x\n1.0 4.0 9.0  9.0");
    assertEval("sapply(c(a=1,2,3,`c+`=3), function(x) { c(x*x) }) ", "  a          c+\n1.0 4.0 9.0 9.0");
    assertEval("sapply(c(a=1,2,3,`c+`=3), function(x) { rep(x,x) }) ", "$a\n1.0\n\n[[2]]\n2.0, 2.0\n\n[[3]]\n3.0, 3.0, 3.0\n\n$`c+`\n3.0, 3.0, 3.0");
    assertEval("l <- (sapply(c(a=1,2,3,`c+`=4), function(i) { if (i==1) { c(x=5) } else if (i==2) {c(z=5) } else if (i==3) { c(1) } else { list(`c+`=3) } })) ; names(l) ", "'a.x', 'z', '', 'c+.c+'");
    assertEval("l <- (sapply(c(a=1,2,3,`c+`=4), function(i) { if (i==1) { c(x=5) } else if (i==2) {c(z=5) } else if (i==3) { c(1) } else { list(`c+`=3,d=5) } })) ; l ", "$a\n  x\n5.0\n\n[[2]]\n  z\n5.0\n\n[[3]]\n1.0\n\n$`c+`\n$`c+`$`c+`\n3.0\n\n$`c+`$d\n5.0");
    assertEval("l <- (sapply(c(a=1,2,3,`c+`=4), function(i) { if (i==1) { list(x=5) } else if (i==2) {list(z=5) } else if (i==3) { list(1) } else { list(`c+`=3) } })) ; l ", "$a.x\n5.0\n\n$z\n5.0\n\n[[3]]\n1.0\n\n$`c+.c+`\n3.0");
    assertEval("sapply(c(a=1,2,3,`c+`=3), function(x) { as.raw(x) }) ", " a       c+\n01 02 03 03");
    assertEval("sapply(c(a=1,2,3,`c+`=3), function(x) { list(z=NULL) }) ", "$a.z\nNULL\n\n$z\nNULL\n\n$z\nNULL\n\n$`c+.z`\nNULL");
    assertEval("sapply(c(a=1,2,3,`c+`=3), function(x) { NULL }) ", "$a\nNULL\n\n[[2]]\nNULL\n\n[[3]]\nNULL\n\n$`c+`\nNULL");
    assertEval("sapply(1:3, function(x) { c(as.character(x),if (x > 2) { x + 2i } else { x }) }) ", "     [,1] [,2]       [,3]\n[1,] '1L' '2L'       '3L'\n[2,] '1L' '2L' '3.0+2.0i'");
    assertEval("sapply(1:3, function(x) { c(x,x) }) ", "     [,1] [,2] [,3]\n[1,]   1L   2L   3L\n[2,]   1L   2L   3L");
    assertEval("sapply(c(TRUE,FALSE,NA), function(x) { c(x,x) }) ", "     [,1]  [,2] [,3]\n[1,] TRUE FALSE   NA\n[2,] TRUE FALSE   NA");
    assertEval("sapply(as.raw(11:13), function(x) { c(x,x) }) ", "     [,1] [,2] [,3]\n[1,]   0b   0c   0d\n[2,]   0b   0c   0d");
    assertEval("l <- list(as.raw(11), TRUE, 2L, 3, 4+1i, 'a') ; sapply(1:6, function(i) { l[[i]] } ) ", "'0b', 'TRUE', '2L', '3.0', '4.0+1.0i', 'a'");
    assertEval("l <- list(as.raw(11), TRUE, 2L, 3, 4+1i) ; sapply(1:5, function(i) { l[[i]] } ) ", "11.0+0.0i, 1.0+0.0i, 2.0+0.0i, 3.0+0.0i, 4.0+1.0i");
    assertEval("l <- list(as.raw(11), TRUE, 2L, 4) ; sapply(1:4, function(i) { l[[i]] } ) ", "11.0, 1.0, 2.0, 4.0");
    assertEval("l <- list(as.raw(11), TRUE, 2L) ; sapply(1:3, function(i) { l[[i]] } ) ", "11L, 1L, 2L");
    assertEval("l <- list(as.raw(11), TRUE) ; sapply(1:2, function(i) { l[[i]] } ) ", "TRUE, TRUE");
    assertEval("sapply(1:3, function(i) { rep(i, i+1) }) ", "[[1]]\n1L, 1L\n\n[[2]]\n2L, 2L, 2L\n\n[[3]]\n3L, 3L, 3L, 3L");
    assertEval("for (z in list(1:3,list(1L,5,10))) { x <- sapply(1:3, function(i,z) { i+z[[i]] }, z) } ; x ", "2.0, 7.0, 13.0");
    assertEval("for (z in list(list(list(10,11),list(11,12)),1:2)) { x <- sapply(1:2, function(i,z) { z[[i]] }, z) } ; x ", "1L, 2L");
    assertEval("for (z in list(list(1:2,1:3),list(list(10,11),list(11,12)),1:2)) { x <- sapply(1:2, function(i,z) { z[[i]] }, z) } ; x ", "1L, 2L");

    assertEval("for(i in 1:2) { x <- sapply(1:2, function(i) { l <- list(1:2,NULL) ; l[[i]] }) } ; x ", "[[1]]\n1L, 2L\n\n[[2]]\nNULL");
    assertEval("typeof({ for(i in 1:2) { x <- sapply(1:2, function(i) { l <- list(1:2,list(1,2)) ; l[[i]] }) } ; x }) ", "'list'");
    assertEval("for(i in 1:2) { x <- sapply(1:2, function(i) { l <- list(1:2,list(1,2)) ; l[[i]] }) } ; x ", "     [,1] [,2]\n[1,]   1L  1.0\n[2,]   2L  2.0");
    assertEval("for (z in list(c(TRUE,FALSE,NA),c(NA,FALSE,FALSE),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) } ; x ", "1.0, 2.0, 3.0");
    assertEval("for (z in list(c('a','b','x'),c('z','z','y'),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) } ; x ", "1.0, 2.0, 3.0");
    assertEval("for (z in list(c(1+2i,3+4i,5+6i),c(2+2i,3,4),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) } ; x ", "1.0, 2.0, 3.0");
    assertEval("y <- NULL ; for (z in list(c(1+2i,3+4i,5+6i),c(2+2i,3,4),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) ; if (is.null(y)) { y <- x } } ; y ", "1.0+2.0i, 3.0+4.0i, 5.0+6.0i");
    assertEval("y <- NULL ; for (z in list(as.raw(11:13),as.raw(21:23),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) ; if (is.null(y)) { y <- x } } ; y ", "0b, 0c, 0d");
    assertEval("for (idxs in list(1:3, c(1,2,3), c('a','x','z'))) { x <- sapply(idxs, function(i) { i }) } ; x ", "  a   x   z\n'a' 'x' 'z'");

    // lapply
    assertEval("for (idxs in list(1:3, c(1,2,3), c(Z='a','x','z'))) { x <- lapply(idxs, function(i) { i }) } ; x ", "$Z\n'a'\n\n[[2]]\n'x'\n\n[[3]]\n'z'");
    assertEval("for (idxs in list(as.list(1:3), c(1,2,3), c(Z='a','x','z'))) { x <- lapply(idxs, function(i) { i }) } ; x ", "$Z\n'a'\n\n[[2]]\n'x'\n\n[[3]]\n'z'");
    assertEval("for (idxs in list(as.list(1:3), c(1,2,3), list(Z='a','x','z'))) { x <- lapply(idxs, function(i) { i }) } ; x ", "$Z\n'a'\n\n[[2]]\n'x'\n\n[[3]]\n'z'");
  }

  @Test
  public void testCat()  {
    assertEval("cat('hi',1:3,'hello') ", "hi 1L 2L 3L hello", "NULL");
    assertEval("cat('hi',NULL,'hello',sep='-') ", "hi-hello", "NULL");
    assertEval("cat('hi',integer(0),'hello',sep='-') ", "hi--hello", "NULL");
    assertEval("cat('hi',1[2],'hello',sep='-') ", "hi-NA-hello", "NULL");
    assertEval("m <- matrix(as.character(1:6), nrow=2) ; cat(m) ", "1L 2L 3L 4L 5L 6L", "NULL");
    assertEval("cat(sep=' ', 'hello') ", "hello", "NULL");
  }

  @Test
  public void testOuter()  {
    assertEval("outer(1:3,1:2) ", "     [,1] [,2]\n[1,]  1.0  2.0\n[2,]  2.0  4.0\n[3,]  3.0  6.0");
    assertEval("outer(1:3,1:2,'*') ", "     [,1] [,2]\n[1,]  1.0  2.0\n[2,]  2.0  4.0\n[3,]  3.0  6.0");
    assertEval("outer(1, 3, '-') ", "     [,1]\n[1,] -2.0");
    assertEval("outer(1:3,1:2, function(x,y,z) { x*y*z }, 10) ", "     [,1] [,2]\n[1,] 10.0 20.0\n[2,] 20.0 40.0\n[3,] 30.0 60.0");
    assertEval("outer(1:2, 1:3, '<') ", "      [,1]  [,2] [,3]\n[1,] FALSE  TRUE TRUE\n[2,] FALSE FALSE TRUE");
    assertEval("outer(1:2, 1:3, '<') ", "      [,1]  [,2] [,3]\n[1,] FALSE  TRUE TRUE\n[2,] FALSE FALSE TRUE");
  }

  @Test
  public void testOperators()  {
    assertEval("`+`(1,2) ", "3.0");
    assertEval("`-`(1,2) ", "-1.0");
    assertEval("`*`(1,2) ", "2.0");
    assertEval("`/`(1,2) ", "0.5");
    assertEval("`%/%`(1,2) ", "0.0");
    assertEval("`%%`(1,2) ", "1.0");
    assertEval("`^`(1,2) ", "1.0");
    assertEval("`!`(TRUE) ", "FALSE");
    assertEval("`||`(TRUE, FALSE) ", "TRUE");
    assertEval("`&&`(TRUE, FALSE) ", "FALSE");
    assertEval("`|`(TRUE, FALSE) ", "TRUE");
    assertEval("`&`(TRUE, FALSE) ", "FALSE");
    assertEval("`%o%`(3,5) ", "matrix(15, nrow=1,ncol=1)");
    assertEval("`%*%`(3,5) ", "matrix(15, nrow=1,ncol=1)");
    assertEval("x <- `+` ; x(2,3) ", "5.0");
    assertEval("x <- `+` ; f <- function() { x <- 1 ; x(2,3) } ; f() ", "5.0");
  }

  @Test
  public void testTriangular()  {
    assertEval("m <- matrix(1:6, nrow=2) ;  upper.tri(m, diag=TRUE) ", "      [,1] [,2] [,3]\n[1,]  TRUE TRUE TRUE\n[2,] FALSE TRUE TRUE");
    assertEval("m <- matrix(1:6, nrow=2) ;  upper.tri(m, diag=FALSE) ", "      [,1]  [,2] [,3]\n[1,] FALSE  TRUE TRUE\n[2,] FALSE FALSE TRUE");
    assertEval("m <- matrix(1:6, nrow=2) ;  lower.tri(m, diag=TRUE) ", "     [,1]  [,2]  [,3]\n[1,] TRUE FALSE FALSE\n[2,] TRUE  TRUE FALSE");
    assertEval("m <- matrix(1:6, nrow=2) ;  lower.tri(m, diag=FALSE) ", "      [,1]  [,2]  [,3]\n[1,] FALSE FALSE FALSE\n[2,]  TRUE FALSE FALSE");

    assertEval("upper.tri(1:3, diag=TRUE) ", "      [,1]\n[1,]  TRUE\n[2,] FALSE\n[3,] FALSE");
    assertEval("upper.tri(1:3, diag=FALSE) ", "      [,1]\n[1,] FALSE\n[2,] FALSE\n[3,] FALSE");
    assertEval("lower.tri(1:3, diag=TRUE) ", "     [,1]\n[1,] TRUE\n[2,] TRUE\n[3,] TRUE");
    assertEval("lower.tri(1:3, diag=FALSE) ", "      [,1]\n[1,] FALSE\n[2,]  TRUE\n[3,]  TRUE");

    assertEval("m <- { matrix( as.character(1:6), nrow=2 ) } ; diag(m) <- c(1,2) ; m ", "      [,1]  [,2] [,3]\n[1,] '1.0'  '3L' '5L'\n[2,]  '2L' '2.0' '6L'");
    assertEval("m <- { matrix( (1:6) * (1+3i), nrow=2 ) } ; diag(m) <- c(1,2) ; m ", "         [,1]     [,2]      [,3]\n[1,] 1.0+0.0i 3.0+9.0i 5.0+15.0i\n[2,] 2.0+6.0i 2.0+0.0i 6.0+18.0i");
    assertEval("m <- { matrix( as.raw(11:16), nrow=2 ) } ; diag(m) <- c(as.raw(1),as.raw(2)) ; m ", "     [,1] [,2] [,3]\n[1,]   01   0d   0f\n[2,]   0c   02   10");
  }

  @Test
  public void testDiagonal()  {
    assertEval("m <- matrix(1:6, nrow=3) ; diag(m) <- c(1,2) ; m ", "     [,1] [,2]\n[1,]  1.0  4.0\n[2,]  2.0  2.0\n[3,]  3.0  6.0");
    assertEval("x <- (m <- matrix(1:6, nrow=3)) ; diag(m) <- c(1,2) ; x ", "     [,1] [,2]\n[1,]   1L   4L\n[2,]   2L   5L\n[3,]   3L   6L");
    assertEval("m <- matrix(1:6, nrow=3) ; f <- function() { diag(m) <- c(100,200) } ; f() ; m ", "     [,1] [,2]\n[1,]   1L   4L\n[2,]   2L   5L\n[3,]   3L   6L");
  }

  @Test
  public void testDimensions()  {
    assertEval("dim(1) ", "NULL");
    assertEval("dim(1:3) ", "NULL");
    assertEval("m <- matrix(1:6, nrow=3) ; dim(m) ", "3L, 2L");

    assertEval("nrow(1) ", "NULL");
    assertEval("nrow(1:3) ", "NULL");
    assertEval("m <- matrix(1:6, nrow=3) ; nrow(m) ", "3L");

    assertEval("ncol(1) ", "NULL");
    assertEval("ncol(1:3) ", "NULL");
    assertEval("m <- matrix(1:6, nrow=3) ; ncol(m) ", "2L");

    assertEval("x <- 1:2 ; dim(x) <- c(1,2) ; x ", "matrix(c(1L,2L), ncol=2)");
    assertEval("z <- 1 ; dim(z) <- c(1,1) ; dim(z) <- NULL ; z ", "1.0");
    assertEvalError("x <- 1:2 ; dim(x) <- c(1,3) ; x ", "dims [product 3] do not match the length of object [2]");
    assertEvalError("x <- 1:2 ; dim(x) <- c(1,NA) ; x ", "the dims contain missing values");
    assertEvalError("x <- 1:2 ; dim(x) <- c(1,-1) ; x ", "the dims contain negative values");
    assertEvalError("x <- 1:2 ; dim(x) <- integer() ; x ", "length-0 dimension vector is invalid");

    assertEval("x <- 1:2 ; attr(x, 'dim') <- c(2,1) ; x ", "matrix(c(1L,2L), nrow=2)");
    assertEval("b <- c(a=1+2i,b=3+4i) ; attr(b,'my') <- 211 ; dim(b) <- c(2,1) ; names(b) ", "NULL");
  }

  @Test
  public void testCumulativeSum()  {
    assertEval("cumsum(1:10) ", "c(1L, 3L, 6L, 10L, 15L, 21L, 28L, 36L, 45L, 55L)");
    assertEval("cumsum(c(1,2,3)) ", "c(1.0, 3.0, 6.0)");
    assertEval("cumsum(rep(1e308, 3) ) ", "c(1.0E308, Inf, Inf)");
    assertEval("cumsum(NA) ", "NA");
    assertEval("cumsum(c(1e308, 1e308, NA, 1, 2)) ", "c(1.0E308, Infinity, NA, NA, NA)");
    assertEval("cumsum(c(2000000000L, 2000000000L)) ", "c(2000000000L, NA)");
    assertEval("cumsum(c(2000000000L, NA, 2000000000L)) ", "c(2000000000L, NA, NA)");
    assertEval("cumsum(as.logical(-2:2)) ", "c(1L, 2L, 2L, 3L, 4L)");
    assertEval("cumsum((1:6)*(1+1i)) ", "c(1.0+1.0i, 3.0+3.0i, 6.0+6.0i, 10.0+10.0i, 15.0+15.0i, 21.0+21.0i)");
    assertEval("cumsum(as.raw(1:6)) ", "c(1.0, 3.0, 6.0, 10.0, 15.0, 21.0)");

    assertEval("cumsum(c(1,2,3,0/0,5)) ", "c(1.0, 3.0, 6.0, NA, NA)");
    assertEval("cumsum(c(1,0/0,5+1i)) ", "c(1.0+0.0i, NaN+0.0i, NaN+1.0i)");
  }

  @Test
  public void testWhich()  {
    assertEval("which(c(TRUE, FALSE, NA, TRUE)) ", "c(1L, 4L)");
    assertEval("which(logical()) ", "integer(0)");
    assertEval("which(c(a=TRUE,b=FALSE,c=TRUE)) ", "c(a=1L,c=3L)");
  }

  @Test
  public void testColumnsRowsStat()  {
    assertEval("m <- matrix(1:6, nrow=2) ; colMeans(m) ", "c(1.5, 3.5, 5.5)");
    assertEval("m <- matrix(1:6, nrow=2) ; colSums(na.rm = FALSE, x = m) ", "c(3.0, 7.0, 11.0)");
    assertEval("m <- matrix(1:6, nrow=2) ; rowMeans(x = m, na.rm = TRUE) ", "c(3.0, 4.0)");
    assertEval("m <- matrix(1:6, nrow=2) ; rowSums(x = m) ", "c(9.0, 12.0)");

    assertEval("m <- matrix(c(1,2,3,4,5,6), nrow=2) ; colMeans(m) ", "c(1.5, 3.5, 5.5)");
    assertEval("m <- matrix(c(1,2,3,4,5,6), nrow=2) ; colSums(m) ", "c(3.0, 7.0, 11.0)");
    assertEval("m <- matrix(c(1,2,3,4,5,6), nrow=2) ; rowMeans(m) ", "c(3.0, 4.0)");
    assertEval("m <- matrix(c(1,2,3,4,5,6), nrow=2) ; rowSums(m) ", "c(9.0, 12.0)");

    assertEval("m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; rowSums(m) ", "c(NA, 12.0)");
    assertEval("m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; rowSums(m, na.rm = TRUE) ", "c(3.0, 12.0)");
    assertEval("m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; rowMeans(m, na.rm = TRUE) ", "c(3.0, 4.0)");

    assertEval("m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colSums(m) ", "c(NA, 7.0, NA)");
    assertEval("m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colSums(na.rm = TRUE, m) ", "c(2.0, 7.0, 6.0)");
    assertEval("m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colMeans(m) ", "c(NA, 3.5, NA)");
    assertEval("m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colMeans(m, na.rm = TRUE) ", "c(2.0, 3.5, 6.0)");

    assertEval("colSums(matrix(as.complex(1:6), nrow=2)) ", "c(3.0+0.0i, 7.0+0.0i, 11.0+0.0i)");
    assertEval("colSums(matrix((1:6)*(1+1i), nrow=2)) ", "c(3.0+3.0i, 7.0+7.0i, 11.0+11.0i)");
    assertEval("colMeans(matrix(as.complex(1:6), nrow=2)) ", "c(1.5+0.0i, 3.5+0.0i, 5.5+0.0i)");
    assertEval("colMeans(matrix((1:6)*(1+1i), nrow=2)) ", "c(1.5+1.5i, 3.5+3.5i, 5.5+5.5i)");
    assertEval("rowSums(matrix(as.complex(1:6), nrow=2)) ", "c(9.0+0.0i, 12.0+0.0i)");
    assertEval("rowSums(matrix((1:6)*(1+1i), nrow=2)) ", "c(9.0+9.0i, 12.0+12.0i)");
    assertEval("rowMeans(matrix(as.complex(1:6), nrow=2)) ", "c(3.0+0.0i, 4.0+0.0i)");
    assertEval("rowMeans(matrix((1:6)*(1+1i), nrow=2)) ", "c(3.0+3.0i, 4.0+4.0i)");

    assertEval("o <- outer(1:3, 1:4, '<') ; colSums(o) ", "c(0.0, 1.0, 2.0, 3.0)");
  }

  @Test
  public void testNChar()  {
    assertEval("nchar(c('hello', 'hi')) ", "c(5L, 2L)");
    assertEval("nchar(c('hello', 'hi', 10, 130)) ", "c(5L, 2L, 2L, 3L)");
    assertEval("nchar(c(10,130)) ", "c(2L, 3L)");
  }

  @Test
  public void testStrSplit()  {
    assertEval("strsplit('helloh', 'h', fixed=TRUE) ", "list(c('', 'ello'))");
    assertEval("strsplit( c('helloh', 'hi'), c('h',''), fixed=TRUE) ", "list(c('', 'ello'), c('h', 'i'))");
    assertEval("strsplit('helloh', '', fixed=TRUE) ", "list(c('h', 'e', 'l', 'l', 'o', 'h'))");
    assertEval("strsplit('helloh', 'h') ", "list(c('', 'ello')");
    assertEval("strsplit( c('helloh', 'hi'), c('h','')) ", "list( c('', 'ello'), c('h', 'i'))");
    assertEval("strsplit('ahoj', split='') [[c(1,2)]] ", "'h'");
  }

  @Test
  public void testPaste()  {
    assertEval("paste(1:2, 1:3, FALSE, collapse=NULL) ", "'1L 1L FALSE', '2L 2L FALSE', '1L 3L FALSE'");
    assertEval("paste(1:2, 1:3, FALSE, collapse='-', sep='+') ", "'1L+1L+FALSE-2L+2L+FALSE-1L+3L+FALSE'");
    assertEval("paste() ", "character(0)");
    assertEval("paste(sep='') ", "character(0)");
    assertEval("a <- as.raw(200) ; b <- as.raw(255) ; paste(a, b) ", "'c8 ff'");

    assertEval("file.path('a', 'b', c('d','e','f')) ", "'a/b/d', 'a/b/e', 'a/b/f'");
    assertEval("file.path() ", "character(0)");
  }

  @Test
  public void testSubstring()  {
    assertEval("substr('123456', start=2, stop=4) ", "'234'");
    assertEval("substr('123456', start=2L, stop=4L) ", "'234'");
    assertEval("substr('123456', start=2.8, stop=4) ", "'234'");
    assertEval("substr(c('hello', 'bye'), start=c(1,2,3), stop=4) ", "'hell', 'ye'");
    assertEval("substr('fastr', start=NA, stop=2) ", "NA");

    assertEval("substring('123456', first=2, last=4) ", "'234'");
    assertEval("substring('123456', first=2.8, last=4) ", "'234'");
    assertEval("substring(c('hello', 'bye'), first=c(1,2,3), last=4) ", "'hell', 'ye', 'll'");
    assertEval("substring('fastr', first=NA, last=2) ", "NA");
  }

  @Test
  public void testOrder()  {
    assertEval("order(1:3) ", "1L, 2L, 3L");
    assertEval("order(3:1) ", "3L, 2L, 1L");
    assertEval("order(c(1,1,1), 3:1) ", "3L, 2L, 1L");
    assertEval("order(c(1,1,1), 3:1, decreasing=FALSE) ", "3L, 2L, 1L");
    assertEval("order(c(1,1,1), 3:1, decreasing=TRUE, na.last=TRUE) ", "1L, 2L, 3L");
    assertEval("order(c(1,1,1), 3:1, decreasing=TRUE, na.last=NA) ", "1L, 2L, 3L");
    assertEval("order(c(1,1,1), 3:1, decreasing=TRUE, na.last=FALSE) ", "1L, 2L, 3L");
    assertEval("order() ", "NULL");
    assertEval("order(c(NA,NA,1), c(2,1,3)) ", "3L, 2L, 1L");
    assertEval("order(c(NA,NA,1), c(1,2,3)) ", "3L, 1L, 2L");
    assertEval("order(c(1,2,3,NA)) ", "1L, 2L, 3L, 4L");
    assertEval("order(c(1,2,3,NA), na.last=FALSE) ", "4L, 1L, 2L, 3L");
    assertEval("order(c(1,2,3,NA), na.last=FALSE, decreasing=TRUE) ", "4L, 3L, 2L, 1L");
    assertEval("order(c(0/0, -1/0, 2)) ", "2L, 3L, 1L");
    assertEval("order(c(0/0, -1/0, 2), na.last=NA) ", "2L, 3L");

    // set GNU-R locale to C - Sys.setlocale("LC_COLLATE", "C")
    assertEval("order(c('a','z','Z','xxxz','zza','b')) ", "3L, 1L, 6L, 4L, 2L, 5L");
  }

  @Test
  public void testMathFunctions()  {
    assertEval("log(1) } ", "0.0");
    assertEval("round( log(10,), digits = 5 ) ", "2.30259");
    assertEval("round( log(10,2), digits = 5 ) ", "3.32193");
    assertEval("round( log(10,10), digits = 5 ) ", "1.0");
    assertEval("m <- matrix(1:4, nrow=2) ; round( log10(m), digits=5 )  ", "        [,1]    [,2]\n[1,]     0.0 0.47712\n[2,] 0.30103 0.60206");

    assertEval("x <- c(a=1, b=10) ; round( c(log(x), log10(x), log2(x)), digits=5 ) ", "  a       b   a   b   a       b\n0.0 2.30259 0.0 1.0 0.0 3.32193");

    assertEval("sqrt(c(a=9,b=81)) ", "  a   b\n3.0 9.0");

    assertEval("round( exp(c(1+1i,-2-3i)), digits=5 ) ", "1.46869+2.28736i, -0.13398-0.0191i");
    assertEval("round( exp(1+2i), digits=5 ) ", "-1.1312+2.47173i");

    assertEval("abs((-1-0i)/(0+0i)) ", "Infinity");
    assertEval("abs((-0-1i)/(0+0i)) ", "Infinity");
    assertEval("abs(NA+0.1) ", "NA");
    assertEval("abs(0/0) ", "NaN");
    assertEval("abs((1:2)[3] ", "NA");
    assertEval("abs(c(0/0,1i)) ", "NaN, 1.0");
    assertEval("abs((0+0i)/0) ", "NaN");
    assertEval("exp(-abs((0+1i)/(0+0i))) ", "0.0");
    assertEval("floor(c(0.2,-3.4)) ", "0.0, -4.0");
    assertEval("ceiling(c(0.2,-3.4,NA,0/0,1/0)) ", "1.0, -3.0, NA, NaN, Infinity");
  }

  @Test
  public void testCharUtils()  {
    assertEval("toupper(c('hello','bye')) ", "'HELLO', 'BYE'");
    assertEval("tolower(c('Hello','ByE')) ", "'hello', 'bye'");
    assertEval("tolower(1E100) ", "'1.0e100'");
    assertEval("toupper(1E100) ", "'1.0E100'");
    assertEval("tolower(c()) ", "character(0)");
    assertEval("tolower(NA) ", "NA");
    assertEval("m <- matrix('hi') ; toupper(m) ", "     [,1]\n[1,] 'HI'");
    assertEval("toupper(c(a='hi', 'hello')) ", "   a        \n'HI' 'HELLO'");
    assertEval("tolower(c(a='HI', 'HELlo')) ", "   a        \n'hi' 'hello'");
  }

  @Test
  public void testTypeOf()  {
    assertEval("typeof(1) ", "'double'");
    assertEval("typeof(1L) ", "'integer'");
    assertEval("typeof(sum) ", "'builtin'");
    assertEval("typeof(function(){}) ", "'closure'");
    assertEval("typeof('hi') ", "'character'");
  }

  @Test
  public void testSub()  {
    assertEval("gsub('a','aa', 'prague alley', fixed=TRUE) ", "'praague aalley'");
    assertEval("sub('a','aa', 'prague alley', fixed=TRUE) ", "'praague alley'");
    assertEval("gsub('a','aa', 'prAgue alley', fixed=TRUE) ", "'prAgue aalley'");
    assertEval("gsub('a','aa', 'prAgue alley', fixed=TRUE, ignore.case=TRUE) ", "'praague aalley'");
    assertEval("gsub('h','', c('hello', 'hi', 'bye'), fixed=TRUE) ", "'ello', 'i', 'bye'");

    assertEval("gsub('a','aa', 'prague alley') ", "'praague aalley'");
    assertEval("sub('a','aa', 'prague alley') ", "'praague alley'");
    assertEval("gsub('a','aa', 'prAgue alley') ", "'prAgue aalley'");
    assertEval("gsub('a','aa', 'prAgue alley', ignore.case=TRUE) ", "'praague aalley'");
    assertEval("gsub('h','', c('hello', 'hi', 'bye') ", "'ello', 'i', 'bye'");

    assertEval("gsub('([a-e])','\\\\1\\\\1', 'prague alley') ", "'praaguee aalleey'");
  }

  @Test
  public void testRegExpr()  {
    assertEval("gregexpr('(a)[^a]\\\\1', c('andrea apart', 'amadeus', NA))", "[[1]]\n6L\n\n[[2]]\n1L\n\n[[3]]\nNA"); // NOTE: this is without attributes
    assertEval("regexpr('(a)[^a]\\\\1', c('andrea apart', 'amadeus', NA))", "6L, 1L, NA"); // NOTE: this is without attributes
  }

  @Test
  public void testLength()  {
    assertEval("x <- 1:4 ; length(x) <- 2 ; x ", "1L, 2L");
    assertEval("x <- 1:2 ; length(x) <- 4 ; x ", "1L, 2L, NA, NA");
    assertEval("x <- 1:2 ; z <- (length(x) <- 4) ; z ", "4.0");
    assertEval("length(c(z=1:4)) ", "4L");
    assertEval("x <- 1 ; f <- function() { length(x) <<- 2 } ; f() ; x ", "1.0, NA");
  }

  @Test
  public void testNames()  {
    assertEval("x <- 1:2 ; names(x) <- c('hello', 'hi'); names(x) } ", "'hello', 'hi'");
    assertEval("x <- 1:2 ; names(x) <- c('hello'); names(x) ", "'hello', NA");
    assertEval("x <- 1:2; names(x) <- c('hello', 'hi') ; x ", "hello hi\n   1L 2L");

    assertEval("x <- c(1,9); names(x) <- c('hello','hi') ; sqrt(x) ", "hello  hi\n  1.0 3.0");
    assertEval("x <- c(1,9); names(x) <- c('hello','hi') ; is.na(x) ", "hello    hi\nFALSE FALSE");
    assertEval("x <- c(1,NA); names(x) <- c('hello','hi') ; cumsum(x) ", "hello hi\n  1.0 NA");
    assertEval("x <- c(1,NA); names(x) <- c(NA,'hi') ; cumsum(x) ", "<NA> hi\n 1.0 NA");
    assertEval("x <- c(1,2); names(x) <- c('A', 'B') ; x + 1 ", "  A   B\n2.0 3.0");
    assertEval("x <- 1:2; names(x) <- c('A', 'B') ; y <- c(1,2,3,4) ; names(y) <- c('X', 'Y', 'Z') ; x + y ", "  X   Y   Z <NA>\n2.0 4.0 4.0  6.0");
    assertEval("x <- 1:2; names(x) <- c('A', 'B') ; abs(x) ", " A  B\n1L 2L");
    assertEval("z <- c(a=1, b=2) ; names(z) <- NULL ; z ", "1.0, 2.0");
  }

  @Test
  public void testRev()  {
    assertEval("rev(c(1+1i, 2+2i)) ", "2.0+2.0i, 1.0+1.0i");
    assertEval("rev(1:3) ", "3L, 2L, 1L");
  }

  @Test
  public void testEnvironment()  {
    // note: this function also includes lookup tests that do not explicitly invoke environment-related builtins

    assertEval("f <- function() { assign('x', 1) ; x } ; f() ", "1.0");
    assertEval("f <- function() { x <- 2 ; g <- function() { x <- 3 ; assign('x', 1, inherits=FALSE) ; x } ; g() } ; f() ", "1.0");
    assertEval("f <- function() { x <- 2 ; g <- function() { assign('x', 1, inherits=FALSE) } ; g() ; x } ; f() ", "2.0");
    assertEval("f <- function() { x <- 2 ; g <- function() { assign('x', 1, inherits=TRUE) } ; g() ; x } ; f() ", "1.0");
    assertEval("f <- function() {  g <- function() { assign('x', 1, inherits=TRUE) } ; g() } ; f() ; x ", "1.0");
    assertEval("x <- 3 ; g <- function() { x } ; f <- function() { assign('x', 2) ; g() } ; f() ", "3.0");
    assertEval("x <- 3 ; f <- function() { assign('x', 2) ; g <- function() { x } ; g() } ; f() ", "2.0");
    assertEval("h <- function() { x <- 3 ; g <- function() { x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; h() ", "3.0");
    assertEval("h <- function() { x <- 3  ; f <- function() { assign('x', 2) ; g <- function() { x } ; g() } ; f() }  ; h() ", "2.0");
    assertEval("x <- 3 ; h <- function() { g <- function() { x } ; f <- function() { assign('x', 2, inherits=TRUE) } ; f() ; g() }  ; h() ", "2.0");
    assertEval("x <- 3 ; h <- function(s) { if (s == 2) { assign('x', 2) } ; x }  ; h(1) ; h(2) ", "2.0");
    assertEval("x <- 3 ; h <- function(s) { y <- x ; if (s == 2) { assign('x', 2) } ; c(y,x) }  ; c(h(1),h(2)) ", "3.0, 3.0, 3.0, 2.0");
    assertEval("g <- function() { x <- 2 ; f <- function() { x ; exists('x') }  ; f() } ; g() ", "TRUE");
    assertEval("g <- function() { f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() ", "FALSE");
    assertEval("g <- function() { f <- function() { if (FALSE) { x } ; assign('x', 1) ; exists('x') }  ; f() } ; g() ", "TRUE");
    assertEval("g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() ", "FALSE");
    assertEval("g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; assign('x', 2) ; exists('x') }  ; f() } ; g() ", "TRUE");
    assertEval("h <- function() { g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() ", "FALSE");
    assertEval("h <- function() { x <- 3 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() ", "TRUE");
    assertEval("f <- function(z) { exists('z') } ; f() ", "TRUE");
    assertEval("f <- function(z) { exists('z') } ; f(a) ", "TRUE");
    assertEval("f <- function() { x <- 3 ; exists('x', inherits=FALSE) } ; f() ", "TRUE");
    assertEval("f <- function() { z <- 3 ; exists('x', inherits=FALSE) } ; f() ", "FALSE");
    assertEval("f <- function() { if (FALSE) { x <- 3 } ; exists('x', inherits=FALSE) } ; f() ", "FALSE");
    assertEval("f <- function() { assign('x', 2) ; exists('x', inherits=FALSE) } ; f() ", "TRUE");
    assertEval("g <- function() { x <- 2 ; f <- function() { if (FALSE) { x <- 3 } ; exists('x') }  ; f() } ; g() ", "TRUE");
    assertEval("g <- function() { x <- 2 ; f <- function() { x <- 5 ; exists('x') }  ; f() } ; g() ", "TRUE");
    assertEval("g <- function() { f <- function() { assign('x', 3) ; if (FALSE) { x } ; exists('x') }  ; f() } ; g() ", "TRUE");
    assertEval("g <- function() { f <- function() { assign('z', 3) ; if (FALSE) { x } ; exists('x') }  ; f() } ; g() ", "FALSE");
    assertEval("h <- function() { assign('x', 1) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() ", "TRUE");
    assertEval("h <- function() { assign('z', 1) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() ", "FALSE");
    assertEval("h <- function() { x <- 3 ; g <- function() { f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() ", "TRUE");

    assertEval("f <- function()  { as.environment(-1) } ; f() ", "<environment: R_GlobalEnv>");
    assertEval("emptyenv() ", "<environment: R_EmptyEnv>");
    assertEval("x <- 3 ; f <- function() { exists('x') } ; f() ", "TRUE");
    assertEval("x <- 3 ; f <- function() { exists('x', inherits=FALSE) } ; f() ", "FALSE");
    assertEval("h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; assign('y', 2, h) ; ls(h) ", "'x', 'y'");
    assertEval("f <- function() { assign('x', 1) ; y <- 2 ; ls() } ; f() ", "'x', 'y'");
    assertEval("f <- function() { x <- 1 ; y <- 2 ; ls() } ; f() ", "'x', 'y'");
    assertEval("f <- function() { assign('x', 1) ; y <- 2 ; if (FALSE) { z <- 3 } ; ls() } ; f() ", "'x', 'y'");
    assertEval("f <- function() { if (FALSE) { x <- 1 } ; y <- 2 ; ls() } ; f() ", "'y'");
    assertEval("f <- function() { for (i in rev(1:10)) { assign(as.character(i), i) } ; ls() } ; length(f()) ", "11L"); // the actual elements are formatted differently from GNU-R, also in different order

    // lookup
    assertEval("f <- function() { x <- 2 ; get('x') } ; f() ", "2.0");
    assertEval("x <- 3 ; f <- function() { get('x') } ; f() ", "3.0");
    assertEval("x <- 3 ; f <- function() { x <- 2 ; get('x') } ; f() ", "2.0");
    assertEval("x <- 3 ; f <- function() { x <- 2; h <- function() {  get('x') }  ; h() } ; f() ", "2.0");
    assertEval("x <- 3 ; f <- function() { assign('x', 4) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x } ; h() } ; f() ", "4.0");
    assertEval("x <- 3 ; f <- function() { assign('x', 4) ; g <- function() { assign('y', 3) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x } ; h() } ; g()  } ; f() ", "4.0");
    assertEval("f <- function() { assign('x', 2, inherits=TRUE) ; assign('x', 1) ; h <- function() { x } ; h() } ; f() ", "1.0");
    assertEval("x <- 3 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x } ; h() } ; f() } ; g() ", "3.0");
    assertEval("x <- 3 ; gg <- function() {  g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x } ; h() } ; f() } ; g() } ; gg() ", "3.0");
    assertEval("h <- function() { x <- 2 ; f <- function() { if (FALSE) { x <- 1 } ; g <- function() { x } ; g() } ; f() } ; h() ", "2.0");
    assertEvalError("get('dummy') ", "object 'dummy' not found");
    assertEval("f <- function() { g <- function() { get('x', inherits=TRUE) } ; g() } ; x <- 3 ; f() ", "3.0");
    assertEval("f <- function() { assign('z', 2) ; g <- function() { get('x', inherits=TRUE) } ; g() } ; x <- 3 ; f() ", "3.0");
    assertEval("f <- function() { assign('x', 3) ; g <- function() { x } ; g() } ; x <- 10 ; f() ", "3.0");
    assertEval("f <- function() { assign('x', 3) ; h <- function() { assign('z', 4) ; g <- function() { x } ; g() } ; h() } ; x <- 10 ; f() ", "3.0");
    assertEval("f <- function() { assign('x', 3) ; h <- function() { g <- function() { x } ; g() } ; h() } ; x <- 10 ; f() ", "3.0");
    assertEval("f <- function() { assign('x', 1) ; g <- function() { assign('z', 2) ; x } ; g() } ; f() ", "1.0");
    assertEval("h <- function() { x <- 3 ; g <- function() { assign('z', 2) ; x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; h() ", "3.0");
    assertEval("h <- function() { x <- 3 ; g <- function() { assign('x', 5) ; x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; h() ", "5.0");
    assertEval("x <- 10 ; g <- function() { x <- 100 ; z <- 2 ; f <- function() { assign('z', 1); x <- x ; x } ; f() } ; g() ", "100.0");
    assertEval("f <- function() { x <- 22 ; get('x', inherits=FALSE) } ; f() ", "22.0");
    assertEvalError("x <- 33 ; f <- function() { if (FALSE) { x <- 22  } ; get('x', inherits=FALSE) } ; f() ", "object 'x' not found");
    assertEvalError("x <- 33 ; f <- function() { get('x', inherits=FALSE) } ; f() ", "object 'x' not found");
    assertEval("x <- 33 ; f <- function() { assign('x', 44) ; get('x', inherits=FALSE) } ; f() ", "44.0");
    assertEvalError("h <- new.env(parent=emptyenv()) ; assign('y', 2, h) ; get('z', h) ", "object 'z' not found");
    assertEval("hh <- new.env() ; assign('z', 3, hh) ; h <- new.env(parent=hh) ; assign('y', 2, h) ; get('z', h) ", "3.0");
    assertEval("g <- function() { if (FALSE) { x <- 2 ; y <- 3} ; f <- function() { if (FALSE) { x } ; assign('y', 2) ; exists('x') }  ; f() } ; g() ", "FALSE");
    assertEval("g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; exists('x') }  ; f() } ; g() ", "TRUE");
    assertEval("g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; h <- function() { exists('x') } ; h() }  ; f() } ; g() ", "TRUE");
    assertEval("g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('y', 2) ; h <- function() { exists('x') } ; h() }  ; f() } ; g() ", "FALSE");
    assertEval("g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; gg <- function() { h <- function() { get('x') } ; h() } ; gg() } ; f() } ; g() ", "2.0");
    assertEval("g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; gg <- function() { h <- function() { exists('x') } ; h() } ; gg() } ; f() } ; g() ", "TRUE");
    assertEval("x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function() { x } } ; f1 <- f(1) ; f2 <- f(2) ; f1() ", "4.0");
    assertEval("x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function() { x } } ; f1 <- f(1) ; f2 <- f(2) ; f2() ; f1() ", "4.0");
    assertEval("f <- function() { x <- 2 ; g <- function() { if (FALSE) { x <- 2 } ; assign('x', 1, inherits=TRUE) } ; g() ; x } ; f() ", "1.0");
    assertEval("h <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; g <- function() { assign('z', 3) ; if (FALSE) { x <- 4 } ;  f <- function() { exists('x') } ; f() } ; g() } ; h() ", "FALSE");

    // lookup with function matching
    assertEval("x <- function(){3} ; f <- function() { assign('x', function(){4}) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x() } ; h() } ; f() ", "4.0");
    assertEval("f <- function() { assign('x', function(){2}, inherits=TRUE) ; assign('x', function(){1}) ; h <- function() { x() } ; h() } ; f() ", "1.0");
    assertEval("x <- function(){3} ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() ", "3.0");
    assertEval("x <- function(){3} ; gg <- function() {  g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() } ; gg() ", "3.0");
    assertEval("h <- function() { x <- function(){2} ; f <- function() { if (FALSE) { x <- 1 } ; g <- function() { x } ; g() } ; f() } ; z <- h() ; z() ", "2.0");
    assertEval("h <- function() { g <- function() {4} ; f <- function() { if (FALSE) { g <- 4 } ; g() } ; f() } ; h() ", "4.0");
    assertEval("h <- function() { assign('f', function() {4}) ; f() } ; h() ", "4.0");
    assertEval("f <- function() { 4 } ; h <- function() { assign('f', 5) ; f() } ; h() ", "4.0");
    assertEval("f <- function() { 4 } ; h <- function() { assign('z', 5) ; f() } ; h() ", "4.0");
    assertEval("gg <- function() {  assign('x', function(){11}) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() } ; gg() ", "11.0");
    assertEval("x <- function(){3} ; gg <- function() { assign('x', 4) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() } ; gg() ", "3.0");
    assertEval("h <- function() { x <- function() {3} ; g <- function() { assign('z', 2) ; x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; z <- h() ; z() ", "3.0");
    assertEval("h <- function() { x <- function() {3} ; g <- function() { assign('x', function() {5} ) ; x() } ; g() } ; h() ", "5.0");
    assertEval("h <- function() { z <- 3 ; x <- function() {3} ; g <- function() { x <- 1 ; assign('z', 5) ; x() } ; g() } ; h() ", "3.0");
    assertEval("h <- function() { x <- function() {3} ; gg <- function() { assign('x', 5) ; g <- function() { x() } ; g() } ; gg() } ; h() ", "3.0");
    assertEval("h <- function() { z <- 2 ; x <- function() {3} ; gg <- function() { assign('z', 5) ; g <- function() { x() } ; g() } ; gg() } ; h() ", "3.0");
    assertEval("h <- function() { x <- function() {3} ; g <- function() { assign('x', function() {4}) ; x() } ; g() } ; h() ", "4.0");
    assertEval("h <- function() { z <- 2 ; x <- function() {3} ; g <- function() { assign('z', 1) ; x() } ; g() } ; h() ", "3.0");
    assertEval("x <- function() { 3 } ; h <- function() { if (FALSE) { x <- 2 } ;  z <- 2  ; g <- function() { assign('z', 1) ; x() } ; g() } ; h() ", "3.0");
    assertEval("x <- function() { 3 } ; h <- function() { g <- function() { f <- function() { x <- 1 ; x() } ; f() } ; g() } ; h() ", "3.0");
    assertEval("g <- function() { assign('myfunc', function(i) { sum(i) });  f <- function() { lapply(2, 'myfunc') } ; f() } ; g() ", "[[1]]\n2.0");
    assertEval("myfunc <- function(i) { sum(i) } ; g <- function() { assign('z', 1);  f <- function() { lapply(2, 'myfunc') } ; f() } ; g() ", "[[1]]\n2.0");
    assertEval("g <- function() { f <- function() { assign('myfunc', function(i) { sum(i) }); lapply(2, 'myfunc') } ; f() } ; g() ", "[[1]]\n2.0");
    assertEval("h <- function() { myfunc <- function(i) { sum(i) } ; g <- function() { myfunc <- 2 ; f <- function() { myfunc(2) } ; f() } ; g() } ; h() ", "2.0");
    assertEval("x <- function() {11} ; g <- function() { f <- function() { assign('x', 2) ; x() } ; f() } ; g() ", "11.0");
    assertEval("g <- function() { myfunc <- function(i) { i+i } ; f <- function() { lapply(2, 'myfunc') } ; f() } ; g() ", "[[1]]\n4.0");
    assertEval("x <- function() {3} ; f <- function(i) { if (i == 1) { assign('x', function() {4}) } ; function() { x() } } ; f1 <- f(1) ; f2 <- f(2) ; f1() ", "4.0");
    assertEval("x <- function() {3} ; f <- function(i) { if (i == 1) { assign('x', function() {4}) } ; function() { x() } } ; f1 <- f(1) ; f2 <- f(2) ; f2() ; f1() ", "4.0");
    assertEval("x <- function() {3} ; f <- function(i) { if (i == 1) { assign('x', function() {4}) } ; function() { x() } } ; f1 <- f(1) ; f2 <- f(2) ; f1() ; f2() ", "3.0");

    // lookup with super assignment
    assertEval("x <- 3 ; f <- function() { assign('x', 4) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x <<- 6 } ; h() ; get('x') } ; f() ", "6.0");
    assertEval("x <- 3 ; f <- function() { assign('x', 4) ; hh <- function() { if (FALSE) { x <- 100 } ; h <- function() { x <<- 6 } ; h() } ; hh() ; get('x') } ; f() ", "6.0");
    assertEval("x <- 3 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x ; hh <- function() { x <<- 4 } ; hh() } ; h() } ; f() } ; g() ; x ", "4.0");
    assertEval("f <- function() { x <- 1 ; g <- function() { h <- function() { x <<- 2 } ; h() } ; g() ; x } ; f() ", "2.0");
    assertEval("g <- function() { if (FALSE) { x <- 2 } ; f <- function() { assign('x', 4) ; x <<- 3 } ; f() } ; g() ; x ", "3.0");
    assertEval("g <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; h <- function() { if (FALSE) { x <- 1 } ; assign('z', 10) ; f <- function() { assign('x', 4) ; x <<- 3 } ; f() } ; h() } ; g() ; x ", "3.0");
    assertEval("gg <- function() { assign('x', 100) ; g <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; h <- function() { if (FALSE) { x <- 1 } ; assign('z', 10) ; f <- function() { assign('x', 4) ; x <<- 3 } ; f() } ; h() } ; g() } ; x <- 10 ; gg() ; x ", "10.0");
    assertEval("gg <- function() { if (FALSE) { x <- 100 } ; g <- function() { if (FALSE) { x <- 100 } ; h <- function() { f <- function() { x <<- 3 } ; f() } ; h() } ; g() } ; x <- 10 ; gg() ; x ", "3.0");
    assertEval("g <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; h <- function() { assign('z', 10) ; f <- function() { x <<- 3 } ; f() } ; h() } ; g() ; x ", "3.0");
    assertEval("g <- function() { x <- 2 ; z <- 3 ; hh <- function() { assign('z', 2) ; h <- function() { f <- function() { x <<- 3 } ; f() } ; h() } ; hh() } ; x <- 10 ; g() ; x ", "10.0");
    assertEval("g <- function() { x <- 2 ; z <- 3 ; hh <- function() { assign('z', 2) ; h <- function() { assign('x', 1); f <- function() { x <<- 3 } ; f() } ; h() } ; hh() ; x } ; x <- 10 ; g() ", "2.0");
    assertEval("x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function(v) { x <<- v} } ; f1 <- f(1) ; f2 <- f(2) ; f1(10) ; f2(11) ; x ", "11.0");
    assertEval("x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function(v) { x <<- v} } ; f1 <- f(1) ; f2 <- f(2) ; f2(10) ; f1(11) ; x ", "10.0");

    // hashmaps
    assertEval("h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; exists('x', h) ", "TRUE");
    assertEval("h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; exists('xx', h) ", "FALSE");
    assertEval("hh <- new.env() ; assign('z', 3, hh) ; h <- new.env(parent=hh) ; assign('y', 2, h) ; exists('z', h) ", "TRUE");
    assertEvalError("ph <- new.env(parent=emptyenv()) ; h <- new.env(parent=ph) ; assign('x', 10, h, inherits=TRUE) ; get('x', ph)", "object 'x' not found");
    assertEval("ph <- new.env() ; h <- new.env(parent=ph) ; assign('x', 2, ph) ; assign('x', 10, h, inherits=TRUE) ; get('x', ph)", "10.0");
    assertEvalError("ph <- new.env() ; h <- new.env(parent=ph) ; assign('x', 2, h) ; assign('x', 10, h, inherits=TRUE) ; get('x', ph)", "object 'x' not found");
    assertEval("ph <- new.env() ; h <- new.env(parent=ph) ; assign('x', 10, h, inherits=TRUE) ; x ", "10.0");
    assertEval("assign('z', 10, inherits=TRUE) ; z ", "10.0");
    assertEval("h <- new.env(parent=globalenv()) ; assign('x', 10, h, inherits=TRUE) ; x ", "10.0");
    assertEval("h <- new.env() ; assign('x', 1, h) ; assign('x', 1, h) ; get('x', h) ", "1.0");
    assertEval("h <- new.env() ; assign('x', 1, h) ; assign('x', 2, h) ; get('x', h) ", "2.0");
    assertEval("h <- new.env() ; u <- 1 ; assign('x', u, h) ; assign('x', u, h) ; get('x', h) ", "1.0");

    // top-level lookups
    assertEval("exists('sum') ", "TRUE");
    assertEval("exists('sum', inherits = FALSE) ", "FALSE");
    assertEval("x <- 1; exists('x', inherits = FALSE) ", "TRUE");

    // globalenv
    assertEval("ls() ", "character(0)");
    assertEval("x <- 1 ; ls(globalenv()) ", "'x'");
    assertEval("ls(.GlobalEnv) ", "character(0)");
    assertEval("x <- 1 ; ls(.GlobalEnv) ", "'x'");
  }

  @Test
  public void testTranspose()  {
    assertEval("t(1:3) ", "     [,1] [,2] [,3]\n[1,]   1L   2L   3L");
    assertEval("t(t(1:3)) ", "     [,1]\n[1,]   1L\n[2,]   2L\n[3,]   3L");
    assertEval("t(t(t(1:3))) ", "     [,1] [,2] [,3]\n[1,]   1L   2L   3L");
    assertEval("t(matrix(1:6, nrow=2)) ", "     [,1] [,2]\n[1,]   1L   2L\n[2,]   3L   4L\n[3,]   5L   6L");
    assertEval("t(t(matrix(1:6, nrow=2))) ", "     [,1] [,2] [,3]\n[1,]   1L   3L   5L\n[2,]   2L   4L   6L");
    assertEval("t(matrix(1:4, nrow=2)) ", "     [,1] [,2]\n[1,]   1L   2L\n[2,]   3L   4L");
    assertEval("t(t(matrix(1:4, nrow=2))) ", "     [,1] [,2]\n[1,]   1L   3L\n[2,]   2L   4L");

    assertEval("m <- matrix(1:49, nrow=7) ; sum(m * t(m)) ", "33369L");
    assertEval("m <- matrix(1:81, nrow=9) ; sum(m * t(m)) ", "145881L");
    assertEval("m <- matrix(-5000:4999, nrow=100) ; sum(m * t(m)) ", "1666502500L");
    assertEval("m <- matrix(c(rep(1:10,100200),100L), nrow=1001) ; sum(m * t(m)) ", "38587000L");

    assertEvalError("t(t) ", "argument is not a matrix");
    assertEval("t(c(1,2,3,4)) ", "     [,1] [,2] [,3] [,4]\n[1,]  1.0  2.0  3.0  4.0");
    assertEval("x <- c(1,2,3,4) ; dim(x) <- 4 ; t(x) ", "     [,1] [,2] [,3] [,4]\n[1,]  1.0  2.0  3.0  4.0");
    assertEval("t(array(c(1,2,3,4),dim=4)) ", "     [,1] [,2] [,3] [,4]\n[1,]  1.0  2.0  3.0  4.0");
    assertEvalError("t(array(c(1,2,3,4),dim=c(4,1,1))) ", "argument is not a matrix");
    assertEval("x <- t(array(as.double(1:4000000),dim=c(2000,2000))) ; x[1:3,1:3] ", "       [,1]   [,2]   [,3]\n[1,]    1.0    2.0    3.0\n[2,] 2001.0 2002.0 2003.0\n[3,] 4001.0 4002.0 4003.0");
    assertEval("x <- array(as.double(1:4000000),dim=c(2000,2000)) ; z <- t(x) ; z[1:3,1:3] ", "       [,1]   [,2]   [,3]\n[1,]    1.0    2.0    3.0\n[2,] 2001.0 2002.0 2003.0\n[3,] 4001.0 4002.0 4003.0");
    assertEval("x <- array(1:4000000,dim=c(2000,2000)) ; z <- t(x) ; z[1:3,1:3] ", "      [,1]  [,2]  [,3]\n[1,]    1L    2L    3L\n[2,] 2001L 2002L 2003L\n[3,] 4001L 4002L 4003L");
    assertEval("x <- array(1:4000000+((1:1000)*(1+2i)),dim=c(2000,2000)) ; z <- t(x) ; z[1:3,1:3] ", "            [,1]        [,2]        [,3]\n[1,]    2.0+2.0i    4.0+4.0i    6.0+6.0i\n[2,] 2002.0+2.0i 2004.0+4.0i 2006.0+6.0i\n[3,] 4002.0+2.0i 4004.0+4.0i 4006.0+6.0i");
    assertEval("x <- array((1:2137^2)*(1+2i),dim=c(2137,2137)) ; z <- t(x) ; z[1:3,1:3] ", "               [,1]           [,2]           [,3]\n[1,]       1.0+2.0i       2.0+4.0i       3.0+6.0i\n[2,] 2138.0+4276.0i 2139.0+4278.0i 2140.0+4280.0i\n[3,] 4275.0+8550.0i 4276.0+8552.0i 4277.0+8554.0i");
    assertEval("x <- array(rev(1:2137^2),dim=c(2137,2137)) ; z <- t(x) ; z[1:3,1:3] ", "         [,1]     [,2]     [,3]\n[1,] 4566769L 4566768L 4566767L\n[2,] 4564632L 4564631L 4564630L\n[3,] 4562495L 4562494L 4562493L");
    assertEval("x <- array(as.double(1:2137^2),dim=c(2137,2137)) ; z <- t(x) ; z[1:3,1:3] ", "       [,1]   [,2]   [,3]\n[1,]    1.0    2.0    3.0\n[2,] 2138.0 2139.0 2140.0\n[3,] 4275.0 4276.0 4277.0");
  }


  @Test
  public void testTypeCheck()  {
    assertEval("is.double(10L) ", "FALSE");
    assertEval("is.double(10) ", "TRUE");
    assertEval("is.double('10') ", "FALSE");
    assertEval("is.numeric(10L) ", "TRUE");
    assertEval("is.numeric(10) ", "TRUE");
    assertEval("is.numeric(TRUE) ", "FALSE");
    assertEval("is.character('hi') ", "TRUE");
    assertEval("is.list(NULL) ", "FALSE");
    assertEval("is.logical(NA) ", "TRUE");
    assertEval("is.logical(1L) ", "FALSE");
    assertEval("is.integer(1) ", "FALSE");
    assertEval("is.integer(1L) ", "TRUE");
    assertEval("is.complex(1i) ", "TRUE");
    assertEval("is.complex(1) ", "FALSE");
    assertEval("is.raw(raw()) ", "TRUE");
    assertEval("is.matrix(1) ", "FALSE");
    assertEval("is.matrix(matrix(1:6, nrow=2)) ", "TRUE");
    assertEval("is.matrix(NULL) ", "FALSE");
  }

  @Test
  public void testOverride()  {
    assertEval("sub <- function(x,y) { x - y }; sub(10,5) ", "5.0");
    assertEval("sub('a','aa', 'prague alley', fixed=TRUE) ", "'praague alley'");
  }

  @Test
  public void testEigen()  {
    // symmetric real input
    assertEval("r <- eigen(matrix(rep(1,4), nrow=2), only.values=FALSE) ; round( r$vectors, digits=5 ) ", "        [,1]     [,2]\n[1,] 0.70711 -0.70711\n[2,] 0.70711  0.70711");
    assertEval("r <- eigen(matrix(rep(1,4), nrow=2), only.values=FALSE) ; round( r$values, digits=5 ) ", "2.0, 0.0");
    assertEval("eigen(10, only.values=FALSE) ", "$values\n10.0\n\n$vectors\n     [,1]\n[1,]  1.0");

    // non-symmetric real input, real output
    assertEval("r <- eigen(matrix(c(1,2,2,3), nrow=2), only.values=FALSE); round( r$vectors, digits=5 ) ", "        [,1]     [,2]\n[1,] 0.52573 -0.85065\n[2,] 0.85065  0.52573");
    assertEval("r <- eigen(matrix(c(1,2,2,3), nrow=2), only.values=FALSE); round( r$values, digits=5 ) ", "4.23607, -0.23607");
    assertEval("r <- eigen(matrix(c(1,2,3,4), nrow=2), only.values=FALSE); round( r$vectors, digits=5 ) ", "         [,1]     [,2]\n[1,] -0.56577 -0.90938\n[2,] -0.82456  0.41597");
    assertEval("r <- eigen(matrix(c(1,2,3,4), nrow=2), only.values=FALSE); round( r$values, digits=5 ) ", "5.37228, -0.37228");

    // non-symmetric real input, complex output
    // FIXME: GNUR is won't print the minus sign for negative zero
    assertEval("r <- eigen(matrix(c(3,-2,4,-1), nrow=2), only.values=FALSE); round( r$vectors, digits=5 ) ", "                  [,1]              [,2]\n[1,]       0.8165+0.0i       0.8165+0.0i\n[2,] -0.40825+0.40825i -0.40825-0.40825i");
    assertEval("r <- eigen(matrix(c(3,-2,4,-1), nrow=2), only.values=FALSE); round( r$values, digits=5 ) ", "1.0+2.0i, 1.0-2.0i");
  }

  @Test
  public void testAttributes()  {
    assertEval("x <- 1; attributes(x) ", "NULL");
    assertEval("x <- 1; names(x) <- 'hello' ; attributes(x) ", "$names\n'hello'");
    assertEval("x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; attributes(x) ", "$myatt\n2L, 3L, 4L");
    assertEval("x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; attr(x, 'myatt1') <- 'hello' ; attributes(x) ", "$myatt\n2L, 3L, 4L\n\n$myatt1\n'hello'");
    assertEval("x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; y <- x; attr(x, 'myatt1') <- 'hello' ; attributes(y) ", "$myatt\n2L, 3L, 4L");
    assertEval("x <- c(a=1, b=2) ; attr(x, 'myatt') <- 2:4 ; y <- x; attr(x, 'myatt1') <- 'hello' ; attributes(y) ", "$names\n'a', 'b'\n\n$myatt\n2L, 3L, 4L");
    assertEval("x <- c(a=1, b=2) ; attr(x, 'names') ", "'a', 'b'");
    assertEval("x <- c(a=1, b=2) ; attr(x, 'na') ", "'a', 'b'");
    assertEval("x <- c(a=1, b=2) ; attr(x, 'mya') <- 1; attr(x, 'b') <- 2; attr(x, 'm') ", "1.0");
    assertEval("x <- 1:2; attr(x, 'aa') <- 1 ; attr(x, 'ab') <- 2; attr(x, 'bb') <- 3; attr(x, 'b') ", "3.0");
    assertEval("z <- 1; attr(z,'a') <- 1; attr(z,'b') <- 2; attr(z,'c') <- 3 ; attr(z,'b') <- NULL ; z ", "1.0\nattr(,'a')\n1.0\nattr(,'c')\n3.0");

    assertEval("x <- 1 ; attributes(x) <- list(hi=3, hello=2) ; x ", "1.0\nattr(,'hi')\n3.0\nattr(,'hello')\n2.0");
    assertEval("x <- 1 ; attributes(x) <- list(hi=3, names='name') ; x ", "name\n 1.0\nattr(,'hi')\n3.0");
    assertEval("x <- c(hello=1) ; attributes(x) <- list(names=NULL) ; x ", "1.0");
    assertEvalError("x <- c(hello=1) ; attributes(x) <- list(hi=1, 2) ; x ", "all attributes must have names [2 does not]");
    assertEval("x <- 1; attributes(x) <- list(my = 1) ; y <- x; attributes(y) <- list(his = 2) ; x ", "1.0\nattr(,'my')\n1.0");
    assertEval("x <- c(hello=1) ; attributes(x) <- list(hi=1) ;  attributes(x) <- NULL ; x ", "1.0");
    assertEval("x <- c(hello=1) ; attributes(x) <- list(hi=1, names=NULL, hello=3, hi=2, hello=NULL) ; x ", "1.0\nattr(,'hi')\n2.0");
  }

  @Test
  public void testUnlist()  {
    assertEval("unlist(list('hello', 'hi')) ", "'hello', 'hi'");
    assertEval("unlist(list(a='hello', b='hi')) ", "      a    b\n'hello' 'hi'");
    assertEval("x <- list(a=1,b=2:3,list(x=FALSE)) ; unlist(x, recursive=FALSE) ", "$a\n1.0\n\n$b1\n2L\n\n$b2\n3L\n\n$x\nFALSE");
    assertEval("x <- list(1,z=list(1,b=22,3)) ; unlist(x, recursive=FALSE) ", "[[1]]\n1.0\n\n$z1\n1.0\n\n$z.b\n22.0\n\n$z3\n3.0");
    assertEval("x <- list(1,z=list(1,b=22,3)) ; unlist(x, recursive=FALSE, use.names=FALSE) ", "[[1]]\n1.0\n\n[[2]]\n1.0\n\n[[3]]\n22.0\n\n[[4]]\n3.0");
    assertEval("x <- list('a', c('b', 'c'), list('d', list('e'))) ; unlist(x) ", "'a', 'b', 'c', 'd', 'e'");
    assertEval("x <- list(NULL, list('d', list(), character())) ; unlist(x) ", "'d'");

    assertEval("x <- list(a=list('1','2',b='3','4')) ; unlist(x) ", " a1  a2 a.b  a4\n'1' '2' '3' '4'");
    assertEval("x <- list(a=list('1','2',b=list('3'))) ; unlist(x) ", " a1  a2 a.b\n'1' '2' '3'");
    assertEval("x <- list(a=list(1,FALSE,b=list(2:4))) ; unlist(x) ", " a1  a2 a.b1 a.b2 a.b3\n1.0 0.0  2.0  3.0  4.0");
  }

  @Test
  public void testOther()  {
    assertEval("rev.mine <- function(x) { if (length(x)) x[length(x):1L] else x } ; rev.mine(1:3) ", "3L, 2L, 1L");
  }

  @Test
  public void testAperm()  {
    // default argument for permutation is transpose
    assertTrue("a = array(1:4,c(2,2)); b = aperm(a); (a[1,1] == b[1,1]) && (a[1,2] == b[2,1]) && (a[2,1] == b[1,2]) && (a[2,2] == b[2,2]); ");

    // default for resize is true
    assertTrue("a = array(1:24,c(2,3,4)); b = aperm(a); dim(b)[1] == 4 && dim(b)[2] == 3 && dim(b)[3] == 2; ");

    // no resize does not change the dimensions
    assertTrue("a = array(1:24,c(2,3,4)); b = aperm(a, resize=FALSE); dim(b)[1] == 2 && dim(b)[2] == 3 && dim(b)[3] == 4; ");

    // correct structure with resize
    assertTrue("a = array(1:24,c(2,3,4)); b = aperm(a, c(2,3,1)); a[1,2,3] == b[2,3,1]; ");

    // correct structure on cubic array
    assertTrue("a = array(1:24,c(3,3,3)); b = aperm(a, c(2,3,1)); a[1,2,3] == b[2,3,1] && a[2,3,1] == b[3,1,2] && a[3,1,2] == b[1,2,3]; ");

    // correct structure on cubic array with no resize
    assertTrue("a = array(1:24,c(3,3,3)); b = aperm(a, c(2,3,1), resize = FALSE); a[1,2,3] == b[2,3,1] && a[2,3,1] == b[3,1,2] && a[3,1,2] == b[1,2,3]; ");

    // correct structure without resize
    assertTrue("a = array(1:24,c(2,3,4)); b = aperm(a, c(2,3,1), resize = FALSE); a[1,2,3] == b[2,1,2]; ");

    // first argument not an array
    assertEvalError("aperm(c(1,2,3)); ", "nvalid first argument, must be an array");

    // invalid perm length
    assertEvalError("aperm(array(1,c(3,3,3)), c(1,2)); ", "'perm' is of wrong length");

    // perm is not a permutation vector
    assertEvalError("aperm(array(1,c(3,3,3)), c(1,2,1)); ", "invalid 'perm' argument");

    // perm value out of bounds
    assertEvalError("aperm(array(1,c(3,3,3)), c(1,2,0)); ", "value out of range in 'perm'");

    // perm specified in complex numbers produces warning
    assertEvalWarning("aperm(array(1:27,c(3,3,3)), c(1+1i,3+3i,2+2i))[1,2,3] == array(1:27,c(3,3,3))[1,3,2]; ", "TRUE", "imaginary parts discarded in coercion");
  }

  @Test
  public void testColStatsMatrix() {
    // colSums on matrix drop dimension
    assertTrue("a = colSums(matrix(1:12,3,4)); is.null(dim(a)); ");

    // colSums on matrix have correct length
    assertTrue("a = colSums(matrix(1:12,3,4)); length(a) == 4; ");

    // colSums on matrix have correct values
    assertTrue("a = colSums(matrix(1:12,3,4)); a[1] == 6 && a[2] == 15 && a[3] == 24 && a[4] == 33; ");
  }

  @Test
  public void testColStatsArray() {
    // colSums on array have correct dimension
    assertTrue("a = colSums(array(1:24,c(2,3,4))); d = dim(a); d[1] == 3 && d[2] == 4; ");

    // colSums on array have correct length
    assertTrue("a = colSums(array(1:24,c(2,3,4))); length(a) == 12; ");

    // colSums on array have correct values
    assertTrue("a = colSums(array(1:24,c(2,3,4))); a[1,1] == 3 && a[2,2] == 19 && a[3,3] == 35 && a[3,4] == 47; ");
  }

  @Test
  public void testRowStats() {
    // rowSums on matrix drop dimension
    assertTrue("a = rowSums(matrix(1:12,3,4)); is.null(dim(a)); ");

    // rowSums on matrix have correct length
    assertTrue("a = rowSums(matrix(1:12,3,4)); length(a) == 3; ");

    // rowSums on matrix have correct values
    assertTrue("a = rowSums(matrix(1:12,3,4)); a[1] == 22 && a[2] == 26 && a[3] == 30; ");
  }

  @Test
  public void testRowStatsArray() {
    // rowSums on array have no dimension
    assertTrue("a = rowSums(array(1:24,c(2,3,4))); is.null(dim(a)); ");

    // row on array have correct length
    assertTrue("a = rowSums(array(1:24,c(2,3,4))); length(a) == 2; ");

    // rowSums on array have correct values
    assertTrue("a = rowSums(array(1:24,c(2,3,4))); a[1] == 144 && a[2] == 156; ");
  }

  @Test
  public void testRecall()  {
    assertEval("f<-function(i) { if(i<=1) 1 else i*Recall(i-1) } ; f(10) ", "3628800.0");
    assertEval("f<-function(i) { if(i<=1) 1 else i*Recall(i-1) } ; g <- f ; f <- sum ; g(10) ", "3628800.0");
    assertEval("f<-function(i) { if (i==1) { 1 } else if (i==2) { 1 } else { Recall(i-1) + Recall(i-2) } } ; f(10) ", "55.0");
    assertEvalError("Recall(10) ", "'Recall' called from outside a closure");
  }

  @Test
  public void testCrossprod()  {
    assertEval("x <- 1:6 ; crossprod(x) ", "     [,1]\n[1,] 91.0");
    assertEval("x <- 1:2 ; crossprod(t(x)) ", "     [,1] [,2]\n[1,]  1.0  2.0\n[2,]  2.0  4.0");
    assertEval("crossprod(1:3, matrix(1:6, ncol=2)) ", "     [,1] [,2]\n[1,] 14.0 32.0");
    assertEval("crossprod(t(1:2), 5) ", "     [,1]\n[1,]  5.0\n[2,] 10.0");
    assertEval("crossprod(c(1,NA,2), matrix(1:6, ncol=2)) ", "     [,1] [,2]\n[1,]   NA   NA");
  }

  @Test
  public void testSort()  {
    assertEval("sort(c(1L,10L,2L)) ", "1L, 2L, 10L");
    assertEval("sort(c(3,10,2)) ", "2.0, 3.0, 10.0");
    assertEval("sort(c(1,2,0/0,NA)) ", "1.0, 2.0");
    assertEval("sort(c(2,1,0/0,NA), na.last=NA) ", "1.0, 2.0");
    assertEval("sort(c(3,0/0,2,NA), na.last=TRUE) ", "2.0, 3.0, NaN, NA");
    assertEval("sort(c(3,NA,0/0,2), na.last=FALSE) ", "NA, NaN, 2.0, 3.0");
    assertEval("sort(c(3L,NA,2L)) ", "2L, 3L");
    assertEval("sort(c(3L,NA,-2L), na.last=TRUE) ", "-2L, 3L, NA");
    assertEval("sort(c(3L,NA,-2L), na.last=FALSE) ", "NA, -2L, 3L");
    assertEval("sort(c(a=NA,b=NA,c=3,d=1),na.last=TRUE, decreasing=TRUE) ", "  c   d  a  b\n3.0 1.0 NA NA");
    assertEval("sort(c(a=NA,b=NA,c=3,d=1),na.last=FALSE, decreasing=FALSE) ", " a  b   d   c\nNA NA 1.0 3.0");
    assertEval("sort(c(a=0/0,b=1/0,c=3,d=NA),na.last=TRUE, decreasing=FALSE) ", "  c        b   a  d\n3.0 Infinity NaN NA");
    assertEval("sort(double()) ", "numeric(0)");
    assertEval("sort(c(a=NA,b=NA,c=3L,d=-1L),na.last=TRUE, decreasing=FALSE) ", "  d  c  a  b\n-1L 3L NA NA");
    assertEval("sort(c(3,NA,1,d=10), decreasing=FALSE, index.return=TRUE) ","$x\n           d\n1.0 3.0 10.0\n\n$ix\n2L, 1L, 3L");
    assertEval("sort(3:1, index.return=TRUE) ", "$x\n1L, 2L, 3L\n\n$ix\n3L, 2L, 1L");
    assertEval("sort(c(TRUE,FALSE,FALSE,NA,FALSE), index.return=TRUE)$ix ", "2L, 3L, 4L, 1L");

    // set GNU-R locale to C - Sys.setlocale("LC_COLLATE", "C")
    assertEval("sort(c('a','z','Z','xxxz','zza','b'), index.return=TRUE)$ix ", "3L, 1L, 6L, 4L, 2L, 5L");
    assertEval("sort(c(a=NA,1,b=NA,0/0,2,3), na.last=TRUE, decreasing=FALSE) ", "             a  b    \n1.0 2.0 3.0 NA NA NaN");
    assertEval("sort(c(a=NA,1L,b=NA,0L,2L,-3L), na.last=TRUE, decreasing=TRUE) ", "              a  b\n2L 1L 0L -3L NA NA");
    assertEval("sort(c(a=NA,1L,b=NA,0L,2L,-3L), na.last=FALSE, decreasing=TRUE) ", " a  b             \nNA NA 2L 1L 0L -3L");
    assertEval("sort(c(a=NA,1L,b=NA,0L,2L,-3L), na.last=NA, decreasing=TRUE) ", "            \n2L 1L 0L -3L");
    assertEval("sort(c('A','a'), decreasing=TRUE) ", "'a', 'A'");
    assertEval("sort(c('a','A'), decreasing=FALSE) ", "'A', 'a'");
    assertEval("sort(c('a','A','z','Z','   01','01',NA), na.last=NA, decreasing=TRUE, index.return=TRUE)$ix ", "3L, 1L, 4L, 2L, 6L, 5L");
    assertEval("sort(c('a','A','z','Z','   01','01',NA), na.last=TRUE, decreasing=FALSE) ", "'   01', '01', 'A', 'Z', 'a', 'z', NA");
    assertEval("sort(c(TRUE,NA,TRUE,NA,FALSE,TRUE,NA), na.last=FALSE, decreasing=FALSE) ", "NA, NA, NA, FALSE, TRUE, TRUE, TRUE");
    assertEval("sort(c(TRUE,NA,TRUE,NA,FALSE,TRUE,NA), na.last=NA, decreasing=TRUE) ", "TRUE, TRUE, TRUE, FALSE");
  }

  @Test
  public void testCbind()  {
    assertEval("cbind(1:3,1:3) ", "     [,1] [,2]\n[1,]   1L   1L\n[2,]   2L   2L\n[3,]   3L   3L");
    assertEval("cbind() ", "NULL");
    assertEval("m <- matrix(1:6, nrow=2) ; cbind(11:12, m) ", "     [,1] [,2] [,3] [,4]\n[1,]  11L   1L   3L   5L\n[2,]  12L   2L   4L   6L");
    assertEval("cbind(list(1,2), TRUE, 'a') ", "     [,1] [,2] [,3]\n[1,]  1.0 TRUE  'a'\n[2,]  2.0 TRUE  'a'");
    assertEvalWarning("cbind(1:3,1:2) ", "     [,1] [,2]\n[1,]   1L   1L\n[2,]   2L   2L\n[3,]   3L   1L", "number of rows of result is not a multiple of vector length (arg 2)");
    assertEval("cbind(1:3,2) ", "     [,1] [,2]\n[1,]  1.0  2.0\n[2,]  2.0  2.0\n[3,]  3.0  2.0");
  }

  @Test
  public void testRank()  {
    assertEval("rank(c(10,100,100,1000)) ", "1.0, 2.5, 2.5, 4.0");
    assertEval("rank(c(1000,100,100,100, 10)) ", "5.0, 3.0, 3.0, 3.0, 1.0");
    assertEval("rank(c(a=2,b=1,c=3,40)) ", "  a   b   c    \n2.0 1.0 3.0 4.0");
    assertEval("rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=NA) ", "  a   b   c   e\n2.0 1.0 3.0 4.0");
    assertEval("rank(c(a=2,b=1,c=3,d=NA,e=40), na.last='keep') ", "  a   b   c  d   e\n2.0 1.0 3.0 NA 4.0");
    assertEval("rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=TRUE) ", "  a   b   c   d   e\n2.0 1.0 3.0 5.0 4.0");
    assertEval("rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=FALSE) ", "  a   b   c   d   e\n3.0 2.0 4.0 1.0 5.0");
    assertEval("rank(c(a=1,b=1,c=3,d=NA,e=3), na.last=FALSE, ties.method='max') ", " a  b  c  d  e\n3L 3L 5L 1L 5L");
    assertEval("rank(c(a=1,b=1,c=3,d=NA,e=3), na.last=NA, ties.method='min') ", " a  b  c  e\n1L 1L 3L 3L");
    assertEval("rank(c(1000, 100, 100, NA, 1, 20), ties.method='first') ", "5L, 3L, 4L, 6L, 1L, 2L");
  }

  @Test
  public void testCor()  {
    assertEval("cor(cbind(c(1:9,0/0), 101:110)) ", "     [,1] [,2]\n[1,]  1.0   NA\n[2,]   NA  1.0");
    assertEval("round( cor(cbind(c(10,5,4,1), c(2,5,10,5))), digits=5 ) ", "         [,1]     [,2]\n[1,]      1.0 -0.53722\n[2,] -0.53722      1.0");
    assertEval("cor(cbind(c(3,2,1), c(1,2,3))) ", "     [,1] [,2]\n[1,]  1.0 -1.0\n[2,] -1.0  1.0");
    assertEvalWarning("cor(cbind(c(1,1,1), c(1,1,1))) ", "     [,1] [,2]\n[1,]  1.0   NA\n[2,]   NA  1.0", "the standard deviation is zero");
  }

  @Test
  public void testDet()  {
    assertEval("det(matrix(c(1,2,4,5),nrow=2)) ", "-3.0");
    assertEval("det(matrix(c(1,-3,4,-5),nrow=2)) ", "7.0");
    assertEval("det(matrix(c(1,0,4,NA),nrow=2)) ", "NA");
  }

  @Test
  public void testFFT()  {
    assertEval("fft(1:4) ","10.0+0.0i, -2.0+2.0i, -2.0+0.0i, -2.0-2.0i");
    assertEval("fft(1:4, inverse=TRUE) ", "10.0+0.0i, -2.0-2.0i, -2.0+0.0i, -2.0+2.0i");
    assertEval("fft(10) ", "10.0+0.0i");
    assertEval("fft(cbind(1:2,3:4)) ", "          [,1]      [,2]\n[1,] 10.0+0.0i -4.0+0.0i\n[2,] -2.0+0.0i  0.0+0.0i");
  }

  @Test
  public void testChol()  {
    assertEval("chol(1) ", "     [,1]\n[1,]  1.0");
    assertEval("round( chol(10), digits=5) ", "        [,1]\n[1,] 3.16228");
    assertEval("m <- matrix(c(5,1,1,3),2) ; round( chol(m), digits=5 ) ", "        [,1]    [,2]\n[1,] 2.23607 0.44721\n[2,]     0.0 1.67332");
    assertEvalError("m <- matrix(c(5,-5,-5,3),2,2) ; chol(m) ", "the leading minor of order 2 is not positive definite");
  }

  @Test
  public void testQr()  {

    assertEval("qr(10, LAPACK=TRUE) ", "$qr\n     [,1]\n[1,] 10.0\n\n$rank\n1L\n\n$qraux\n0.0\n\n$pivot\n1L\nattr(,'useLAPACK')\nTRUE");
    assertEval("round( qr(matrix(1:6,nrow=2), LAPACK=TRUE)$qr, digits=5) ", "         [,1]     [,2]     [,3]\n[1,] -7.81025 -2.17663 -4.99344\n[2,]  0.46837  0.51215  0.25607");
    assertEval("qr(matrix(1:6,nrow=2), LAPACK=FALSE)$pivot ", "1L, 2L, 3L");
    assertEval("qr(matrix(1:6,nrow=2), LAPACK=FALSE)$rank ", "2L");
    assertEval("round( qr(matrix(1:6,nrow=2), LAPACK=FALSE)$qraux, digits=5 ) ", "1.44721, 0.89443, 1.78885");
    assertEval("round( qr(matrix(c(3,2,-3,-4),nrow=2), LAPACK=FALSE)$qr, digits=5 ) ", "         [,1]    [,2]\n[1,] -3.60555 4.71495\n[2,]   0.5547 -1.6641");

    // qr.coef
    assertEvalError("x <- qr(cbind(1:10,2:11), LAPACK=TRUE) ; qr.coef(x, 1:2) ", "right-hand side should have 10 not 2 rows");
    assertEval("x <- qr(t(cbind(1:10,2:11)), LAPACK=TRUE) ; qr.coef(x, 1:2) ", "1.0, NA, NA, NA, NA, NA, NA, NA, NA, 0.0");
    assertEval(" { x <- qr(cbind(1:10,2:11), LAPACK=TRUE) ; round( qr.coef(x, 1:10), digits=5 ) ", "1.0, 0.0");
    assertEval("x <- qr(c(3,1,2), LAPACK=TRUE) ; round( qr.coef(x, c(1,3,2)), digits=5 ) ", "0.71429");
    // FIXME: GNU-R will print negative zero as zero
    assertEval("x <- qr(t(cbind(1:10,2:11)), LAPACK=FALSE) ; qr.coef(x, 1:2) ", "1.0, -0.0, NA, NA, NA, NA, NA, NA, NA, NA");
    assertEval("x <- qr(c(3,1,2), LAPACK=FALSE) ; round( qr.coef(x, c(1,3,2)), digits=5 ) ", "0.71429");
    assertEval("m <- matrix(c(1,0,0,0,1,0,0,0,1),nrow=3) ; x <- qr(m, LAPACK=FALSE) ; qr.coef(x, 1:3) ", "1.0, 2.0, 3.0");
    assertEval("x <- qr(cbind(1:3,2:4), LAPACK=FALSE) ; round( qr.coef(x, 1:3), digits=5 ) ", "1.0, 0.0");

    // qr.solve
    assertEval("round( qr.solve(qr(c(1,3,4,2)), c(1,2,3,4)), digits=5 ) ", "0.9");
    assertEval("round( qr.solve(c(1,3,4,2), c(1,2,3,4)), digits=5) ", "0.9");
  }

  @Test
  public void testComplex()  {
    assertEval("x <- 1:2 ; attr(x,'my') <- 2 ; Im(x) ", "0.0, 0.0\nattr(,'my')\n2.0");
    assertEval("x <- c(1+2i,3-4i) ; attr(x,'my') <- 2 ; Im(x) ", "2.0, -4.0\nattr(,'my')\n2.0");
    assertEval("x <- 1:2 ; attr(x,'my') <- 2 ; Re(x) ", "1.0, 2.0\nattr(,'my')\n2.0");
    assertEval("x <- c(1+2i,3-4i) ; attr(x,'my') <- 2 ; Re(x) ", "1.0, 3.0\nattr(,'my')\n2.0");
  }

  @Test
  public void testRound()  {
    assertEval("round(0.4) ", "0.0");
    assertEval("round(0.5) ", "0.0");
    assertEval("round(0.6) ", "1.0");
    assertEval("round(1.5) ", "2.0");
    assertEval("round(1L) ", "1.0");
    assertEval("round(1.123456,digit=2.8) ", "1.123");
    assertEval("round(1/0) ", "Infinity");
  }

  @Test
  public void testRandom()  {

    assertEval("round( rnorm(3), digits = 5 ) ", "-1.26974, -0.33447, 3.03882");
    assertEval("round( rnorm(3,1000,10), digits = 5 ) ", "987.30263, 996.65534, 1030.38818");
    assertEval("round( rnorm(3,c(1000,2,3),c(10,11)), digits = 5 ) ", "987.30263, -1.67912, 33.38818");

    assertEval("round( runif(3), digits = 5 ) ", "0.10209, 0.85416, 0.36901");
    assertEval("round( runif(3,1,10), digits = 5 ) ", "1.9188, 8.68741, 4.32113");
    assertEval("round( runif(3,1:3,3:2), digits = 5 ) ", "1.20418, 2.0, 3.0");

    assertEval("round( rgamma(3,1), digits = 5 ) ", "0.39205, 0.46677, 0.52786");
    assertEval("round( rgamma(3,0.5,scale=1:3), digits = 5 ) ", "0.01461, 11.74775, 0.61223");
    assertEval("round( rgamma(3,0.5,rate=1:3), digits = 5 ) ", "0.01461, 2.93694, 0.06803");

    assertEval("round( rbinom(3,3,0.9), digits = 5 ) ", "3.0, 2.0, 3.0");
    assertEval("round( rbinom(3,10,(1:5)/5), digits = 5 ) ", "0.0, 6.0, 7.0");

    assertEval("round( rlnorm(3), digits = 5 ) ", "0.28091, 0.71572, 20.88056");
    assertEval("round( rlnorm(3,sdlog=c(10,3,0.5)), digits = 5 ) ", "0.0, 0.36663, 4.56952");

    assertEval("round( rcauchy(3), digits = 5 ) ", "0.33219, -0.49318, 2.29137");
    assertEval("round( rcauchy(3, scale=4, location=1:3), digits = 5 ) ", "2.32876, 0.02726, 12.16546");
  }

  @Test
  public void testDelayedAssign()  {
    assertEval("delayedAssign('x', y); y <- 10; x ", "10.0");
    assertEval("delayedAssign('x', a+b); a <- 1 ; b <- 3 ; x ", "4.0");
    assertEval("f <- function() { delayedAssign('x', y); y <- 10; x  } ; f() ", "10.0");
    assertEval("h <- new.env(parent=emptyenv()) ; delayedAssign('x', y, h, h) ; assign('y', 2, h) ; get('x', h) ", "2.0");
    assertEval("h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; delayedAssign('x', y, h, h) ; assign('y', 2, h) ; get('x', h) ", "2.0");
    assertEvalError("f <- function() { delayedAssign('x', y); delayedAssign('y', x) ; x } ; f() ", "promise already under evaluation: recursive default argument reference?");
    assertEval("f <- function(...) { delayedAssign('x', ..1) ; y <<- x } ; f(10) ; y ", "10.0");
    assertEval("f <- function() { delayedAssign('x', 3); delayedAssign('x', 2); x } ; f() ", "2.0");
    assertEval("f <- function() { x <- 4 ; delayedAssign('x', y); y <- 10; x  } ; f() ", "10.0");
  }

  @Test
  public void testMissing()  {
    assertEval("f <- function(a = 2 + 3) { missing(a) } ; f() ", "TRUE");
    assertEval("f <- function(a = z) { missing(a) } ; f() ", "TRUE");
    assertEval("f <- function(a = 2 + 3) { a;  missing(a) } ; f() ", "TRUE");
    assertEval("f <- function(a) { g(a) } ;  g <- function(b) { missing(b) } ; f() ", "TRUE");
    assertEval("f <- function(a = 2) { g(a) } ; g <- function(b) { missing(b) } ; f() ", "FALSE");
    assertEval("f <- function(a = z) {  g(a) } ; g <- function(b) { missing(b) } ; f() ", "FALSE");
    assertEval("f <- function(a = z, z) {  g(a) } ; g <- function(b) { missing(b) } ; f() ", "TRUE");
    assertEval("f <- function(a) { g(a) } ; g <- function(b=2) { missing(b) } ; f() ", "TRUE");
    assertEval("f <- function(x = y, y = x) { g(x, y) } ; g <- function(x, y) { missing(x) } ; f() ", "TRUE");
    assertEval("f <- function(a,b,c) { missing(b) } ; f(1,,2) ", "TRUE");
    assertEval("g <- function(a, b, c) { b } ; f <- function(a,b,c) { g(a,b=2,c) } ; f(1,,2) ", "2.0"); // not really the builtin, but somewhat related
    assertEval("f <- function(x) { missing(x) } ; f(a) ", "FALSE");
    assertEval("f <- function(a) { g <- function(b) { before <- missing(b) ; a <<- 2 ; after <- missing(b) ; c(before, after) } ; g(a) } ; f() ", "TRUE, FALSE");
    assertEval("f <- function(...) { g(...) } ;  g <- function(b=2) { missing(b) } ; f() ", "TRUE");
    assertEval("f <- function(...) { missing(..2) } ; f(x + z, a * b) ", "FALSE");
  }

  @Test
  public void testQuote()  {
    assertEval("quote(1:3) ", "1.0 : 3.0"); // specific to fastr output format
    assertEval("quote(list(1,2)) ", "list(1.0, 2.0)"); // specific to fastr output format
    assertEval("typeof(quote(1)) ", "'double'");
    assertEval("typeof(quote(x + y)) ", "'language'");
    assertEval("quote(x <- x + 1) ", "x <- x + 1.0"); // specific to fastr output format
    assertEval("typeof(quote(x)) ", "'symbol'");

    assertEval("quote(`a` <- 1) ", "a <- 1.0");
    assertEval("quote(`a.` <- 1) ", "a. <- 1.0");
    assertEval("quote(`.a` <- 1) ", ".a <- 1.0");
    assertEval("quote(`a+` <- 1) ", "`a+` <- 1.0");
    assertEval("quote(`_a` <- 1) ", "`_a` <- 1.0");
    assertEval("quote(`a_` <- 1) ", "a_ <- 1.0");
    assertEval("quote(f(g(x)) <- 2) ", "f(g(x)) <- 2.0");
    assertEval("quote(f(x) <- 2) ", "f(x) <- 2.0");
    assertEval("quote(f(x,1,2,z+1) <- 2) ", "f(x, 1.0, 2.0, z + 1.0) <- 2.0");
    assertEval("g <- function() { f(x,1,2,z+1) <- 2 } ; g ", "function () { f(x, 1.0, 2.0, z + 1.0) <- 2.0 ");
  }

  @Test
  public void testSubstitute()  {
    assertEval("substitute(x + y, list(x=1)) ", "1.0 + y");
    assertEval("f <- function(expr) { substitute(expr) } ; f(a * b) ", "a * b");
    assertEval("f <- function() { delayedAssign('expr', a * b) ; substitute(expr) } ; f() ", "a * b");
    assertEval("f <- function() { delayedAssign('expr', a * b) ; substitute(dummy) } ; f() ", "dummy");
    assertEval("delayedAssign('expr', a * b) ; substitute(expr) ", "expr");
    assertEval("f <- function(expr) { expr ; substitute(expr) } ; a <- 10; b <- 2; f(a * b) ", "a * b");
    assertEval("f <- function(expra, exprb) { substitute(expra + exprb) } ; f(a * b, a + b) ", "a * b + a + b");
    assertEval("f <- function(y) { substitute(y) } ; f() ", "");
    assertEval("f <- function(y) { substitute(y) } ; typeof(f()) ", "'symbol'");
    assertEval("f <- function(z) { g <- function(y) { substitute(y)  } ; g(z) } ; f(a + d) ", "z");
    assertEval("f <- function(x) { g <- function() { substitute(x) } ; g() } ;  f(a * b) ", "x");
    assertEval("substitute(a, list(a = quote(x + y), x = 1)) ", "x + y");
    assertEval("f <- function(x = y, y = x) { substitute(x) } ; f() ", "y");
    assertEval("f <- function(a, b=a, c=b, d=c) { substitute(d) } ; f(x + y) ", "c");
    assertEval("substitute(if(a) { x } else { x * a }, list(a = quote(x + y), x = 1)) ", "if(x + y) { 1.0 } else { 1.0 * (x + y) "); // specific to fastr output format
    assertEval("substitute(function(x, a) { x + a }, list(a = quote(x + y), x = 1)) ", "function(x, a) { 1.0 + x + y "); // specific to fastr output format
    assertEval("substitute(a[x], list(a = quote(x + y), x = 1)) ", "x + y[1.0]");  // specific to fastr output format
    assertEval("f <- function(x) { substitute(x, list(a=1,b=2)) } ; f(a + b) ", "x");
    assertEval("f <- function() { substitute(x(1:10), list(x=quote(sum))) } ; f() ", "sum(1.0 : 10.0)"); // specific to fastr output format
    assertEval("env <- new.env() ; z <- 0 ; delayedAssign('var', z+2, assign.env=env) ; substitute(var, env=env) ", "z + 2.0");
    assertEval("env <- new.env() ; z <- 0 ; delayedAssign('var', z+2, assign.env=env) ; z <- 10 ; substitute(var, env=env) ", "z + 2.0");
    assertEval("f <- function() { substitute(list(a=1,b=2,...,3,...)) } ; f() ", "list(a=1.0, b=2.0, ..., 3.0, ...)");
    assertEval("f <- function(...) { substitute(list(a=1,b=2,...,3,...)) } ; f() ", "list(a=1.0, b=2.0, 3.0)");
    assertEval("f <- function(...) { substitute(list(a=1,b=2,...,3,...)) } ; f(x + z, a * b) ", "list(a=1.0, b=2.0, x + z, a * b, 3.0, x + z, a * b)");
    assertEval("f <- function(...) { substitute(list(...)) } ; f(x + z, a * b) ", "list(x + z, a * b)");

  }

  @Test
  public void testInvocation()  {
    assertEvalError("rnorm(n=1,n=2) ", "formal argument 'n' matched by multiple actual arguments");
    assertEvalError("rnorm(s=1,s=1) ", "formal argument 'sd' matched by multiple actual arguments");
    assertEvalError("matrix(1:4,n=2) ", "argument 2 matches multiple formal arguments");
    assertEvalError("matrix(x=1) ", "unused argument(s) (x = 1.0)");

    assertEval("round( rnorm(1,), digits = 5 ) ", "-1.26974");

    assertEvalError("max(1,2,) ", "argument 3 is empty");
    assertEval("matrix(da=1:3,1) ", "     [,1] [,2] [,3]\n[1,]   1L   2L   3L");

    assertEval("f <- function(...) { g <- function() { list(...)$a } ; g() } ; f(a=1) ", "1.0");
    assertEval("f <- function(...) { l <- list(...) ; l[[1]] <- 10; ..1 } ; f(11,12,13) ", "11.0");
    assertEval("g <- function(...) { length(list(...)) } ; f <- function(...) { g(..., ...) } ; f(z = 1, g = 31) ", "4L");
    assertEval("g <- function(...) { max(...) } ; g(1,2) ", "2.0");
    assertEval("g <- function(...) { `-`(...) } ; g(1,2) ", "-1.0");
    assertEval("f <- function(...) { list(a=1,...) } ; f(b=2,3) ", "$a\n1.0\n\n$b\n2.0\n\n[[3]]\n3.0");
    assertEval("f <- function(...) { substitute(...) } ; f(x + z) } ", "x + z");
    assertEval("f <- function(a, ...) { list(...) } ; f(1) ", "list()");
    assertEval("f <- function(...) { args <- list(...) ; args$name } ; f(name = 42) ", "42.0");
    assertEval("p <- function(prefix, ...) { cat(prefix, ..., '\n') } ; p('INFO', 'msg:', 'Hello', 42) ", "INFO msg: Hello 42.0 \n", "NULL");
  }

  @Test
  public void testEval()  {
    assertEval("eval(quote(x+x), list(x=1)) ", "2.0");
    assertEval("y <- 2; eval(quote(x+y), list(x=1)) ", "3.0");
    assertEval("y <- 2; x <- 4; eval(x + y, list(x=1)) ", "6.0");
    assertEval("y <- 2; x <- 2 ; eval(quote(x+y), -1) ", "4.0");
    assertEvalError("f <- function() { quote(x) } ; l <- f() ; eval(l) ", "object 'x' not found");
    assertEvalError("f <- function() { quote(x) } ; eval(f()) ", "object 'x' not found");
    assertEvalError("f <- function() { quote({ x[1] <- 2 }) } ; eval(f()) ", "object 'x' not found");
  }

  @Test
  public void testDeparse()  {
    assertEval("f <- function(x) { deparse(substitute(x)) } ; f(a + b * (c - d)) ", "'a + b * (c - d)'");
  }

  @Test
  public void testDiagnostics()  {
    assertEvalError("f <- function() { stop('hello','world') } ; f() ", "helloworld");
  }

  @Test
  public void testSprintf()  {
    assertEval("sprintf('%d', 10) ", "'10'");
    assertEval("sprintf('%7.3f', 10.1) ", "' 10.100'");
    assertEval("sprintf('%03d', 1:3) ", "'001', '002', '003'");
    assertEval("sprintf('%3d', 1:3) ", "'  1', '  2', '  3'");
    assertEval("sprintf('Hello %*d', 3, 2) ", "'Hello   2'");
    assertEval("sprintf('Hello %*2$d', 3, 2) ", "'Hello  3'");
    assertEval("sprintf('Hello %2$*2$d', 3, 2) ", "'Hello  2'");
    assertEval("sprintf('%4X', 26) ", "'  1A'");
    assertEval("sprintf('%04X', 26) ", "'001A'");

    assertEvalError("sprintf(1:3) ", "'fmt' is not a character vector");
    assertEval("sprintf('%s',NULL) ", "character(0)");
    assertEval("sprintf(c('%f','%e'),1) ", "'1.000000', '1.000000e+00'");
    assertEval("sprintf(c('%f','%% %e'),1) ", "'1.000000', '% 1.000000e+00'");
    assertEval("sprintf(c('%f','%e %%'),1) ", "'1.000000', '1.000000e+00 %'");
    assertEval("sprintf('second %2$1.0f, first %1$5.2f, third %3$1.0f', 3.141592, 2, 3) ", "'second 2, first  3.14, third 3'"); // from GNU-R help page
    assertEval("sprintf('res %4$6d',1,2,3,4,5,6,7,8,9,10,11) ", "'res      4'");
    assertEval("sprintf('res %11$06d',1,2,3,4,5,6,7,8,9,10,11) ", "'res 000011'");
    assertEvalError("sprintf('Hello %2$*2*$d', 3, 2) ", "at most one asterisk '*' is supported in each conversion specification");
    assertEvalError("sprintf('res %11$0.6d',1,2,3) ", "reference to non-existent argument 11");
    assertEval("sprintf('Hello %1$*11$d', 3, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12) ", "'Hello            3'");
    assertEvalError("sprintf('Hello %1$*99$d', 3, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12) ", "reference to non-existent argument 99");
    assertEvalError("sprintf('Hello %1$*3$d', 3, 2, 'hello') ", "argument for '*' conversion specification must be a number");
    assertEval("sprintf('Hello %1$*3$d', 3, 2, 4L) ", "'Hello    3'");
    assertEvalError("sprintf('Hello %d\n') ", "too few arguments");
    assertEvalError("sprintf('Hello %*d', 2) ", "too few arguments");
    assertEvalError("sprintf('Hello %d %d %*d', 3, 2) ", "too few arguments");
    assertEvalError("sprintf('Hello %*d', 2e100, 3) ", "argument for '*' conversion specification must be a number");
    assertEval("sprintf('Hello %*i', 2, 3) ", "'Hello  3'");
    assertEvalError("sprintf('Hello %Q') ", "unrecognized format specification '%Q'");
    assertEvalError("sprintf('Hello %') ", "unrecognized format specification '%'");
    assertEval("sprintf('Hello %d == %s', TRUE, TRUE) ", "'Hello 1 == TRUE'");
    assertEval("sprintf('Hello %d == %s', 1L, 1L) ", "'Hello 1 == 1L'");
    assertEvalError("sprintf('Hello %d == %s', 1+2i, 1+3i) ", "unsupported type");
    assertEval("sprintf('Hello %s', 'World!') ", "'Hello World!'");
    assertEvalError("sprintf('Hello %d', 1e100) ", "invalid format '%d'; use format %f, %e, %g or %a for numeric objects");
    assertEval("sprintf('Hello %d', 100) ", "'Hello 100'");
    assertEval("sprintf('Hello %f %f %f %f', 0/0, -1/0, 1/0, 1[2]) ", "'Hello NaN -Inf Inf NA'");
    assertEval("sprintf('Hello %5.f %5.f %5.f %5.f', 0/0, -1/0, 1/0, 1[2]) ", "'Hello   NaN  -Inf   Inf    NA'");
    assertEval("sprintf('% f',1.234556) ", "' 1.234556'");
    assertEval("sprintf('Hello %s', 0/0) ", "'Hello NaN'");
    assertEvalError("sprintf('Hello %x', '0/0') ", "invalid format '%x'; use format %s for character objects");
    assertEval("sprintf('Hello %x', 1L[2]) ", "'Hello NA'");
    assertEval("sprintf('Hello %g', NA) ", "'Hello NA'");
    assertEval("sprintf('Hello %g', 1L[2]) ", "'Hello NA'");
    assertEvalError("sprintf('Hello %X', TRUE) ", "invalid format '%X'; use format %d or %i for logical objects");
    assertEval("sprintf('Hello %i', NA) ", "'Hello NA'");
  }

  @Test
  public void testIdentical()  {
    assertEval("identical(1,1) ", "TRUE");
    assertEval("identical(1L,1) ", "FALSE");
    assertEval("identical(1:3, c(1L,2L,3L)) ", "TRUE");
    assertEval("identical(0/0,1[2]) ", "FALSE");
    assertEval("identical(list(1, list(2)), list(list(1), 1)) ", "FALSE");
    assertEval("identical(list(1, list(2)), list(1, list(2))) ", "TRUE");
    assertEval("x <- 1 ; attr(x, 'my') <- 10; identical(x, 1) ", "FALSE");
    assertEval("x <- 1 ; attr(x, 'my') <- 10; y <- 1 ; attr(y, 'my') <- 10 ; identical(x,y) ", "TRUE");
    assertEval("x <- 1 ; attr(x, 'my') <- 10; y <- 1 ; attr(y, 'my') <- 11 ; identical(x,y) ", "FALSE");
    assertEval("x <- 1 ; attr(x, 'hello') <- 2 ; attr(x, 'my') <- 10;  attr(x, 'hello') <- NULL ; y <- 1 ; attr(y, 'my') <- 10 ; identical(x,y) ", "TRUE");

    assertEval("identical(1,c) ", "FALSE");
    assertEval("identical(c,1) ", "FALSE");
    assertEval("identical(1:4, matrix(1:4,nrow=2)) ", "FALSE");
    assertEval("identical(1:4, c(a=1L,b=2L,3L,4L)) ", "FALSE");
    assertEval("identical(as.list(1:4), c(a=1L,b=2L,3L,4L)) ", "FALSE");
    assertEval("identical(as.list(1:4),1:4) ", "FALSE");
    assertEval("identical(c,c) ", "TRUE");
    assertEval("identical('1+2i',1+2i) ", "FALSE");
    assertEval("identical(1L, 1:1) ", "TRUE");
    assertEval("identical('hello', 'hello') ", "TRUE");
    assertEval("identical(1+2i, 0+1.0+2.0i-0) ", "TRUE");
    assertEval("identical(1+2i, 0+1.0+2.0i-0.001) ", "FALSE");
    assertEval("identical(0+0i,0) ", "FALSE");
    assertEval("identical(TRUE,as.logical(10)) ", "TRUE");
    assertEval("identical(TRUE,1L) ", "FALSE");
    assertEval("identical(as.raw(11), as.raw(10+1)) ", "TRUE");
    assertEval("identical(as.raw(11), 11) ", "FALSE");
    assertEval("identical(11, as.raw(11)) ", "FALSE");
    assertEval("identical(NULL,0) ", "FALSE");
    assertEval("identical(list(list(1,2),list(3)), list(list(1,2),list(3+0))) ", "TRUE");
    assertEval("identical(list(list(1,2),list(3)), list(list(1,2),list(3+0), list(4))) ", "FALSE");
    assertEval("identical(c('hello','hi'),c('hello','hI')) ", "FALSE");
    assertEval("identical(c('hello','hi'),c('hello',NA)) ", "FALSE");
    assertEval("identical(c('hello',NA),c('hello',NA)) ", "TRUE");
    assertEval("x <- 'hi' ; identical(c('hello',x),c('hello',x)) ", "TRUE");
    assertEval("identical(c('hello',NA),c('hello','x')) ", "FALSE");
    assertEval("identical(c('hello',NA),c('hello')) ", "FALSE");
    assertEval("identical(c(0/0,NA),c(NA,0/0)) ", "FALSE");
    assertEval("identical(c(1/0,-3/0),c(2/0,-1e100/0)) ", "TRUE");
    assertEval("identical(c(1/0,-3/0),c(0/0,NA)) ", "FALSE");
    assertEval("identical(c(0/0,NA),c(1/0,-3/0)) ", "FALSE");
    assertEval("identical(c(1+1,NA),c(2,NA)) ", "TRUE");
    assertEval("identical(c(1+2i,3+4i), c(1+2i,3+2i)) ", "FALSE");
    assertEval("identical(c(1+2i,3+4i), c(1+2i,2+4i)) ", "FALSE");
    assertEval("identical(c(1+2i,3+4i), c(1+2i)) ", "FALSE");
    assertEval("identical(c(1,2),c(1)) ", "FALSE");
    assertEval("identical(1:2,1:1) ", "FALSE");
    assertEval("identical(1:2,c(1L,3L)) ", "FALSE");
    assertEval("identical(c(TRUE,FALSE), c(TRUE,NA)) ", "FALSE");
    assertEval("identical(c(TRUE,FALSE), c(TRUE)) ", "FALSE");
    assertEval("identical(as.raw(11:12), as.raw(11)) ", "FALSE");
    assertEval("identical(as.raw(11:12), as.raw(c(11,13))) ", "FALSE");
    assertEval("x <- 1 ; attr(x,'my') <- 1 ; identical(1, x) ", "FALSE");
    assertEval("x <- 1 ; attr(x,'my') <- 1 ; attr(x,'my') <- NULL ; identical(1, x) ", "TRUE");
    assertEval("x <- 1 ; attr(x,'my') <- 1 ; attr(x,'my') <- NULL ; identical(x, 1) ", "TRUE");
    assertEval("x <- 1 ; attr(x,'my') <- 1 ; attr(x,'my') <- NULL ; y <- 1 ; attr(y,'hi') <- 2 ; identical(x, y) ", "FALSE");
    assertEval("x <- 1 ; attr(x,'my') <- 1 ; y <- 1 ; attr(y,'my') <- 2 ; identical(x, y) ", "FALSE");
    assertEval("l <- list(1,2,3); l[[2]] <- NULL; identical(l, list(1,3)) ", "TRUE");
    assertEval("x <- 1 ; attr(x,'my') <- 1 ; y <- 1 ; attr(y,'hi') <- 1 ; identical(x, y) ", "FALSE");
    assertEval("identical(c(a=1,b=2,c=3),c(a=1,aa=2,c=3)) ", "FALSE");
    assertEval("identical(c(a=1,b=2,c=3),c(a=1,b=2)) ", "FALSE");
    assertEval("identical(c(a=1,b=2),c(a=1,b=2)) ", "TRUE");
    assertEval("identical(c(a=1,b=2),c(1,2)) ", "FALSE");
    assertEval("identical(c(1,2), c(a=1,b=2)) ", "FALSE");
    assertEval("x <- list(1,b=2,3) ; x[[2]] <- NULL ; identical(x,list(1,3)) ", "FALSE");
  }

  @Test
  public void testWorkingDirectory()  {
    assertEval("cur <- getwd(); cur1 <- setwd(getwd()) ; cur2 <- getwd() ; cur == cur1 && cur == cur2 ", "TRUE");
    assertEvalError("setwd(1) ", "character argument expected");
    assertEvalError("setwd(character()) ", "character argument expected");
    assertEval("cur <- getwd(); cur1 <- setwd(c(cur, 'dummy')) ; cur2 <- getwd() ; cur == cur1  ", "TRUE");
  }

  @Test
  public void testFileListing()  {
    assertEval("list.files('test/r/simple/data/tree1') ", "'bar.txt', 'dummy.txt', 'subdir'");
    assertEval("list.files('test/r/simple/data/tree1', recursive=TRUE) ", "'bar.txt', 'dummy.txt', 'subdir/subbar.txt', 'subdir/subdummy.txt'");
    assertEval("list.files('test/r/simple/data/tree1', recursive=TRUE, pattern='.*dummy.*') ", "'dummy.txt', 'subdir/subdummy.txt'");
    assertEval("list.files('test/r/simple/data/tree1', recursive=TRUE, pattern='dummy') ", "'dummy.txt', 'subdir/subdummy.txt'");
    assertEval("list.files('test/r/simple/data/tree1', pattern='*.tx') ", "'bar.txt', 'dummy.txt'");
  }

  @Test
  public void testLogical()  {
    assertEval("all(TRUE, FALSE, NA,  na.rm=FALSE) ", "FALSE");
    assertEval("all(TRUE, FALSE, NA,  na.rm=TRUE) ", "FALSE");
    assertEval("all(TRUE, TRUE, NA,  na.rm=TRUE) ", "TRUE");
    assertEval("all(TRUE, TRUE, NA,  na.rm=FALSE) ", "NA");
    assertEval("all() ", "TRUE");

    assertEval("any() ", "FALSE");
    assertEval("any(TRUE, TRUE, NA,  na.rm=TRUE) ", "TRUE");
    assertEval("any(TRUE, FALSE, NA,  na.rm=TRUE) ", "TRUE");
    assertEval("any(FALSE, NA,  na.rm=TRUE) ", "FALSE");
    assertEval("any(FALSE, NA,  na.rm=FALSE) ", "NA");
  }

  @Test
  public void testSource()  {
    assertEval("source('test/r/simple/data/tree2/setx.r') ; x ", "10.0");
    assertEval("source('test/r/simple/data/tree2/setx.r', local=TRUE) ; x ", "10.0");
    assertEval("x <- 1; f <- function() { source('test/r/simple/data/tree2/setx.r', local=TRUE) ; x } ; c(f(), x) ", "10.0, 1.0");
    assertEval("x <- 1; f <- function() { source('test/r/simple/data/tree2/setx.r', local=FALSE) ; x } ; c(f(), x) ", "10.0, 10.0");
    assertEval("x <- 1; f <- function() { source('test/r/simple/data/tree2/incx.r', local=FALSE) ; x } ; c(f(), x) ", "2.0, 2.0");
    assertEval("x <- 1; f <- function() { source('test/r/simple/data/tree2/incx.r', local=TRUE) ; x } ; c(f(), x) ", "2.0, 1.0");
  }

  @Test
  public void testCall()  {
    assertEval("f <- function(a, b) { a + b } ; l <- call('f', 2, 3) ; eval(l) ", "5.0");
    assertEval("f <- function(a, b) { a + b } ; x <- 1 ; y <- 2 ; l <- call('f', x, y) ; x <- 10 ; eval(l) ", "3.0");
  }

  @Test
  public void testTime()  {
    assertEval("s <- proc.time()[3] ; e <- proc.time()[3] ; e >= s ", "elapsed\n   TRUE");
  }

  @Test
  public void testScan()  {
    assertEval("scan(file='test/r/simple/data/coldata/test_comp.inp', what=1+2i) ", "Read 3 items.\n", "1.0+2.0i, 3.141+0.0i, 1.0+0.0i");
    assertEval("scan(file='test/r/simple/data/coldata/test_dbl.inp', what=1) ", "Read 4 items.\n", "10.1, 11.0, 1.0E100, 1.0E-8");
    assertEvalError("scan(file='test/r/simple/data/coldata/test_dbl_error.inp', what=1) ", "scan() expected 'a real', got 'dummy'");
    assertEvalError("scan(file='test/r/simple/data/coldata/test_comp_error.inp', what=1+0i) ", "scan() expected 'a complex', got '1+3idummy'");
    assertEval("scan(file='test/r/simple/data/coldata/test_int.inp', what=1L) ", "Read 6 items.\n", "1L, 12L, -13L, 0L, NA, NA");
    assertEvalError("scan(file='test/r/simple/data/coldata/test_int_error.inp', what=1L) ", "scan() expected 'an integer', got '-1xx3'");
    assertEval("scan(file='test/r/simple/data/coldata/test_dbl1.inp', what=1) ", "Read 6 items.\n", "NaN, -Infinity, Infinity, NA, 0.0, -0.0");
    assertEval("scan(file='test/r/simple/data/coldata/test_comp1.inp', what=1+2i) ", "Read 5 items.\n", "1.0+2.0i, NA, 3.141+0.0i, NaN+0.0i, 1.0+0.0i");
    assertEval("scan(file='test/r/simple/data/coldata/test_log.inp', what=TRUE) ", "Read 6 items.\n", "TRUE, FALSE, NA, FALSE, FALSE, TRUE");
    assertEvalError("scan(file='test/r/simple/data/coldata/test_log_error.inp', what=TRUE) ", "scan() expected 'a logical', got 'Naa'");
    assertEval("scan(file='test/r/simple/data/coldata/test_raw.inp', what=as.raw(10)) ", "Read 11 items.\n", "10, 11, fe, 0e, ff, ff, ab, ab, 00, 00, ee");
    assertEvalError("scan(file='test/r/simple/data/coldata/test_raw_error.inp', what=as.raw(10)) ", "scan() expected 'a raw', got 'X'");
    assertEvalError("scan(file='test/r/simple/data/coldata/test_raw.inp', what=function(){3}) ", "invalid 'what' argument");
  }
}
