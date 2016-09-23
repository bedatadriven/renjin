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
package org.renjin.gcc.codegen.type.primitive.op;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


public class MinMaxValue implements JExpr {

  private GimpleOp op;
  private JExpr x;
  private JExpr y;

  public MinMaxValue(GimpleOp op, JExpr x, JExpr y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Nonnull
  @Override
  public Type getType() {
    return x.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    x.load(mv);
    y.load(mv);

    Type type = x.getType();
    if(!type.equals(y.getType())) {
      throw new UnsupportedOperationException(String.format(
          "Types must be the same: %s != %s", x.getType(), y.getType()));
    }
    
    String methodName;
    switch (op) {
      case MIN_EXPR:
        methodName = "min";
        break;
      case MAX_EXPR:
        methodName = "max";
        break;
      default:
        throw new IllegalArgumentException("op: " + op);
    }

    mv.invokestatic(Math.class, methodName, Type.getMethodDescriptor(type, type, type));
  }
}
