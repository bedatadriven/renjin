package org.renjin.primitives.print;


import org.apache.commons.math.complex.Complex;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.sexp.DoubleVector;

import java.text.DecimalFormat;

public class ComplexPrinter implements Function<Complex, String> {
  
  private DecimalFormat format = new DecimalFormat("0.000000");
  
  @Override
  public String apply(Complex input) {
    if(DoubleVector.isNA(input.getReal())) {
      return "NA";
    }
    return format.format(input.getReal()) + "+" + format.format(input.getImaginary()) + "i";
  }
}
