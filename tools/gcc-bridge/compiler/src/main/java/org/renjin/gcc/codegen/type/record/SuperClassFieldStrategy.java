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
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.FieldStrategy;
import org.renjin.gcc.codegen.type.record.unit.RecordUnitPtrExpr;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

import static org.renjin.repackaged.asm.Type.*;

/**
 * Models a field at the beginning of a record as a JVM superclass
 */
public class SuperClassFieldStrategy extends FieldStrategy {
  
  private RecordClassTypeStrategy fieldTypeStrategy;

  public SuperClassFieldStrategy(RecordClassTypeStrategy fieldTypeStrategy) {
    this.fieldTypeStrategy = fieldTypeStrategy;
  }

  @Override
  public void writeFields(ClassVisitor cv) {
    // NOOP 
  }

  @Override
  public GExpr memberExpr(MethodGenerator mv, final JExpr instance, final int offset, int size, GimpleType expectedType) {

    if(offset != 0) {
      throw new IllegalStateException("offset = " + offset);
    }

    JExpr superInstance = new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return fieldTypeStrategy.getJvmType();
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        instance.load(mv);
      }
    };

    return new RecordValue(fieldTypeStrategy.getLayout(), superInstance,
        new RecordUnitPtrExpr(fieldTypeStrategy.getLayout(), superInstance));
  }

  @Override
  public void copy(MethodGenerator mv, JExpr source, JExpr dest) {
    // call super.set()
    dest.load(mv);
    source.load(mv);
    mv.invokevirtual(fieldTypeStrategy.getJvmType(), "set", 
        getMethodDescriptor(VOID_TYPE, fieldTypeStrategy.getJvmType()), false);
  }

  @Override
  public void memset(MethodGenerator mv, JExpr instance, JExpr byteValue, JExpr byteCount) {
    // super.memset(byteValue, count)
    Type superType = fieldTypeStrategy.getJvmType();
    instance.load(mv);
    byteValue.load(mv);
    byteCount.load(mv);
    mv.invokevirtual(superType, "memset", getMethodDescriptor(VOID_TYPE, INT_TYPE, INT_TYPE), false);
  }

  public Type getType() {
    return fieldTypeStrategy.getJvmType();
  }
}
