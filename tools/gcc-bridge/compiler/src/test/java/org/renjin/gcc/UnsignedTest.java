package org.renjin.gcc;

import com.google.common.primitives.UnsignedInts;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

/**
 * Validate approaches to unsigned integers
 */
public class UnsignedTest {
  
  
  @Test
  public void unsignedInt32ToInt64() {
    int unsignedInt32 = UnsignedInts.parseUnsignedInt("4294967295");
    assertThat("signed value < 0", unsignedInt32, lessThan(0));
    
    // Can we just cast up and preserve the value?
    // NOPE: need to preserve the value
    long unsignedInt64 = unsignedInt32 & 0xffffffffL;
    
    assertThat(unsignedInt64, equalTo(4294967295L));
    
  }
  
}
