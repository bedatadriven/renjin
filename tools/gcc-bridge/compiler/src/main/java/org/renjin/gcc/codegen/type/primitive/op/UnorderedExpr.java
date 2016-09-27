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
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Determines whether {@code x} and {@code y} are "unordered", that is, 
 * if one or both arguments are NaN.
 */
public class UnorderedExpr implements JExpr {
  
  private JExpr x;
  private JExpr y;

  public UnorderedExpr(JExpr x, JExpr y) {
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
    Label unordered = new Label();
    Label exit = new Label();
    
    emitIsNaN(mv, x);
    // 1 = not a number
    // 0 = not (not a number)
    // If not equal to zero, then x is 
    mv.ifne(unordered);
    
    // If x was not NaN, then we have to check y too
    emitIsNaN(mv, y);
    mv.ifne(unordered);
    
    // Ordered!
    mv.iconst(0);
    mv.goTo(exit);
    
    // Unordered
    mv.mark(unordered);
    mv.iconst(1);
    
    // Exit
    mv.mark(exit);
  }

  private void emitIsNaN(MethodGenerator mv, JExpr value) {
    value.load(mv);

    switch (value.getType().getSort()) {
      case Type.DOUBLE:
        mv.invokestatic(Double.class, "isNaN", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.DOUBLE_TYPE));
        break;
      
      case Type.FLOAT:
        mv.invokestatic(Double.class, "isNaN", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.FLOAT_TYPE));
        break;          
    
      default:
        throw new IllegalStateException("type: " + value.getType());
    }
  }
}
