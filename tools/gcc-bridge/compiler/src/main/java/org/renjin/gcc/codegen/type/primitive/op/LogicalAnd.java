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
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Logical binary operator, such as TRUTH_OR, TRUTH_AND
 */
public class LogicalAnd implements JExpr {
  
  private JExpr x;
  private JExpr y;

  public LogicalAnd(JExpr x, JExpr y) {
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
    Label falseLabel = new Label();
    Label exitLabel = new Label();

    // if x is false, then can jump right away to false
    x.load(mv);
    jumpIfFalse(mv, falseLabel);

    // Otherwise need to check y
    y.load(mv);
    jumpIfFalse(mv, falseLabel);
    
    // TRUE: emit 1
    mv.iconst(1);
    mv.goTo(exitLabel);
    
    // FALSE: emit 0
    mv.mark(falseLabel);
    mv.iconst(0);
    
    mv.mark(exitLabel);
  }

  private void jumpIfFalse(MethodGenerator mv, Label trueLabel) {
    mv.visitJumpInsn(Opcodes.IFEQ, trueLabel);
  }
}
