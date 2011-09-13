
package r.base.random;

import r.jvmi.annotations.Primitive;
import r.lang.IntVector;
import r.lang.exception.EvalException;


public class RNG {
  
  /*
   * Default RNG and N01 objects.
   */
  public static RNGtype RNG_kind = RNGtype.MERSENNE_TWISTER; //default
  public static N01type N01_kind = N01type.INVERSION; //default
  /*
   * R uses a int vector of length 625
   * for Random seeds.
   */
  static int[] dummy = new int[625];
  
  static double d2_32 = 4294967296.;/* = (double) */

  static double i2_32m1 = 2.328306437080797e-10;/* = 1/(2^32 - 1) */

  static double KT = 9.31322574615479e-10; /*= 2^-30 */
  
  @Primitive("RNGkind")
  public static IntVector RNGkind(int kind, int normalkind){
    /*
     * This method requires more than those codes. I will implement them as soon as possible.
     */
    try{
      RNG.RNG_kind = RNGtype.values()[kind];
    }catch (Exception e){
      throw new EvalException("RNGkind: unimplemented RNG kind "+kind);
    }
    
    try{
      RNG.N01_kind = N01type.values()[normalkind];
    }catch(Exception e){
      throw new EvalException("invalid Normal type in RNGkind");
    }
    
    return(new IntVector(RNG.RNG_kind.ordinal(), RNG.N01_kind.ordinal()));
  }
  
  
  static double fixup(double x) {
    if (x <= 0.0) {
      return 0.5 * i2_32m1;
    }
    if ((1.0 - x) <= 0.0) {
      return 1.0 - 0.5 * i2_32m1;
    }
    return x;
  }
  
  
  
 public double unif_rand(){
    double value;

    switch(RNG_kind) {

    case WICHMANN_HILL:
	throw new EvalException(RNG_kind+" not implemented yet");
        
    case MARSAGLIA_MULTICARRY:
	throw new EvalException(RNG_kind+" not implemented yet");
    case SUPER_DUPER:
	throw new EvalException(RNG_kind+" not implemented yet");
    case MERSENNE_TWISTER:
	return fixup(MT_genrand());
    case KNUTH_TAOCP:
    case KNUTH_TAOCP2:
	throw new EvalException(RNG_kind+" not implemented yet");
    case USER_UNIF:
	throw new EvalException(RNG_kind+" not implemented yet");
    default:
	throw new EvalException(RNG_kind+" not implemented yet");
    }
}

 
 
 /*
  * Methods for MERSENNE_TWISTER
  */
 
 static double MT_genrand(){
    throw new EvalException("MT_genrand not implemented yet");
}


}
