package org.renjin;


import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class StatsPackageTest extends  EvalTestCase {

  @Test
  public void dist() {
    assumingBasePackagesLoad();

    eval("print(dist(1:10))");
  }

  @Test
  public void asMatrix() {
    assumingBasePackagesLoad();

    eval("m <- as.matrix(dist(1:10))");

    assertThat(eval("dim(m)"), equalTo(c_i(10,10)));
  }

}
