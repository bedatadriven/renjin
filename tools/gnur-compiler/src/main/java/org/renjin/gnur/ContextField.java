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
package org.renjin.gnur;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ContextField {
  private final Type contextClass;
  private String name;
  private Type type;
  private final Optional<JExpr> initialValue;


  public ContextField(Type contextClass, String varName, Type type, Optional<JExpr> initialValue) {
    this.contextClass = contextClass;
    name = varName;
    this.type = type;
    this.initialValue = initialValue;
  }

  public String getVarName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public String getGetterName() {
    return "get__" + name;
  }

  public String getGetterDescriptor() {
    return Type.getMethodDescriptor(type);
  }

  public String getSetterName() {
    return "set__" + name;
  }

  public String getSetterDescriptor() {
    return Type.getMethodDescriptor(Type.VOID_TYPE, type);
  }

  public Optional<JExpr> getInitialValue() {
    return initialValue;
  }

  public JLValue jvalue() {
    return new JLValue() {

      @Nonnull
      @Override
      public Type getType() {
        return type;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        if(mv.getOwnerClass().equals(contextClass)) {
          // In the context of our own constructor, use an instance field
          mv.visitVarInsn(Opcodes.ALOAD, 0);
          mv.visitFieldInsn(Opcodes.GETFIELD, contextClass.getInternalName(), name, type.getDescriptor());
        } else {
          // Otherwise use the static getter
          mv.invokestatic(contextClass, getGetterName(), getGetterDescriptor());
        }
      }

      @Override
      public void store(MethodGenerator mv, JExpr expr) {
        if(mv.getOwnerClass().equals(contextClass)) {
          // In the context of our own constructor, use an instance field
          mv.visitVarInsn(Opcodes.ALOAD, 0);
          expr.load(mv);
          mv.visitFieldInsn(Opcodes.PUTFIELD, contextClass.getInternalName(), name, type.getDescriptor());
        } else {
          // Otherwise use the static setter
          expr.load(mv);
          mv.invokestatic(contextClass, getSetterName(), getSetterDescriptor());
        }
      }
    };
  }

  public void writeFieldInit(MethodGenerator mv) {
    initialValue.ifPresent(value -> {
      mv.load(0, contextClass);
      value.load(mv);
      mv.putfield(contextClass, name, type);
    });
  }
}
