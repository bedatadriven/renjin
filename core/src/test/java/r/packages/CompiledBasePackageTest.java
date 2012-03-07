package r.packages;

import org.junit.Ignore;
import org.junit.Test;

import r.EvalTestCase;
import r.compiler.runtime.PackageLoader;
import r.lang.Context;
import r.lang.Environment;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class CompiledBasePackageTest extends EvalTestCase {

  @Test
  @Ignore("WIP")
  public void testLoad() throws Exception {
    
    Class<PackageLoader> loaderClass = (Class<PackageLoader>) Class.forName("org.renjin.base.Loader");
    PackageLoader loader = loaderClass.newInstance();
    
    Environment baseEnv = topLevelContext.getEnvironment().getBaseEnvironment();
    loader.load(topLevelContext, baseEnv);
    
    assertThat(baseEnv.getVariable("xor").force().getClass().getSimpleName(), equalTo("xor"));
    assertThat(eval("xor(TRUE,TRUE)"), equalTo(c(false)));
  }
  
}
