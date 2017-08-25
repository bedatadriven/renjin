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
package org.renjin.gcc.codegen.vptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.type.*;
import org.renjin.gcc.codegen.var.LocalVarAllocator;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleConstructor;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.runtime.MixedPtr;
import org.renjin.gcc.runtime.PointerPtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Type;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class VArrayStrategy implements TypeStrategy<VArrayExpr> {

  private GimpleArrayType arrayType;

  public VArrayStrategy(GimpleArrayType arrayType) {
    this.arrayType = arrayType;
  }

  @Override
  public ParamStrategy getParamStrategy() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public ValueFunction getValueFunction() {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VArrayExpr variable(GimpleVarDecl decl, VarAllocator allocator) {
    JExpr malloc = VPtrStrategy.malloc(arrayType, Expressions.constantInt(arrayType.sizeOf())).getRef();
    JLValue var = allocator.reserve(decl.getName(), Type.getType(Ptr.class), malloc);

    return new VArrayExpr(arrayType, new VPtrExpr(var));
  }

  @Override
  public VArrayExpr providedGlobalVariable(GimpleVarDecl decl, Field javaField) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public VArrayExpr constructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {

    if(arrayType.getComponentType() instanceof GimpleIndirectType) {
      return pointerArrayConstructorExpr(exprFactory, mv, value);
    }
    if(arrayType.getComponentType() instanceof GimpleRecordType) {
      return mixedRecordArrayConstructor(exprFactory, mv, value);
    }

    throw new UnsupportedOperationException("TODO: " + arrayType);
  }

  private VArrayExpr mixedRecordArrayConstructor(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {

    // Create a temporary variable for this constructed record array.
    Type pointerType = Type.getType(MixedPtr.class);
    VPtrExpr malloc = VPtrStrategy.malloc(pointerType, Expressions.constantInt(arrayType.sizeOf()));
    VPtrExpr tempVar = new VPtrExpr(mv.getLocalVarAllocator().reserve(pointerType));
    tempVar.store(mv, malloc);

    int offset = 0;
    for (GimpleConstructor.Element element : value.getElements()) {
      GimpleConstructor recordCtor = (GimpleConstructor) element.getValue();
      for (GimpleConstructor.Element fieldCtor : recordCtor.getElements()) {
        GimpleFieldRef field = (GimpleFieldRef) fieldCtor.getField();
        GExpr fieldExpr = exprFactory.findGenerator(fieldCtor.getValue(), field.getType());

        tempVar.valueOf(field.getType(), Expressions.constantInt(offset + field.getOffsetBytes()))
            .store(mv, fieldExpr);
      }
      offset += arrayType.getComponentType().sizeOf();
    }

    return new VArrayExpr(arrayType, tempVar);
  }

  private VArrayExpr pointerArrayConstructorExpr(ExprFactory exprFactory, MethodGenerator mv, GimpleConstructor value) {

    List<JExpr> pointers = new ArrayList<>();
    for (GimpleConstructor.Element element : value.getElements()) {
      GExpr ptrExpr = exprFactory.findGenerator(element.getValue(), arrayType.getComponentType());
      pointers.add(ptrExpr.toVPtrExpr().getRef());
    }

    JExpr array = Expressions.newArray(Type.getType(Ptr.class), arrayType.getElementCount(), pointers);

    VPtrExpr pointer = new VPtrExpr(Expressions.newObject(Type.getType(PointerPtr.class), array));
    VArrayExpr arrayExpr = new VArrayExpr(arrayType, pointer);

    return arrayExpr;
  }

  @Override
  public FieldStrategy fieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public FieldStrategy addressableFieldGenerator(Type className, String fieldName) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public PointerTypeStrategy pointerTo() {
    return new VPtrStrategy(arrayType);
  }

  @Override
  public VArrayStrategy arrayOf(GimpleArrayType arrayType) {
    return new VArrayStrategy(arrayType);
  }

  @Override
  public VArrayExpr cast(MethodGenerator mv, GExpr value, TypeStrategy typeStrategy) throws UnsupportedCastException {
    throw new UnsupportedOperationException("TODO");
  }
}
