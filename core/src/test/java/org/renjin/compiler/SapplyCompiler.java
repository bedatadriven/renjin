package org.renjin.compiler;


import org.junit.Test;
import org.renjin.EvalTestCase;

public class SapplyCompiler extends EvalTestCase {

  @Test
  public void simple() {

    eval("x <- sapply(1:1e6, function(x) x*2 ");
  }


}
