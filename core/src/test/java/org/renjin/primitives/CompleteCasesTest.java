package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class CompleteCasesTest extends EvalTestCase {

  @Test
  public void test() {
    assertThat(eval(".Internal(complete.cases(1:3, 1:3))"), equalTo(c(true, true, true)));
    assertThat(eval(".Internal(complete.cases(1:3, c(1,NA,2)))"), equalTo(c(true, false, true)));

  }
}
