package r.packages; 

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

public class AspectPackageTest extends PackageTest {
  
  @Test
  @Ignore("not __quite_ working...")
  public void simple() {
    
    eval("library('aspect')");
    eval("data('wurzer')");
    
   
    eval("res.cor <- corAspect(wurzer, aspect='aspectSum', level=c(rep('nominal',2), rep('ordinal',5), 'nominal'), itmax=1)");
    eval("print(res.cor)");
    
    assertThat( eval("res.cor$catscores$know_location"), closeTo(matrix(
        row(0.7621564),
        row(-1.3120667)), 0.0001));
    
    assertThat( eval("res.cor$catscores$used_terminal"), closeTo(matrix(
        row( 0.5361205 ),
        row(-1.8652524 )), 0.0001));

    assertThat( eval("res.cor$catscores$gprs_umts"), closeTo(matrix(
        row(-2.2990569 ),
        row(-2.2990569 ),
        row(-2.2990569 ),
        row( 0.3873411 )), 0.0001));

    assertThat( eval("res.cor$loss"), equalTo(c(17.3821)));
        
  }
}
