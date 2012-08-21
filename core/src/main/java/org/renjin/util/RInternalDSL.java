package org.renjin.util;

import org.apache.commons.math.complex.Complex;
import org.renjin.sexp.*;


public class RInternalDSL {
  public static Complex complex(double real) {
    return new Complex(real, 0);
  }
  
  public static Complex complex(double real, double imaginary) {
    return new Complex(real, imaginary);
  }
  
  public static SEXP c(Complex... values) {
    return new ComplexVector(values);
  }

  public static SEXP c(boolean... values) {
    return new LogicalArrayVector(values);
  }

  public static SEXP c(Logical... values) {
    return new LogicalArrayVector(values);
  }

  public static SEXP c(String... values) {
    return new StringArrayVector(values);
  }

  public static SEXP c(double... values) {
    return new DoubleArrayVector(values);
  }
  
  public static SEXP c(Raw... values){
    return new RawVector(values);
  }
}
