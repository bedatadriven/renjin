package org.renjin.compiler;

import org.easymock.internal.ThrowableWrapper;
import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class LoopCompilerTest extends EvalTestCase {
  
  @Test
  public void repeatLoop() {
    eval("s <- 0");
    eval("i <- 0");
    eval("repeat { s <- s + i; i <- i+1; if(i > 10000) break; }");
    
    assertThat(eval("s"), equalTo(c(50005000d)));
    assertThat(eval("i"), equalTo(c(10001)));
  }


  @Test
  public void whileLoop() {
    try {
      eval("s <- 0");
      eval("i <- 0");
      eval("while(i <= 10000) { s <- s + i; i <- i+1 }");

      assertThat(eval("s"), equalTo(c(50005000d)));
      assertThat(eval("i"), equalTo(c(10001)));
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
}
