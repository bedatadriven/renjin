/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Generates a boolean value based on a condition
 */
public class ConditionExpr implements JExpr {
  
  private ConditionGenerator condition;

  public ConditionExpr(ConditionGenerator condition) {
    this.condition = condition;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    
    // Push this value as a boolean on the stack.
    // Requires a jump
    Label trueLabel = new Label();
    Label falseLabel = new Label();
    Label exitLabel = new Label();

    condition.emitJump(mv, trueLabel, falseLabel);

    // if false
    mv.mark(falseLabel);
    mv.iconst(0);
    mv.goTo(exitLabel);

    // if true
    mv.mark(trueLabel);
    mv.iconst(1);

    // done
    mv.mark(exitLabel);
  }

}
