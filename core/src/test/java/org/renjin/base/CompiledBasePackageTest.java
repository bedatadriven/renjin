package org.renjin.base;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.compiler.runtime.PackageLoader;
import org.renjin.sexp.Environment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CompiledBasePackageTest extends EvalTestCase {

  @Test
  @Ignore("WIP")
  public void testLoad() throws Exception {
    
    Class<PackageLoader> loaderClass = (Class<PackageLoader>) Class.forName("org.renjin.base.Loader");
    PackageLoader loader = loaderClass.newInstance();
    
    Environment baseEnv = topLevelContext.getEnvironment().getBaseEnvironment();
    loader.load(topLevelContext, baseEnv);
    
    assertThat(baseEnv.getVariable("xor").force(topLevelContext).getClass().getSimpleName(), equalTo("xor"));
    assertThat(eval("xor(TRUE,TRUE)"), equalTo(c(false)));
  }
  
}
