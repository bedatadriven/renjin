package r.base;

import org.apache.commons.math.complex.Complex;

import r.jvmi.wrapper.generator.scalars.ComplexType;
import r.lang.ComplexVector;
import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.Null;
import r.lang.SEXP;

public class ComplexGroup {

  public static DoubleVector Mod(DoubleVector x) {
    return x;
  }
 
  public static IntVector Mod(IntVector x) {
    return x;
  }

  public static Null Mod(Null x) {
    return x;
  }
  
  public static double Mod(Complex z){
    return z.abs();
  }
  
  public static double Arg(Complex z){
    return z.getArgument();
  }
  
  public static Complex complex(double x, double y){
    java.lang.System.out.println(y);
    return new Complex(x,y);
  }
  
  public static double Re(Complex z){
    return z.getReal();
  }
  
  public static double Im(Complex z){
    return z.getImaginary();
  }
  
  
  public static Complex Conj(Complex z){
    java.lang.System.out.println("Im(z): " + z.getImaginary());
    java.lang.System.out.println("Re(z): " + z.getReal());
    return new Complex(z.getReal(),-1*z.getImaginary());
  }
  
}
