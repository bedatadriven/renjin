/**
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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.Map;

/**
 * Reads the initial value of a variable from the R environment
 */
public class ReadEnvironment implements Expression {

  private Symbol name;
  private ValueBounds valueBounds;

  public ReadEnvironment(Symbol name, ValueBounds valueBounds) {
    this.name = name;
    this.valueBounds = valueBounds;
  }

  @Override
  public boolean isPure() {
    return false;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {
    mv.visitVarInsn(Opcodes.ALOAD, emitContext.getEnvironmentVarIndex());
    mv.visitLdcInsn(name.getPrintName());
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Symbol.class), "get", 
        Type.getMethodDescriptor(Type.getType(Symbol.class), Type.getType(String.class)), false);
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Environment.class), "findVariableUnsafe",
        Type.getMethodDescriptor(Type.getType(SEXP.class), Type.getType(Symbol.class)), false);
    mv.visitVarInsn(Opcodes.ALOAD, emitContext.getContextVarIndex());
    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(SEXP.class), "force", 
        Type.getMethodDescriptor(Type.getType(SEXP.class), Type.getType(Context.class)), true);
    return 2;
  }

  @Override
  public Type getType() {
    return Type.getType(SEXP.class);
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    return valueBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IllegalArgumentException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IllegalArgumentException();
  }

  @Override
  public String toString() {
    return "read(" + name + " = " + valueBounds + ")";
  }
}
