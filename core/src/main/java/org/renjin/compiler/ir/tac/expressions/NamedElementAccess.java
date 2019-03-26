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
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.eval.Context;
import org.renjin.primitives.special.DollarFunction;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

import java.util.Map;

/**
 * Element access in the form x$name
 */
public class NamedElementAccess implements Expression {

  private Expression expression;
  private final FunctionCall call;
  private String memberName;
  private ValueBounds valueBounds;

  public NamedElementAccess(Expression expression, FunctionCall call, String memberName) {
    this.expression = expression;
    this.call = call;
    this.memberName = memberName;
    
    valueBounds = ValueBounds.UNBOUNDED;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {
    
    ValueBounds argumentBounds = typeMap.get(expression);
    if(argumentBounds.isConstant()) {
      // Handle the cases where the $ function is pure
      // if the object is a environment or an external pointer, then
      // the operation may have side-effects.
      SEXP object = argumentBounds.getConstantValue();
      if(object instanceof ListVector) {
        valueBounds = ValueBounds.constantValue(DollarFunction.fromList((ListVector) object, memberName));
      } else if(object instanceof PairList) {
        valueBounds = ValueBounds.constantValue(DollarFunction.fromPairList((PairList) object, memberName));
      }
    }
    
    return valueBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return valueBounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    CompiledSexp compiledExpr = expression.getCompiledExpr(emitContext);
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {
        // SEXP apply(Context context, Environment rho, FunctionCall call, SEXP object, String name)
        mv.visitVarInsn(Opcodes.ALOAD, context.getContextVarIndex());
        mv.visitVarInsn(Opcodes.ALOAD, context.getEnvironmentVarIndex());
        emitContext.constantSexp(call).loadSexp(context, mv);
        mv.checkcast(Type.getType(FunctionCall.class));
        compiledExpr.loadSexp(context, mv);
        mv.aconst(memberName);
        mv.invokestatic(Type.getInternalName(DollarFunction.class), "apply",
            Type.getMethodDescriptor(Type.getType(SEXP.class),
                Type.getType(Context.class),
                Type.getType(Environment.class),
                Type.getType(FunctionCall.class),
                Type.getType(SEXP.class),
                Type.getType(String.class)), false);
      }
    };
  }


  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex != 0) {
      throw new IllegalArgumentException("childIndex:" + childIndex);
    }
    expression = child;
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    return expression;
  }


  @Override
  public String toString() {
    return expression + "$" + memberName;
  }

}
