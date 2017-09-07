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
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.gcc.codegen.type.SingleFieldStrategy;
import org.renjin.gcc.codegen.type.primitive.FieldValue;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrExpr;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.Type;

import static org.renjin.repackaged.asm.Type.getMethodDescriptor;

/**
 * Generates a field with a record *value* type
 */
public class RecordClassFieldStrategy extends SingleFieldStrategy {
  private RecordClassTypeStrategy strategy;

  public RecordClassFieldStrategy(RecordClassTypeStrategy strategy, Type declaringClass, String fieldName) {
    super(declaringClass, fieldName, strategy.getJvmType());
    this.strategy = strategy;
  }

  @Override
  public void emitInstanceInit(MethodGenerator mv) {
    JExpr thisRef = Expressions.thisValue(this.ownerClass);
    JLValue fieldRef = Expressions.field(thisRef, strategy.getJvmType(), fieldName);
    
    JExpr newInstance = Expressions.newObject(strategy.getJvmType());

    fieldRef.store(mv, newInstance);
  }

  @Override
  public RecordValue memberExpr(MethodGenerator mv, JExpr instance, int offset, int size, GimpleType expectedType) {

    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }

    JLValue value = Expressions.field(instance, strategy.getJvmType(), fieldName);
    RecordUnitPtrExpr address = new RecordUnitPtrExpr(strategy.getLayout(), value);
    
    return new RecordValue(strategy.getLayout(), value, address);
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    // Load the field value onto the stack
    instance.load(mv);
    mv.getfield(ownerClass, fieldName, fieldType);
    
    // Invoke the field's class's memset() method
    byteValue.load(mv);
    byteCount.load(mv);
    mv.invokevirtual(fieldType, "memset", getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE), false);
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    FieldValue sourceExpr = new FieldValue(source, fieldName, fieldType);
    FieldValue destExpr = new FieldValue(dest, fieldName, fieldType);
    RecordValue clonedValue = strategy.clone(mv, new RecordValue(strategy.getLayout(), sourceExpr));
    destExpr.store(mv, clonedValue.unwrap());
  }
}
