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
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Divides two unsigned ints via long
 */
public class UnsignedIntDiv implements JExpr {

  private JExpr dividend;
  private JExpr divisor;

  public UnsignedIntDiv(JExpr dividend, JExpr divisor) {
    this.dividend = dividend;
    this.divisor = divisor;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.INT_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    dividend.load(mv);
    CastGenerator.castUnsignedInt32ToInt64(mv);
    divisor.load(mv);
    CastGenerator.castUnsignedInt32ToInt64(mv);
    mv.visitInsn(Opcodes.LDIV);
    CastGenerator.castInt64ToUnsignedInt32(mv);
  }
}
