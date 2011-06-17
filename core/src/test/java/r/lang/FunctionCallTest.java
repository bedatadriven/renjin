package r.lang;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class FunctionCallTest {

  
  @Test
  public void doClone() {
    FunctionCall call = FunctionCall.newCall(new Symbol("+"), new DoubleVector(1), new DoubleVector(1));
    assertThat( call, equalTo( call.clone() ) );
    
  }
  
}
