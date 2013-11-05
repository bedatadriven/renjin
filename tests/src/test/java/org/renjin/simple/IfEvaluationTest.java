package org.renjin.simple;

import org.junit.Test;

public class IfEvaluationTest extends SimpleTestBase {

  @Test
  public void testIf2()  {
    assertEval("if(TRUE) 1 else 2", "1.0");
    assertEval("if(FALSE) 1 else 2", "2.0");
  }

  @Test
  public void testIfNot1()  {
    assertEval("if(!FALSE) 1 else 2", "1.0");
    assertEval("if(!TRUE) 1 else 2", "2.0");
  }

  @Test
  public void testIf()  {
    assertEval("{ x <- 2 ; if (1==x) TRUE else 2 }", "2.0");
    assertEvalError("{ x <- 2 ; if (NA) x <- 3 ; x }", "missing value where TRUE/FALSE needed");
    assertEvalError("{ f <- function(x) { if (x) 1 else 2 } ; f(NA)  }", "missing value where TRUE/FALSE needed");
    assertEvalError("{ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(NA) }", "missing value where TRUE/FALSE needed");
    assertEval("{ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(TRUE) }", "1.0");
    assertEval("{ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(FALSE) }", "2.0");

    assertEvalError("{ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(\"hello\") }", "argument is not interpretable as logical");
    assertEvalError("{ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(logical()) }", "argument is of length zero");
    assertEvalWarning("{ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(1:3) }", "1.0", "the condition has length > 1 and only the first element will be used");
    assertEvalError("{ f <- function(x) { if (x==2) 1 else 2 } ; f(1) ; f(NA) }", "missing value where TRUE/FALSE needed");

    assertEval("{ if (TRUE==FALSE) TRUE else FALSE }", "FALSE");
    assertEvalError("{ if (NA==TRUE) TRUE else FALSE }", "missing value where TRUE/FALSE needed");
    assertEvalError("{ if (TRUE==NA) TRUE else FALSE }", "missing value where TRUE/FALSE needed");
    assertEval("{ if (FALSE==TRUE) TRUE else FALSE }", "FALSE");
    assertEval("{ if (FALSE==1) TRUE else FALSE }", "FALSE");
    assertEval("{ f <- function(v) { if (FALSE==v) TRUE else FALSE } ; f(TRUE) ; f(1) }", "FALSE");
  }

  @Test
  public void testCast()  {
    assertEvalWarning("{ f <- function(a) { if (is.na(a)) { 1 } else { 2 } } ; f(5) ; f(1:3)}", "2.0", "Warning in is.na(a): the condition has length > 1 and only the first element will be used");
    assertEvalWarning("{ if (1:3) { TRUE } }", "TRUE", "the condition has length > 1 and only the first element will be used");
    assertEvalError("{ if (integer()) { TRUE } }", "argument is of length zero");
    assertEvalError("{ if (1[2:1]) { TRUE } }", "argument is not interpretable as logical");
    assertEvalWarning("{ if (c(0,0,0)) { TRUE } else { 2 } }", "2.0", "the condition has length > 1 and only the first element will be used");
    assertEvalWarning("{ if (c(1L,0L,0L)) { TRUE } else { 2 } }", "TRUE", "the condition has length > 1 and only the first element will be used");
    assertEvalWarning("{ if (c(0L,0L,0L)) { TRUE } else { 2 } }", "2.0", "the condition has length > 1 and only the first element will be used");
    assertEvalError("{ if (c(1L[2],0L,0L)) { TRUE } else { 2 } }", "argument is not interpretable as logical");
    assertEvalWarning("{ f <- function(cond) { if (cond) { TRUE } else { 2 } } ; f(1:3) ; f(2) }", "TRUE", "the condition has length > 1 and only the first element will be used");
    assertEvalWarning("{ f <- function(cond) { if (cond) { TRUE } else { 2 }  } ; f(c(TRUE,FALSE)) ; f(FALSE) }", "2.0", "the condition has length > 1 and only the first element will be used");
    assertEvalError("{ f <- function(cond) { if (cond) { TRUE } else { 2 }  } ; f(logical()) }", "argument is of length zero");
    assertEvalWarning("{ f <- function(cond) { if (cond) { TRUE } else { 2 }  } ; f(c(TRUE,FALSE)) ; f(1) }", "TRUE", "the condition has length > 1 and only the first element will be used");
  }

}
