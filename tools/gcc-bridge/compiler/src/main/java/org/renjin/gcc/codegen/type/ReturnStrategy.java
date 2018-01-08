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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Type;

/**
 * Provides a strategy for return values from methods.
 * 
 * <p>Because the JVM will only let us return a single value from a method,
 * we have to be sometimes creative in returning things like fat pointers, which
 * we represent using an array <i>and</i> and integer offset.</p>
 * 
 * @see org.renjin.gcc.codegen.vptr.VPtrReturnStrategy
 * @see org.renjin.gcc.codegen.type.complex.ComplexReturnStrategy
 */
public interface ReturnStrategy {

  /**
   * 
   * @return the JVM return type
   */
  Type getType();


  /**
   * Converts if necessary the expression to be returned to a single value.
   */
  JExpr marshall(GExpr expr);


  /**
   * Converts a function call return value to an expression if necessary.
   */
  GExpr unmarshall(MethodGenerator mv, JExpr callExpr, TypeStrategy lhsTypeStrategy);

  /**
   * Sometimes C code doesn't return a value despite having a non-void return type. In this case, 
   * we just need to push SOMETHING onto the stack.
   */
  JExpr getDefaultReturnValue();

}
