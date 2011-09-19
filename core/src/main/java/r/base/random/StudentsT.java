package r.base.random;

import r.util.CDefines;

public class StudentsT {

  /*
   * if df is infinite then a normal random should be returned.
   * I don't know the way of getting whether df is infinite or not
   * so i am leaving it as is. Something needed:
   * 
   * if (df is infinite){
   * return(Normal.norm_rand());
   * }else{
   * return Normal.norm_rand() / Math.sqrt(ChiSquare.rchisq(df) / df);
   * }
   */
  public static double rt(double df) {
    if (Double.isNaN(df) || df <= 0.0) {
      return (Double.NaN);
    }

    return Normal.norm_rand() / Math.sqrt(ChiSquare.rchisq(df) / df);

  }
}
