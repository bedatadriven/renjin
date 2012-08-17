package org.renjin;


import org.junit.Test;

public class StatsPackageTest extends  EvalTestCase {

  @Test
  public void dist() {
    assumingBasePackagesLoad();

    eval("print(dist(1:10))");
  }

}
