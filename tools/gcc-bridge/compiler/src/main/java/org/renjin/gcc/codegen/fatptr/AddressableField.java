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
package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import java.util.Optional;


public class AddressableField extends FieldStrategy {

  private final Type recordType;
  private final String arrayField;
  private final Type arrayType;
  private ValueFunction valueFunction;
  

  public AddressableField(Type recordType, String fieldName, ValueFunction valueFunction) {
    this.recordType = recordType;
    this.arrayField = fieldName;
    this.arrayType = Type.getType("[" + valueFunction.getValueType().getDescriptor());
    this.valueFunction = valueFunction;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayField, arrayType.getDescriptor(), null, null);
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    
    // Reference value types like records and fat pointers may need
    // to initialize a value
    Optional<JExpr> initialValues = valueFunction.getValueConstructor();
    
    // Allocate a unit array store the value
    // (for value types like complex, this might actually be several elements)
    JExpr unitArray = Expressions.newArray(valueFunction.getValueType(), valueFunction.getElementLength(), initialValues);

    // Store this to the array field
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    unitArray.load(mv);
    mv.putfield(recordType, arrayField, arrayType);
  }

  @Override
  public GExpr memberExpr(MethodGenerator mv, JExpr instance, int offset, int size, GimpleType expectedType) {

    if(offset != 0) {
      throw new UnsupportedOperationException("TODO: offset = " + offset);
    }

    return dereference(instance);
  }

  private GExpr dereference(JExpr instance) {
    JExpr arrayExpr = Expressions.field(instance, arrayType, arrayField);

    return valueFunction.dereference(arrayExpr, Expressions.zero());
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    GExpr sourceExpr = dereference(source);
    GExpr destExpr = dereference(dest);
    destExpr.store(mv, sourceExpr);
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    JLValue arrayField = Expressions.field(instance, arrayType, this.arrayField);
    valueFunction.memorySet(mv, arrayField, Expressions.zero(), byteValue, byteCount);
  }

}
