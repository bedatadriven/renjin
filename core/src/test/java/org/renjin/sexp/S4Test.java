package org.renjin.sexp;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;

public class S4Test extends EvalTestCase {

  @Before
  public void setUpS4() throws IOException {
    topLevelContext.init();
  }
  
  @Ignore("Not quiiitee working.")
  @Test
  public void firstTest() {
    eval("library(methods)");
    eval("print(search())");
    eval("setClass('BMI', representation(weight='numeric', size='numeric'))");
   
    
  }
}
