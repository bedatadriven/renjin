package org.renjin.primitives.random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;

public class SamplingTest extends EvalTestCase {

  @Test
  public void withReplacement() throws IOException {
    double delta = 0.00001;

    assumingBasePackagesLoad();
    eval("x<-1:5");
    eval("p<-c(0.00, 0.00, 0.00, 1, 0.00)");

    assertThat(eval("sample(x,1L,TRUE,p)").asReal(), closeTo(4, delta));
  }

  @Test
  public void withReplacementUniform() throws IOException {
    double delta = 0.1;
    assumingBasePackagesLoad();
    eval("x<-1:5");
    assertThat(eval("mean(sample(x, 10000L, TRUE, rep(1/5,5)))").asReal(), closeTo(3.0, delta));
  }

  @Test
  public void withoutReplacement() throws IOException {
    assumingBasePackagesLoad();
    eval("x<-c(1,2,3,4,5,10,9,8,7,6)");
    assertThat(eval("sort(sample(x, 10L, FALSE))"), equalTo(c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
  }
  
  @Test
  public void minimumParametersCall() throws IOException {
    assumingBasePackagesLoad();
    eval("x<-c(1,2,3,4,5,10,9,8,7,6)");
    assertThat(eval("sort(sample(x, 10L))"), equalTo(c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)));
  }
}
