package org.renjin.stats;

import org.junit.Test;

public class ModelFrameTest extends EvalTestCase {

  @Test
  public void modelFrame() {
    
    eval("f <- function(x) 2*x ");
    eval("x <- 1:10 ");
    eval("y <- 1:10 ");
    
    eval("formula <- f(y) ~ x");
    eval("print(model.frame(formula))");
    
    
    
  }
  
}
