package org.renjin.stats.internals;

import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CompleteCasesTest extends EvalTestCase {

  @Test
  public void test() {
    assertThat(eval(".Internal(complete.cases(1:3, 1:3))"), equalTo(c(true, true, true)));
    assertThat(eval(".Internal(complete.cases(1:3, c(1,NA,2)))"), equalTo(c(true, false, true)));
    assertThat(eval(".Internal(complete.cases(list(1:3,1:3), 1:3, 1:3))"), 
        equalTo(c(true, true, true)));
  }
  
  @Test
  public void matrices() {
    eval("x <- matrix(1:8, nrow=4)");
    assertThat(eval(".Internal(complete.cases(x))"), equalTo(c(true, true, true, true)));

  }
}
