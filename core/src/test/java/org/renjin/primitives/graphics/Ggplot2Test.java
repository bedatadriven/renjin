package org.renjin.primitives.graphics;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;

public class Ggplot2Test extends EvalTestCase {

  @Before
  public void setup() throws IOException {
    topLevelContext.init();
    eval("library(ggplot2, lib.loc='src/test/resources/')");
  }
  
  @Ignore("not yet working")
  @Test
  public void test() {
    eval("data(mtcars)");
    eval("print(mtcars)");
    eval("c <- ggplot(mtcars, aes(factor(cyl)))");
    eval("c + geom_bar()");
  }
  
}

