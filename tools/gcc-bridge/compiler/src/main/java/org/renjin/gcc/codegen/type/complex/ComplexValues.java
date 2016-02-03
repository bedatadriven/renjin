package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;

/**
 * Operations on complex values
 */
public class ComplexValues {
  
  public static ComplexValue add(ComplexValue x, ComplexValue y) {
    Value real = Values.add(x.getRealValue(), y.getRealValue());
    Value im = Values.add(x.getImaginaryValue(), y.getImaginaryValue());
    return new ComplexValue(real, im);
  }
  
  public static ComplexValue subtract(ComplexValue x, ComplexValue y) {
    Value real = Values.difference(x.getRealValue(), y.getRealValue());
    Value im = Values.difference(x.getImaginaryValue(), y.getImaginaryValue());
    return new ComplexValue(real, im);
  }
  
  public static ComplexValue multiply(ComplexValue x, ComplexValue y) {
    //(a + bi)(c + di) = (ac - bd) + (bc + ad)i
    Value a = x.getRealValue();
    Value b = x.getImaginaryValue();
    Value c = y.getRealValue();
    Value d = y.getImaginaryValue();
    
    Value real = Values.difference(Values.product(a, c), Values.product(b, d));
    Value im = Values.add(Values.product(b, c), Values.product(a, d));
    
    return new ComplexValue(real, im);
  }
}
