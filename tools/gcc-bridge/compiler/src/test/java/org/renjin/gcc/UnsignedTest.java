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
  
  @Test
  public void signedInt8ToUnsignedInt16() {
    
   // assertThat((int) Math.pow(2, 8), equalTo(2 << 8));
    
    // C Standard says:
    // If the destination type is unsigned, the resulting value is the least unsigned integer congruent 
    // to the source integer (modulo 2^n where n is the number of bits used to represent the unsigned type).
    

    int signed = -62;
    int unsigned = (-62) & (-1 >>> 16);
  
    System.out.println(-1 >>> 16);
    
    assertThat(unsigned, equalTo( 65474));
  }
  
}
