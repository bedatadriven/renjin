package r.base.random;

import r.EvalTestCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import org.junit.Test;

public class TestRNG extends EvalTestCase {

  @Test
  public void RNGkind() {
    try {
      topLevelContext.init();
    } catch (Exception e) {
    }
    assertThat(eval("RNGkind('W','I')"), equalTo(c("Wichmann-Hill", "Inversion")));
    assertThat(eval("RNGkind('Mer','Ahr')"), equalTo(c("Mersenne-Twister", "Ahrens-Dieter")));    
  }
  
  @Test
  public void unif_rand(){
    RNG.RNGkind(RNGtype.MERSENNE_TWISTER.ordinal(), N01type.INVERSION.ordinal());
    RNG.set_seed(1, RNG.RNG_kind.ordinal(), RNG.N01_kind.ordinal());
  }
  
  @Test
  public void runif(){
    try{
      topLevelContext.init();
    }catch (Exception e){
      
    }
    eval("set.seed(12345, 'Mersenne-Twister','I')");
    assertThat(eval("runif(1,0,2)").asReal(), closeTo(1.7763822156, 0.000001));
  }
  
  @Test
  public void norm_rand(){
    
  }
}
