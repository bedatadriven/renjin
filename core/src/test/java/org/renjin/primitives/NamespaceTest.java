package org.renjin.primitives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import r.EvalTestCase;

public class NamespaceTest extends EvalTestCase {
  
  @Test
  public void isNamespace() {
    assertThat(eval(".Internal(isNamespaceEnv(baseenv()))"), equalTo(c(false)));
    assertThat(eval(".Internal(isNamespaceEnv(globalenv()))"), equalTo(c(false)));
     
  }
}
