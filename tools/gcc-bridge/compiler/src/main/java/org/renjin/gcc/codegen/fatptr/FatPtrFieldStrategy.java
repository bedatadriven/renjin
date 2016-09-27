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
package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;


public class FatPtrFieldStrategy extends FieldStrategy {

  private Type ownerClass;
  private ValueFunction valueFunction;
  private String arrayField;
  private String offsetField;
  private Type arrayType;

  public FatPtrFieldStrategy(Type ownerClass, ValueFunction valueFunction, String name, Type arrayType) {
    this.ownerClass = ownerClass;
    this.valueFunction = valueFunction;
    this.arrayField = name;
    this.offsetField = name + "$offset";
    this.arrayType = arrayType;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    cv.visitField(Opcodes.ACC_PUBLIC, arrayField, arrayType.getDescriptor(), null, null);
    cv.visitField(Opcodes.ACC_PUBLIC, offsetField, "I", null, null);
  }

  @Override
  public FatPtrPair memberExpr(JExpr instance, int offset, int size, TypeStrategy expectedType) {

    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }
    return memberExpr(instance);
  }

  private FatPtrPair memberExpr(JExpr instance) {
    JExpr arrayExpr = Expressions.field(instance, arrayType, arrayField);
    JExpr offsetExpr = Expressions.field(instance, Type.INT_TYPE, offsetField);
    return new FatPtrPair(valueFunction, arrayExpr, offsetExpr);
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    FatPtrPair sourceExpr = memberExpr(source);
    FatPtrPair destExpr = memberExpr(dest);
    destExpr.store(mv, sourceExpr);
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    // Any value will lead to a garbage pointer, so we assume
    // that assigning NULL will have the same effect
    instance.load(mv);
    mv.aconst(null);
    mv.putfield(ownerClass, arrayField, arrayType);
    
    instance.load(mv);
    byteValue.load(mv);
    mv.putfield(ownerClass, offsetField, Type.INT_TYPE);
  }

}
