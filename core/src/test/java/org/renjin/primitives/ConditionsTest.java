package org.renjin.primitives;


import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConditionsTest extends EvalTestCase {

  @Test
  public void catchStop() {
    assumingBasePackagesLoad();
    assertThat(eval("tryCatch(stop('foo'), error = function(...) 41)"), equalTo(c(41)));
    assertThat(eval("tryCatch(nonExistantVar + 1, error = function(...) 42)"), equalTo(c(42)));
  }
  
  @Test
  public void supressWarnings() {
    assumingBasePackagesLoad();
    
    eval("x <- suppressWarnings({ warning('foo'); 42 })");
   
    assertThat(eval("x"), equalTo(c(42)));
  }
}
