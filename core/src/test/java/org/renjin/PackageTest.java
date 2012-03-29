package org.renjin;

import java.io.IOException;

import org.junit.Before;


public abstract class PackageTest extends EvalTestCase {
  
  @Before
  public void initContext() throws IOException {
    assumingBasePackagesLoad();
  }
  

}
