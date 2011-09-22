package r.packages; 

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import r.lang.FunctionCall;

public class AspectPackageTest extends PackageTest {
  
  @Test
  public void simple() {
    
    eval("library('aspect')");
    eval("data('wurzer')");

    assertThat(eval("typeof(wurzer[,1])"), equalTo(c("double")));
    
    //eval("environment(corAspect)$expandFrame(wurzer)");
    
   // eval("res.cor <- corAspect(wurzer, aspect='aspectSum', level=c(rep('nominal',2), rep('ordinal',5), 'nominal'))");
    //System.out.println( global.getVariable("res.cor") );
    
  }

  
}
