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
package org.renjin.gcc.codegen.type.fun;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;

public class FunPtrField extends SingleFieldStrategy {

  public FunPtrField(Type ownerClass, String fieldName) {
    super(ownerClass, fieldName, FunPtrStrategy.METHOD_HANDLE_TYPE);
  }

  @Override
  public GExpr memberExpr(MethodGenerator mv, JExpr instance, int offset, int size, GimpleType expectedType) {
    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }
    return new FunPtrExpr(Expressions.field(instance, FunPtrStrategy.METHOD_HANDLE_TYPE, fieldName));
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    memsetReference(mv, instance, byteValue, byteCount);
  }

}
