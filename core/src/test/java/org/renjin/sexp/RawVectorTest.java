package org.renjin.sexp;

import com.google.common.primitives.UnsignedBytes;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class RawVectorTest {

  @Test
  public void asCharacter() {
    RawVector vector = new RawVector(UnsignedBytes.checkedCast(1));
    assertThat(vector.getElementAsString(0), equalTo("01"));

    vector = new RawVector(UnsignedBytes.checkedCast(128));
    assertThat(vector.getElementAsString(0), equalTo("80"));


  }
  
}