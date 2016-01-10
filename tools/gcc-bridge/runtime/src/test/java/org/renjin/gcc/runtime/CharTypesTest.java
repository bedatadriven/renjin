package org.renjin.gcc.runtime;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;

public class CharTypesTest {

  @Test
  public void isWhitespace() {
    
    BytePtr str = BytePtr.nullTerminatedString(" ", StandardCharsets.UTF_8);
    char mask = CharTypes.TABLE_PTR.get().array[128 + str.array[0]];
    int whitespaceBit = (1 << 5) << 8;
    
    assertTrue((mask & whitespaceBit) != 0);
  }
  
}