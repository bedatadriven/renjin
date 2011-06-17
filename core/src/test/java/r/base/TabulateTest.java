package r.base;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import r.EvalTestCase;

public class TabulateTest extends EvalTestCase {

  @Test
  public void tabulate() {
    assertThat( eval(".C('R_tabulate', c(2L, 3L, 5L), 3L, 5L, 1:5, PACKAGE='base')$ans "), equalTo( c_i(0,1,1,0,1)));
    
  }
  
}
