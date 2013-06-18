package org.renjin.primitives;

import org.apache.commons.math.complex.Complex;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.DataParallel;
import org.renjin.invoke.annotations.Internal;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Null;


public class ComplexGroup {

  @Builtin
  public static DoubleVector Mod(DoubleVector x) {
    return x;
  }

  @Builtin
  public static IntVector Mod(IntVector x) {
    return x;
  }

  @Builtin
  public static Null Mod(Null x) {
    return x;
  }

  @Builtin
  @DataParallel
  public static double Mod(Complex z){
    return z.abs();
  }

  @Builtin
  @DataParallel
  public static double Arg(Complex z){
    return z.getArgument();
  }

  @Internal
  public static ComplexVector complex(int lengthOut, AtomicVector realVector, AtomicVector imaginaryVector) {
    if(realVector.length() > lengthOut) {
      lengthOut = realVector.length();
    }
    if(imaginaryVector.length() > lengthOut) {
      lengthOut = imaginaryVector.length();
    }
    
    ComplexVector.Builder result = new ComplexVector.Builder(0, lengthOut); 
    for(int i=0; i!=lengthOut;++i) {
      double real = 0;
      double imaginary = 0;
      if(realVector.length() > 0) {
        real = realVector.getElementAsDouble(i % realVector.length());
      }
      if(imaginaryVector.length() > 0) {
        imaginary = imaginaryVector.getElementAsDouble(i % imaginaryVector.length());
      }
      result.add(new Complex(real, imaginary));
    }
    return result.build();
  }

  @Builtin
  @DataParallel
  public static double Re(Complex z){
    return z.getReal();
  }

  @Builtin
  @DataParallel
  public static double Im(Complex z){
    return z.getImaginary();
  }
  
  @Builtin
  @DataParallel
  public static Complex Conj(Complex z){
    return new Complex(z.getReal(),-1*z.getImaginary());
  }
}
