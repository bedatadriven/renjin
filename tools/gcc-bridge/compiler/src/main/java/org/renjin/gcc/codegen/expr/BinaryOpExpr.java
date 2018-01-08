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
package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

public class BinaryOpExpr implements JExpr {

  private int opcode;
  private Type resultType;
  private JExpr x;
  private JExpr y;


  public BinaryOpExpr(int opcode, JExpr x, JExpr y) {
    this.opcode = opcode;
    this.resultType = x.getType();
    this.x = x;
    this.y = y;
  }

  public BinaryOpExpr(int opcode, Type resultType, JExpr x, JExpr y) {
    this.opcode = opcode;
    this.resultType = resultType;
    this.x = x;
    this.y = y;
  }

  public int getOpcode() {
    return opcode;
  }

  public JExpr getX() {
    return x;
  }

  public JExpr getY() {
    return y;
  }

  @Nonnull
  @Override
  public Type getType() {
    return resultType;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    x.load(mv);
    y.load(mv);
    mv.visitInsn(x.getType().getOpcode(opcode));
  }
}
