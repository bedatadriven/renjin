/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
