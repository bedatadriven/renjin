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
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.util.Printer;

import javax.annotation.Nonnull;

/**
 * Generates bytecode for a binary operation on primitives (IMUL, DMUL, IADD, etc)
 */
public class PrimitiveBinOpGenerator implements JExpr {

  private int opCode;
  private final JExpr x;
  private final JExpr y;

  public PrimitiveBinOpGenerator(GimpleOp op, JExpr x, JExpr y) {
    this.opCode = opCodeFor(op);
    this.x = x;
    this.y = y;

    checkTypes();
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
    mv.visitInsn(getType().getOpcode(opCode));
  }

  private static int opCodeFor(GimpleOp op) {
    switch (op) {

      case MULT_EXPR:
        return Opcodes.IMUL;
      case PLUS_EXPR:
        return Opcodes.IADD;
      case MINUS_EXPR:
        return Opcodes.ISUB;
      
      case RDIV_EXPR:
      case TRUNC_DIV_EXPR:
      case EXACT_DIV_EXPR:
        return Opcodes.IDIV;

      case TRUNC_MOD_EXPR:
        return Opcodes.IREM;
      
      case BIT_AND_EXPR:
        return Opcodes.IAND;
      case BIT_IOR_EXPR:
        return Opcodes.IOR;
      case BIT_XOR_EXPR:
        return Opcodes.IXOR;
    }
    return 0;
  }

  private void checkTypes() {
    Type tx = x.getType();
    Type ty = y.getType();

    if(!tx.equals(ty)) {
      throw new IllegalStateException(String.format(
          "Incompatible types for %s: %s != %s", Printer.OPCODES[opCode], tx, ty));
    }
  }

  @Override
  public String toString() {
    return "(" + x + " " + Printer.OPCODES[opCode] + " " + y + ")";
  }

}
