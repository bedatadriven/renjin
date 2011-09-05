
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
    String s = Integer.toHexString(this.getValue());
    if(s.length()==1) s="0"+s;
    return(s);
  }
  
  public Raw[] getAsZerosAndOnes(){
    Raw[] raws = new Raw[8];
    int val = this.getValue();
    int k;
    for (int i=0;i<8;i++){
      k = val % 2;
      val = val / 2;
      raws[8-i-1] = new Raw(k);
    }
    return(raws);
  }
  
  
}
