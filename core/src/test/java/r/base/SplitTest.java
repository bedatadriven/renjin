package r.base;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import r.EvalTestCase;

public class SplitTest extends EvalTestCase {

  @Test
  public void split() throws IOException {
    topLevelContext.init();
    eval("x <- split(c(10:1), c(1,2,1,2,1,2,1,2,3,3))");
    
    assertThat(eval("x$`1`"), equalTo(c_i(10,8,6,4)));
    assertThat(eval("x$`2`"), equalTo(c_i(9,7,5,3)));
    assertThat(eval("x$`3`"), equalTo(c_i(2,1)));
  }
  
  
}
