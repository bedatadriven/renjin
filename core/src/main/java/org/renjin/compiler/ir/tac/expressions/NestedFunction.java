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
package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

public class NestedFunction implements Expression {

  private static final ValueBounds BOUNDS = ValueBounds.builder()
      .setTypeSet(TypeSet.FUNCTION)
      .build();

  private final PairList formals;
  private final SEXP body;

  public NestedFunction(PairList formals, SEXP body) {
    this.formals = formals;
    this.body = body;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(ValueBoundsMap typeMap) {
    return BOUNDS;
  }

  @Override
  public ValueBounds getValueBounds() {
    return BOUNDS;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {

        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(Closure.class));
        mv.dup();
        mv.visitVarInsn(Opcodes.ALOAD, emitContext.getEnvironmentVarIndex());
        emitContext.constantSexp(formals).loadSexp(emitContext, mv);
        mv.checkcast(Type.getType(PairList.class));
        emitContext.constantSexp(body).loadSexp(emitContext, mv);

        mv.invokespecial(Type.getInternalName(Closure.class), "<init>",
            Type.getMethodDescriptor(Type.VOID_TYPE,
              Type.getType(Environment.class),
              Type.getType(PairList.class),
              Type.getType(SEXP.class)), false);
      }
    };
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public int getChildCount() {
    return 0;
  }

  @Override
  public Expression childAt(int index) {
    throw new IndexOutOfBoundsException();
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    s.append("function(");
    boolean needsComma = false;
    for (PairList.Node node : formals.nodes()) {
      if(needsComma) {
        s.append(", ");
      }
      s.append(node.getName());
      needsComma = true;
    }
    s.append(") { ... }");
    return s.toString();
  }
}
