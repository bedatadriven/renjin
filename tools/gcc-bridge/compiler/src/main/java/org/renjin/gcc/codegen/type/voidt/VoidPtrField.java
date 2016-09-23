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
package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.primitive.FieldValue;
import org.renjin.repackaged.asm.Type;

/**
 * Strategy for {@code void* } fields, compiled as a field of type
 * {@code java.lang.Object}
 */
public class VoidPtrField extends SingleFieldStrategy {


  public VoidPtrField(Type ownerClass, String fieldName) {
    super(ownerClass, fieldName, VoidPtrStrategy.OBJECT_TYPE);
  }

  @Override
  public VoidPtr memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {

    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }
    
    FieldValue ref = new FieldValue(instance, fieldName, VoidPtrStrategy.OBJECT_TYPE);
    return new VoidPtr(ref);
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    instance.load(mv);
    mv.aconst(null);
    mv.putfield(ownerClass, fieldName, VoidPtrStrategy.OBJECT_TYPE);
  }

}
