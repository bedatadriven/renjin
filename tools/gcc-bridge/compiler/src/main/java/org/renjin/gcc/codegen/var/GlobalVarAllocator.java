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
package org.renjin.gcc.codegen.var;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;
import org.renjin.repackaged.asm.ClassVisitor;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Allocates global variables as static fields within a class.
 */
public class GlobalVarAllocator extends VarAllocator {

  public class StaticField implements JLValue {

    private String name;
    private Type type;
    private Optional<JExpr> initialValue;

    public StaticField(String name, Type type, Optional<JExpr> initialValue) {
      this.name = name;
      this.type = type;
      this.initialValue = initialValue;
    }

    @Nonnull
    @Override
    public Type getType() {
      return type;
    }

    public Type getDeclaringClass() {
      return declaringClass;
    }

    public String getName() {
      return name;
    }

    @Override
    public void load(@Nonnull MethodGenerator mv) {
      mv.visitFieldInsn(Opcodes.GETSTATIC, declaringClass.getInternalName(), name, type.getDescriptor());
    }

    @Override
    public void store(MethodGenerator mv, JExpr value) {
      value.load(mv);
      mv.visitFieldInsn(Opcodes.PUTSTATIC, declaringClass.getInternalName(), name, type.getDescriptor());
    }
  }
  
  private final Type declaringClass;
  private final List<StaticField> fields = Lists.newArrayList();
  private final Set<String> fieldNames = Sets.newHashSet();

  public GlobalVarAllocator(String declaringClass) {
    this.declaringClass = Type.getType("L" + declaringClass + ";");
    assert this.declaringClass.getSort() == Type.OBJECT;
  }

  @Override
  public StaticField reserve(String name, Type type) {
    return reserve(name, type, Optional.empty());
  }

  public StaticField reserve(String name, Type type, Optional<JExpr> initialValue) {
    String fieldName = toJavaSafeName(name);
    if(fieldNames.contains(fieldName)) {
      throw new InternalCompilerException("Duplicate field name generated '" + name + "' [" + fieldName + "]");
    }
    fieldNames.add(fieldName);
    StaticField field = new StaticField(fieldName, type, initialValue);
    fields.add(field);
    return field;
  }

  @Override
  public StaticField reserve(String name, Type type, JExpr initialValue) {
    return reserve(name, type, Optional.of(initialValue));
  }

  public void writeFields(ClassVisitor cv) {
    for (StaticField field : fields) {
      cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, field.name, field.type.getDescriptor(), null, null);
    }
  }
  
  public boolean needsStaticInitializer() {
    for (StaticField field : fields) {
      if(field.initialValue.isPresent()) {
        return true;
      }
    }
    return false;
  }
  
  public void writeFieldInitialization(MethodGenerator mv) {
    for (StaticField field : fields) {
      if(field.initialValue.isPresent()) {
        JExpr initialValue = field.initialValue.get();
        initialValue.load(mv);
        mv.putstatic(declaringClass.getInternalName(), field.name, field.type.getDescriptor());
      }
    }
  }
  
}
