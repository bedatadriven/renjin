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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.GSimpleExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for returning types whose values can be represented as a {@link JExpr}
 */
public final class SimpleReturnStrategy implements ReturnStrategy {

  private final SimpleTypeStrategy strategy;
  private Type type;

  public SimpleReturnStrategy(SimpleTypeStrategy strategy) {
    this.type = strategy.getJvmType();
    this.strategy = strategy;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public JExpr marshall(GExpr expr) {
    return ((GSimpleExpr) expr).unwrap();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr callExpr, TypeStrategy lhsTypeStrategy) {
    GExpr result = strategy.wrap(Expressions.cast(callExpr, type));
    try {
      return lhsTypeStrategy.cast(mv, result);
    } catch (UnsupportedCastException e) {
      throw new InternalCompilerException("Cannot cast from " + strategy + " to " + lhsTypeStrategy, e);
    }
  }

  @Override
  public JExpr getDefaultReturnValue() {
    switch (type.getSort()) {
      case Type.OBJECT:
      case Type.ARRAY:
      case Type.METHOD:
        return Expressions.nullRef(type); 
      
      default:
        return Expressions.zero(type);
    }
  }
}
