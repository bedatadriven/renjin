package org.renjin.gcc.translate.type;

import org.junit.Test;
import org.objectweb.asm.Type;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ImPrimitiveTypeTest {

  @Test
  public void testType() {
    assertThat(ImPrimitiveType.BOOLEAN.jvmParamType(), equalTo(Type.BOOLEAN_TYPE));
  }
}