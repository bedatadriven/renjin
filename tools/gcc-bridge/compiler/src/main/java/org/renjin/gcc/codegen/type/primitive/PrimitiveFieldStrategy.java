/*
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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;

public class PrimitiveFieldStrategy extends SingleFieldStrategy {

  private PrimitiveType type;

  public PrimitiveFieldStrategy(Type ownerClass, String fieldName, PrimitiveType fieldType) {
    super(ownerClass, fieldName, fieldType.jvmType());
    this.type = fieldType;
  }

  @Override
  public GExpr memberExpr(MethodGenerator mv, JExpr instance, int offset, int size, GimpleType expectedType) {

    JLValue fieldExpr = Expressions.field(instance, fieldType, fieldName);

    if(expectedType instanceof GimplePrimitiveType) {
      GimplePrimitiveType primitiveType = (GimplePrimitiveType) expectedType;
      if(!fieldExpr.getType().equals(primitiveType.jvmType())) {
        throw new UnsupportedOperationException("TODO: expectedType = " + expectedType);
      }

      if(size != 0 && (offset != 0 || size != type.gimpleType().getSize())) {
        if(!primitiveType.jvmType().equals(Type.BYTE_TYPE)) {
          throw new UnsupportedOperationException(
              String.format("Unsupported bitfield: expected type = %s, offset = %d, size = %d",
                  expectedType, offset, size));
        }
        fieldExpr = new BitFieldExpr(ownerClass, instance, fieldName, offset, size);
      }
      return type.fromStackValue(fieldExpr);

    } else {
      throw new UnsupportedOperationException("expectedType: " + expectedType);
    }
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    instance.load(mv);
    byteValue.load(mv);
    mv.invokestatic(Wrappers.wrapperType(fieldType), "memset", Type.getMethodDescriptor(fieldType, Type.INT_TYPE));
    mv.putfield(ownerClass, fieldName, fieldType);
  }
}
