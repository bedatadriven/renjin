package r.base;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import r.EvalTestCase;

public class ComplexNumberTest extends EvalTestCase {
  
  @Test
  public void imaginaryPartOfConvertedDoubleShouldBeZero() throws IOException {
    assertThat( eval("Im(as.complex(1))"), equalTo(c(0)) );
  }
  
  @Test
  public void realPartOfConvertedDoubleShouldMatch() throws IOException{
    assertThat(eval("Re(as.complex(1))"),equalTo(c(1)));
    assertThat(eval("Re(as.complex(2))"),equalTo(c(2)));
  }
  
  @Test
  public void vectorizedReal() throws IOException{
    eval("sqrt(c(1,4,9))");
    assertThat(eval("Re(c(as.complex(1),as.complex(2)))[1]"),equalTo(c(1)));
    assertThat(eval("Re(c(as.complex(1),as.complex(2)))[2]"),equalTo(c(2)));
  }
  
  @Test
  public void sizeAt0_1() throws IOException{
    assumingBasePackagesLoad();
    //    assertThat(eval("Mod(complex(real=0,imaginary=1))"),equalTo(c(1)));
    assertThat(eval("Mod(complex(0,1))"),equalTo(c(1)));
    assertThat(eval("Mod(complex(0,9))"),equalTo(c(9)));
  }
  
  @Test
  public void argumentAt0_1() throws IOException{
    assumingBasePackagesLoad();
//    assertThat(eval("Mod(complex(real=0,imaginary=1))"),equalTo(c(1)));
    assertThat(eval("Arg(complex(0, 1))/pi"),equalTo(c(0.5)));
  }
  
  @Test
  public void polarCoordinatesAt1_0() throws IOException{
    assumingBasePackagesLoad();
    //    assertThat(eval("Mod(complex(real=0,imaginary=1))"),equalTo(c(1)));
    assertThat(eval("Mod(complex(1,0))"),equalTo(c(1)));
    assertThat(eval("Mod(complex(9,0))"),equalTo(c(9)));
  }
  
  @Test
  public void complexConjugate() throws IOException{
    assumingBasePackagesLoad();
    assertThat(eval("Im(Conj(complex(0,1)))"),equalTo(c(-1)));
  }
}
