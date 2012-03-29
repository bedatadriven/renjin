package org.renjin.util;

import org.apache.commons.math.complex.Complex;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Raw;
import org.renjin.sexp.RawVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;


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
    return new LogicalVector(values);
  }

  public static SEXP c(Logical... values) {
    return new LogicalVector(values);
  }

  public static SEXP c(String... values) {
    return new StringVector(values);
  }

  public static SEXP c(double... values) {
    return new DoubleVector(values);
  }
  
  public static SEXP c(Raw... values){
    return new RawVector(values);
  }
}
