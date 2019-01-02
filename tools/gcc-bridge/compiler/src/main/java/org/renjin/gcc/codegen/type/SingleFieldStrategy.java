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
package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.primitive.FieldValue;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

public abstract class SingleFieldStrategy extends FieldStrategy {
  
  protected final Type ownerClass;
  protected final String fieldName;
  protected final Type fieldType;

  public SingleFieldStrategy(Type ownerClass, String fieldName, Type fieldType) {
    this.ownerClass = ownerClass;
    this.fieldName = fieldName;
    this.fieldType = fieldType;
    Preconditions.checkNotNull(fieldName);
    Preconditions.checkArgument(!fieldName.isEmpty(), "fieldName cannot be empty");
  }

  @Override
  public final void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, fieldName, fieldType.getDescriptor(), null, null);
  }
  
  protected final void memsetReference(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr count) {
    instance.load(mv);
    mv.aconst(null);
    mv.putfield(ownerClass, fieldName, fieldType);
  }
  
  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    FieldValue sourceExpr = new FieldValue(source, fieldName, fieldType);
    FieldValue destExpr = new FieldValue(dest, fieldName, fieldType);
    destExpr.store(mv, sourceExpr);
  }
}
