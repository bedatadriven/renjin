package r.util;

import org.apache.commons.math.complex.Complex;

import r.lang.ComplexVector;
import r.lang.DoubleVector;
import r.lang.Logical;
import r.lang.LogicalVector;
import r.lang.Raw;
import r.lang.RawVector;
import r.lang.SEXP;
import r.lang.StringVector;

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
