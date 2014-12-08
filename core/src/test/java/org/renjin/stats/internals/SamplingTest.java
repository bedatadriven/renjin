package org.renjin.stats.internals;

import org.junit.Test;
import org.renjin.EvalTestCase;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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

  @Test
  public void testWithoutReplacement() throws IOException {
    assumingBasePackagesLoad();
    eval("set.seed(12345);");
    assertThat(eval("sample(c(1,2,3,4,5,6,7,8,9,10), 10)"), equalTo(c(8, 10, 7, 9, 3, 1, 2, 4, 6, 5)));
  }

  @Test
  public void testWithReplacement() throws IOException {
    assumingBasePackagesLoad();
    eval("set.seed(12345);");
    assertThat(eval("sample(c(1,2,3,4,5,6,7,8,9,10), replace = T)"), equalTo(c(8, 9, 8, 9, 5, 2, 4, 6, 8, 10)));
  }

  @Test
  public void testWithProb() throws IOException {
    assumingBasePackagesLoad();
    eval("set.seed(12345);");
    assertThat(eval("sample(c(1,2,3,4), prob = c(0.1, 0.15, 0.25, 0.50))"), equalTo(c(3, 1, 4, 2)));
  }

  @Test
  public void testWithWeights() throws IOException {
    assumingBasePackagesLoad();
    eval("set.seed(12345);");
    assertThat(eval("sample(c(1,2,3,4), prob = c(1, 100, 10, 7))"), equalTo(c(2,4,3,1)));
  }

  @Test
  public void testSmallSample() throws IOException {
    assumingBasePackagesLoad();
    eval("set.seed(12345);");
    assertThat(eval("sample(c(20,30,40,38,27,29,32,100,24), 5)"), equalTo(c(32, 100, 29, 24, 40)));
  }
}
