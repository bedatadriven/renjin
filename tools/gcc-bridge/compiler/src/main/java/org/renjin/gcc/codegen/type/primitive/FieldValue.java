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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;


public class FieldValue implements JLValue {
  
  private JExpr instance;
  private String fieldName;
  private Type fieldType;

  public FieldValue(JExpr instance, String fieldName, Type fieldType) {
    this.instance = instance;
    this.fieldName = fieldName;
    this.fieldType = fieldType;
  }

  @Nonnull
  @Override
  public Type getType() {
    return fieldType;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    instance.load(mv);
    mv.getfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
  }

  @Override
  public void store(MethodGenerator mv, JExpr value) {
    instance.load(mv);
    value.load(mv);
    mv.putfield(instance.getType().getInternalName(), fieldName, fieldType.getDescriptor());
  }
}
