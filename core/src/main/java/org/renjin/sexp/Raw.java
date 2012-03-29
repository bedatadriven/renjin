
package org.renjin.sexp;

import com.google.common.primitives.UnsignedBytes;


public class Raw {
  
  public static int NUM_BITS = 8;
  public static int BITS_PER_INTEGER = 32;
  private byte internalValue = -128;
  
  
  /**
   * 
   * @param value Integer between 0 and 255
   */
  public void setValue(int value) {
    this.internalValue = UnsignedBytes.checkedCast(value);
 //   this.internalValue = (byte) (value - 128);
  }
  
  /**
   * 
   * @return The byte value of Raw object. 
   * Byte is not between -128 and 127 as Java bytes.
   * It is within the range 0<byte<255 (unsigned char)
   */
  public int getValue(){
    return UnsignedBytes.toInt(internalValue);
//    return(internalValue + 128);
  }
  
  
  public Raw(int value){
    setValue(value);
  }
  
  public Raw(byte value) {
    this.internalValue = value;
  }
  
  public Raw() {
    this.internalValue = (byte)RawVector.NA;
  }
  
  @Override
  public String toString() {
    String s = Integer.toHexString(this.getValue());
    if(s.length()==1) s="0"+s;
    return(s);
  }
  
  public Raw[] getAsZerosAndOnes() {
    Raw[] raws = new Raw[Raw.NUM_BITS];
    int val = this.getValue();
    int k;
    for (int i=0;i<Raw.NUM_BITS;i++){
      k = val % 2;
      val = val / 2;
      raws[Raw.NUM_BITS-i-1] = new Raw(k);
    }
    return(raws);
  }
  
  public byte getAsByte() {
    return(this.internalValue);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Raw)) {
      return (false);
    }
    Raw r = (Raw) o;
    if (r.internalValue == this.internalValue) {
      return (true);
    }
    return (false);
  }
  
   
}
