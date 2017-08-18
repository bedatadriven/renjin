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
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.fatptr.Memset;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;


public class RecordArrayField extends FieldStrategy {
  
  private Type declaringClass;
  private String name;
  private RecordArrayValueFunction valueFunction;
  private Type arrayType;
  private int arrayLength;

  public RecordArrayField(Type declaringClass, String name,
                          Type elementType, int arrayLength, GimpleRecordType gimpleRecordType) {
    this.declaringClass = declaringClass;
    this.name = name;
    this.valueFunction = new RecordArrayValueFunction(elementType, arrayLength, gimpleRecordType);
    this.arrayType = Type.getType("[" + elementType.getDescriptor());
    this.arrayLength = arrayLength;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, name, arrayType.getDescriptor(), null, null).visitEnd();
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    JExpr sourceArray = Expressions.field(source, arrayType, name);
    JExpr destArray = Expressions.field(dest, arrayType, name);

    mv.arrayCopy(
        sourceArray, Expressions.constantInt(0),
        destArray, Expressions.constantInt(0),
        Expressions.constantInt(arrayLength));
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    JExpr arrayExpr = Expressions.field(instance, arrayType, name);
    
    Memset.primitiveMemset(mv, valueFunction.getValueType(), arrayExpr, Expressions.zero(), byteValue, byteCount);
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    JLValue arrayField = Expressions.field(Expressions.thisValue(declaringClass), arrayType, name);
    JExpr newArray = Expressions.newArray(Wrappers.componentType(arrayType), arrayLength);

    arrayField.store(mv, newArray);
  }

  @Override
  public GExpr memberExpr(MethodGenerator mv, JExpr instance, int offset, int size, GimpleType expectedType) {


    JLValue arrayField = Expressions.field(instance, arrayType, name);
    JExpr offsetIndex = Expressions.constantInt(offset / 8 / valueFunction.getArrayElementBytes());

    return valueFunction.dereference(arrayField, offsetIndex);
  }

}
