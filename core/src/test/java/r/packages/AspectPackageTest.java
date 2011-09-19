package r.packages;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

public class AspectPackageTest extends PackageTest {
  
  @Test
  @Ignore("not working")
  public void simple() {
    
    eval("library('aspect')");
    eval("data('wurzer')");
        
    assertThat(eval("typeof(wurzer[,1])"), equalTo(c("double")));
    
    eval("res.cor <- corAspect(wurzer, apsect='aspectEigen', level=c(rep('nominal',2), rep('ordinal',5), 'nominal'))");

    System.out.println( global.getVariable("res.cor") );
    
  }

}
