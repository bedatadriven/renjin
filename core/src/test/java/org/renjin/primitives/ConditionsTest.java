package org.renjin.primitives;


import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConditionsTest extends EvalTestCase {

  @Test
  @Ignore("not yet impl")
  public void catchStop() {
    assumingBasePackagesLoad();
    eval("x <- tryCatch(stop('foo'), error = function(...) 41) ");
    assertThat(eval("x"), equalTo(c(42)));
  }
}
