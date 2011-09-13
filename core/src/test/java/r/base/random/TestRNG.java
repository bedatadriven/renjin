package r.base.random;

import r.EvalTestCase;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
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
    RNG.GetRNGstate();
    RNG.RNG_Init(RNG.RNG_kind, 123456);
    System.out.println(RNG.unif_rand());
  }
}
