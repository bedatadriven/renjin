package org.renjin.compiler.ir;

import org.junit.Test;
import org.objectweb.asm.Type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ValueBoundsTest {

  @Test
  public void test() {
    
    assertThat(ValueBounds.INT_PRIMITIVE.storageType(), equalTo(Type.INT_TYPE));
    
    
  }
  
  
}