package r.packages; 

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AspectPackageTest extends PackageTest {
  
  @Test
  public void simple() {
    
    eval("library('aspect')");
    eval("data('wurzer')");

    eval("print(aspect:::burtTable(wurzer))");
    
    eval("res.cor <- corAspect(wurzer, aspect='aspectSum', level=c(rep('nominal',2), rep('ordinal',5), 'nominal'))");
    eval("print(res.cor$catscores)");
    
    assertThat( eval("res.cor$catscores$know_location"), closeTo(matrix(
        row( 0.7621564),
        row(-1.3120667)), 0.0001)); 
    
    assertThat( eval("res.cor$catscores$used_terminal"), closeTo(matrix(
        row( 0.5361205 ),
        row(-1.8652524 )), 0.0001));

    assertThat( eval("res.cor$catscores$home"), closeTo(matrix(
        row(-0.8825501 ),
        row( 0.9569379 ),
        row( 1.2254568 ),
        row( 1.2254568 )), 0.0001));


    assertThat( eval("res.cor$loss"), closeTo(c(17.38621), 0.00001));
    assertThat( eval("res.cor$eigencor"), 
          closeTo(c(2.5993424,1.0973193,0.9905595,0.7878082,0.6615520,0.5355071,0.3407006,0.2559578),0.001));
    assertThat( eval("res.cor$niter"), equalTo(c(4)));
  }

  @Test
  public void ordinal() {
    eval("library('aspect')");
    eval("data('wurzer')");
   
    eval("m <- length(wurzer)");
    eval("data <- wurzer");
    
    assertThat( eval("ncat <- sapply(1:m,function(j) length(table(data[,j])))"), equalTo(c_i( 2, 2, 4, 5, 5, 4, 4, 3)));
    assertThat( eval("ccat <- c(0,cumsum(ncat))"), equalTo(c(0,  2,  4,  8, 13, 18, 22, 26 ,29)));
    
    
   eval("res.cor <- corAspect(wurzer[,c(1,2,4)], aspect='aspectSum', level=rep('nominal',3), itmax=1)");

    eval("print(res.cor$catscores)");
  }
  
}
