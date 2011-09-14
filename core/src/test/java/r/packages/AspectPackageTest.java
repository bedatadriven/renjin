package r.packages;

import org.junit.Ignore;
import org.junit.Test;

public class AspectPackageTest extends PackageTest {
  
  @Test
  @Ignore("too slow!")
  public void simple() {
    
    eval("library('aspect')");
    eval("data('wurzer')");
        
    eval("res.cor <- corAspect(wurzer, apsect='aspectEigen', level=c(rep('nominal',2), rep('ordinal',5), 'nominal'))");

    System.out.println( global.getVariable("res.cor") );
    
  }

}
