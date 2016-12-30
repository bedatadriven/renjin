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
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

public class BitwiseNot implements JExpr {

  private final JExpr argument;

  public BitwiseNot(JExpr argument) {
    this.argument = argument;
  }

  @Nonnull
  @Override
  public Type getType() {
    return argument.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    // Unary bitwise complement operator is implemented
    // as an XOR operation with -1 (all bits set)
    argument.load(mv);
    
    if(argument.getType().equals(Type.INT_TYPE)) {
      mv.iconst(-1);
      mv.xor(Type.INT_TYPE);

    } else if (argument.getType().equals(Type.BYTE_TYPE)) {
      mv.iconst(0xFF);
      mv.xor(Type.BYTE_TYPE);

    } else if (argument.getType().equals(Type.BOOLEAN_TYPE)) {
      mv.iconst(0x1);
      mv.xor(Type.BOOLEAN_TYPE);

    } else if (argument.getType().equals(Type.CHAR_TYPE)) {
      // unsigned 16 bit
      mv.iconst(0xFFFF);
      mv.xor(Type.CHAR_TYPE);
      
    } else {
      throw new UnsupportedOperationException("Unsupported type for bitwise not operator: "  + argument.getType());
    }
  }
}
