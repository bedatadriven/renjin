package r.base;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import r.EvalTestCase;

public class GraphicsTest extends EvalTestCase {
  
  
  @Test
  @Ignore("work in progress")
  public void simplestPossible() throws IOException {
    topLevelContext.init();
    eval("barplot(c(1,2,3), main='Distribution', xlab='Number')");
  }

}
