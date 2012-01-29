package r.packages;

import java.io.IOException;

import org.junit.Before;

import r.EvalTestCase;

public abstract class PackageTest extends EvalTestCase {
  
  @Before
  public void initContext() throws IOException {
    assumingBasePackagesLoad();
  }
  

}
