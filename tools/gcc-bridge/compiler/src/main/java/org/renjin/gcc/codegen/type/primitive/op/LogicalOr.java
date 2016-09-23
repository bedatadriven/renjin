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
public class LogicalOr implements JExpr {
  
  private JExpr x;
  private JExpr y;

  public LogicalOr(JExpr x, JExpr y) {
    this.x = x;
    this.y = y;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    Label trueLabel = new Label();
    Label exitLabel = new Label();
    
    x.load(mv);
    
    // if x is true, then can jump right away to true
    jumpIfTrue(mv, trueLabel);

    // Otherwise need to check y
    y.load(mv);
    jumpIfTrue(mv, trueLabel);
    
    // FALSE: emit 0
    mv.iconst(0);
    mv.goTo(exitLabel);
    
    // TRUE: emit 1
    mv.mark(trueLabel);
    mv.iconst(1);
    
    mv.mark(exitLabel);
  }

  private void jumpIfTrue(MethodGenerator mv, Label trueLabel) {
    mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
  }
}
