package org.renjin.stats;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ModelFrameTest extends EvalTestCase {

  @Test
  public void modelFrame() {
    
    eval("f <- function(x) 2*x ");
    eval("x <- 1:10 ");
    eval("y <- 1:10 ");
    
    eval("formula <- f(y) ~ x");
    eval("print(model.frame(formula))");
    
  }
  
  @Test
  public void simpleModelMatrix() {
    eval("x <- 1:10 ");
    eval("y <- 10:1 ");
    eval("z <- rnorm(10) ");
    
    eval("print(mm <- model.matrix(~x+y+z))");
    assertThat(eval("attr(mm, 'assign')"), equalTo(c_i(0, 1, 2, 3)));
  }
  
  @Test
  public void modelMatrixFactors() {
    eval("x <- c('Good', 'Bad', 'Ugly', 'Good') ");
    
    eval("print(mm <- model.matrix(~x))");
    eval("print(attributes(mm))");
    assertThat(eval("mm[,2]"), equalTo(c(1, 0, 0, 1)));
    assertThat(eval("mm[,3]"), equalTo(c(0, 0, 1, 0)));
    assertThat(eval("colnames(mm)"), equalTo(c("(Intercept)", "xGood", "xUgly")));
    assertThat(eval("attr(mm, 'contrasts')$x"), equalTo(c("contr.treatment")));
  }
  
  @Test
  public void simpleInteraction() {
    eval("x <- 1:10 ");
    eval("y <- 10:1 ");
    
    eval("print(mm <- model.matrix(~x*y))"); 
  }
 
  
  @Test
  public void interactionWithFactors() {
    eval("x <- 1:10 ");
    eval("y <- rep(c('Good', 'Bad', 'Ugly'), length=10) ");
    
    eval("print(mm <- model.matrix(~x*y))"); 
  }
  
  @Test
  public void dotInFormula() {
    eval("df <- data.frame(x=1:3,y=(1:3)*2, z=(1:3)*6)");
    
    eval("model.matrix(x ~ ., data = df)");
    
  }
  
}
