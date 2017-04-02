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
import org.renjin.gcc.codegen.fatptr.FatPtrPair;
import org.renjin.gcc.codegen.fatptr.FatPtrStrategy;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.fatptr.Wrappers;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveValueFunction;
import org.renjin.gcc.codegen.type.voidt.VoidPtrValueFunction;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

/**
 * A field which can point to any primitive type.
 */
public class PrimitivePointerUnionField extends FieldStrategy {

  private static final Type OBJECT_TYPE = Type.getType(Object.class);
  
  private Type declaringClass;
  private String arrayFieldName;
  private final String offsetFieldName;

  public PrimitivePointerUnionField(Type declaringClass, String name) {
    this.declaringClass = declaringClass;
    this.arrayFieldName = name;
    this.offsetFieldName = name + "$offset";
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayFieldName,  OBJECT_TYPE.getDescriptor(), null, null);
    cv.visitField(Opcodes.ACC_PUBLIC, offsetFieldName, Type.INT_TYPE.getDescriptor(), null, null);
  }

  @Override
  public GExpr memberExpr(MethodGenerator mv, JExpr instance, int offset, int size, TypeStrategy expectedType) {

    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }

    JLValue arrayExpr = Expressions.field(instance, OBJECT_TYPE, arrayFieldName);
    JLValue offsetExpr = Expressions.field(instance, Type.INT_TYPE, offsetFieldName);

    if(expectedType == null) {
      return new FatPtrPair(new VoidPtrValueFunction(), arrayExpr, offsetExpr);

    } else if(expectedType instanceof FatPtrStrategy) {
      ValueFunction valueFunction = expectedType.getValueFunction();
      if(valueFunction instanceof PrimitiveValueFunction) {
        Type baseType = valueFunction.getValueType();
        Type expectedArrayType = Wrappers.valueArrayType(baseType);
        JExpr castedArrayExpr = Expressions.cast(arrayExpr, expectedArrayType);

        return new FatPtrPair(new PrimitiveValueFunction(baseType), castedArrayExpr, offsetExpr);
      }
    } 
    throw new UnsupportedOperationException("Type: " + expectedType);
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {

    JLValue sourceArray = Expressions.field(source, Type.getType(Object.class), arrayFieldName);
    JLValue sourceOffset = Expressions.field(source, Type.INT_TYPE, offsetFieldName);
    JLValue destArray = Expressions.field(dest, Type.getType(Object.class), arrayFieldName);
    JLValue destOffset = Expressions.field(dest, Type.INT_TYPE, offsetFieldName);
    
    destArray.store(mv, sourceArray);
    destOffset.store(mv, sourceOffset);
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    instance.load(mv);
    mv.aconst(null);
    mv.putfield(declaringClass, arrayFieldName, OBJECT_TYPE);
    
    instance.load(mv);
    byteValue.load(mv);
    mv.putfield(declaringClass, offsetFieldName, Type.INT_TYPE);
  }

}
