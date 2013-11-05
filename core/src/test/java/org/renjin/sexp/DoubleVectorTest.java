package org.renjin.sexp;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class DoubleVectorTest {

  @Test
  public void notWiderThanComplex() {
    assertFalse(DoubleVector.VECTOR_TYPE.isWiderThanOrEqualTo(ComplexVector.VECTOR_TYPE));
  }
}
