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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

import javax.annotation.Nonnull;


public class BitFieldExpr implements JLValue {

  private JLValue byteValue;
  private int offset;
  private int size;

  public BitFieldExpr(Type ownerClass, JExpr instance, String fieldName, int offset, int size) {
    assert instance.getType().equals(ownerClass);
    this.byteValue = Expressions.field(instance, Type.BYTE_TYPE, fieldName);
    this.offset = offset;
    this.size = size;
  }

  public BitFieldExpr(JLValue byteValue, int offset, int size) {
    this.byteValue = byteValue;
    this.offset = offset;
    this.size = size;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.BYTE_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {

    byteValue.load(mv);

    // shift from our range
    if(offset != 0) {
      mv.iconst(offset);
      mv.ushr(Type.BYTE_TYPE);
    }
    
    // zero out values outside our range
    mv.iconst((1 << size) - 1);
    mv.and(Type.BYTE_TYPE);
  }

  @Override
  public void store(MethodGenerator mv, final JExpr rhs) {
    Preconditions.checkArgument(rhs.getType().equals(Type.BYTE_TYPE) ||
                                rhs.getType().equals(Type.INT_TYPE));

    // Store to the field
    byteValue.store(mv, new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.BYTE_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {

        // Given a bit value like
        //    V1: 1010 0000
        // And a current value of
        //    V0: 0110 1101
        //
        // We need to set the field to the value
        // V0 | (V1 & MASK)

        // Load the original value
        byteValue.load(mv);

        // Load the new value
        rhs.load(mv);

        // Zero out any bits outside our bit range
        mv.iconst((1 << size) - 1);
        mv.and(Type.BYTE_TYPE);

        // Shift right to our range
        if(offset != 0) {
          mv.iconst(offset);
          mv.shl(Type.BYTE_TYPE);
        }

        // Or with the existing value
        mv.or(Type.BYTE_TYPE);
      }
    });
  }

}
