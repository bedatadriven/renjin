package org.renjin.sexp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FunctionCallTest {

  
  @Test
  public void doClone() {
    FunctionCall call = FunctionCall.newCall(Symbol.get("+"), new DoubleArrayVector(1), new DoubleArrayVector(1));
    assertThat( call, equalTo( call.clone() ) );
    
  }
  
}
