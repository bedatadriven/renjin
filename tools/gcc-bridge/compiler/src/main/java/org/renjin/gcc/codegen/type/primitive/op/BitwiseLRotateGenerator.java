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
import org.renjin.repackaged.guava.base.Preconditions;

import javax.annotation.Nonnull;

import static org.renjin.repackaged.asm.Type.INT_TYPE;


public class BitwiseLRotateGenerator implements JExpr {
  
  private JExpr bits;
  private JExpr k;

  public BitwiseLRotateGenerator(JExpr bits, JExpr k) {
    this.bits = bits;
    this.k = k;
    Preconditions.checkArgument(bits.getType() == Type.INT_TYPE);
  }

  @Nonnull
  @Override
  public Type getType() {
    return bits.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    
    //(bits >>> k) | (bits << (Integer.SIZE - k));


//    0: iload_0
//    1: iload_1
//    2: iushr
    bits.load(mv);
    k.load(mv);
    mv.ushr(bits.getType());
    
//    3: iload_0
//    4: bipush        32
//    6: iload_1
//    7: isub
//    8: ishl
//    9: ior
    bits.load(mv);
    mv.iconst(32);
    k.load(mv);
    mv.sub(INT_TYPE);
    mv.shl(INT_TYPE);
    mv.or(INT_TYPE);
  }

}
