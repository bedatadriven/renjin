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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.Type;

public class PrimitiveReturnStrategy implements ReturnStrategy {

  private final PrimitiveType type;

  public PrimitiveReturnStrategy(PrimitiveType type) {
    this.type = type;
  }

  @Override
  public Type getType() {
    return type.jvmType();
  }

  @Override
  public JExpr marshall(GExpr expr) {
    return expr.toPrimitiveExpr().jexpr();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr callExpr, TypeStrategy lhsTypeStrategy) {
    return type.fromNonStackValue(Expressions.castPrimitive(callExpr, type.jvmType()));
  }

  @Override
  public JExpr getDefaultReturnValue() {
    return Expressions.zero(type.jvmType());
  }
}
