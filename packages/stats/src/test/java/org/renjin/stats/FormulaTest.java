package org.renjin.stats;

import org.junit.Test;
import org.renjin.eval.Context;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FormulaTest extends EvalTestCase {

  @Test
  public void test() throws IOException {


    eval("f <- g <- ~a");
    eval("f[[2]] <- f[[2]]");
    assertThat(eval("class(f)"), equalTo(c("formula")));
  }

}
