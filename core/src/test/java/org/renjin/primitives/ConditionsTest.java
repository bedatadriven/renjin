package org.renjin.primitives;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.renjin.EvalTestCase;

public class ConditionsTest extends EvalTestCase {

  @Test
  public void catchStop() {
    assumingBasePackagesLoad();
    assertThat(eval("tryCatch(stop('foo'), error = function(...) 41)"), equalTo(c(41)));
    assertThat(eval("tryCatch(nonExistantVar + 1, error = function(...) 42)"), equalTo(c(42)));
  }
}
