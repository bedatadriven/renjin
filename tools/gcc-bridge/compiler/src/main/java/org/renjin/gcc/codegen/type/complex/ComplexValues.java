package org.renjin.gcc.codegen.type.complex;

import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;

/**
 * Operations on complex values
 */
public class ComplexValues {
  
  public static ComplexValue add(ComplexValue x, ComplexValue y) {
    JExpr real = Expressions.sum(x.getRealJExpr(), y.getRealJExpr());
    JExpr im = Expressions.sum(x.getImaginaryJExpr(), y.getImaginaryJExpr());
    return new ComplexValue(real, im);
  }
  
  public static ComplexValue subtract(ComplexValue x, ComplexValue y) {
    JExpr real = Expressions.difference(x.getRealJExpr(), y.getRealJExpr());
    JExpr im = Expressions.difference(x.getImaginaryJExpr(), y.getImaginaryJExpr());
    return new ComplexValue(real, im);
  }
  
  public static ComplexValue multiply(ComplexValue x, ComplexValue y) {
    //(a + bi)(c + di) = (ac - bd) + (bc + ad)i
    JExpr a = x.getRealJExpr();
    JExpr b = x.getImaginaryJExpr();
    JExpr c = y.getRealJExpr();
    JExpr d = y.getImaginaryJExpr();
    
    JExpr real = Expressions.difference(Expressions.product(a, c), Expressions.product(b, d));
    JExpr im = Expressions.sum(Expressions.product(b, c), Expressions.product(a, d));
    
    return new ComplexValue(real, im);
  }
}
