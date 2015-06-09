
package org.renjin;

import org.junit.Test;

public class DebugTestCase extends EvalTestCase{

  @Test
  public void debugEvalTest() {
    eval("foo <- function(x) sqrt(x)");
    eval("bar <- function(y, b) sum(y)*b");
    eval("z <- 1:100");
    eval("b <- 42");
    eval("togglehorriddebug()");
    eval("a <- bar(foo(z), b)");
  }
}
