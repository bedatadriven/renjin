package org.renjin.primitives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import r.EvalTestCase;
import r.lang.exception.EvalException;

public class VApplyTest extends EvalTestCase {

  @Before
  public void defineVapply() {
    eval("vapply <- function (X, FUN, FUN.VALUE, ..., USE.NAMES = TRUE)  " +
        " .Internal(vapply(X, FUN, FUN.VALUE, USE.NAMES))");
  }
  
  @Test
  public void vapplySimple() {
    assertThat(eval("vapply(c(4,16,64), sqrt, 1)"), equalTo(c(2,4,8)));
  }
  
  @Test
  public void vapplyWithElipses() {
    assertThat(eval("vapply(1:4, `-`, 1, 1)"), equalTo(c(0,1,2,3)));
  }
  
  @Test
  public void names() {
    assertThat(eval("names(vapply(c(a=4,b=16,c=64), sqrt, 1))"), equalTo(c("a","b","c")));
  }
  
  @Test(expected=EvalException.class)
  public void vapplyTypeProblem() {
    eval("vapply(c(4,16,64), sqrt, TRUE)");
  }
}
