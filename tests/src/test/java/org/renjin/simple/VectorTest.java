package org.renjin.simple;

import org.junit.Test;

public class VectorTest extends SimpleTestBase {

  @Test
  public void testScalarIndex()  {
    assertEval("{ x<-1:10; x[3] }", "3L");
    assertEval("{ x<-1:10; x[3L] }", "3L");
    assertEval("{ x<-c(1,2,3); x[3] }", "3.0");
    assertEval("{ x<-c(1,2,3); x[3L] }", "3.0");
    assertEval("{ x<-1:3; x[0-2] }", "1L, 3L");
    assertEval("{ x<-1:3; x[FALSE] }", "integer(0)");
    assertEval("{ x<-1:3; x[TRUE] }", "1L, 2L, 3L");
    assertEval("{ x<-c(TRUE,TRUE,FALSE); x[0-2] }", "TRUE, FALSE");
    assertEval("{ x<-c(1,2);x[[0-1]] }", "2.0");
    assertEval("{ x<-c(1,2);x[0-3] }", "1.0, 2.0");
    assertEval("{ x<-10; x[0-1] }", "numeric(0)");
    assertEval("{ x<-10; x[NA] }", "NA");

    assertEval("{ x <- c(a=1, b=2, c=3) ; x[2] }", "  b\n2.0");
    assertEval("{ x <- c(a=1, b=2, c=3) ; x[[2]] }", "2.0");
    assertEval("{ x <- c(a=\"A\", b=\"B\", c=\"C\") ; x[-2] }", "  a   c\n\"A\" \"C\"");
    assertEval("{ x <- c(a=1+2i, b=2+3i, c=3) ; x[-2] }", "       a        c\n1.0+2.0i 3.0+0.0i");
    assertEval("{ x <- c(a=1, b=2, c=3) ; x[-2] }", "  a   c\n1.0 3.0");
    assertEval("{ x <- c(a=1L, b=2L, c=3L) ; x[-2] }", " a  c\n1L 3L");
    assertEval("{ x <- c(a=TRUE, b=FALSE, c=NA) ; x[-2] }", "   a  c\nTRUE NA");
    assertEval("{ x <- c(a=as.raw(10), b=as.raw(11), c=as.raw(12)) ; x[-2] }", " a  c\n0a 0c");

    assertEval("{ x <- c(a=1L, b=2L, c=3L) ; x[0] }", "named integer(0)");
    assertEval("{ x <- c(a=1L, b=2L, c=3L) ; x[10] }", "<NA>\n  NA");
    assertEval("{ x <- c(a=TRUE, b=FALSE, c=NA) ; x[0] }", "named logical(0)");
    assertEval("{ x <- c(TRUE, FALSE, NA) ; x[0] }", "logical(0)");
    assertEval("{ x <- list(1L, 2L, 3L) ; x[10] }", "[[1]]\nNULL");
    assertEval("{ x <- list(a=1L, b=2L, c=3L) ; x[0] }", "named list()");
    assertEval("{ x <- c(a=\"A\", b=\"B\", c=\"C\") ; x[10] }", "<NA>\n  NA");
    assertEval("{ x <- c(a=\"A\", b=\"B\", c=\"C\") ; x[0] }", "named character(0)");
    assertEval("{ x <- c(a=1+1i, b=2+2i, c=3+3i) ; x[10] }", "<NA>\n  NA");
    assertEval("{ x <- c(a=1+1i, b=2+2i, c=3+3i) ; x[0] }", "named complex(0)");
    assertEval("{ x <- c(a=as.raw(10), b=as.raw(11), c=as.raw(12)) ; x[10] }", "<NA>\n  00");
    assertEval("{ x <- c(a=as.raw(10), b=as.raw(11), c=as.raw(12)) ; x[0] }", "named raw(0)");
    assertEval("{ x <- c(a=1, b=2, c=3) ; x[10] }", "<NA>\n  NA");
    assertEval("{ x <- c(a=1, b=2, c=3) ; x[0] }", "named numeric(0)");
    assertEval("{ x <- c(a=1,b=2,c=3,d=4) ; x[\"b\"] }", "  b\n2.0");
    assertEval("{ x <- c(a=1,b=2,c=3,d=4) ; x[\"d\"] }", "  d\n4.0");

    assertEval("{ x <- 1 ; attr(x, \"hi\") <- 2; x[2] <- 2; attr(x, \"hi\") }", "2.0");

    assertEvalError("{ x<-function() {1} ; y <- 2;  x[y] }", "object of type 'closure' is not subsettable");
    assertEvalError("{ x<-function() {1} ; y <- 2;  y[x] }", "invalid subscript type 'closure'");
    assertEval("{ x<-5:1 ; y <- -1L;  x[y] }", "4L, 3L, 2L, 1L");
    assertEval("{ x<-5:1 ; y <- 6L;  x[y] }", "NA");
    assertEval("{ x<-5:1 ; y <- 2L;  x[[y]] }", "4L");
    assertEval("{ x<-as.list(5:1) ; y <- 2L;  x[[y]] }", "4L");
    assertEvalError("{ x<-as.list(5:1) ; y <- 1:2;  x[[y]] }", "subscript out of bounds");
    assertEvalError("{ x<-function() {1} ; x[2L] }", "object of type 'closure' is not subsettable");
    assertEval("{ x <- c(1,4) ; y <- -1L ; x[y] }", "4.0");
    assertEval("{ x <- c(1,4) ; y <- 10L ; x[y] }", "NA");
    assertEval("{ x <- c(1,4) ; y <- -1 ; x[y] }", "4.0");
    assertEval("{ x <- c(1,4) ; y <- 10 ; x[y] }", "NA");
    assertEval("{ x <- c(a=1,b=2) ; y <- 2L ; x[y] }", "  b\n2.0");
    assertEval("{ x <- 1:4 ; y <- -1 ; x[y] }", "2L, 3L, 4L");
    assertEval("{ x <- 1:4 ; y <- 10 ; x[y] }", "NA");
    assertEval("{ x <- c(a=1,b=2) ; y <- 2 ; x[y] }", "  b\n2.0");
    assertEval("{ x <- list(1,2,3,4) ; y <- 3 ; x[y] }", "[[1]]\n3.0");
    assertEval("{ x <- list(1,2,3,4) ; y <- 3 ; x[[y]] }", "3.0");
    assertEvalError("{ x <- function(){3} ; y <- 3 ; x[[y]] }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(x,i) { x[[i]]} ; f(list(1,2,3,4), 3); f(f,2) }", "object of type 'closure' is not subsettable");
    assertEval("{ x <- list(1,4) ; y <- -1 ; x[y] }", "[[1]]\n4.0");
    assertEval("{ x <- list(1,4) ; y <- 4 ; x[y] }", "[[1]]\nNULL");
    assertEval("{ x <- list(a=1,b=4) ; y <- 2 ; x[y] }", "$b\n4.0");
    assertEval("{ f <- function(x,i) { x[i] } ; x <- c(a=1,b=2) ; f(x,\"a\") }", "  a\n1.0");
    assertEval("{ f <- function(x,i) { x[i] } ; x <- c(a=1,b=2) ; f(x,\"a\") ; f(x,2) }", "  b\n2.0");
    assertEvalError("{ f <- function(x,i) { x[i] } ; x <- c(a=1,b=2) ; f(x,\"a\") ; f(function(){3},\"b\") }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(1,2),FALSE) }", "attempt to select less than one element");
    assertEval("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(1,2),TRUE) }", "1.0");
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(1,2),1+0i) }", "invalid subscript type 'complex'");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(), NA) }", "[[1]]\nNULL");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(integer(), NA) }", "NA");
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:3,4) }", "subscript out of bounds");
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:3,NA) }", "subscript out of bounds");
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:3,-1) }", "attempt to select more than one element");
    assertEval("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,-1) }", "2L");
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(2,-2) }", "attempt to select less than one element");
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(2,-3) }", "attempt to select less than one element"); // like GNU-R, but is it a bug?
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:4,-3) }", "attempt to select more than one element");
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,-3) }", "attempt to select more than one element");
    assertEval("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,-2) }", "1L");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,NA) }", "NA, NA");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,-4) }", "1L, 2L");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=1L,b=2L),0) }", "named integer(0)");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,0) }", "integer(0)");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:2,-2) }", "1L");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),NA) }", "NA, NA");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),-4) }", "TRUE, FALSE");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),0) }", "logical(0)");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=TRUE,b=FALSE),0) }", "named logical(0)");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),-2) }", "TRUE");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(TRUE,FALSE),4) }", "NA");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=TRUE,b=FALSE),4) }", "<NA>\n  NA");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(1,2),-4) }", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(1,2),4) }", "[[1]]\nNULL");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(list(a=1,b=2),4) }", "$<NA>\nNULL");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(\"a\",\"b\"),4) }", "NA");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(\"a\",\"b\"),NA) }", "NA, NA");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(\"a\",\"b\"),-4) }", "\"a\", \"b\"");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(\"a\",\"b\"),0) }", "character(0)");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=\"a\",b=\"b\"),0) }", "named character(0)");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(1+2i,3+4i),NA) }", "NA, NA");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(1+2i,3+4i),-4) }", "1.0+2.0i, 3.0+4.0i");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(1+2i,3+4i),4) }", "NA");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(c(a=1+2i,b=3+4i),4) }", "<NA>\n  NA");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(as.raw(c(10,11)),-4) }", "0a, 0b");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(as.raw(c(10,11)),0) }", "raw(0)");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(as.raw(c(10,11)),4) }", "00");

    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(1+2i,3+4i) ; attr(z, \"my\") <- 1 ; f(z,-10) }", "1.0+2.0i, 3.0+4.0i");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(1,3) ; attr(z, \"my\") <- 1 ; f(z,-10) }", "1.0, 3.0");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(1L,3L) ; attr(z, \"my\") <- 1 ; f(z,-10) }", "1L, 3L");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(TRUE,FALSE) ; attr(z, \"my\") <- 1 ; f(z,-10) }", "TRUE, FALSE");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(a=\"a\",b=\"b\") ; attr(z, \"my\") <- 1 ; f(z,-10) }", "  a   b\n\"a\" \"b\"");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; z <- c(a=as.raw(10),b=as.raw(11)) ; attr(z, \"my\") <- 1 ; f(z,-10) }", " a  b\n0a 0b");

    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:3,c(TRUE,FALSE)) }", "attempt to select more than one element");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:3,c(TRUE,FALSE)) }", "1L, 3L");
    assertEval("{ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:3,c(1,2)) }", "1L, 2L");
    assertEvalError("{ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), \"a\") ; f(1:3,c(3,3)) }", "attempt to select more than one element");
    assertEvalError(" { x <- 1:3 ; x[[NULL]] }", "attempt to select less than one element");
    assertEval("{ x <- as.list(1:2) ; f <- function(i) { x[i] <- NULL ; x } ; f(1) ; f(NULL) }", "[[1]]\n1L\n\n[[2]]\n2L");
    assertEvalError("{ x <- as.list(1:2) ; f <- function(i) { x[[i]] <- NULL ; x } ; f(1) ; f(as.raw(10)) }", "invalid subscript type 'raw'");

    assertEvalError("{ x <- 1:3 ; x[2] <- integer() }", "replacement has length zero");
    assertEvalError("{ x <- 1:3 ; x[[TRUE]] <- 1:2 }", "more elements supplied than there are to replace");
    assertEval("{ x <- 1:3 ; x[TRUE] <- 10 ; x }", "10.0, 10.0, 10.0");
    assertEval("{ x <- 1:3 ; x[[TRUE]] <- 10 ; x }", "10.0, 2.0, 3.0");
    assertEvalError("{ x <- 1:3 ; x[[FALSE]] <- 10 ; x }", "attempt to select less than one element");
    assertEvalError("{ x <- 1:3 ; x[[NA]] <- 10 ; x }", "attempt to select more than one element");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:3, 1L, 10) ; f(c(1,2), \"hello\", TRUE) ; f(1:2, list(1), 3) }", "invalid subscript type 'list'");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:3, 1L, 10) ; f(c(1,2), \"hello\", TRUE) ; f(1:2, list(), 3) }", "attempt to select less than one element");
    assertEvalError("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(1:3, 1L, 10) ; f(c(1,2), \"hello\", TRUE) ; f(1:2, 1+2i, 3) }", "invalid subscript type 'complex'");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:3, 1L, 10) ; f(c(1,2), \"hello\", TRUE) ; f(1:2, 1, 3:4) }", "more elements supplied than there are to replace");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:3, 1L, 10) ; f(c(1,2), \"hello\", TRUE) ; f(1:2, as.integer(NA), 3:4) }", "more elements supplied than there are to replace");
    assertEvalError("{ x <- 1:2 ; x[as.integer(NA)] <- 3:4 }", "NAs are not allowed in subscripted assignments");

    assertEval("{ b <- c(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[1] <- 3+1i ; b }", "         [,1]\n[1,] 3.0+1.0i\n[2,] 3.0+4.0i");
    assertEval("{ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[\"hello\"] <- NULL ; b }", "[[1]]\n1.0+2.0i\n\n[[2]]\n3.0+4.0i");
  }


  @Test
  public void testVectorIndex()  {
    assertEval("{ x<-1:5 ; x[3:4] }", "3L, 4L");
    assertEval("{ x<-1:5 ; x[4:3] }", "4L, 3L");
    assertEval("{ x<-c(1,2,3,4,5) ; x[4:3] }", "4.0, 3.0");
    assertEval("{ (1:5)[3:4] }", "3L, 4L");
    assertEval("{ x<-(1:5)[2:4] ; x[2:1] }", "3L, 2L");
    assertEval("{ x<-1:5;x[c(0-2,0-3)] }", "1L, 4L, 5L");
    assertEval("{ x<-1:5;x[c(0-2,0-3,0,0,0)] }", "1L, 4L, 5L");
    assertEval("{ x<-1:5;x[c(2,5,4,3,3,3,0)] }", "2L, 5L, 4L, 3L, 3L, 3L");
    assertEval("{ x<-1:5;x[c(2L,5L,4L,3L,3L,3L,0L)] }", "2L, 5L, 4L, 3L, 3L, 3L");
    assertEval("{ f<-function(x, i) { x[i] } ; f(1:3,3:1) ; f(1:5,c(0,0,0,0-2)) }", "1L, 3L, 4L, 5L");
    assertEval("{ f<-function(x, i) { x[i] } ; f(1:3,0-3) ; f(1:5,c(0,0,0,0-2)) }", "1L, 3L, 4L, 5L");
    assertEval("{ f<-function(x, i) { x[i] } ; f(1:3,0L-3L) ; f(1:5,c(0,0,0,0-2)) }", "1L, 3L, 4L, 5L");
    assertEval("{ x<-1:5 ; x[c(TRUE,FALSE)] }", "1L, 3L, 5L");
    assertEval("{ x<-1:5 ; x[c(TRUE,TRUE,TRUE,NA)] }", "1L, 2L, 3L, NA, 5L");
    assertEval("{ x<-1:5 ; x[c(TRUE,TRUE,TRUE,FALSE,FALSE,FALSE,FALSE,TRUE,NA)] }", "1L, 2L, 3L, NA, NA");
    assertEval("{ f<-function(i) { x<-1:5 ; x[i] } ; f(1) ; f(1L) ; f(TRUE) }", "1L, 2L, 3L, 4L, 5L");
    assertEval("{ f<-function(i) { x<-1:5 ; x[i] } ; f(1) ; f(TRUE) ; f(1L)  }", "1L");
    assertEval("{ f<-function(i) { x<-1:5 ; x[i] } ; f(1) ; f(TRUE) ; f(c(3,2))  }", "3L, 2L");
    assertEval("{ f<-function(i) { x<-1:5 ; x[i] } ; f(1)  ; f(3:4) }", "3L, 4L");
    assertEval("{ f<-function(i) { x<-1:5 ; x[i] } ; f(c(TRUE,FALSE))  ; f(3:4) }", "3L, 4L");
    assertEval("{ x<-as.complex(c(1,2,3,4)) ; x[2:4] }", "2.0+0.0i, 3.0+0.0i, 4.0+0.0i");
    assertEval("{ x<-as.raw(c(1,2,3,4)) ; x[2:4] }", "02, 03, 04");

    assertEval("{ x<-c(1,2,3,4) ; names(x) <- c(\"a\",\"b\",\"c\",\"d\") ; x[c(10,2,3,0)] }", "<NA>   b   c\n  NA 2.0 3.0");
    assertEval("{ x<-c(1,2,3,4) ; names(x) <- c(\"a\",\"b\",\"c\",\"d\") ; x[c(10,2,3)] }", "<NA>   b   c\n  NA 2.0 3.0");
    assertEval("{ x<-c(1,2,3,4) ; names(x) <- c(\"a\",\"b\",\"c\",\"d\") ; x[c(-2,-4,0)] }", "  a   c\n1.0 3.0");
    assertEval("{ x<-c(1,2) ; names(x) <- c(\"a\",\"b\") ; x[c(FALSE,TRUE,NA,FALSE)] }", "  b <NA>\n2.0   NA");
    assertEval("{ x<-c(1,2) ; names(x) <- c(\"a\",\"b\") ; x[c(FALSE,TRUE)] }", "  b\n2.0");

    assertEval("{ x <- c(a=1,b=2,c=3,d=4) ; x[character()] }", "named numeric(0)");
    assertEval("{ x <- c(a=1,b=2,c=3,d=4) ; x[c(\"b\",\"b\",\"d\",\"a\",\"a\")] }", "  b   b   d   a   a\n2.0 2.0 4.0 1.0 1.0");
    assertEval("{ x <- c(a=as.raw(10),b=as.raw(11),c=as.raw(12),d=as.raw(13)) ; f <- function(s) { x[s] } ; f(TRUE) ; f(1L) ; f(as.character(NA)) }", "<NA>\n  00");
    assertEval("{ x <- c(a=1,b=2,c=3,d=4) ; f <- function(s) { x[s] } ; f(TRUE) ; f(1L) ; f(\"b\") }", "  b\n2.0");
    assertEval("{ x <- c(a=as.raw(10),b=as.raw(11),c=as.raw(12),d=as.raw(13)) ; f <- function(s) { x[c(s,s)] } ; f(TRUE) ; f(1L) ; f(as.character(NA)) }", "<NA> <NA>\n  00   00");
    assertEval("{ x <- c(a=1,b=2,c=3,d=4) ; f <- function(s) { x[c(s,s)] } ; f(TRUE) ; f(1L) ; f(\"b\") }", "  b   b\n2.0 2.0");

    assertEval("{ x <- 1;  y<-c(1,1) ; x[y] }", "1.0, 1.0");
    assertEval("{ x <- 1L;  y<-c(1,1) ; x[y] }", "1L, 1L");
    assertEval("{ x <- TRUE;  y<-c(1,1) ; x[y] }", "TRUE, TRUE");
    assertEval("{ x <- \"hi\";  y<-c(1,1) ; x[y] }", "\"hi\", \"hi\"");
    assertEval("{ x <- 1+2i;  y<-c(1,2) ; x[y] }", "1.0+2.0i, NA");

    // logical equality selection
    assertEval("{ f<-function(x,l) { x[l == 3] } ; f(c(1,2,3), c(1,2,3)) ; f(c(1,2,3), 1:3) ; f(1:3, c(3,3,2)) }", "1L, 2L");
    assertEval("{ f<-function(x,l) { x[l == 3] <- 4 } ; f(c(1,2,3), c(1,2,3)) ; f(c(1,2,3), 1:3) ; f(1:3, c(3,3,2)) }", "4.0");

    assertEvalError("{ x <- function(){3} ; x[3:2] }", "object of type 'closure' is not subsettable");
    assertEvalError("{ x <- c(1,2,3) ; x[-1:2] }", "only 0's may be mixed with negative subscripts");
    assertEval("{ x <- c(TRUE,FALSE,TRUE) ; x[2:3] }", "FALSE, TRUE");
    assertEval("{ x <- c(1+2i,3+4i,5+6i) ; x[2:3] }", "3.0+4.0i, 5.0+6.0i");
    assertEval("{ x <- c(1+2i,3+4i,5+6i) ; x[c(2,3,NA)] }", "3.0+4.0i, 5.0+6.0i, NA");
    assertEvalError("{ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,3,NA)] }", "only 0's may be mixed with negative subscripts");
    assertEvalError("{ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,NA)] }", "only 0's may be mixed with negative subscripts");
    assertEval("{ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,-4,-5)] }", "1.0+2.0i");
    assertEval("{ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,-4,-5,-5)] }", "1.0+2.0i");
    assertEval("{ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,-4,-5,-2)] }", "1.0+2.0i");
    assertEvalError("{ f <- function(b,i) { b[i] } ; x <- c(1+2i,3+4i,5+6i) ; f(x,c(1,2)) ; f(x,c(1+2i)) }", "invalid subscript type 'complex'");
    assertEval("{ x <- c(TRUE,FALSE,TRUE) ; x[integer()] }", "logical(0)");
    assertEvalError("{ f <- function(b) { b[integer()] } ; f(c(TRUE,FALSE,TRUE)) ; f(f) }", "object of type 'closure' is not subsettable");

    assertEval("{ x <- c(1,2,3,2) ; x[x==2] }", "2.0, 2.0");
    assertEval("{ x <- c(1,2,3,2) ; x[c(3,4,2)==2] }", "3.0");
    assertEval("{ x <- c(a=1,x=2,b=3,y=2) ; x[c(3,4,2)==2] }", "  b\n3.0");
    assertEval("{ x <- c(a=1,x=2,b=3,y=2) ; x[c(3,4,2,1)==2] }", "  b\n3.0");
    assertEval("{ x <- c(as.double(1:2000)) ; x[c(1,3,3,3,1:1996)==3] }", "2.0, 3.0, 4.0, 7.0");
    assertEval("{ x <- c(as.double(1:2000)) ; x[c(NA,3,3,NA,1:1996)==3] }", "NA, 2.0, 3.0, NA, 7.0");
    assertEval("{ x <- c(as.double(1:2000)) ; sum(x[rep(3, 2000)==3]) }", "2001000.0");
    assertEval("{ x <- c(1,2,3,2) ; x[c(3,4,2,NA)==2] }", "3.0, NA");

    assertEval("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,TRUE)) ; f(1:3,3:1) }", "3L, 2L, 1L");
    assertEval("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,TRUE)) ; f(c(a=1,b=2,c=3),3:1) }", "  c   b   a\n3.0 2.0 1.0");
    assertEvalError("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,TRUE)) ; f(function(){2},3:1) }", "object of type 'closure' is not subsettable");
    assertEval("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,NA)) }", "1L, NA");
    assertEval("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,NA,NA,NA)) }", "1L, NA, NA, NA");
    assertEval("{ f <- function(b,i) { b[i] } ; f(c(a=1,b=2,c=3), c(TRUE,NA,FALSE,FALSE,TRUE)) }", "  a <NA> <NA>\n1.0   NA   NA");
    assertEval(" { f <- function(b,i) { b[i] } ; f(c(a=1,b=2,c=3), c(TRUE,NA)) }", "  a <NA>   c\n1.0   NA 3.0");
    assertEvalError("{ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE)) ; f(f, c(TRUE,NA)) }", "object of type 'closure' is not subsettable");
    assertEval("{ f <- function(b,i) { b[i] } ; f(1:3, logical()) }", "integer(0)");
    assertEval("{ f <- function(b,i) { b[i] } ; f(c(a=1L,b=2L,c=3L), logical()) }", "named integer(0)");

    assertEval("{ f <- function(b,i) { b[i] } ; f(c(a=1,b=2,c=3), character()) }", "named numeric(0)");
    assertEval("{ f <- function(b,i) { b[i] } ; f(c(1,2,3), character()) }", "numeric(0)");
    assertEval("{ f <- function(b,i) { b[i] } ; f(c(1,2,3), c(\"hello\",\"hi\")) }", "NA, NA");
    assertEvalError("{ f <- function(b,i) { b[i] } ; f(1:3, c(\"h\",\"hi\")) ; f(function(){3},\"hi\") }", "object of type 'closure' is not subsettable");
    assertEval("{ f <- function(b,i) { b[i] } ; f(1:3, c(\"h\",\"hi\")) ; f(1:3,TRUE) }", "1L, 2L, 3L");

    assertEval("{ x <- list(1,2,list(3)) ; x[[c(3,1)]] }", "3.0");
    assertEvalError("{ x <- list(1,2,list(3)) ; x[[c(4,1)]] }", "no such index at level 1");
    assertEval("{ x <- list(1,2,list(3)) ; x[[c(3,NA)]] }", "NULL");
    assertEvalError("{ x <- list(1,2,list(3)) ; x[[c(NA,1)]] }", "no such index at level 1");
    assertEval("{ x <- list(1,list(3)) ; x[[c(-1,1)]] }", "3.0");
    assertEvalError("{ l <- list(1,list(2)) ; l[[integer()]] }", "attempt to select less than one element");
    assertEval("{ l <- list(1,list(2)) ; f <- function(i) { l[[i]] } ; f(c(2,1)) ; f(1) }", "1.0");
    assertEval("{ l <- list(1,function(){3}) ; f <- function(i) { l[[i]] } ; f(c(2)) }", "function () { 3.0 }");
    assertEvalError("{ l <- list(1,NULL) ; f <- function(i) { l[[i]] } ; f(c(2,1)) }", "subscript out of bounds");
    assertEvalError("{ f <- function(i) { l[[i]] } ; l <- list(1, f) ; f(c(2,1)) }", "invalid type/length (closure/1) in vector allocation");
    assertEvalError("{ f <- function(i) { l[[i]] } ; l <- list(1, 1:3) ; f(c(2,NA)) }", "subscript out of bounds");
    assertEval("{ f <- function(i) { l[[i]] } ; l <- list(1, as.list(1:3)) ; f(c(2,NA)) }", "NULL");
    assertEvalError("{ f <- function(i) { l[[i]] } ; l <- list(1, 1:3) ; f(c(2,-4)) }", "attempt to select more than one element");
    assertEvalError("{ f <- function(i) { l[[i]] } ; l <- list(1, 2) ; f(c(2,-1)) }", "attempt to select less than one element");
    assertEval("{ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,-1)) }", "3.0");
    assertEval("{ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,-2)) }", "2.0");
    assertEvalError("{ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,-4)) }", "attempt to select more than one element");
    assertEvalError("{ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,0)) }", "attempt to select less than one element");
    assertEvalError("{ x <- list(a=1,b=function(){3},d=list(x=3)) ; x[[c(2,10)]] }", "subscript out of bounds");
    assertEvalError("{ x <- list(a=1,b=function(){3},d=list(x=3)) ; x[[c(2,-3)]] }", "attempt to select less than one element");

    assertEval("{ x <- list(a=1,b=2,d=list(x=3)) ; x[[c(\"d\",\"x\")]] }", "3.0");
    assertEvalError("{ x <- list(a=1,b=2,d=list(x=3)) ; x[[c(\"z\",\"x\")]] }", "no such index at level 1");
    assertEvalError("{ x <- list(a=1,b=2,d=list(x=3)) ; x[[c(\"z\",NA)]] }", "no such index at level 1");
    assertEval("{ x <- list(a=1,b=2,d=list(x=3)) ; x[[c(\"d\",NA)]] }", "NULL");
    assertEvalError("{ x <- list(a=1,b=2,d=list(x=3)) ; x[[c(NA,\"x\")]] }", "no such index at level 1");
    assertEvalError("{ x <- list(a=1,b=2,d=list(x=3)) ; x[[character()]] }", "attempt to select less than one element");
    assertEval("{ x <- list(a=1,b=2,d=list(x=3)) ; f <- function(i) { x[[i]] } ; f(c(\"d\",\"x\")) ; f(\"b\") }", "2.0");
    assertEvalError("{ x <- list(a=1,b=function(){3},d=list(x=3)) ; f <- function(i) { x[[i]] } ; f(c(\"d\",\"x\")) ; f(c(\"b\",\"z\")) }", "subscript out of bounds");
    assertEvalError("{ x <- c(a=1,b=2) ; x[[c(\"a\",\"a\")]] }", "attempt to select more than one element");
    assertEvalError("{ x <- list(1,2) ; x[[c(\"a\",\"a\")]] }", "no such index at level 1");
    assertEvalError("{ x <- list(a=1,b=1:3) ; x[[c(\"b\",\"a\")]] }", "subscript out of bounds");
    assertEvalError("{ x <- list(a=1,b=1:3) ; x[[2+3i]] }", "invalid subscript type 'complex'");
    assertEvalError("{ x <- list(a=1,b=1:3) ; f <- function(i) { x[[i]] } ; f(c(2,2)) ; f(2+3i) }", "invalid subscript type 'complex'");
    assertEvalError("{ x <- list(a=1,b=1:3) ; f <- function(i) { x[[i]] } ; f(c(2,2)) ; x <- f ; f(2+3i) }", "object of type 'closure' is not subsettable");
    assertEvalError("{ x <- 1:3; x[list(2,3)] }", "invalid subscript type 'list'");
    assertEvalError("{ x <- 1:3; x[function(){3}] }", "invalid subscript type 'closure'");
    assertEvalError("{ x <- 1:2; x[[list()]] }", "attempt to select less than one element");
    assertEvalError("{ x <- 1:2; x[[list(-0,-1)]] }", "attempt to select more than one element");
    assertEvalError("{ x <- 1:2; x[[list(0)]] }", "invalid subscript type 'list'");
    assertEvalError("{ f <- function(b,i) { b[[i]] } ; f(list(1,list(2)),c(2,1)) ; f(1:3,list(1)) }", "invalid subscript type 'list'");

    assertEval("{ f <- function(b,i) { b[i] } ; f(1:3,c(2,1)) ; f(1:3,c(TRUE,FALSE)) }", "1L, 3L");
    assertEval("{ f <- function(b,i) { b[i] } ; f(1:3,c(2,1)) ; f(1:3,NULL) }", "integer(0)");
    assertEvalError("{ f <- function(b,i) { b[i] } ; f(1:3,c(2,1)) ; f(1:3,as.raw(c(10,11))) }", "invalid subscript type 'raw'");

    assertEvalError("{ l <- list(1,2) ; l[[c(1,1,2,3,4,3)]] }", "recursive indexing failed at level 2");
    assertEvalError("{ l <- list(list(1,2),2) ; l[[c(1,1,2,3,4,3)]] }", "recursive indexing failed at level 3");
  }

  @Test
  public void testScalarUpdate()  {
    assertEval("{ x<-1:3; x[1]<-100L; x }", "100L, 2L, 3L");
    assertEval("{ x<-c(1,2,3); x[2L]<-100L; x }", "1.0, 100.0, 3.0");
    assertEval("{ x<-c(1,2,3); x[2L]<-100; x }", "1.0, 100.0, 3.0");
    assertEval("{ x<-c(1,2,3); x[2]<-FALSE; x }", "1.0, 0.0, 3.0");
    assertEval("{ x<-1:5; x[2]<-1000; x[3] <- TRUE; x[8]<-3L; x }", "1.0, 1000.0, 1.0, 4.0, 5.0, NA, NA, 3.0");
    assertEval("{ x<-5:1; x[0-2]<-1000; x }", "1000.0, 4.0, 1000.0, 1000.0, 1000.0");
    assertEval("{ x<-c(); x[[TRUE]] <- 2; x }", "2.0");
    assertEval("{ x<-1:2; x[[0-2]]<-100; x }", "100.0, 2.0");
    assertEval("{ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,3L) ; f(c(1L,2L),2,3) }", "1.0, 3.0, 3.0, 4.0, 5.0");
    assertEval("{ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,3L) ; f(c(1L,2L),8,3L) }", "1L, 2L, 3L, 4L, 5L, NA, NA, 3L");
    assertEval("{ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,FALSE) ; f(c(1L,2L),2,3) }", "1.0, 3.0, 3.0, 4.0, 5.0");
    assertEval("{ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,FALSE) ; f(c(1L,2L),8,TRUE) }", "1L, 2L, 3L, 4L, 5L, NA, NA, 1L");

    assertEval("{ a <- c(1L,2L,3L); a <- 1:5; a[3] <- TRUE; a }", "1L, 2L, 1L, 4L, 5L");
    assertEval("{ x <- 1:3 ; x[2] <- \"hi\"; x }", "\"1L\", \"hi\", \"3L\"");
    assertEval("{ x <- c(1,2,3) ; x[2] <- \"hi\"; x }", "\"1.0\", \"hi\", \"3.0\"");
    assertEval("{ x <- c(TRUE,FALSE,FALSE) ; x[2] <- \"hi\"; x }", "\"TRUE\", \"hi\", \"FALSE\"");
    assertEval("{ x <- c(2,3,4) ; x[1] <- 3+4i ; x  }", "3.0+4.0i, 3.0+0.0i, 4.0+0.0i");

    assertEvalError("{ f <- function() { a[3] <- 4 } ; f() }", "object 'a' not found");
    assertEvalError("{ l <- quote(a[3] <- 4) ; f <- function() { eval(l) } ; f() }", "object 'a' not found");
    assertEvalError("{ l <- quote(a[3] <- 4) ; eval(l) ; f() }", "object 'a' not found");
    assertEval("{ b <- c(1,2) ; x <- b ; b[2L] <- 3 ; b }", "1.0, 3.0");
    assertEval("{ b <- c(1,2) ; b[0L] <- 3 ; b }", "1.0, 2.0");
    assertEval("{ b <- c(1,2) ; b[0] <- 1+2i ; b }", "1.0+0.0i, 2.0+0.0i");
    assertEvalError("{ x[3] <<- 10 }", "object 'x' not found");
    assertEval("{ b <- c(1,2) ; b[5L] <- 3 ; b }", "1.0, 2.0, NA, NA, 3.0");
    assertEvalWarning("{ b <- c(1,2) ; z <- c(10,11) ; attr(z,\"my\") <- 4 ; b[2] <- z ; b }", "1.0, 10.0", "number of items to replace is not a multiple of replacement length");
    assertEval("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),10L) ; f(1,3) }", "1.0, 3.0");
    assertEval("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),10L) ; f(1L,3) }", "1.0, 3.0");
    assertEval("{ b <- c(1L,2L) ; b[3] <- 13L ; b }", "1L, 2L, 13L");
    assertEval("{ b <- c(1L,2L) ; b[0] <- 13L ; b }", "1L, 2L");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; b <- c(10L,2L) ; b[0] <- TRUE ; b }", "10L, 2L");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; b <- c(10L,2L) ; b[3] <- TRUE ; b }", "10L, 2L, 1L");
    assertEval("{ b <- c(1L,2L) ; b[2] <- FALSE ; b }", "1L, 0L");
    assertEval("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),TRUE) ; f(1L,3) }", "1.0, 3.0");
    assertEval("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),TRUE) ; f(10,3) }", "10.0, 3.0");
    assertEval("{ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10) ; f(b,13L) }", "1.0, 13.0");
    assertEval(" { b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10) ; f(1:3,13L) }", "1L, 13L, 3L");
    assertEval("{ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10) ; f(c(1,2),10) }", "1.0, 10.0");
    assertEval("{ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10L) ; f(1:3,13L) }", "1L, 13L, 3L");
    assertEval("{ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10L) ; f(b,13) }", "1.0, 13.0");
    assertEval("{ b <- c(1,2) ; z <- b ; b[3L] <- 3L ; b }", "1.0, 2.0, 3.0");
    assertEval("{ b <- c(1,2) ; z <- b ; b[-2] <- 3L ; b }", "3.0, 2.0");
    assertEval("{ b <- c(1,2) ; z <- b ; b[3L] <- FALSE ; b }", "1.0, 2.0, 0.0");
    assertEval("{ b <- c(1,2) ; z <- b ; b[-10L] <- FALSE ; b }", "0.0, 0.0");
    assertEval("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1,2),FALSE) ; f(10L,3) }", "10.0, 3.0");
    assertEval("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(1,2),FALSE) ; f(10,3) }", "10.0, 3.0");
    assertEval("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(TRUE,NA),FALSE) ; f(c(FALSE,TRUE),3) }", "0.0, 3.0");
    assertEval("{ f <- function(b,v) { b[2] <- v ; b } ; f(c(TRUE,NA),FALSE) ; f(3,3) }", "3.0, 3.0");
    assertEval("{ b <- c(TRUE,NA) ; z <- b ; b[-10L] <- FALSE ; b }", "FALSE, FALSE");
    assertEval("{ b <- c(TRUE,NA) ; z <- b ; b[4L] <- FALSE ; b }", "TRUE, NA, NA, FALSE");
    assertEval("{ b <- list(TRUE,NA) ; z <- b ; b[[4L]] <- FALSE ; b }", "[[1]]\nTRUE\n\n[[2]]\nNA\n\n[[3]]\nNULL\n\n[[4]]\nFALSE");
    assertEval("{ b <- list(TRUE,NA) ; z <- b ; b[[-1L]] <- FALSE ; b }", "[[1]]\nTRUE\n\n[[2]]\nFALSE");
    assertEval("{ f <- function(b,v) { b[[2]] <- v ; b } ; f(list(TRUE,NA),FALSE) ; f(3,3) }", "3.0, 3.0");
    assertEval("{ f <- function(b,v) { b[[2]] <- v ; b } ; f(list(TRUE,NA),FALSE) ; f(list(3),NULL) }", "[[1]]\n3.0");
    assertEval("{ f <- function(b,v) { b[[2]] <- v ; b } ; f(list(TRUE,NA),FALSE) ; f(list(),NULL) }", "list()");
    assertEval("{ f <- function(b,v) { b[[2]] <- v ; b } ; f(c(\"a\",\"b\"),\"d\") ; f(1:3,\"x\") }", "\"1L\", \"x\", \"3L\"");
    assertEvalError("{ f <- function(b,v) { b[[2]] <- v ; b } ; f(c(\"a\",\"b\"),\"d\") ; f(c(\"a\",\"b\"),NULL) }", "more elements supplied than there are to replace");
    assertEval("{ b <- c(\"a\",\"b\") ; z <- b ; b[[-1L]] <- \"xx\" ; b }", "\"a\", \"xx\"");
    assertEval("{ b <- c(\"a\",\"b\") ; z <- b ; b[[3L]] <- \"xx\" ; b }", "\"a\", \"b\", \"xx\"");
    assertEval("{ b <- c(1,2) ; b[3] <- 2+3i ; b }", "1.0+0.0i, 2.0+0.0i, 2.0+3.0i");
    assertEval("{ b <- c(1+2i,3+4i) ; b[3] <- 2 ; b }", "1.0+2.0i, 3.0+4.0i, 2.0+0.0i");
    assertEval("{ b <- c(TRUE,NA) ; b[3] <- FALSE ; b }", "TRUE, NA, FALSE");
    assertEvalError("{ b <- as.raw(c(1,2)) ; b[3] <- 3 ; b }", "incompatible types (from double to raw) in subassignment type fix");
    assertEvalError("{ b <- c(1,2) ; b[3] <- as.raw(13) ; b }", "incompatible types (from raw to double) in subassignment type fix");
    assertEval("{ b <- as.raw(c(1,2)) ; b[3] <- as.raw(13) ; b }", "01, 02, 0d");
    assertEval("{ b <- as.raw(c(1,2)) ; b[as.double(NA)] <- as.raw(13) ; b }", "01, 02");
    assertEval("{ b <- as.raw(c(1,2)) ; b[[-2]] <- as.raw(13) ; b }", "0d, 02");
    assertEval("{ b <- as.raw(c(1,2)) ; b[[-1]] <- as.raw(13) ; b }", "01, 0d");
    assertEvalError("{ b <- as.raw(c(1,2)) ; b[[-3]] <- as.raw(13) ; b }", "attempt to select more than one element");
    assertEvalError("{ b <- as.raw(1) ; b[[-3]] <- as.raw(13) ; b }", "attempt to select less than one element");
    assertEvalError("{ b <- as.raw(c(1,2,3)) ; b[[-2]] <- as.raw(13) ; b }", "attempt to select more than one element");
    assertEvalError("{ f <- function(b,i) { b[i] <- 1 } ; f(1:3,2) ; f(f, 3) }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(b,i) { b[i] <- 1 } ; f(1:3,2) ; f(1:2, f) }", "invalid subscript type 'closure'");
    assertEvalError("{ f <- function(b,v) { b[2] <- v } ; f(1:3,2) ; f(1:2, f) }", "incompatible types (from closure to integer) in subassignment type fix");

    assertEval("{ x <- c(a=1+2i, b=3+4i) ; x[\"a\"] <- 10 ; x }", "        a        b\n10.0+0.0i 3.0+4.0i");
    assertEvalError(" { x <- c(a=1+2i, b=3+4i) ; x[\"a\"] <- as.raw(13) ; x }", "incompatible types (from raw to complex) in subassignment type fix");
    assertEval("{ x <- as.raw(c(10,11)) ; x[\"a\"] <- as.raw(13) ; x }", "       a\n0a 0b 0d");
    assertEvalError(" { x <- as.raw(c(10,11)) ; x[\"a\"] <- NA ; x }", "incompatible types (from logical to raw) in subassignment type fix");
    assertEval("{ x <- 1:2 ; x[\"a\"] <- 10+3i ; x }", "                          a\n1.0+0.0i 2.0+0.0i 10.0+3.0i");
    assertEval("{ x <- c(a=1+2i, b=3+4i) ; x[\"a\"] <- \"hi\" ; x }", "   a          b\n\"hi\" \"3.0+4.0i\"");
    assertEval("{ x <- 1:2 ; x[\"a\"] <- 10 ; x }", "           a\n1.0 2.0 10.0");
    assertEval("{ x <- c(a=1,a=2) ; x[\"a\"] <- 10L ; x }", "   a   a\n10.0 2.0");
    assertEval("{ x <- 1:2 ; x[\"a\"] <- FALSE ; x }", "       a\n1L 2L 0L");
    assertEval("{ x <- c(aa=TRUE,b=FALSE) ; x[\"a\"] <- 2L ; x }", "aa  b  a\n1L 0L 2L");
    assertEval("{ x <- c(aa=TRUE) ; x[[\"a\"]] <- list(2L) ; x }", "$aa\nTRUE\n\n$a\n$a[[1]]\n2L");
    assertEval("{ x <- c(aa=TRUE) ; x[\"a\"] <- list(2L) ; x }", "$aa\nTRUE\n\n$a\n2L");
    assertEval("{ x <- c(b=2,a=3) ; z <- x ; x[\"a\"] <- 1 ; x }", "  b   a\n2.0 1.0");

    assertEval("{ x <- list(1,2) ; dim(x) <- c(2,1) ; x[[3]] <- NULL ; x }", "     [,1]\n[1,]  1.0\n[2,]  2.0");
    assertEval("{ x <- list(1,2) ; dim(x) <- c(2,1) ; x[3] <- NULL ; x }", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEval("{ x <- list(1,2) ; dim(x) <- c(2,1) ; x[2] <- NULL ; x }", "[[1]]\n1.0");
    assertEval("{ x <- list(1,2) ; dim(x) <- c(2,1) ; x[[2]] <- NULL ; x }", "[[1]]\n1.0");
    assertEvalError("{ x <- list(1,2) ; x[[0]] <- NULL ; x }", "attempt to select less than one element");
    assertEvalError("{ x <- list(1,2) ; x[[NA]] <- NULL ; x }", "attempt to select more than one element");
    assertEval("{ x <- list(1,2) ; x[0] <- NULL ; x }", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEval("{ x <- list(1,2) ; x[NA] <- NULL ; x }", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEval("{ x <- list(1,2) ; x[as.integer(NA)] <- NULL ; x }", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEval("{ x <- list(1,2) ; x[-1] <- NULL ; x }", "[[1]]\n1.0");
    assertEvalError("{ x <- list(1,2,3) ; x[[-1]] <- NULL ; x }", "attempt to select more than one element");
    assertEvalError("{ x <- list(1,2,3) ; x[[-5]] <- NULL ; x }", "attempt to select more than one element");
    assertEvalError("{ x <- list(1) ; x[[-2]] <- NULL ; x }", "attempt to select less than one element");
    assertEvalError("{ x <- list(1) ; x[[-1]] <- NULL ; x }", "attempt to select less than one element");
    assertEval("{ x <- list(3,4) ; x[[-1]] <- NULL ; x }", "[[1]]\n3.0");
    assertEval("{ x <- list(3,4) ; x[[-2]] <- NULL ; x }", "[[1]]\n4.0");
    assertEvalError("{ x <- list(3,4) ; x[[-10]] <- NULL ; x }", "attempt to select more than one element");
    assertEval("{ x <- list(a=3,b=4) ; x[[\"a\"]] <- NULL ; x }", "$b\n4.0");
    assertEval("{ x <- list(a=3,b=4) ; x[\"z\"] <- NULL ; x }", "$a\n3.0\n\n$b\n4.0");
    assertEvalError("{ x <- 4:10 ; x[[\"z\"]] <- NULL ; x }", "more elements supplied than there are to replace");
    assertEval("{ x <- as.list(1:2) ; x[[\"z\"]] <- NULL ; x }", "[[1]]\n1L\n\n[[2]]\n2L");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,-2,10) }", "10.0, 2.0");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,2,10) ; f(1:2,as.integer(NA), 10) }", "1.0, 2.0");
    assertEvalError("{ x <- 1:2; x[[as.integer(NA)]] <- 10 ; x }", "attempt to select more than one element");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; v } ; f(1:2,\"hi\",3L) ; f(1:2,c(2),10) ; f(1:2,as.integer(NA), 10) }", "attempt to select more than one element");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,c(2),10) ; f(1:2,2, 10) }", "1.0, 10.0");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,c(2),10) ; f(1:2,0, 10) }", "attempt to select less than one element");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,2,10) ; f(1:2,1:3, 10) }", "attempt to select more than one element");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,2,10) ; f(as.list(1:2),1:3, 10) }", "recursive indexing failed at level 2");

    assertEval("{ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[3] <- NULL ; b }", "[[1]]\n1.0+2.0i\n\n[[2]]\n3.0+4.0i");

    assertEval("{ l <- list(1,2) ; l[[2]] <- as.raw(13) ; l }", "[[1]]\n1.0\n\n[[2]]\n0d");
  }


  @Test
  public void testVectorUpdate()  {
    assertEval("{ a <- c(1,2,3) ; b <- a; a[1] <- 4L; a }", "4.0, 2.0, 3.0");
    assertEval("{ a <- c(1,2,3) ; b <- a; a[2] <- 4L; a }", "1.0, 4.0, 3.0");
    assertEval("{ a <- c(1,2,3) ; b <- a; a[3] <- 4L; a }", "1.0, 2.0, 4.0");
    // logical value inserted to double vector
    assertEval("{ a <- c(2.1,2.2,2.3); b <- a; a[[1]] <- TRUE; a }", "1.0, 2.2, 2.3");
    assertEval("{ a <- c(2.1,2.2,2.3); b <- a; a[[2]] <- TRUE; a }", "2.1, 1.0, 2.3");
    assertEval("{ a <- c(2.1,2.2,2.3); b <- a; a[[3]] <- TRUE; a }", "2.1, 2.2, 1.0");
    // logical value inserted into logical vector
    assertEval("{ a <- c(TRUE,TRUE,TRUE); b <- a; a[[1]] <- FALSE; a }", "FALSE, TRUE, TRUE");
    assertEval("{ a <- c(TRUE,TRUE,TRUE); b <- a; a[[2]] <- FALSE; a }", "TRUE, FALSE, TRUE");
    assertEval("{ a <- c(TRUE,TRUE,TRUE); b <- a; a[[3]] <- FALSE; a }", "TRUE, TRUE, FALSE");
    assertEval("{ x<-c(1,2,3,4,5); x[3:4]<-c(300L,400L); x }", "1.0, 2.0, 300.0, 400.0, 5.0");
    assertEval("{ x<-c(1,2,3,4,5); x[4:3]<-c(300L,400L); x }", "1.0, 2.0, 400.0, 300.0, 5.0");
    assertEval("{ x<-1:5; x[4:3]<-c(300L,400L); x }", "1L, 2L, 400L, 300L, 5L");
    assertEval("{ x<-5:1; x[3:4]<-c(300L,400L); x }", "5L, 4L, 300L, 400L, 1L");
    assertEval("{ x<-5:1; x[3:4]<-c(300,400); x }", "5.0, 4.0, 300.0, 400.0, 1.0");
    assertEval("{ x<-1:5; x[c(0-2,0-3,0-3,0-100,0)]<-256; x }", "256.0, 2.0, 3.0, 256.0, 256.0");
    assertEval("{ x<-1:5; x[c(4,2,3)]<-c(256L,257L,258L); x }", "1L, 257L, 258L, 256L, 5L");
    assertEval("{ x<-c(1,2,3,4,5); x[c(TRUE,FALSE)] <- 1000; x }", "1000.0, 2.0, 1000.0, 4.0, 1000.0");
    assertEval("{ x<-c(1,2,3,4,5,6); x[c(TRUE,TRUE,FALSE)] <- c(1000L,2000L) ; x }", "1000.0, 2000.0, 3.0, 1000.0, 2000.0, 6.0");
    assertEval("{ x<-c(1,2,3,4,5); x[c(TRUE,FALSE,TRUE,TRUE,FALSE)] <- c(1000,2000,3000); x }", "1000.0, 2.0, 2000.0, 3000.0, 5.0");
    assertEval("{ x<-c(1,2,3,4,5); x[c(TRUE,FALSE,TRUE,TRUE,0)] <- c(1000,2000,3000); x }", "3000.0, 2.0, 3.0, 4.0, 5.0");
    assertEval("{ x<-1:3; x[c(TRUE, FALSE, TRUE)] <- c(TRUE,FALSE); x }", "1L, 2L, 0L");
    assertEval("{ x<-c(TRUE,TRUE,FALSE); x[c(TRUE, FALSE, TRUE)] <- c(FALSE,TRUE); x }", "FALSE, TRUE, TRUE");
    assertEval("{ x<-c(TRUE,TRUE,FALSE); x[c(TRUE, FALSE, TRUE)] <- c(1000,2000); x }", "1000.0, 1.0, 2000.0");
    assertEval("{ x<-11:9 ; x[c(TRUE, FALSE, TRUE)] <- c(1000,2000); x }", "1000.0, 10.0, 2000.0");
    assertEval("{ l <- double() ; l[c(TRUE,TRUE)] <-2 ; l}", "2.0, 2.0");
    assertEval("{ l <- double() ; l[c(FALSE,TRUE)] <-2 ; l}", "NA, 2.0");

    assertEval("{ a<- c('a','b','c','d'); a[3:4] <- c(4,5); a}", "\"a\", \"b\", \"4.0\", \"5.0\"");
    assertEval("{ a<- c('a','b','c','d'); a[3:4] <- c(4L,5L); a}", "\"a\", \"b\", \"4L\", \"5L\"");
    assertEval("{ a<- c('a','b','c','d'); a[3:4] <- c(TRUE,FALSE); a}", "\"a\", \"b\", \"TRUE\", \"FALSE\"");


    assertEval("{ f<-function(i,v) { x<-1:5 ; x[i]<-v ; x } ; f(1,1) ; f(1L,TRUE) ; f(2,TRUE) }", "1L, 1L, 3L, 4L, 5L");
    assertEval("{ f<-function(i,v) { x<-1:5 ; x[[i]]<-v ; x } ; f(1,1) ; f(1L,TRUE) ; f(2,TRUE) }", "1L, 1L, 3L, 4L, 5L");
    assertEval("{ f<-function(i,v) { x<-1:5 ; x[i]<-v ; x } ; f(3:2,1) ; f(1L,TRUE) ; f(2:4,4:2) }", "1L, 4L, 3L, 2L, 5L");
    assertEval("{ f<-function(i,v) { x<-1:5 ; x[i]<-v ; x } ; f(c(3,2),1) ; f(1L,TRUE) ; f(2:4,c(4,3,2)) }", "1.0, 4.0, 3.0, 2.0, 5.0");
    assertEval("{ f<-function(b,i,v) { b[i]<-v ; b } ; f(1:4,4:1,TRUE) ; f(c(3,2,1),8,10) }", "3.0, 2.0, 1.0, NA, NA, NA, NA, 10.0");
    assertEval("{ f<-function(b,i,v) { b[i]<-v ; b } ; f(1:4,4:1,TRUE) ; f(c(3,2,1),8,10) ; f(c(TRUE,FALSE),TRUE,FALSE) }", "FALSE, FALSE");
    assertEval("{ x<-c(TRUE,TRUE,FALSE,TRUE) ; x[3:2] <- TRUE; x }", "TRUE, TRUE, TRUE, TRUE");

    assertEval("{ x<-1:3 ; y<-(x[2]<-100) ; y }", "100.0");
    assertEval("{ x<-1:5 ; x[x[4]<-2] <- (x[4]<-100) ; x }", "1.0, 100.0, 3.0, 2.0, 5.0");
    assertEval("{ x<-1:5 ; x[3] <- (x[4]<-100) ; x }", "1.0, 2.0, 100.0, 100.0, 5.0");
    assertEval("{ x<-5:1 ; x[x[2]<-2] }", "4L");
    assertEval("{ x<-5:1 ; x[x[2]<-2] <- (x[3]<-50) ; x }", "5.0, 50.0, 50.0, 2.0, 1.0");

    assertEval("{ v<-1:3 ; v[TRUE] <- 100 ; v }", "100.0, 100.0, 100.0");
    assertEval("{ v<-1:3 ; v[-1] <- c(100,101) ; v }", "1.0, 100.0, 101.0");
    assertEval("{ v<-1:3 ; v[TRUE] <- c(100,101,102) ; v }", "100.0, 101.0, 102.0");

    assertEval("{ x <- c(a=1,b=2,c=3) ; x[2]<-10; x }", "  a    b   c\n1.0 10.0 3.0");
    assertEval("{ x <- c(a=1,b=2,c=3) ; x[2:3]<-10; x }", "  a    b    c\n1.0 10.0 10.0");
    assertEval("{ x <- c(a=1,b=2,c=3) ; x[c(2,3)]<-10; x }", "  a    b    c\n1.0 10.0 10.0");
    assertEval("{ x <- c(a=1,b=2,c=3) ; x[c(TRUE,TRUE,FALSE)]<-10; x }", "   a    b   c\n10.0 10.0 3.0");
    assertEval("{ x <- c(a=1,b=2) ; x[2:3]<-10; x }", "  a    b     \n1.0 10.0 10.0");
    assertEval("{ x <- c(a=1,b=2) ; x[c(2,3)]<-10; x }", "  a    b     \n1.0 10.0 10.0");
    assertEval("{ x <- c(a=1,b=2) ; x[3]<-10; x }", "  a   b     \n1.0 2.0 10.0");
    assertEval("{ x <- matrix(1:2) ; x[c(FALSE,FALSE,TRUE)]<-10; x }", "1.0, 2.0, 10.0");
    assertEval("{ x <- 1:2 ; x[c(FALSE,FALSE,TRUE)]<-10; x }", "1.0, 2.0, 10.0");
    assertEval("{ x <- c(a=1,b=2) ; x[c(FALSE,FALSE,TRUE)]<-10; x }", "  a   b     \n1.0 2.0 10.0");

    assertEval("{ x<-c(a=1,b=2,c=3) ; x[[\"b\"]]<-200; x }", "  a     b   c\n1.0 200.0 3.0");
    assertEval("{ x<-c(a=1,b=2,c=3) ; x[[\"d\"]]<-200; x }", "  a   b   c     d\n1.0 2.0 3.0 200.0");
    assertEval("{ x<-c() ; x[c(\"a\",\"b\",\"c\",\"d\")]<-c(1,2); x }", "  a   b   c   d\n1.0 2.0 1.0 2.0");
    assertEval("{ x<-c(a=1,b=2,c=3) ; x[\"d\"]<-4 ; x }", "  a   b   c   d\n1.0 2.0 3.0 4.0");
    assertEval("{ x<-c(a=1,b=2,c=3) ; x[c(\"d\",\"e\")]<-c(4,5) ; x }", "  a   b   c   d   e\n1.0 2.0 3.0 4.0 5.0");
    assertEval("{ x<-c(a=1,b=2,c=3) ; x[c(\"d\",\"a\",\"d\",\"a\")]<-c(4,5) ; x }", "  a   b   c   d\n5.0 2.0 3.0 4.0");

    assertEval("{ a = c(1, 2); a[['a']] = 67; a; }", "           a\n1.0 2.0 67.0");
    assertEval("{ a = c(a=1,2,3); a[['x']] = 67; a; }", "  a            x\n1.0 2.0 3.0 67.0");

    assertEval("{ x <- c(TRUE,TRUE,TRUE,TRUE); x[2:3] <- c(FALSE,FALSE); x }", "TRUE, FALSE, FALSE, TRUE");
    assertEval("{ x <- c(TRUE,TRUE,TRUE,TRUE); x[3:2] <- c(FALSE,TRUE); x }", "TRUE, TRUE, FALSE, TRUE");

    assertEval("{ x <- c('a','b','c','d'); x[2:3] <- 'x'; x}", "\"a\", \"x\", \"x\", \"d\"");
    assertEval("{ x <- c('a','b','c','d'); x[2:3] <- c('x','y'); x}", "\"a\", \"x\", \"y\", \"d\"");
    assertEval("{ x <- c('a','b','c','d'); x[3:2] <- c('x','y'); x}", "\"a\", \"y\", \"x\", \"d\"");

    assertEval("{ x <- c('a','b','c','d'); x[c(TRUE,FALSE,TRUE)] <- c('x','y','z'); x }", "\"x\", \"b\", \"y\", \"z\"");

    assertEval("{ x <- c(TRUE,TRUE,TRUE,TRUE); x[c(TRUE,TRUE,FALSE)] <- c(10L,20L,30L); x }", "10L, 20L, 1L, 30L");
    assertEval("{ x <- c(1L,1L,1L,1L); x[c(TRUE,TRUE,FALSE)] <- c('a','b','c'); x}", "\"a\", \"b\", \"1L\", \"c\"");
    assertEval("{ x <- c(TRUE,TRUE,TRUE,TRUE); x[c(TRUE,TRUE,FALSE)] <- list(10L,20L,30L); x }", "[[1]]\n10L\n\n[[2]]\n20L\n\n[[3]]\nTRUE\n\n[[4]]\n30L");

    assertEval("{ x <- c(); x[c('a','b')] <- c(1L,2L); x }", " a  b\n1L 2L");
    assertEval("{ x <- c(); x[c('a','b')] <- c(TRUE,FALSE); x }", "   a     b\nTRUE FALSE");
    assertEval("{ x <- c(); x[c('a','b')] <- c('a','b'); x }", "  a   b\n\"a\" \"b\"");
    assertEval("{ x <- list(); x[c('a','b')] <- c('a','b'); x }", "$a\n\"a\"\n\n$b\n\"b\"");
    assertEval("{ x <- list(); x[c('a','b')] <- list('a','b'); x }", "$a\n\"a\"\n\n$b\n\"b\"");

    // negative tests
    assertEvalWarning("{ x = c(1,2,3,4); x[x %% 2 == 0] <- c(1,2,3,4); }", "1.0, 2.0, 3.0, 4.0", "number of items to replace is not a multiple of replacement length");
    assertEvalError("{ x <- 1:3 ; x[c(-2, 1)] <- 10 }", "only 0's may be mixed with negative subscripts");

    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, 10) ; f(1:2, 1:2, 11) }", "11.0, 11.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, TRUE) }", "[[1]]\nTRUE\n\n[[2]]\nTRUE");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, 11L) }", "[[1]]\n11L\n\n[[2]]\n11L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, TRUE) ;  f(list(1,2), 1:2, as.raw(10))}", "[[1]]\n0a\n\n[[2]]\n0a");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(list(1,2), 1:2, c(1+2i,3+4i))}", "[[1]]\n1.0+2.0i\n\n[[2]]\n3.0+4.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(1:2, 1:2, c(10,5))}", "10.0, 5.0");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(1:2, c(0,0), as.raw(c(11,23)))}", "incompatible types (from raw to integer) in subassignment type fix");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, TRUE) ;  f(list(1,2), -1:1, c(2,10,5)) }", "only 0's may be mixed with negative subscripts");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(list(1,2), 1:3, c(2,10,5)) }", "[[1]]\n2.0\n\n[[2]]\n10.0\n\n[[3]]\n5.0");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(list(1,2), -10:10, 1:3) }", "only 0's may be mixed with negative subscripts");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2,3,4,5), 4:3, c(TRUE,NA)) }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\nNA\n\n[[4]]\nTRUE\n\n[[5]]\n5.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2,3,4), seq(1L,4L,2L), c(TRUE,NA)) }", "[[1]]\nTRUE\n\n[[2]]\n2.0\n\n[[3]]\nNA\n\n[[4]]\n4.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2),1:2,3:4) }", "[[1]]\n3L\n\n[[2]]\n4L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2),1:2,c(4,3)) }", "[[1]]\n4.0\n\n[[2]]\n3.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2),1:2,c(1+2i,3+2i)) }", "[[1]]\n1.0+2.0i\n\n[[2]]\n3.0+2.0i");

    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,1+2i) }", "1.0+2.0i, 1.0+2.0i, 10.0+0.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(3,NA)) }", "3.0, NA, 10.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(3L,NA)) }", "3.0, NA, 10.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(TRUE,FALSE)) }", "1.0, 0.0, 10.0");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(TRUE,FALSE)) ; f(c(10,4), 2:1, as.raw(10)) }", "incompatible types (from raw to double) in subassignment type fix");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(TRUE,FALSE)) ; f(c(10L,4L), 2:1, 1+2i) }", "1.0+2.0i, 1.0+2.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),-1:0,c(TRUE,FALSE)) }", "1.0, 1.0, 0.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10), seq(2L,4L,2L) ,c(TRUE,FALSE)) }", "1.0, 1.0, 10.0, 0.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.double(1:5), seq(1L,6L,2L) ,c(TRUE,FALSE,NA)) }", "1.0, 2.0, 0.0, 4.0, NA");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.double(1:5), seq(7L,1L,-3L) ,c(TRUE,FALSE,NA)) }", "NA, 2.0, 3.0, 0.0, 5.0, NA, 1.0");

    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),2:1,1+2i) }", "1.0+2.0i, 1.0+2.0i, 10.0+0.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),2:1,c(3,NA)) }", "NA, 3.0, 10.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),2:1,c(3L,NA)) }", "NA, 3L, 10L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),1:2,c(TRUE,FALSE)) }", "1L, 0L, 10L");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),1:2,c(TRUE,FALSE)) ; f(c(10L,4L), 2:1, as.raw(10)) }", "incompatible types (from raw to integer) in subassignment type fix");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),1:2,c(TRUE,FALSE)) ; f(c(10,4), 2:1, 1+2i) }", "1.0+2.0i, 1.0+2.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:5, seq(1L,6L,2L) ,c(TRUE,FALSE,NA)) }", "1L, 2L, 0L, 4L, NA");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2, seq(1L,6L,2L) ,c(TRUE,FALSE,NA)) }", "1L, 2L, 0L, NA, NA");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2, seq(1L,-8L,-2L) ,c(TRUE,FALSE,NA)) }", "only 0's may be mixed with negative subscripts");

    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,FALSE,NA),2:1,1+2i) }", "1.0+2.0i, 1.0+2.0i, NA");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,NA,FALSE),2:1,c(TRUE,NA)) }", "NA, TRUE, FALSE");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,NA,FALSE),2:0,c(TRUE,NA)) }", "NA, TRUE, FALSE");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,NA,FALSE),3:4,c(TRUE,NA)) }", "TRUE, NA, TRUE, NA");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.logical(-3:3),seq(1L,7L,3L),c(TRUE,NA,FALSE)) }", "TRUE, TRUE, TRUE, NA, TRUE, TRUE, FALSE");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,FALSE),2:1,c(NA,NA)) ; f(c(TRUE,FALSE),1:2,3:4) }", "3L, 4L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,FALSE),2:1,c(NA,NA)) ; f(10:11,1:2,c(NA,FALSE)) }", "NA, 0L");

    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"a\",\"b\"),2:1,1+2i) }", "\"1.0+2.0i\", \"1.0+2.0i\"");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.character(-3:3),seq(1L,7L,3L),c(\"A\",\"a\",\"XX\")) }", "\"A\", \"-2L\", \"-1L\", \"a\", \"1L\", \"2L\", \"XX\"");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"hello\",\"hi\",\"X\"), -1:-2, \"ZZ\") }", "\"hello\", \"hi\", \"ZZ\"");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"hello\",\"hi\",\"X\"), 3:4, \"ZZ\") }", "\"hello\", \"hi\", \"ZZ\", \"ZZ\"");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"hello\",\"hi\",\"X\"), 1:2, c(\"ZZ\",\"xx\")) ; f(1:4,1:2,NA) }", "NA, NA, 3L, 4L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(\"hello\",\"hi\",\"X\"), 1:2, c(\"ZZ\",\"xx\")) ; f(as.character(1:2),1:2,NA) }", "NA, NA");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1+2i,2+3i), 1:2, c(10+1i,2+4i)) }", "10.0+1.0i, 2.0+4.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.raw(1:3), 1:2, as.raw(40:41)) }", "28, 29, 03");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1+2i,2+3i), 1:2, as.raw(10:11)) }", "incompatible types (from raw to complex) in subassignment type fix");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.raw(10:11), 1:2, c(10+1i, 11)) }", "incompatible types (from complex to raw) in subassignment type fix");

    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(1:2, c(0,0), c(1+2i,3+4i))}", "1.0+0.0i, 2.0+0.0i");
    assertEvalError(" { f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1:2, f) }", "incompatible types (from closure to integer) in subassignment type fix");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1:2, 3:4); f(c(TRUE,FALSE), 2:1, 1:2) }", "2L, 1L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1:2, 3:4); f(3:4, 2:1, c(NA,FALSE)) }", "0L, NA");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(f, 1:2, 1:3) }", "object of type 'closure' is not subsettable");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(TRUE,FALSE,NA), 1:2, c(FALSE,TRUE)) }", "FALSE, TRUE, NA");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4), 1:2, c(NA,NA)) }", "NA, NA");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4), 1:2, c(\"hello\",\"hi\")) }", "\"hello\", \"hi\"");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4,8), 1:2, list(3,TRUE)) }", "[[1]]\n3.0\n\n[[2]]\nTRUE\n\n[[3]]\n8.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); l <- list(3,5L) ; dim(l) <- c(2,1) ; f(5:6,1:2,c(3,4)) ; f(l, 1:2, list(3,TRUE)) }", "     [,1]\n[1,]  3.0\n[2,] TRUE");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); l <- list(3,5L) ; dim(l) <- c(2,1) ; f(5:6,1:2,c(3,4)) ; f(list(3,TRUE), 1:2, l) }", "[[1]]\n3.0\n\n[[2]]\n5L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); l <- c(3,5L) ; dim(l) <- c(2,1) ; f(5:6,1:2,c(3,4)) ; f(l, 1:2, c(3,TRUE)) }", "     [,1]\n[1,]  3.0\n[2,]  1.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); l <- list(3,5L) ; dim(l) <- c(2,1) ; f(5:6,1:2,c(3,4)) ; m <- c(3,TRUE) ; dim(m) <- c(1,2) ; f(m, 1:2, l) }", "[[1]]\n3.0\n\n[[2]]\n5L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4,8), -1:-2, 10) }", "3.0, 4.0, 10.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4,8), 3:4, 10) }", "3.0, 4.0, 10.0, 10.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(1:8, seq(1L,7L,3L), c(10,100,1000)) }", "10.0, 2.0, 3.0, 100.0, 5.0, 6.0, 1000.0, 8.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; z <- f(1:8, seq(1L,7L,3L), list(10,100,1000)) ; sum(as.double(z)) }", "1134.0");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; x <- list(1,2) ; attr(x,\"my\") <- 10 ; f(x, 1:2, c(10,11)) }", "[[1]]\n10.0\n\n[[2]]\n11.0\nattr(,\"my\")\n10.0");

    assertEval("{ b <- 1:3 ; b[c(3,2)] <- list(TRUE,10) ; b }", "[[1]]\n1L\n\n[[2]]\n10.0\n\n[[3]]\nTRUE");
    assertEval("{ b <- as.raw(11:13) ; b[c(3,2)] <- list(2) ; b }", "[[1]]\n0b\n\n[[2]]\n2.0\n\n[[3]]\n2.0");
    assertEval("{ b <- as.raw(11:13) ; b[c(3,2)] <- as.raw(2) ; b }", "0b, 02, 02");
    assertEvalError("{ b <- as.raw(11:13) ; b[c(3,2)] <- 2 ; b }", "incompatible types (from double to raw) in subassignment type fix");
    assertEval("{ b <- c(TRUE,NA,FALSE) ; b[c(3,2)] <- FALSE ; b }", "TRUE, FALSE, FALSE");
    assertEval("{ b <- 1:4 ; b[c(3,2)] <- c(NA,NA) ; b }", "1L, NA, NA, 4L");
    assertEval("{ b <- c(TRUE,FALSE) ; b[c(3,2)] <- 5:6 ; b }", "1L, 6L, 5L");
    assertEval("{ b <- c(1+2i,3+4i) ; b[c(3,2)] <- 5:6 ; b }", "1.0+2.0i, 6.0+0.0i, 5.0+0.0i");
    assertEval("{ b <- 3:4 ; b[c(3,2)] <- c(1+2i,3+4i) ; b }", "3.0+0.0i, 3.0+4.0i, 1.0+2.0i");
    assertEval("{ b <- c(\"hello\",\"hi\") ; b[c(3,2)] <- c(2,3) ; b }", "\"hello\", \"3.0\", \"2.0\"");
    assertEval("{ b <- 3:4 ; b[c(3,2)] <- c(\"X\",\"xx\") ; b }", "\"3L\", \"xx\", \"X\"");
    assertEvalError("{ b <- 3:4 ; b[c(NA)] <- c(2,7) ; b }", "NAs are not allowed in subscripted assignments");
    assertEvalError("{ b <- 3:4 ; b[c(NA,1)] <- c(2,10) ; b }", "NAs are not allowed in subscripted assignments");
    assertEvalError("{ b <- 3:4 ; b[[c(NA,1)]] <- c(2,10) ; b }", "attempt to select more than one element");
    assertEvalWarning("{ b <- 3:4 ; b[c(0,1)] <- c(2,10,11) ; b }", "2.0, 4.0", "number of items to replace is not a multiple of replacement length");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(3:4, c(1,2), c(10,11)) ; f(4:5, as.integer(NA), 2) }", "4.0, 5.0");
    assertEvalError("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(3:4, c(1,2), c(10,11)) ; f(4:5, c(1,-1), 2) }", "only 0's may be mixed with negative subscripts");
    assertEvalError("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(3:4, c(1,2), c(10,11)) ; f(4:5, c(NA,-1), 2) }", "only 0's may be mixed with negative subscripts");

    assertEval("{ b <- c(1,4,5) ; x <- c(2,8,2) ; b[x==2] <- c(10,11) ; b }", "10.0, 4.0, 11.0");
    assertEval("{ b <- c(1,4,5) ; z <- b ; x <- c(2,8,2) ; b[x==2] <- c(10,11) ; b }", "10.0, 4.0, 11.0");
    assertEvalWarning("{ b <- c(1,4,5) ;  x <- c(2,2) ; b[x==2] <- c(10,11) ; b }", "10.0, 11.0, 10.0", "number of items to replace is not a multiple of replacement length");
    assertEvalError("{ b <- c(1,2,5) ;  x <- c(2,2,NA) ; b[x==2] <- c(10,11,3) ; b }", "NAs are not allowed in subscripted assignments");
    assertEval("{ b <- c(1,2,5) ;  x <- as.double(NA) ; attr(x,\"my\") <- 2 ; b[c(1,NA,2)==2] <- x ; b }", "1.0, 2.0, NA");
    assertEvalWarning("{ b <- c(1,2,5) ;  x <- c(2,2,-1) ; b[x==2] <- c(10,11,5) ; b }", "10.0, 11.0, 5.0", "number of items to replace is not a multiple of replacement length");

    assertEval("{ b <- c(1,2,5) ; b[integer()] <- NULL ; b }", "1.0, 2.0, 5.0");
    assertEvalError("{ b <- c(1,2,5) ; b[c(1)] <- NULL ; b }", "replacement has length zero");
    assertEval("{ b <- c(1,2,5) ; attr(b,\"my\") <- 10 ; b[integer()] <- NULL ; b }", "1.0, 2.0, 5.0\nattr(,\"my\")\n10.0");
    assertEval("{ b <- list(1,2,5) ; b[c(1,1,5)] <- NULL ; b }", "[[1]]\n2.0\n\n[[2]]\n5.0\n\n[[3]]\nNULL");
    assertEval("{ b <- list(1,2,5) ; b[c(-1,-4,-5,-1,-5)] <- NULL ; b }", "[[1]]\n1.0");
    assertEval("{ b <- list(1,2,5) ; b[c(1,1,0,NA,5,5,7)] <- NULL ; b }", "[[1]]\n2.0\n\n[[2]]\n5.0\n\n[[3]]\nNULL\n\n[[4]]\nNULL");
    assertEval("{ b <- list(1,2,5) ; b[c(0,-1)] <- NULL ; b }", "[[1]]\n1.0");
    assertEval("{ b <- list(1,2,5) ; b[c(1,NA)] <- NULL ; b }", "[[1]]\n2.0\n\n[[2]]\n5.0");
    assertEvalError("{ b <- list(1,2,5) ; b[c(-1,NA)] <- NULL ; b }", "only 0's may be mixed with negative subscripts");
    assertEvalError("{ b <- list(1,2,5) ; b[c(-1,1)] <- NULL ; b }", "only 0's may be mixed with negative subscripts");
    assertEval("{ b <- list(x=1,y=2,z=5) ; b[c(0,-1)] <- NULL ; b }", "$x\n1.0");
    assertEval("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,-1)] <- NULL ; b }", "[[1]]\n1.0");
    assertEval("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,0)] <- NULL ; b }", "     [,1] [,2] [,3]\n[1,]  1.0  2.0  5.0");
    assertEval("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(-10,-20,0)] <- NULL ; b }", "list()");
    assertEval("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,0,-1,-2,-3)] <- NULL ; b }", "     [,1] [,2] [,3]\n[1,]  1.0  2.0  5.0");
    assertEval("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,3,5)] <- NULL ; b }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\nNULL");
    assertEvalError("{ b <- c(1,2,5) ; b[c(0,3,5)] <- NULL ; b }", "replacement has length zero");

    assertEvalError("{ b <- c(1,2,5) ; b[c(TRUE,FALSE,FALSE)] <- NULL ; b }", "replacement has length zero");
    assertEval("{ b <- c(1,2,5) ; b[logical()] <- NULL ; b }", "1.0, 2.0, 5.0");
    assertEvalError("{ b <- c(1,2,5) ; b[c(TRUE,NA,TRUE)] <- list(TRUE,1+2i) ; b }", "NAs are not allowed in subscripted assignments");
    assertEval("{ b <- c(1,2,5) ; b[c(TRUE,FALSE,TRUE)] <- list(TRUE,1+2i) ; b }", "[[1]]\nTRUE\n\n[[2]]\n2.0\n\n[[3]]\n1.0+2.0i");
    assertEval("{ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(TRUE,FALSE,TRUE)] <- list(TRUE,1+2i) ; b }", "     [,1] [,2]     [,3]\n[1,] TRUE  2.0 1.0+2.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; x <- list(1,2,5) ; dim(x) <- c(1,3) ; f(x, c(FALSE,TRUE,TRUE), list(TRUE,1+2i)) }", "     [,1] [,2]     [,3]\n[1,]  1.0 TRUE 1.0+2.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; x <- as.raw(10:12) ; dim(x) <- c(1,3) ; f(x, c(FALSE,TRUE,TRUE), as.raw(21:22)) }", "     [,1] [,2] [,3]\n[1,]   0a   15   16");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; x <- as.raw(10:12) ; dim(x) <- c(1,3) ; f(x, c(FALSE,TRUE,TRUE), 21:22) }", "incompatible types (from integer to raw) in subassignment type fix");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; x <- 10:12 ; dim(x) <- c(1,3) ; f(x, c(FALSE,TRUE,TRUE), as.raw(21:22)) }", "incompatible types (from raw to integer) in subassignment type fix");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(\"a\",\"XX\",\"b\"), c(FALSE,TRUE,TRUE), 21:22) }", "\"a\", \"21L\", \"22L\"");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(10,12,3), c(FALSE,TRUE,TRUE), c(\"hi\",NA)) }", "\"10.0\", \"hi\", NA");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(10,12,3), c(FALSE,TRUE,TRUE), c(1+2i,10)) }", "10.0+0.0i, 1.0+2.0i, 10.0+0.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(3+4i,5+6i), c(FALSE,TRUE,TRUE), c(\"hi\",NA)) }", "\"3.0+4.0i\", \"hi\", NA");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(3+4i,5+6i), c(FALSE,TRUE,TRUE), c(NA,1+10i)) }", "3.0+4.0i, NA, 1.0+10.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(TRUE,FALSE), c(FALSE,TRUE,TRUE), c(NA,2L)) }", "1L, NA, 2L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), c(NA,FALSE)) }", "3L, NA, 0L");
    assertEvalWarning("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), 4:6) }", "3L, 4L, 5L", "number of items to replace is not a multiple of replacement length");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(TRUE,TRUE,FALSE), c(FALSE,TRUE,TRUE), c(TRUE,NA)) }", "TRUE, TRUE, NA");
    assertEval(" { f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), c(NA,FALSE)) }", "3L, NA, 0L");
    assertEvalWarning("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), 4:6) }", "3L, 4L, 5L", "number of items to replace is not a multiple of replacement length");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,NA), 4) }", "3.0, 4.0, 5.0");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,NA), 4:5) }", "NAs are not allowed in subscripted assignments");
    assertEvalError("{ f <- function(b, i, v) { b[[i]] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,NA), 4:5) }", "attempt to select more than one element");
    assertEval("{ b <- as.list(3:6) ; dim(b) <- c(4,1) ; b[c(TRUE,FALSE)] <- NULL ; b }", "[[1]]\n4L\n\n[[2]]\n6L");
    assertEval("{ b <- as.list(3:6) ; names(b) <- c(\"X\",\"Y\",\"Z\",\"Q\") ; b[c(TRUE,FALSE)] <- NULL ; b }", "$Y\n4L\n\n$Q\n6L");
    assertEval("{ b <- as.list(3:6) ; names(b) <- c(\"X\",\"Y\",\"Z\",\"Q\") ; b[c(FALSE,FALSE)] <- NULL ; b }", "$X\n3L\n\n$Y\n4L\n\n$Z\n5L\n\n$Q\n6L");
    assertEval("{ b <- as.list(3:6) ; dim(b) <- c(1,4) ; b[c(FALSE,FALSE)] <- NULL ; b }", "     [,1] [,2] [,3] [,4]\n[1,]   3L   4L   5L   6L");
    assertEval("{ b <- as.list(3:6) ; dim(b) <- c(1,4) ; b[c(FALSE,FALSE,TRUE)] <- NULL ; b }", "[[1]]\n3L\n\n[[2]]\n4L\n\n[[3]]\n6L");
    assertEval("{ b <- as.list(3:5) ; dim(b) <- c(1,3) ; b[c(FALSE,FALSE,FALSE)] <- NULL ; b }", "     [,1] [,2] [,3]\n[1,]   3L   4L   5L");
    assertEval("{ b <- as.list(3:5) ; dim(b) <- c(1,3) ; b[c(FALSE,TRUE,NA)] <- NULL ; b }", "[[1]]\n3L\n\n[[2]]\n5L");
    assertEval("{ b <- 1:3 ; b[integer()] <- 3:5 ; b }", "1L, 2L, 3L");

    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) }", "[[1]]\n1.0+2.0i\n\n[[2]]\n2.0");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(1:2, c(TRUE,FALSE), list(TRUE)) }", "[[1]]\nTRUE\n\n[[2]]\n2L");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), TRUE) }", "[[1]]\nTRUE\n\n[[2]]\n2L");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), 1+2i) }", "[[1]]\n1.0+2.0i\n\n[[2]]\n2L");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), 10) }", "[[1]]\n10.0\n\n[[2]]\n2L");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), 10L) }", "[[1]]\n10L\n\n[[2]]\n2L");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,NA), list(1+2i)) }", "[[1]]\n1.0+2.0i\n\n[[2]]\n2.0");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,NA), 10) }", "[[1]]\n10.0\n\n[[2]]\n2.0");
    assertEvalError("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,NA), c(10,11)) }", "NAs are not allowed in subscripted assignments");
    assertEvalError("{ f <- function(b,i,v) { b[i] <- v ; b } ; x <- list(1,2) ; z <- x ; f(x, c(TRUE,NA), c(10,11)) }", "NAs are not allowed in subscripted assignments");
    assertEvalWarning("{ x <- list(1,2) ; attr(x,\"my\") <- 10; x[c(TRUE,TRUE)] <- c(10,11,12); x }", "[[1]]\n10.0\n\n[[2]]\n11.0\nattr(,\"my\")\n10.0", "number of items to replace is not a multiple of replacement length");
    assertEvalWarning("{ x <- list(1,0) ; x[as.logical(x)] <- c(10,11); x }", "[[1]]\n10.0\n\n[[2]]\n0.0", "number of items to replace is not a multiple of replacement length");
    assertEval("{ x <- list(1,0) ; x[is.na(x)] <- c(10,11); x }", "[[1]]\n1.0\n\n[[2]]\n0.0");
    assertEvalWarning("{ x <- list(1,0) ; x[c(TRUE,FALSE)] <- x[2:1] ; x }", "[[1]]\n0.0\n\n[[2]]\n0.0", "number of items to replace is not a multiple of replacement length");
    assertEvalWarning("{ x <- list(1,0) ; attr(x,\"my\") <- 20 ; x[c(TRUE,FALSE)] <- c(11,12) ; x }", "[[1]]\n11.0\n\n[[2]]\n0.0\nattr(,\"my\")\n20.0", "number of items to replace is not a multiple of replacement length");
    assertEval("{ x <- list(1,0) ; x[is.na(x)] <- c(10L,11L); x }", "[[1]]\n1.0\n\n[[2]]\n0.0");
    assertEval("{ x <- list(1,0) ; x[c(TRUE,TRUE)] <- c(TRUE,NA); x }", "[[1]]\nTRUE\n\n[[2]]\nNA");
    assertEval("{ x <- list(1,0) ; x[logical()] <- c(TRUE,NA); x }", "[[1]]\n1.0\n\n[[2]]\n0.0");

    assertEval("{ x <- c(1,0) ; x[c(TRUE,TRUE)] <- c(TRUE,NA); x }", "1.0, NA");
    assertEval("{ x <- c(1,0) ; x[c(TRUE,TRUE)] <- 3:4; x }", "3.0, 4.0");
    assertEval("{ x <- c(1,0) ; x[logical()] <- 3:4; x }", "1.0, 0.0");
    assertEval("{ x <- c(1,0) ; attr(x,\"my\") <- 1 ; x[c(TRUE,TRUE)] <- c(NA,TRUE); x }", "NA, 1.0\nattr(,\"my\")\n1.0");
    assertEvalError("{ x <- c(1,0) ; x[c(NA,TRUE)] <- c(NA,TRUE); x }", "NAs are not allowed in subscripted assignments");
    assertEvalError("{ x <- c(1,0) ; z <- x ; x[c(NA,TRUE)] <- c(NA,TRUE); x }", "NAs are not allowed in subscripted assignments");
    assertEval("{ x <- c(1,0) ; z <- x ; x[c(NA,TRUE)] <- TRUE; x }", "1.0, 1.0");
    assertEval("{ x <- c(1,0)  ; x[is.na(x)] <- TRUE; x }", "1.0, 0.0");
    assertEval("{ x <- c(1,0)  ; x[c(TRUE,TRUE)] <- rev(x) ; x }", "0.0, 1.0");
    assertEval("{ x <- c(1,0) ; f <- function(v) { x[c(TRUE,TRUE)] <- v ; x } ; f(1:2) ; f(c(1,2)) }", "1.0, 2.0");
    assertEval("{ x <- c(1,0) ; f <- function(v) { x[c(TRUE,TRUE)] <- v ; x } ; f(1:2) ; f(1+2i) }", "1.0+2.0i, 1.0+2.0i");

    assertEval("{ b <- list(1,2,3) ; attr(b,\"my\") <- 12; b[2] <- NULL ; b }", "[[1]]\n1.0\n\n[[2]]\n3.0\nattr(,\"my\")\n12.0");
    assertEval("{ b <- list(1,2,3) ; attr(b,\"my\") <- 12; b[2:3] <- NULL ; b }", "[[1]]\n1.0\nattr(,\"my\")\n12.0");

    assertEval("{ x <- 1:2 ; x[c(TRUE,FALSE,FALSE,TRUE)] <- 3:4 ; x }", "3L, 2L, NA, 4L");
    assertEvalError("{ x <- 1:2 ; x[c(TRUE,FALSE,FALSE,NA)] <- 3:4 ; x }", "NAs are not allowed in subscripted assignments");
    assertEval("{ x <- 1:2 ; x[c(TRUE,FALSE,FALSE,NA)] <- 3L ; x }", "3L, 2L, NA, NA");
    assertEval("{ x <- 1:2 ; x[c(TRUE,NA)] <- 3L ; x }", "3L, 2L");
    assertEvalError("{ x <- 1:2 ; x[c(TRUE,NA)] <- 2:3 ; x }", "NAs are not allowed in subscripted assignments");
    assertEval("{ x <- c(1L,2L) ; x[c(TRUE,FALSE)] <- 3L ; x }", "3L, 2L");
    assertEval("{ x <- c(1L,2L) ; x[c(TRUE,NA)] <- 3L ; x }", "3L, 2L");
    assertEval("{ x <- c(1L,2L) ; x[TRUE] <- 3L ; x }", "3L, 3L");
    assertEval("{ x <- c(1L,2L,3L,4L) ; x[c(TRUE,FALSE)] <- 5:6 ; x }", "5L, 2L, 6L, 4L");
    assertEval("{ x <- c(1L,2L,3L,4L) ; attr(x,\"my\") <- 0 ;  x[c(TRUE,FALSE)] <- 5:6 ; x }", "5L, 2L, 6L, 4L\nattr(,\"my\")\n0.0");
    assertEval("{ x <- c(1L,2L,3L,4L) ;  x[is.na(x)] <- 5:6 ; x }", "1L, 2L, 3L, 4L");
    assertEvalWarning("{ x <- c(1L,2L,3L,4L) ; x[c(TRUE,FALSE)] <- rev(x) ; x }", "4L, 2L, 3L, 4L", "number of items to replace is not a multiple of replacement length");
    assertEval("{ x <- c(1L,2L) ; x[logical()] <- 3L ; x }", "1L, 2L");

    assertEval("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE)] <- c(FALSE,NA) ; b }", "FALSE, NA, NA, TRUE");
    assertEval("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,FALSE)] <- c(FALSE,NA) ; b }", "FALSE, NA, FALSE, NA");
    assertEvalError("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,NA)] <- c(FALSE,NA) ; b }", "NAs are not allowed in subscripted assignments");
    assertEvalWarning("{ b <- c(TRUE,NA,FALSE) ; b[c(TRUE,TRUE)] <- c(FALSE,NA) ; b }", "FALSE, NA, FALSE", "number of items to replace is not a multiple of replacement length");
    assertEval("{ b <- c(TRUE,NA,FALSE) ; b[c(TRUE,FALSE,TRUE,TRUE)] <- c(FALSE,NA,NA) ; b }", "FALSE, NA, NA, NA");
    assertEval("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,TRUE,NA)] <- FALSE ; b }", "FALSE, NA, FALSE, TRUE");
    assertEval("{ b <- c(TRUE,NA,FALSE,TRUE) ; z <- b ; b[c(TRUE,FALSE,TRUE,NA)] <- FALSE ; b }", "FALSE, NA, FALSE, TRUE");
    assertEval("{ b <- c(TRUE,NA,FALSE,TRUE) ; attr(b,\"my\") <- 10 ; b[c(TRUE,FALSE,TRUE,NA)] <- FALSE ; b }", "FALSE, NA, FALSE, TRUE\nattr(,\"my\")\n10.0");
    assertEvalWarning("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,TRUE,FALSE)] <- b ; b }", "TRUE, NA, NA, TRUE", "number of items to replace is not a multiple of replacement length");
    assertEval("{ b <- c(TRUE,FALSE,FALSE,TRUE) ; b[b] <- c(TRUE,FALSE) ; b }", "TRUE, FALSE, FALSE, FALSE");
    assertEvalWarning("{ f <- function(b,i,v) { b[b] <- b ; b } ; f(c(TRUE,FALSE,FALSE,TRUE)) ; f(1:3) }", "1L, 2L, 3L", "number of items to replace is not a multiple of replacement length");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(TRUE,FALSE,FALSE,TRUE),c(TRUE,FALSE), NA) ; f(1:4, c(TRUE,TRUE), NA) }", "NA, NA, NA, NA");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(TRUE,FALSE,FALSE,TRUE),c(TRUE,FALSE), NA) ; f(c(FALSE,FALSE,TRUE), c(TRUE,TRUE), c(1,2,3)) }", "1.0, 2.0, 3.0");
    assertEval("{ b <- c(TRUE,NA,FALSE,TRUE) ; b[logical()] <- c(FALSE,NA) ; b }", "TRUE, NA, FALSE, TRUE");

    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE)] <- \"X\" ; b }", "\"X\", \"b\", \"X\"");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE,TRUE,TRUE)] <- \"X\" ; b }", "\"X\", \"b\", \"X\", \"X\"");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE,TRUE,NA)] <- \"X\" ; b }", "\"X\", \"b\", \"X\", NA");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE,NA)] <- \"X\" ; b }", "\"X\", \"b\", \"c\"");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; b[logical()] <- \"X\" ; b }", "\"a\", \"b\", \"c\"");
    assertEval("{ b <- 1:3 ; dim(b) <- c(1,3) ;  b[integer()] <- 3:5 ; b }", "     [,1] [,2] [,3]\n[1,]   1L   2L   3L");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; b[logical()] <- \"X\" ; b }", "\"a\", \"b\", \"c\"");
    assertEvalError("{ b <- c(\"a\",\"b\",\"c\") ; b[c(FALSE,NA,NA)] <- c(\"X\",\"y\") ; b }", "NAs are not allowed in subscripted assignments");
    assertEvalWarning("{ b <- c(\"a\",\"b\",\"c\") ; b[c(FALSE,TRUE,TRUE)] <- c(\"X\",\"y\",\"z\") ; b }", "\"a\", \"X\", \"y\"", "number of items to replace is not a multiple of replacement length");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; x <- b ; b[c(FALSE,TRUE,TRUE)] <- c(\"X\",\"z\") ; b } ", "\"a\", \"X\", \"z\"");
    assertEvalError("{ b <- c(\"a\",\"b\",\"c\") ; x <- b ; b[c(FALSE,TRUE,NA)] <- c(\"X\",\"z\") ; b }", "NAs are not allowed in subscripted assignments");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; b[is.na(b)] <- c(\"X\",\"z\") ; b }", "\"a\", \"b\", \"c\"");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; attr(b,\"my\") <- 211 ; b[c(FALSE,TRUE)] <- c(\"X\") ; b }", "\"a\", \"X\", \"c\"\nattr(,\"my\")\n211.0");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,TRUE,TRUE)] <- rev(as.character(b)) ; b }", "\"c\", \"b\", \"a\"");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(\"a\",\"b\",\"c\"),c(TRUE,FALSE),c(\"A\",\"X\")) ; f(1:3,c(TRUE,FALSE),4) }", "4.0, 2.0, 4.0");
    assertEval("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(\"a\",\"b\",\"c\"),c(TRUE,FALSE),c(\"A\",\"X\")) ; f(c(\"A\",\"X\"),c(TRUE,FALSE),4) }", "\"4.0\", \"X\"");
    assertEval("{ b <- c(\"a\",\"b\",\"c\") ; b[c(TRUE,FALSE,TRUE)] <- c(1+2i,3+4i) ; b }", "\"1.0+2.0i\", \"b\", \"3.0+4.0i\"");
    assertEvalError("{ b <- as.raw(1:5) ; b[c(TRUE,FALSE,TRUE)] <- c(1+2i,3+4i) ; b }", "incompatible types (from complex to raw) in subassignment type fix");

    assertEvalError("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(\"a\",\"b\",\"c\"),c(TRUE,FALSE),c(\"A\",\"X\")) ; f(f,c(TRUE,FALSE),4) }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(\"a\",\"b\",\"c\"),c(TRUE,FALSE),c(\"A\",\"X\")) ; f(c(\"A\",\"X\"),c(TRUE,FALSE),f) }", "incompatible types (from closure to character) in subassignment type fix");

    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:2,\"hi\",3L) ; f(1:2,c(2),10) ; f(1:2, -1, 10) }", "1.0, 10.0");
    assertEval("{ x <- c(); f <- function(i, v) { x[i] <- v ; x } ; f(1:2,3:4); f(c(1,2),c(TRUE,FALSE)) }", "TRUE, FALSE");
    assertEval("{ x <- c(); f <- function(i, v) { x[i] <- v ; x } ; f(1:2,3:4); f(c(\"a\",\"b\"),c(TRUE,FALSE)) }", "   a     b\nTRUE FALSE");

    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), 2, NULL) }", "[[1]]\n1.0\n\n[[2]]\n3.0");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(c(1,2,3), 2, NULL) }", "replacement has length zero");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(c(1,2,3), 2:3, NULL) }", "replacement has length zero");
    assertEvalError("{ f <- function(b, i, v) { b[[i]] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(c(1,2,3), 2:3, NULL) }", "attempt to select more than one element");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), 3L, NULL) }", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), 3:2, NULL) }", "[[1]]\n1.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), c(2,3), NULL) }", "[[1]]\n1.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), NULL, NULL) }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\n3.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), c(TRUE,TRUE,FALSE), NULL) }", "[[1]]\n3.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; l <- list(1,2,3) ; dim(l) <- c(1,3) ; z <- f(l, c(TRUE,TRUE,FALSE), NULL) ; z }", "[[1]]\n3.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; l <- list(1,2,3) ; dim(l) <- c(1,3) ; z <- f(l, NULL, NULL) ; z }", "     [,1] [,2] [,3]\n[1,]  1.0  2.0  3.0");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; l <- list(1,2,3) ; dim(l) <- c(1,3) ; z <- f(l, c, NULL) ; z }", "invalid subscript type 'builtin'");
    assertEvalError(" { f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; l <- list(1,2,3) ; dim(l) <- c(1,3) ; z <- f(l, c(1+2i,3+4i), NULL) ; z }", "invalid subscript type 'complex'");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), 3:1, 10) }", "[[1]]\n10.0\n\n[[2]]\n10.0\n\n[[3]]\n10.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), c(3,3,2), 10) }", "[[1]]\n1.0\n\n[[2]]\n10.0\n\n[[3]]\n10.0");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c(\"b\",\"c\",\"a\"), 14:16) ; f(list(1,2,3), list(1,3,3), 10) }", "invalid subscript type 'list'");
  }


  @Test
  public void testListDefinitions()  {
    assertEval("{ list(1:4) }", "[[1]]\n1L, 2L, 3L, 4L");
    assertEval("{ list(1,list(2,list(3,4))) }", "[[1]]\n1.0\n\n[[2]]\n[[2]][[1]]\n2.0\n\n[[2]][[2]]\n[[2]][[2]][[1]]\n3.0\n\n[[2]][[2]][[2]]\n4.0");

    assertEval("{ list(1,b=list(2,3)) }", "[[1]]\n1.0\n\n$b\n$b[[1]]\n2.0\n\n$b[[2]]\n3.0");
    assertEval("{ list(1,b=list(c=2,3)) }", "[[1]]\n1.0\n\n$b\n$b$c\n2.0\n\n$b[[2]]\n3.0");
    assertEval("{ list(list(c=2)) }", "[[1]]\n[[1]]$c\n2.0");
  }

  @Test
  public void testListAccess()  {
    // indexing
    assertEval("{ l<-list(1,2L,TRUE) ; l[[2]] }", "2L");
    assertEval("{ l<-list(1,2L,TRUE) ; l[c(FALSE,FALSE,TRUE)] }", "[[1]]\nTRUE");
    assertEval("{ l<-list(1,2L,TRUE) ; l[FALSE] }", "list()");
    assertEval("{ l<-list(1,2L,TRUE) ; l[-2] }", "[[1]]\n1.0\n\n[[2]]\nTRUE");
    assertEval("{ l<-list(1,2L,TRUE) ; l[NA] }", "[[1]]\nNULL\n\n[[2]]\nNULL\n\n[[3]]\nNULL");
    assertEval("{ l<-list(1,2,3) ; l[c(1,2)] }", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEval("{ l<-list(1,2,3) ; l[c(2)] }", "[[1]]\n2.0");
    assertEval("{ x<-list(1,2L,TRUE,FALSE,5) ; x[2:4] }", "[[1]]\n2L\n\n[[2]]\nTRUE\n\n[[3]]\nFALSE");
    assertEval("{ x<-list(1,2L,TRUE,FALSE,5) ; x[4:2] }", "[[1]]\nFALSE\n\n[[2]]\nTRUE\n\n[[3]]\n2L");
    assertEval("{ x<-list(1,2L,TRUE,FALSE,5) ; x[c(-2,-3)] }", "[[1]]\n1.0\n\n[[2]]\nFALSE\n\n[[3]]\n5.0");
    assertEval("{ x<-list(1,2L,TRUE,FALSE,5) ; x[c(-2,-3,-4,0,0,0)] }", "[[1]]\n1.0\n\n[[2]]\n5.0");
    assertEval("{ x<-list(1,2L,TRUE,FALSE,5) ; x[c(2,5,4,3,3,3,0)] }", "[[1]]\n2L\n\n[[2]]\n5.0\n\n[[3]]\nFALSE\n\n[[4]]\nTRUE\n\n[[5]]\nTRUE\n\n[[6]]\nTRUE");
    assertEval("{ x<-list(1,2L,TRUE,FALSE,5) ; x[c(2L,5L,4L,3L,3L,3L,0L)] }", "[[1]]\n2L\n\n[[2]]\n5.0\n\n[[3]]\nFALSE\n\n[[4]]\nTRUE\n\n[[5]]\nTRUE\n\n[[6]]\nTRUE");
    assertEval("{ m<-list(1,2) ; m[NULL] }", "list()");

    // indexing with rewriting
    assertEval("{ f<-function(x, i) { x[i] } ; f(list(1,2,3),3:1) ; f(list(1L,2L,3L,4L,5L),c(0,0,0,0-2)) }", "[[1]]\n1L\n\n[[2]]\n3L\n\n[[3]]\n4L\n\n[[4]]\n5L");
    assertEval("{ x<-list(1,2,3,4,5) ; x[c(TRUE,TRUE,TRUE,FALSE,FALSE,FALSE,FALSE,TRUE,NA)] }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\n3.0\n\n[[4]]\nNULL\n\n[[5]]\nNULL");
    assertEval("{ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(1) ; f(1L) ; f(TRUE) }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\n3.0\n\n[[4]]\n4.0\n\n[[5]]\n5.0");
    assertEval("{ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(1) ; f(TRUE) ; f(1L)  }", "[[1]]\n1.0");
    assertEval("{ f<-function(i) { x<-list(1L,2L,3L,4L,5L) ; x[i] } ; f(1) ; f(TRUE) ; f(c(3,2))  }", "[[1]]\n3L\n\n[[2]]\n2L");
    assertEval("{ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(1)  ; f(3:4) }", "[[1]]\n3.0\n\n[[2]]\n4.0");
    assertEval("{ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(c(TRUE,FALSE))  ; f(3:4) }", "[[1]]\n3.0\n\n[[2]]\n4.0");

    // recursive indexing
    assertEval("{ l<-(list(list(1,2),list(3,4))); l[[c(1,2)]] }", "2.0");
    assertEval("{ l<-(list(list(1,2),list(3,4))); l[[c(1,-2)]] }", "1.0");
    assertEval("{ l<-(list(list(1,2),list(3,4))); l[[c(1,-1)]] }", "2.0");
    assertEval("{ l<-(list(list(1,2),list(3,4))); l[[c(1,TRUE)]] }", "1.0");
    assertEval("{ l<-(list(list(1,2),c(3,4))); l[[c(2,1)]] }", "3.0");
    assertEval("{ l <- list(a=1,b=2,c=list(d=3,e=list(f=4))) ; l[[c(3,2)]] }", "$f\n4.0");
    assertEval("{ l <- list(a=1,b=2,c=list(d=3,e=list(f=4))) ; l[[c(3,1)]] }", "3.0");

    assertEval("{ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c(\"c\",\"e\")]] }", "  f\n4.0");
    assertEval("{ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c(\"c\",\"e\", \"f\")]] }", "4.0");
    assertEval("{ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c(\"c\")]] }", "$d\n3.0\n\n$e\n  f\n4.0");
    assertEval("{ f <- function(b, i, v) { b[[i]] <- v ; b } ; f(1:3,2,2) ; f(1:3,\"X\",2) ; f(list(1,list(2)),c(2,1),4) }", "[[1]]\n1.0\n\n[[2]]\n[[2]][[1]]\n4.0");
  }

  @Test
  public void testListUpdate()  {
    // scalar update
    assertEval("{ l<-list(1,2L,TRUE) ; l[[2]]<-100 ; l }", "[[1]]\n1.0\n\n[[2]]\n100.0\n\n[[3]]\nTRUE");
    assertEval("{ l<-list(1,2L,TRUE) ; l[[5]]<-100 ; l }", "[[1]]\n1.0\n\n[[2]]\n2L\n\n[[3]]\nTRUE\n\n[[4]]\nNULL\n\n[[5]]\n100.0");
    assertEval("{ l<-list(1,2L,TRUE) ; l[[3]]<-list(100) ; l }", "[[1]]\n1.0\n\n[[2]]\n2L\n\n[[3]]\n[[3]][[1]]\n100.0");
    assertEval("{ v<-1:3 ; v[2] <- list(100) ; v }", "[[1]]\n1L\n\n[[2]]\n100.0\n\n[[3]]\n3L");
    assertEval("{ v<-1:3 ; v[[2]] <- list(100) ; v }", "[[1]]\n1L\n\n[[2]]\n[[2]][[1]]\n100.0\n\n[[3]]\n3L");
    assertEval("{ l <- list() ; l[[1]] <-2 ; l}", "[[1]]\n2.0");
    assertEval("{ l<-list() ; x <- 1:3 ; l[[1]] <- x  ; l }", "[[1]]\n1L, 2L, 3L");
    assertEval("{ l <- list(1,2,3) ; l[2] <- list(100) ; l[2] }", "[[1]]\n100.0");
    assertEval("{ l <- list(1,2,3) ; l[[2]] <- list(100) ; l[2] }", "[[1]]\n[[1]][[1]]\n100.0");

    // element deletion
    assertEval("{ m<-list(1,2) ; m[TRUE] <- NULL ; m }", "list()");
    assertEval("{ m<-list(1,2) ; m[[TRUE]] <- NULL ; m }", "[[1]]\n2.0");
    assertEval("{ m<-list(1,2) ; m[[1]] <- NULL ; m }", "[[1]]\n2.0");
    assertEval("{ m<-list(1,2) ; m[[-1]] <- NULL ; m }", "[[1]]\n1.0");
    assertEval("{ m<-list(1,2) ; m[[-2]] <- NULL ; m }", "[[1]]\n2.0");
    assertEval("{ l <- matrix(list(1,2)) ; l[3] <- NULL ; l }", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEval("{ l <- matrix(list(1,2)) ; l[[3]] <- NULL ; l }", "     [,1]\n[1,]  1.0\n[2,]  2.0");
    assertEval("{ l <- matrix(list(1,2)) ; l[[4]] <- NULL ; l }", "     [,1]\n[1,]  1.0\n[2,]  2.0");
    assertEval("{ l <- matrix(list(1,2)) ; l[4] <- NULL ; l }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\nNULL");
    assertEval("{ l <- list(a=1,b=2,c=3) ; l[1] <- NULL ; l }", "$b\n2.0\n\n$c\n3.0");
    assertEval("{ l <- list(a=1,b=2,c=3) ; l[3] <- NULL ; l }", "$a\n1.0\n\n$b\n2.0");

    assertEval("{ l <- list(a=1,b=2,c=3) ; l[5] <- NULL ; l}", "$a\n1.0\n\n$b\n2.0\n\n$c\n3.0\n\n[[4]]\nNULL");
    assertEval("{ l <- list(a=1,b=2,c=3) ; l[4] <- NULL ; l}", "$a\n1.0\n\n$b\n2.0\n\n$c\n3.0");
    assertEval("{ l <- list(a=1,b=2,c=3) ; l[[5]] <- NULL ; l}", "$a\n1.0\n\n$b\n2.0\n\n$c\n3.0");
    assertEval("{ l <- list(a=1,b=2,c=3) ; l[[4]] <- NULL ; l}", "$a\n1.0\n\n$b\n2.0\n\n$c\n3.0");

    assertEval("{ l <- list(1,2); l[0] <- NULL; l}", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEvalError("{ l <- list(1,2); l[[0]] }", "attempt to select less than one element");

    // vector update
    assertEval("{ l <- list(1,2,3) ; l[c(2,3)] <- c(20,30) ; l }", "[[1]]\n1.0\n\n[[2]]\n20.0\n\n[[3]]\n30.0");
    assertEval("{ l <- list(1,2,3) ; l[c(2:3)] <- c(20,30) ; l }", "[[1]]\n1.0\n\n[[2]]\n20.0\n\n[[3]]\n30.0");
    assertEval("{ l <- list(1,2,3) ; l[-1] <- c(20,30) ; l }", "[[1]]\n1.0\n\n[[2]]\n20.0\n\n[[3]]\n30.0");
    assertEval("{ l <- list(1,2,3) ; l[-1L] <- c(20,30) ; l }", "[[1]]\n1.0\n\n[[2]]\n20.0\n\n[[3]]\n30.0");
    assertEval("{ l <- list(1,2,3) ; l[c(FALSE,TRUE,TRUE)] <- c(20,30) ; l }", "[[1]]\n1.0\n\n[[2]]\n20.0\n\n[[3]]\n30.0");
    assertEval("{ l <- list() ; l[c(TRUE,TRUE)] <-2 ; l }", "[[1]]\n2.0\n\n[[2]]\n2.0");
    assertEval("{ x <- 1:3 ; l <- list(1) ; l[[TRUE]] <- x ; l[[1]] } ", "1L, 2L, 3L");

    assertEval("{ x<-list(1,2,3,4,5); x[3:4]<-c(300L,400L); x }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\n300L\n\n[[4]]\n400L\n\n[[5]]\n5.0");
    assertEval("{ x<-list(1,2,3,4,5); x[4:3]<-c(300L,400L); x }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\n400L\n\n[[4]]\n300L\n\n[[5]]\n5.0");
    assertEval("{ x<-list(1,2L,TRUE,TRUE,FALSE); x[c(-2,-3,-3,-100,0)]<-256; x }", "[[1]]\n256.0\n\n[[2]]\n2L\n\n[[3]]\nTRUE\n\n[[4]]\n256.0\n\n[[5]]\n256.0");
    assertEval("{ x<-list(1,2L,list(3,list(4)),list(5)) ; x[c(4,2,3)]<-list(256L,257L,258L); x }", "[[1]]\n1.0\n\n[[2]]\n257L\n\n[[3]]\n258L\n\n[[4]]\n256L");
    assertEval("{ x<-list(FALSE,NULL,3L,4L,5.5); x[c(TRUE,FALSE)] <- 1000; x }", "[[1]]\n1000.0\n\n[[2]]\nNULL\n\n[[3]]\n1000.0\n\n[[4]]\n4L\n\n[[5]]\n1000.0");
    assertEval("{ x<-list(11,10,9) ; x[c(TRUE, FALSE, TRUE)] <- c(1000,2000); x }", "[[1]]\n1000.0\n\n[[2]]\n10.0\n\n[[3]]\n2000.0");
    assertEval("{ l <- list(1,2,3) ; x <- list(100) ; y <- x; l[1:1] <- x ; l[[1]] }", "100.0");
    assertEval("{ l <- list(1,2,3) ; x <- list(100) ; y <- x; l[[1:1]] <- x ; l[[1]] }", "[[1]]\n100.0");

    // vector element deletion
    assertEval("{ v<-list(1,2,3) ; v[c(2,3,NA,7,0)] <- NULL ; v }", "[[1]]\n1.0\n\n[[2]]\nNULL\n\n[[3]]\nNULL\n\n[[4]]\nNULL");
    assertEval("{ v<-list(1,2,3) ; v[c(2,3,4)] <- NULL ; v }", "[[1]]\n1.0");
    assertEval("{ v<-list(1,2,3) ; v[c(-1,-2,-6)] <- NULL ; v }", "[[1]]\n1.0\n\n[[2]]\n2.0");
    assertEval("{ v<-list(1,2,3) ; v[c(TRUE,FALSE,TRUE)] <- NULL ; v }", "[[1]]\n2.0");
    assertEval("{ v<-list(1,2,3) ; v[c()] <- NULL ; v }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\n3.0");
    assertEval("{ v<-list(1,2,3) ; v[integer()] <- NULL ; v }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\n3.0");
    assertEval("{ v<-list(1,2,3) ; v[double()] <- NULL ; v }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\n3.0");
    assertEval("{ v<-list(1,2,3) ; v[logical()] <- NULL ; v }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n[[3]]\n3.0");
    assertEval("{ v<-list(1,2,3) ; v[c(TRUE,FALSE)] <- NULL ; v }", "[[1]]\n2.0");
    assertEval("{ v<-list(1,2,3) ; v[c(TRUE,FALSE,FALSE,FALSE,FALSE,TRUE)] <- NULL ; v }", "[[1]]\n2.0\n\n[[2]]\n3.0\n\n[[3]]\nNULL\n\n[[4]]\nNULL");

    assertEval("{ l<-list(a=1,b=2,c=3,d=4); l[c(-1,-3)] <- NULL ; l}", "$a\n1.0\n\n$c\n3.0");
    assertEval("{ l<-list(a=1,b=2,c=3,d=4); l[c(-1,-10)] <- NULL ; l}", "$a\n1.0");
    assertEval("{ l<-list(a=1,b=2,c=3,d=4); l[c(2,3)] <- NULL ; l}", "$a\n1.0\n\n$d\n4.0");
    assertEval("{ l<-list(a=1,b=2,c=3,d=4); l[c(2,3,5)] <- NULL ; l}", "$a\n1.0\n\n$d\n4.0");
    assertEval("{ l<-list(a=1,b=2,c=3,d=4); l[c(2,3,6)] <- NULL ; l}", "$a\n1.0\n\n$d\n4.0\n\n[[3]]\nNULL");
    assertEval("{ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,TRUE,FALSE,TRUE)] <- NULL ; l}", "$c\n3.0");
    assertEval("{ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,FALSE)] <- NULL ; l}", "$b\n2.0\n\n$d\n4.0");
    assertEval("{ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,FALSE,FALSE,TRUE,FALSE,NA,TRUE,TRUE)] <- NULL ; l}", "$b\n2.0\n\n$c\n3.0\n\n[[3]]\nNULL\n\n[[4]]\nNULL");

    assertEval("{ l <- list(a=1,b=2,c=3) ; l[[\"b\"]] <- NULL ; l }", "$a\n1.0\n\n$c\n3.0");

    // recursive indexing
    assertEval("{ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- NULL ; l }", "[[1]]\n1.0\n\n[[2]]\n[[2]][[1]]\n2.0");
    assertEval("{ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- 4 ; l }", "[[1]]\n1.0\n\n[[2]]\n[[2]][[1]]\n2.0\n\n[[2]][[2]]\n4.0");
    assertEval("{ l <- list(1,list(2,list(3))) ; l[[1]] <- NULL ; l }", "[[1]]\n[[1]][[1]]\n2.0\n\n[[1]][[2]]\n[[1]][[2]][[1]]\n3.0");
    assertEval("{ l <- list(1,list(2,list(3))) ; l[[1]] <- 5 ; l }", "[[1]]\n5.0\n\n[[2]]\n[[2]][[1]]\n2.0\n\n[[2]][[2]]\n[[2]][[2]][[1]]\n3.0");

    assertEval("{ l<-list(a=1,b=2,list(c=3,d=4,list(e=5:6,f=100))) ; l[[c(3,3,1)]] <- NULL ; l }", "$a\n1.0\n\n$b\n2.0\n\n[[3]]\n[[3]]$c\n3.0\n\n[[3]]$d\n4.0\n\n[[3]][[3]]\n[[3]][[3]]$f\n100.0");
    assertEval("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"zz\")]] <- 100 ; l }", "$a\n1.0\n\n$b\n2.0\n\n$c\n$c$d\n1.0\n\n$c$e\n2.0\n\n$c$f\n  x   y   z    zz\n1.0 2.0 3.0 100.0");
    assertEval("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"z\")]] <- 100 ; l }", "$a\n1.0\n\n$b\n2.0\n\n$c\n$c$d\n1.0\n\n$c$e\n2.0\n\n$c$f\n  x   y     z\n1.0 2.0 100.0");
    assertEval("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\")]] <- NULL ; l }", "$a\n1.0\n\n$b\n2.0\n\n$c\n$c$d\n1.0\n\n$c$e\n2.0");
    assertEval("{ l<-list(a=1,b=2,c=3) ; l[c(\"a\",\"a\",\"a\",\"c\")] <- NULL ; l }", "$b\n2.0");
    assertEval("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\",\"zz\")]] <- 100L ; l }", "$a\n1L\n\n$b\n2L\n\n$c\n$c$d\n1L\n\n$c$e\n2L\n\n$c$f\n x  y  z   zz\n1L 2L 3L 100L");
    assertEval("{ l<-list(a=TRUE,b=FALSE,c=list(d=TRUE,e=FALSE,f=c(x=TRUE,y=FALSE,z=TRUE))) ; l[[c(\"c\",\"f\",\"zz\")]] <- TRUE ; l }", "$a\nTRUE\n\n$b\nFALSE\n\n$c\n$c$d\nTRUE\n\n$c$e\nFALSE\n\n$c$f\n   x     y    z   zz\nTRUE FALSE TRUE TRUE");
    assertEval("{ l<-list(a=\"a\",b=\"b\",c=list(d=\"cd\",e=\"ce\",f=c(x=\"cfx\",y=\"cfy\",z=\"cfz\"))) ; l[[c(\"c\",\"f\",\"zz\")]] <- \"cfzz\" ; l }", "$a\n\"a\"\n\n$b\n\"b\"\n\n$c\n$c$d\n\"cd\"\n\n$c$e\n\"ce\"\n\n$c$f\n    x     y     z     zz\n\"cfx\" \"cfy\" \"cfz\" \"cfzz\"");

    assertEval("{ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c(\"c\",\"f\",\"zz\")]] <- list(100) ; l }", "$a\n1.0\n\n$b\n2.0\n\n$c\n$c$d\n1.0\n\n$c$e\n2.0\n\n$c$f\n$c$f$x\n1.0\n\n$c$f$y\n2.0\n\n$c$f$z\n3.0\n\n$c$f$zz\n$c$f$zz[[1]]\n100.0");
    assertEval("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\")]] <- 100L ; l }", "$a\n1L\n\n$b\n2L\n\n$c\n$c$d\n1L\n\n$c$e\n2L\n\n$c$f\n100L");
    assertEval("{ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c(\"c\",\"f\")]] <- list(haha=\"gaga\") ; l }", "$a\n1L\n\n$b\n2L\n\n$c\n$c$d\n1L\n\n$c$e\n2L\n\n$c$f\n$c$f$haha\n\"gaga\"");

    assertEvalError("{ l <- list(list(1,2),2) ; l[[c(1,1,2,3,4,3)]] <- 10 ; l }", "recursive indexing failed at level 3");
    assertEvalError("{ l <- list(1,2) ; l[[c(1,1,2,3,4,3)]] <- 10 ; l }", "recursive indexing failed at level 2");

    // copying
    assertEval("{ x<-c(1,2,3) ; y<-x ; x[2]<-100 ; y }", "1.0, 2.0, 3.0");
    assertEval("{ l<-list() ; x <- 1:3 ; l[[1]] <- x; x[2] <- 100L; l[[1]] }", "1L, 2L, 3L");
    assertEval("{ l <- list(1, list(2)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }", "2.0");
    assertEval("{ l <- list(1, list(2,3,4)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }", "2.0");
    assertEval("{ x <- c(1L,2L,3L) ; l <- list(1) ; l[[1]] <- x ; x[2] <- 100L ; l[[1]] }", "1L, 2L, 3L");
    assertEval("{ l <- list(100) ; f <- function() { l[[1]] <- 2 } ; f() ; l }", "[[1]]\n100.0");
    assertEval("{ l <- list(100,200,300,400,500) ; f <- function() { l[[3]] <- 2 } ; f() ; l }", "[[1]]\n100.0\n\n[[2]]\n200.0\n\n[[3]]\n300.0\n\n[[4]]\n400.0\n\n[[5]]\n500.0");
    assertEval("{ x <-2L ; y <- x; x[1] <- 211L ; y }", "2L");
    assertEval("{ f <- function() { l[1:2] <- x ; x[1] <- 211L  ; l[1] } ; l <- 1:3 ; x <- 10L ; f() }", "10L");

    assertEvalError("{ l <- as.list(1:3) ; l[[0]] <- 2 }", "attempt to select less than one element");
    assertEvalError("{ x <- as.list(1:3) ; x[[integer()]] <- 3 }", "attempt to select less than one element");
    assertEval("{ x <- list(1,list(2,3),4) ; x[[c(2,3)]] <- 3 ; x }", "[[1]]\n1.0\n\n[[2]]\n[[2]][[1]]\n2.0\n\n[[2]][[2]]\n3.0\n\n[[2]][[3]]\n3.0\n\n[[3]]\n4.0");
    assertEval("{ x <- list(1,list(2,3),4) ; z <- x[[2]] ; x[[c(2,3)]] <- 3 ; z }", "[[1]]\n2.0\n\n[[2]]\n3.0");
    assertEval("{ x <- list(1,list(2,3),4) ; z <- list(x,x) ; u <- list(z,z) ; u[[c(2,2,3)]] <- 6 ; unlist(u) }", "1.0, 2.0, 3.0, 4.0, 1.0, 2.0, 3.0, 4.0, 1.0, 2.0, 3.0, 4.0, 1.0, 2.0, 3.0, 6.0");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, 3) }", "[[1]]\n1.0\n\n[[2]]\n3.0\n\n[[3]]\n3.0");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(f,f), c(1,1), 3) }","object of type 'closure' is not subsettable");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, NULL) }", "[[1]]\n1.0\n\n[[2]]\n3.0");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(c(1,2,3), 2L, NULL) }","more elements supplied than there are to replace");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(c(1,2,3), 2L, 1:2) }", "more elements supplied than there are to replace");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(c(1,2,3), f, 2) }", "invalid subscript type 'closure'");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(c(1,2,3), \"hello\", 2) }", "            hello\n1.0 2.0 3.0   2.0");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(x=3)),c(\"b\",\"x\"),10) }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n$b\n$b$x\n10.0");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(x=3)),character(),10) }", "attempt to select less than one element");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=c(x=3)),c(\"b\",\"x\"),10) }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n$b\n   x\n10.0");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(c(1,2,b=c(x=3)),c(\"b\",\"x\"),10) }", "attempt to select more than one element");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(c(1,2,b=c(x=3)),c(\"b\"),10) }", "        b.x    b\n1.0 2.0 3.0 10.0");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2, list(3)),c(\"b\",\"x\"),10) }", "no such index at level 1");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(3)),c(\"a\",\"x\"),10) }", "no such index at level 1");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(a=list(x=1,y=2),3),4),c(\"b\",\"a\",\"x\"),10) }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n$b\n$b$a\n$b$a$x\n10.0\n\n$b$a$y\n2.0\n\n$b[[2]]\n3.0\n\n[[4]]\n4.0");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=f),c(\"b\",\"x\"),3) }", "object of type 'closure' is not subsettable");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=2),\"b\",NULL) }", "$a\n1.0");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(c(a=1,b=2),\"b\",NULL) }", "more elements supplied than there are to replace");
    assertEval("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=list(2)),\"b\",double()) }", "$a\n1.0\n\n$b\nnumeric(0)");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=c(a=2)),c(\"b\",\"a\"),1:3) }", "more elements supplied than there are to replace");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=c(a=2)),1+2i,1:3) }", "invalid subscript type 'complex'");
    assertEval(" { f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(list(a=1,b=c(a=2)),c(TRUE,TRUE),3) }", "$a\n3.0\n\n$b\n  a\n2.0");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(f,TRUE,3) }", "object of type 'closure' is not subsettable");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(c(a=1,b=2),\"b\",as.raw(12)) }", "incompatible types (from raw to double) in subassignment type fix");
    assertEvalError("{ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c(\"b\",\"a\"),10) ; f(c(a=1,b=2),c(1+2i,3+4i),as.raw(12)) }", "invalid subscript type 'complex'");
    assertEval("{ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c(2,2,3,2)]] <- 10 ; l }", "$a\n1.0\n\n$b\n2.0\n\n$cd\n$cd$c\n3.0\n\n$cd$d\n4.0");
    assertEval("{ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c(\"xy\",\"y\",\"cd\",\"d\")]] <- 10 ; l }", "$a\n1.0\n\n$b\n2.0\n\n$cd\n$cd$c\n3.0\n\n$cd$d\n4.0");
  }


  @Test
  public void testStringUpdate()  {
    assertEval("{ a <- 'hello'; a[[5]] <- 'done'; a[[3]] <- 'muhuhu'; a; }", "\"hello\", NA, \"muhuhu\", NA, \"done\"");
    assertEval("{ a <- 'hello'; a[[5]] <- 'done'; b <- a; b[[3]] <- 'muhuhu'; b; }", "\"hello\", NA, \"muhuhu\", NA, \"done\"");

    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(1:3,\"a\",4) }", "              a\n1.0 2.0 3.0 4.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(NULL,\"a\",4) }", "  a\n4.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(NULL,c(\"a\",\"X\"),4:5) }", " a  X\n4L 5L");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(double(),c(\"a\",\"X\"),4:5) }", "  a   X\n4.0 5.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(double(),c(\"a\",\"X\"),list(3,TRUE)) }", "$a\n3.0\n\n$X\nTRUE");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.raw(11:13),c(\"a\",\"X\"),list(3,TRUE)) }", "[[1]]\n0b\n\n[[2]]\n0c\n\n[[3]]\n0d\n\n$a\n3.0\n\n$X\nTRUE");
    assertEval("{ b <- c(11,12) ; b[\"\"] <- 100 ; b }", "               \n11.0 12.0 100.0"); // note the whitespace
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(1,a=2),c(\"a\",\"X\",\"a\"),list(3,TRUE,FALSE)) }", "[[1]]\n1.0\n\n$a\nFALSE\n\n$X\nTRUE");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"a\",\"X\",\"a\"),list(3,TRUE,FALSE)) }", "$X\nTRUE\n\n$a\nFALSE");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.raw(c(13,14)),c(\"a\",\"X\",\"a\"),c(3,TRUE,FALSE)) }", "incompatible types (from double to raw) in subassignment type fix");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),as.character(NA),as.complex(23)) }", "                         <NA>\n13.0+0.0i 14.0+0.0i 23.0+0.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),character(),as.complex(23)) }", "13.0+0.0i, 14.0+0.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),c(\"\",\"\",\"\"),as.complex(23)) }", "                                                 \n13.0+0.0i 14.0+0.0i 23.0+0.0i 23.0+0.0i 23.0+0.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),c(\"\",\"\",NA),as.complex(23)) }", "                                             <NA>\n13.0+0.0i 14.0+0.0i 23.0+0.0i 23.0+0.0i 23.0+0.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.raw(c(13,14)),c(\"a\",\"X\",\"a\"),as.raw(23)) }", "       a  X\n0d 0e 17 17");
    assertEvalWarning("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"a\",\"X\",\"a\",\"b\"),list(3,TRUE,FALSE)) }", "$X\nTRUE\n\n$a\nFALSE\n\n$b\n3.0", "number of items to replace is not a multiple of replacement length");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),list(3,TRUE,FALSE)) }", "$X\n3.0\n\n$a\n2.0\n\n$b\nTRUE\n\n$<NA>\nFALSE");
    assertEvalError("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),as.raw(10)) }", "incompatible types (from raw to double) in subassignment type fix");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),as.complex(10)) }", "        X        a         b      <NA>\n10.0+0.0i 2.0+0.0i 10.0+0.0i 10.0+0.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),1:3) }", "  X   a   b <NA>\n1.0 2.0 2.0  3.0");
    assertEvalWarning("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(c(X=1,a=2),c(\"X\",\"b\",NA),c(TRUE,NA)) }", "  X   a  b <NA>\n1.0 2.0 NA  1.0", "number of items to replace is not a multiple of replacement length");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(c(X=1L,a=2L),c(\"X\",\"b\",NA),c(TRUE,NA,FALSE)) }", " X  a  b <NA>\n1L 2L NA   0L");
    assertEvalError("{ f <- function(b, i, v) { b[[i]] <- v ; b } ; f(1+2i,3:1,4:6) ; f(c(X=1L,a=2L),c(\"X\",\"b\",NA),NULL) }", "attempt to select more than one element");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(list(X=1L,a=2L),c(\"X\",\"b\",NA),NULL) }", "$a\n2L");

    assertEval("{ b <- c(a=1+2i,b=3+4i) ; dim(b) <- c(2,1) ; b[c(\"a\",\"b\")] <- 3+1i ; b }", "                         a        b\n1.0+2.0i 3.0+4.0i 3.0+1.0i 3.0+1.0i");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; b <- list(1L,2L) ; attr(b,\"my\") <- 21 ; f(b,c(\"X\",\"b\",NA),NULL) }", "[[1]]\n1L\n\n[[2]]\n2L\nattr(,\"my\")\n21.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; b <- list(b=1L,2L) ; attr(b,\"my\") <- 21 ; f(b,c(\"X\",\"b\",NA),NULL) }", "[[1]]\n2L\nattr(,\"my\")\n21.0");
    assertEval("{ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; b <- list(b=1L,2L) ; attr(b,\"my\") <- 21 ; f(b,c(\"ZZ\",\"ZZ\",NA),NULL) }", "$b\n1L\n\n[[2]]\n2L\nattr(,\"my\")\n21.0");

    assertEval("{ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[c(\"hello\",\"hi\")] <- NULL ; b }", "[[1]]\n1.0+2.0i\n\n[[2]]\n3.0+4.0i");
  }

  @Test
  public void testGenericUpdate()  {
    assertEval("{ a <- TRUE; a[[2]] <- FALSE; a; }", "TRUE, FALSE");
  }

  @Test
  public void testSuperUpdate()  {
    assertEval("{ x <- 1:3 ; f <- function() { x[2] <<- 100 } ; f() ; x }", "1.0, 100.0, 3.0");
    assertEval("{ x <- 1:3 ; f <- function() { x[2] <- 10 ; x[2] <<- 100 ; x[2] <- 1000 } ; f() ; x }", "1.0, 100.0, 3.0");
  }

  @Test
  public void testMatrixIndex()  {
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[1,2] }", "3L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[1,] }", "1L, 3L, 5L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[1,,drop=FALSE] }", "     [,1] [,2] [,3]\n[1,]   1L   3L   5L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,1] }", "1L, 2L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,] }", "     [,1] [,2] [,3]\n[1,]   1L   3L   5L\n[2,]   2L   4L   6L");

    assertEval("{ m <- matrix(1:6, nrow=2) ; m[1:2,2:3] }", "     [,1] [,2]\n[1,]   3L   5L\n[2,]   4L   6L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[1:2,-1] }", "     [,1] [,2]\n[1,]   3L   5L\n[2,]   4L   6L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,-1] }", "     [,1] [,2]\n[1,]   3L   5L\n[2,]   4L   6L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,c(-1,0,0,-1)] }", "     [,1] [,2]\n[1,]   3L   5L\n[2,]   4L   6L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,c(1,NA,1,NA)] }", "     [,1] [,2] [,3] [,4]\n[1,]   1L   NA   1L   NA\n[2,]   2L   NA   2L   NA");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,1[2],drop=FALSE] }", "     [,1]\n[1,]   NA\n[2,]   NA");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[,c(NA,1,0)] }", "     [,1] [,2]\n[1,]   NA   1L\n[2,]   NA   2L");

    assertEval("{ m <- matrix(1:16, nrow=8) ; m[c(TRUE,FALSE,FALSE),c(FALSE,NA), drop=FALSE]}", "     [,1]\n[1,]   NA\n[2,]   NA\n[3,]   NA");
    assertEval("{ m <- matrix(1:16, nrow=8) ; m[c(TRUE,FALSE),c(FALSE,TRUE), drop=TRUE]}", "9L, 11L, 13L, 15L");
    assertEval("{ m <- matrix(1:16, nrow=8) ; m[c(TRUE,FALSE,FALSE),c(FALSE,TRUE), drop=TRUE]}", "9L, 12L, 15L");

    assertEval("{ m <- matrix(1:6, nrow=3) ; f <- function(i,j) { m[i,j] } ; f(1,c(1,2)) ; f(1,c(-1,0,-1,-10)) }", "4L");
    assertEval("{ m <- matrix(1:6, nrow=3) ; f <- function(i,j) { m[i,j] } ; f(1,c(1,2)) ; f(c(TRUE),c(FALSE,TRUE)) }", "4L, 5L, 6L");

    assertEval("{ m <- matrix(1:6, nrow=2) ; x<-2 ; m[[1,x]] }", "3L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[[1,2]] }", "3L");

    assertEval("{ m <- matrix(1:6, nrow=2) ; f <- function(i,j) { m[i,j] } ;  f(1,1); f(1,1:3) }", "1L, 3L, 5L");
    assertEval("{ m <- matrix(1:4, nrow=2) ; m[[2,1,drop=FALSE]] }", "2L");

    assertEval("{ m <- matrix(1:6, nrow=2) ; m[1:2,0:1] }", "1L, 2L");
    assertEval("{ m <- matrix(1:6, nrow=2) ; m[1:2,0:1] ; m[1:2,1:1] }", "1L, 2L");
  }

  @Test
  public void testIn()  {
    assertEval("{ 1:3 %in% 1:10 }", "TRUE, TRUE, TRUE");
    assertEval("{ 1 %in% 1:10 }", "TRUE");
    assertEval("{ c(\"1L\",\"hello\") %in% 1:10 }", "TRUE, FALSE");
    assertEval("{ (1 + 2i) %in% c(1+10i, 1+4i, 2+2i, 1+2i) }", "TRUE");
    assertEval("{ as.logical(-1:1) %in% TRUE }", "TRUE, FALSE, TRUE");
    assertEvalError("{ x <- function(){1} ; x %in% TRUE }", "'match' requires vector arguments");
  }

  @Test
  public void testEmptyUpdate()  {
    assertEval("{ a <- list(); a$a = 6; a; }", "$a\n6.0");
    assertEval("{ a <- list(); a[['b']] = 6; a; }", "$b\n6.0");
  }

  @Test
  public void testFieldAccess()  {
    assertEval("{ a <- list(a = 1, b = 2); a$a; }", "1.0");
    assertEval("{ a <- list(a = 1, b = 2); a$b; }", "2.0");
    assertEval("{ a <- list(a = 1, b = 2); a$c; }", "NULL");
    assertEval("{ a <- list(a = 1, b = 2); a$a <- 67; a; }", "$a\n67.0\n\n$b\n2.0");
    assertEval("{ a <- list(a = 1, b = 2); a$b <- 67; a; }", "$a\n1.0\n\n$b\n67.0");
    assertEval("{ a <- list(a = 1, b = 2); a$c <- 67; a; }", "$a\n1.0\n\n$b\n2.0\n\n$c\n67.0");
    assertEval("{ v <- list(xb=1, b=2, aa=3, aa=4) ; v$aa }", "3.0");
    assertEval("{ x <- list(1, 2) ; x$b }", "NULL");
    assertEvalError("{ x <- list(a=1, b=2) ; f <- function(x) { x$b } ; f(x) ; f(1:3) }", "$ operator is invalid for atomic vectors");
    assertEval("{ x <- list(a=1, b=2) ; f <- function(x) { x$b } ; f(x) ; f(x) }", "2.0");
    assertEval("{ x <- list(a=1, b=2) ; f <- function(x) { x$b } ; f(x) ; x <- list(c=2,b=10) ; f(x) }", "10.0");

    // partial matching
    assertEval("{ v <- list(xb=1, b=2, aa=3, aa=4) ; v$x }", "1.0");
    assertEval("{ v <- list(xb=1, b=2, aa=3, aa=4) ; v$a }", "NULL");
    assertEval("{ f <- function(v) { v$x } ; f(list(xa=1, xb=2, hello=3)) ; f(list(y=2,x=3)) }", "3.0");

    // rewriting
    assertEval("{ f <- function(v) { v$x } ; f(list(xa=1, xb=2, hello=3)) ; l <- list(y=2,x=3) ; f(l) ; l[[2]] <- 4 ; f(l) }", "4.0");

    // make sure that dollar only works for lists
    assertEvalError("{ a <- c(a=1,b=2); a$a; }", "$ operator is invalid for atomic vectors");
    // make sure that coercion returns warning
    assertEvalWarning("{ a <- c(1,2); a$a = 3; a; }", "[[1]]\n1.0\n\n[[2]]\n2.0\n\n$a\n3.0", "Coercing LHS to a list");

    assertEval("{ l <- list(a=1,b=2,c=3) ; z <- l ; l$b <- 10 ; z }", "$a\n1.0\n\n$b\n2.0\n\n$c\n3.0");
    assertEval("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1),11) }", "$a\n1.0\n\n$z\n11.0");
    assertEval("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2,z=3),10) }", "$a\n1.0\n\n$b\n2.0\n\n$z\n10.0");
    assertEvalWarning("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(c(a=1,b=2,z=3),10) }", "$a\n1.0\n\n$b\n2.0\n\n$z\n10.0", "Coercing LHS to a list");
    assertEval("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(list(a=1,z=2),10) }", "$a\n1.0\n\n$z\n10.0");
    assertEvalWarning("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(c(a=1,z=2),10) }", "$a\n1.0\n\n$z\n10.0", "Coercing LHS to a list");
    assertEval("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(l <- list(a=1,z=2),10) }", "$a\n1.0\n\n$z\n10.0");
    assertEval("{ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(l <- list(a=1,z=2),10) ; l }", "$a\n1.0\n\n$z\n2.0");
    assertEval("{ x <- list(a=1,b=2,c=3) ; x$z <- NULL ; x }", "$a\n1.0\n\n$b\n2.0\n\n$c\n3.0");
    assertEval("{ x <- list(a=1,b=2,c=3) ; x$a <- NULL ; x }", "$b\n2.0\n\n$c\n3.0");
    assertEval("{ x <- list(a=1,b=2,c=3) ; attr(x, \"my\") <- 10 ; x$a <- NULL ; x }", "$b\n2.0\n\n$c\n3.0\nattr(,\"my\")\n10.0");
    assertEval("{ f <- function(x, v) { x$a <- v ; x } ; x <- list(a=1,b=2,c=3) ; z <- x ; f(x, 10) ; f(x,NULL) }", "$b\n2.0\n\n$c\n3.0");
  }

  @Test
  public void testDynamic()  {
    assertEval("{ l <- quote(x[1] <- 1) ; f <- function() { eval(l) } ; x <- 10 ; f() ; x }", "10.0");
    assertEval("{ l <- quote(x[1] <- 1) ; f <- function() { eval(l) ; x <<- 10 ; get(\"x\") } ; x <- 20 ; f() }", "1.0");
  }
}
