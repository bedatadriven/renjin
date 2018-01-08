/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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


import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for returning a complex value as a {@code double[2]} or {@code float[2]}
 */
public class ComplexReturnStrategy implements ReturnStrategy {
  
  private GimpleComplexType type;

  public ComplexReturnStrategy(GimpleComplexType type) {
    this.type = type;
  }

  @Override
  public Type getType() {
    return type.getJvmPartArrayType();
  }

  @Override
  public JExpr marshall(GExpr expr) {
    ComplexExpr complexExpr = (ComplexExpr) expr;
    return Expressions.newArray(
        complexExpr.getRealJExpr(),
        complexExpr.getImaginaryJExpr());
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr callExpr, TypeStrategy lhsTypeStrategy) {
    // Allocate a temporary variable for the array so that it's 
    // components can be accessed
    JLValue array = mv.getLocalVarAllocator().reserve("retval", callExpr.getType());
    array.store(mv, callExpr);
    JExpr realValue = Expressions.elementAt(array, 0);
    JExpr imaginaryValue = Expressions.elementAt(array, 1);
    
    return new ComplexExpr(realValue, imaginaryValue);
  }

  @Override
  public JExpr getDefaultReturnValue() {
    JExpr zero = Expressions.zero(type.getJvmPartType());
    
    return Expressions.newArray(zero, zero);
  }

}
