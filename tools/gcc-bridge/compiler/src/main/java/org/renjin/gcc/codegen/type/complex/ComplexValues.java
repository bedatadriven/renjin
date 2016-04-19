package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;

/**
 * Operations on complex values
 */
public class ComplexValues {
  
  public static ComplexValue add(ComplexValue x, ComplexValue y) {
    SimpleExpr real = Expressions.sum(x.getRealValue(), y.getRealValue());
    SimpleExpr im = Expressions.sum(x.getImaginaryValue(), y.getImaginaryValue());
    return new ComplexValue(real, im);
  }
  
  public static ComplexValue subtract(ComplexValue x, ComplexValue y) {
    SimpleExpr real = Expressions.difference(x.getRealValue(), y.getRealValue());
    SimpleExpr im = Expressions.difference(x.getImaginaryValue(), y.getImaginaryValue());
    return new ComplexValue(real, im);
  }
  
  public static ComplexValue multiply(ComplexValue x, ComplexValue y) {
    //(a + bi)(c + di) = (ac - bd) + (bc + ad)i
    SimpleExpr a = x.getRealValue();
    SimpleExpr b = x.getImaginaryValue();
    SimpleExpr c = y.getRealValue();
    SimpleExpr d = y.getImaginaryValue();
    
    SimpleExpr real = Expressions.difference(Expressions.product(a, c), Expressions.product(b, d));
    SimpleExpr im = Expressions.sum(Expressions.product(b, c), Expressions.product(a, d));
    
    return new ComplexValue(real, im);
  }
}
