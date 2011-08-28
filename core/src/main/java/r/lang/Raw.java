
package r.lang;


public class Raw {
  
  private byte internalValue = -128;
  
  /**
   * 
   * @param value Integer between 0 and 255
   */
  public void setValue(int value){
    this.internalValue = (byte) (value - 128);
  }
  
  /**
   * 
   * @return The byte value of Raw object. 
   * Byte is not between -128 and 127 as Java bytes.
   * It is within the range 0<byte<255 (unsigned char)
   */
  public int getValue(){
    return(internalValue + 128);
  }
  
  
  public Raw(int value){
    this.internalValue = (byte)(value - 128);
  }
  
  public Raw(){
    this.internalValue = (byte)RawVector.NA;
  }
  
  @Override
  public String toString(){
    return (Integer.toHexString(this.getValue()));
  }
  
  
}
