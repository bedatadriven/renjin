package org.renjin.stats.internals;

import org.junit.Test;
import org.renjin.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CovarianceTest extends EvalTestCase {


  @Test
  public void covDimTest() {
    
    eval("x <- 1:344");
    eval("y <- .Internal(cov(x, NULL, 1, FALSE))");
    
    assertThat(eval("dim(y)"), equalTo(NULL));
  }

}