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
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Generates bytecode for left and right bitwise shifts
 */
public class BitwiseShift implements JExpr {

  private final GimpleOp op;
  private GimpleIntegerType type;
  private final JExpr x;
  private final JExpr y;

  public BitwiseShift(GimpleOp op, GimpleType type, JExpr x, JExpr y) {
    this.op = op;
    this.type = (GimpleIntegerType) type;
    this.x = x;
    this.y = y;

//    if(!checkTypes()) {
//      throw new UnsupportedOperationException("Shift operations require types (int, int) or (long, int), found: " +
//          this.x.getType() + ", " + this.y.getType());
//    }
  }

  private boolean checkTypes() {
    Type tx = x.getType();
    Type ty = y.getType();

    return (tx.equals(Type.INT_TYPE) || tx.equals(Type.LONG_TYPE)) && ty.equals(tx);
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

    switch (op) {
      case LSHIFT_EXPR:
        // Shifting left has (bitwise) the same result on signed and unsigned integers
        mv.shl(type);
        break;

      case RSHIFT_EXPR:
        // For right shifts, we need to consider whether the type is signed
        if(this.type.isUnsigned()) {
          mv.ushr(type);

        } else {
          mv.shr(type);
        }
        break;
      
      default:
        throw new UnsupportedOperationException("Op: " + op);
    }
  }
}
