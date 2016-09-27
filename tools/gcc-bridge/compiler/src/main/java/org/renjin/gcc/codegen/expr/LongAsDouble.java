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
package org.renjin.gcc.codegen.expr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


public class LongAsDouble implements JLValue {
  private JExpr longValue;

  public LongAsDouble(JExpr longValue) {
    this.longValue = longValue;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.DOUBLE_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    // Load long as double
    longValue.load(mv);
    mv.invokestatic(Double.class, "longBitsToDouble", 
        Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.LONG_TYPE));
  }

  @Override
  public void store(MethodGenerator mv, JExpr expr) {
    // Store double as long

    ((JLValue) longValue).store(mv, new DoubleAsLong(expr));
  }


  
}
