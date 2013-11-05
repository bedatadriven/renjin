package org.renjin.simple;

import org.junit.Test;

// NOTE: some tests relating to attributes are also in BuiltinsTest.testAttributes
public class AttributesTest extends SimpleTestBase {

  @Test
  public void testDefinition() {
    assertEval("{ x <- as.raw(10) ; attr(x, \"hi\") <- 2 ;  x }", "0a\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- TRUE ; attr(x, \"hi\") <- 2 ;  x }", "TRUE\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- 1L ; attr(x, \"hi\") <- 2 ;  x }", "1L\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- 1 ; attr(x, \"hi\") <- 2 ;  x }", "1.0\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- 1+1i ; attr(x, \"hi\") <- 2 ;  x }", "1.0+1.0i\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- \"s\" ; attr(x, \"hi\") <- 2 ;  x }", "\"s\"\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- c(1L, 2L) ; attr(x, \"hi\") <- 2; x }", "1L, 2L\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- c(1, 2) ; attr(x, \"hi\") <- 2; x }", "1.0, 2.0\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- c(1L, 2L) ; attr(x, \"hi\") <- 2; attr(x, \"hello\") <- 1:2 ;  x }", "1L, 2L\nattr(,\"hi\")\n2.0\nattr(,\"hello\")\n1L, 2L");

    assertEval("{ x <- c(hello=9) ; attr(x, \"hi\") <- 2 ;  y <- x ; y }", "hello\n  9.0\nattr(,\"hi\")\n2.0");

    assertEval("{ x <- c(hello=1) ; attr(x, \"hi\") <- 2 ;  attr(x,\"names\") <- \"HELLO\" ; x }", "HELLO\n  1.0\nattr(,\"hi\")\n2.0");
  }

  @Test
  public void testArithmeticPropagation()  {
    assertEval("{ x <- 1:2;  attr(x, \"hi\") <- 2 ;  x+1:4 }", "2L, 4L, 4L, 6L");
    assertEval("{ x <- c(1+1i,2+2i);  attr(x, \"hi\") <- 3 ; y <- 2:3 ; attr(y,\"zz\") <- 2; x+y }", "3.0+1.0i, 5.0+2.0i\nattr(,\"zz\")\n2.0\nattr(,\"hi\")\n3.0");
    assertEval("{ x <- 1+1i;  attr(x, \"hi\") <- 1+2 ; y <- 2:3 ; attr(y,\"zz\") <- 2; x+y }", "3.0+1.0i, 4.0+1.0i\nattr(,\"zz\")\n2.0");
    assertEval("{ x <- c(1+1i, 2+2i) ;  attr(x, \"hi\") <- 3 ; attr(x, \"hihi\") <- 10 ; y <- c(2+2i, 3+3i) ; attr(y,\"zz\") <- 2; attr(y,\"hi\") <-3; attr(y,\"bye\") <- 4 ; x+y }", "3.0+3.0i, 5.0+5.0i\nattr(,\"zz\")\n2.0\nattr(,\"hi\")\n3.0\nattr(,\"bye\")\n4.0\nattr(,\"hihi\")\n10.0");
    assertEval("{ x <- 1+1i;  attr(x, \"hi\") <- 1+2 ; y <- 2:3 ;  x+y }", "3.0+1.0i, 4.0+1.0i");
    assertEval("{ x <- 1 ; attr(x, \"my\") <- 2; 2+x }", "3.0\nattr(,\"my\")\n2.0");

    assertEval("{ x <- c(a=1) ; y <- c(b=2,c=3) ; x + y }", "  b   c\n3.0 4.0");
    assertEval("{ x <- c(a=1) ; y <- c(b=2,c=3) ; y + x }", "  b   c\n3.0 4.0");

    assertEval("{ x <- 1:2;  attr(x, \"hi\") <- 2 ;  x+1 }", "2.0, 3.0\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- 1:2;  attr(x, \"hi\") <- 2 ; y <- 2:3 ; attr(y,\"hello\") <- 3; x+y }", "3L, 5L\nattr(,\"hello\")\n3.0\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- 1;  attr(x, \"hi\") <- 1+2 ; y <- 2:3 ; attr(y, \"zz\") <- 2; x+y }", "3.0, 4.0\nattr(,\"zz\")\n2.0");
    assertEval("{ x <- 1:2 ;  attr(x, \"hi\") <- 3 ; attr(x, \"hihi\") <- 10 ; y <- 2:3 ; attr(y,\"zz\") <- 2; attr(y,\"hi\") <-3; attr(y,\"bye\") <- 4 ; x+y }", "3L, 5L\nattr(,\"zz\")\n2.0\nattr(,\"hi\")\n3.0\nattr(,\"bye\")\n4.0\nattr(,\"hihi\")\n10.0");

    assertEval("{ x <- c(a=1,b=2) ;  attr(x, \"hi\") <- 2 ;  -x  }", "   a    b\n-1.0 -2.0\nattr(,\"hi\")\n2.0");

    assertEval("{ x <- 1:2;  attr(x, \"hi\") <- 2 ;  x & x }", "TRUE, TRUE");
    assertEval("{ x <- as.raw(1:2);  attr(x, \"hi\") <- 2 ;  x & x }", "01, 02");
    assertEval("{ x <- 1:2 ;  attr(x, \"hi\") <- 2 ;  !x  }", "FALSE, FALSE");
    assertEval("{ x <- c(a=FALSE,b=TRUE) ;  attr(x, \"hi\") <- 2 ;  !x  }", "   a     b\nTRUE FALSE");
  }

  @Test
  public void testCasts() {
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1 ; as.character(x) }", "\"1.0\", \"2.0\"");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1 ; as.double(x) }", "1.0, 2.0");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1 ; as.integer(x) }", "1L, 2L");
  }

  @Test
  public void testArrayPropagation()  {
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; x[c(1,1)] }", "  a   a\n1.0 1.0");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; x[\"a\"] <- 2 ; x }", "  a   b\n2.0 2.0\nattr(,\"myatt\")\n1.0");
    assertEval("{ x <- c(a=TRUE, b=FALSE) ; attr(x, \"myatt\") <- 1; x[2] <- 2 ; x }", "  a   b\n1.0 2.0\nattr(,\"myatt\")\n1.0");
    assertEval("{ x <- TRUE ; attr(x, \"myatt\") <- 1; x[2] <- 2 ; x }", "1.0, 2.0\nattr(,\"myatt\")\n1.0");
    assertEval("{ x <- TRUE ; attr(x, \"myatt\") <- 1; x[1] <- 2 ; x }", "2.0\nattr(,\"myatt\")\n1.0");
    assertEval("{ m <- matrix(rep(1,4), nrow=2) ; attr(m, \"a\") <- 1 ;  m[2,2] <- 1+1i ; m }", "         [,1]     [,2]\n[1,] 1.0+0.0i 1.0+0.0i\n[2,] 1.0+0.0i 1.0+1.0i\nattr(,\"a\")\n1.0");
    assertEval("{ a <- array(c(1,1), dim=c(1,2)) ; attr(a, \"a\") <- 1 ;  a[1,1] <- 1+1i ; a }", "         [,1]     [,2]\n[1,] 1.0+1.0i 1.0+0.0i\nattr(,\"a\")\n1.0");
  }

  @Test
  public void testBuiltinPropagation()  {
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1 ; abs(x) }", "  a   b\n1.0 2.0\nattr(,\"myatt\")\n1.0");
    assertEval("{ m <- matrix(1:6, nrow=2) ; attr(m,\"a\") <- 1 ;  aperm(m) }", "     [,1] [,2]\n[1,]   1L   2L\n[2,]   3L   4L\n[3,]   5L   6L");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1 ; sapply(1:2, function(z) {x}) }", "     [,1] [,2]\n[1,]  1.0  1.0\n[2,]  2.0  2.0");
    assertEval("{ x <- c(a=1) ; attr(x, \"myatt\") <- 1 ; lapply(1:2, function(z) {x}) }", "[[1]]\n  a\n1.0\nattr(,\"myatt\")\n1.0\n\n[[2]]\n  a\n1.0\nattr(,\"myatt\")\n1.0");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; array(x) }", "[1] 1.0 2.0");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; matrix(x) }", "     [,1]\n[1,]  1.0\n[2,]  2.0");
    assertEval("{ x <- \"a\" ; attr(x, \"myatt\") <- 1; toupper(x) }", "\"A\"\nattr(,\"myatt\")\n1.0");
    assertEval("{ x <- 1 ; attr(x, \"myatt\") <- 1; x:x }", "1L");
    assertEval("{ x <- 1 ; attr(x, \"myatt\") <- 1; c(x, x, x) }", "1.0, 1.0, 1.0");
    assertEval("{ x <- 1 ; attr(x, \"myatt\") <- 1; cumsum(c(x, x, x)) }", "1.0, 2.0, 3.0");
    assertEval("{ m <- matrix(1:6, nrow=2) ; attr(m,\"a\") <- 1 ;  diag(m) <- c(1,1) ; m }", "     [,1] [,2] [,3]\n[1,]  1.0  3.0  5.0\n[2,]  2.0  1.0  6.0\nattr(,\"a\")\n1.0");
    assertEval("{ m <- matrix(c(1,1,1,1), nrow=2) ; attr(m,\"a\") <- 1 ;  r <- eigen(m) ; r$vectors <- round(r$vectors, digits=5) ; r  }", "$values\n2.0, 0.0\n\n$vectors\n        [,1]     [,2]\n[1,] 0.70711 -0.70711\n[2,] 0.70711  0.70711");
    assertEval("{ x <- 1 ; attr(x, \"myatt\") <- 1; round(exp(x), digits=5) }", "2.71828\nattr(,\"myatt\")\n1.0");
    assertEval("{ x <- 1 ; attr(x, \"myatt\") <- 1; min(x) }", "1.0");
    assertEval("{ x <- c(a=1) ; attr(x, \"myatt\") <- 1; log10(x) }", "  a\n0.0\nattr(,\"myatt\")\n1.0");
    assertEval("{ x <- c(a=1) ; attr(x, \"myatt\") <- 1; nchar(x) }", " a\n3L"); // specific to FAST-R debugging format
    assertEval("{ x <- 1 ; attr(x, \"myatt\") <- 1; x%o%x }", "     [,1]\n[1,]  1.0");
    assertEval("{ x <- 1 ; attr(x, \"myatt\") <- 1; rep(x,2) }", "1.0, 1.0");
    assertEval("{ x <- c(a=TRUE) ; attr(x, \"myatt\") <- 1; rep(x,2) }", "   a    a\nTRUE TRUE");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; rev(x) }", "  b   a\n2.0 1.0");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; seq(x) }", "1L, 2L");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; order(x) }", "1L, 2L");
    assertEval("{ x <- c(hello=1, hi=9) ; attr(x, \"hi\") <- 2 ;  sqrt(x) }", "hello  hi\n  1.0 3.0\nattr(,\"hi\")\n2.0");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; sum(x) }", "3.0");
    assertEval("{ m <- matrix(1:6, nrow=2) ; attr(m,\"a\") <- 1 ;  t(m) }", "     [,1] [,2]\n[1,]   1L   2L\n[2,]   3L   4L\n[3,]   5L   6L\nattr(,\"a\")\n1.0");
    assertEval("{ m <- 1:3 ; attr(m,\"a\") <- 1 ;  t(m) }", "     [,1] [,2] [,3]\n[1,]   1L   2L   3L\nattr(,\"a\")\n1.0");
    assertEval("{ m <- matrix(rep(1,4), nrow=2) ; attr(m,\"a\") <- 1 ;  upper.tri(m) }", "      [,1]  [,2]\n[1,] FALSE  TRUE\n[2,] FALSE FALSE");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; unlist(x) }", "  a   b\n1.0 2.0\nattr(,\"myatt\")\n1.0");
    assertEval("{ x <- c(a=1, b=2) ; attr(x, \"myatt\") <- 1; unlist(list(x,x)) }", "  a   b   a   b\n1.0 2.0 1.0 2.0");
  }

  @Test
  public void testOtherPropagation()  {
    assertEval("{ x <- 1:2;  attr(x, \"hi\") <- 2 ;  x == x }", "TRUE, TRUE");
  }
}
