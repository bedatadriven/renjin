package org.renjin.sexp;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;

public class FunctionCallTest {

  
  @Test
  public void doClone() {
    FunctionCall call = FunctionCall.newCall(Symbol.get("+"), new DoubleVector(1), new DoubleVector(1));
    assertThat( call, equalTo( call.clone() ) );
    
  }
  
}
