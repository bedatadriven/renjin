package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class SplitTest extends EvalTestCase {

  @Test
  public void split() throws IOException {
    assumingBasePackagesLoad();
    
    eval("x <- split(c(10:1), c(1,2,1,2,1,2,1,2,3,3))");
    
    assertThat(eval("x$`1`"), equalTo(c_i(10,8,6,4)));
    assertThat(eval("x$`2`"), equalTo(c_i(9,7,5,3)));
    assertThat(eval("x$`3`"), equalTo(c_i(2,1)));
  }
  
  @Test
  public void splitWithMissing() throws IOException {
    assumingBasePackagesLoad();
    
    eval("x <- split(c(10:1), c(1,2,1,2,1,2,1,2,3,NA))");
    eval("print(x)");
    assertThat(eval("x$`1`"), equalTo(c_i(10,8,6,4)));
    assertThat(eval("x$`2`"), equalTo(c_i(9,7,5,3)));
    assertThat(eval("x$`3`"), equalTo(c_i(2)));
  }
  
  @Test
  public void splitWithNames() {
    assumingBasePackagesLoad();
    
    eval("x <- split(c(a=1,b=2), c(1,2))");
    assertThat(eval("names(x)"), equalTo(c("1", "2")));
    assertThat(eval("names(x[[1]])"), equalTo(c("a")));
  }

  @Test
  public void split1dArrayWithNames() {
    assumingBasePackagesLoad();

    eval("a <- 1:2");
    eval("dim(a) <- 2");
    eval("names(a) <- c('x','y')");
    
    eval("x <- split(a, c(1,2))");
    assertThat(eval("names(x)"), equalTo(c("1", "2")));
    assertThat(eval("names(x[[1]])"), equalTo(c("x")));
  }
  
}
