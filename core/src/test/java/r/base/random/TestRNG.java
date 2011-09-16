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
    RNG.RNG_kind = RNGtype.SUPER_DUPER;
    System.out.println(RNG.RNG_kind);
    

    RNG.RNG_Table[RNG.RNG_kind.ordinal()].i_seed[0] = 102;
    RNG.RNG_Table[RNG.RNG_kind.ordinal()].i_seed[0] = 10000;
    RNG.RNG_Table[RNG.RNG_kind.ordinal()].i_seed[1] = 20000;
    RNG.PutRNGstate();
    
    for (int i=0;i<10;i++){
    RNG.GetRNGstate();
          RNG.FixupSeeds(RNGtype.SUPER_DUPER, false);
      //System.out.println(RNG.seeds.toString());
      //RNG.GetRNGstate();
      System.out.println(RNG.unif_rand());
      RNG.PutRNGstate();
    }
    
    

  }
}
