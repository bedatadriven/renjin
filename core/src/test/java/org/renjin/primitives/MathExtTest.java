package org.renjin.primitives;


import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MathExtTest {

  @Test
  public void roundTest() {
    assertThat(MathExt.round(7.0, 20), equalTo(7.0));
    assertThat(MathExt.round(7.0, 1), equalTo(7.0));
    assertThat(MathExt.round(7.3234324, 0), equalTo(7.0));
    assertThat(MathExt.round(1d/3d, 5), equalTo(0.33333));
    
    assertThat(MathExt.round(7.*Math.pow(10,20)), equalTo(7e20));
  }
  
  
}
