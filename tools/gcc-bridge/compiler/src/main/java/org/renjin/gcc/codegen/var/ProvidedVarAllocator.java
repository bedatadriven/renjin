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
package org.renjin.gcc.codegen.var;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

/**
 * Allocates global variables that are declared in an existing class.
 */
public class ProvidedVarAllocator extends VarAllocator {
  
  private final Class declaringClass;

  public ProvidedVarAllocator(Class declaringClass) {
    this.declaringClass = declaringClass;
  }

  

  @Override
  public JLValue reserve(final String name, final Type type) {
    // verify that the field already exists in this class
    Field field;
    try {
      field = declaringClass.getField(name);
    } catch (NoSuchFieldException e) {
      throw new InternalCompilerException("The field '" + name + "' does not exist in class " + 
          declaringClass.getName());
    }
    Type declaredType = Type.getType(field.getType());
    if(!declaredType.equals(type)) {
      throw new InternalCompilerException(String.format(
          "Type mismatch between provided field '%s: expected %s but found %s", name, type, declaredType));
    }
    return new JLValue() {

      @Nonnull
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(declaringClass), name, type.getDescriptor());
      }

      @Override
      public void store(MethodGenerator mv, JExpr value) {
        value.load(mv);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, Type.getInternalName(declaringClass), name, type.getDescriptor());
      }
    };
  }

  @Override
  public JLValue reserve(String name, Type type, JExpr initialValue) {
    throw new UnsupportedOperationException("TO CHECK");
  }
  
}
