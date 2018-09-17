/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.ir.tac.expressions;

import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.codegen.BytecodeTypes;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ScalarExpr;
import org.renjin.compiler.codegen.expr.SexpExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.codegen.var.VariableStrategy;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;

import java.util.Collections;
import java.util.Map;

public class ApplyExpression implements Expression {

  private Expression vector;
  private InlinedFunction function;
  private ValueBounds functionResultBounds = ValueBounds.UNBOUNDED;
  private ValueBounds resultBounds;

  public ApplyExpression(Expression vector, InlinedFunction function) {
    this.vector = vector;
    this.function = function;
    this.resultBounds = ValueBounds.builder()
        .setTypeSet(TypeSet.LIST)
        .build();
  }

  @Override
  public boolean isPure() {
    return false;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {

    ValueBounds vectorBounds = typeMap.get(vector);
    ValueBounds elementBounds;
    if(TypeSet.isDefinitelyAtomic(vectorBounds.getTypeSet())) {
      elementBounds = ValueBounds.builder()
          .setTypeSet(vectorBounds.getTypeSet())
          .addFlags(ValueBounds.LENGTH_ONE)
          .addFlagsFrom(vectorBounds, ValueBounds.FLAG_NO_NA | ValueBounds.FLAG_POSITIVE)
          .build();
    } else {
      elementBounds = ValueBounds.UNBOUNDED;
    }

    functionResultBounds = function.updateBounds(Collections.singletonList(new ArgumentBounds(elementBounds)));

    resultBounds = ValueBounds.builder()
        .setTypeSet(TypeSet.LIST)
        .addFlagsFrom(vectorBounds, ValueBounds.LENGTH_NON_ZERO)
        .build();

    return resultBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return resultBounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new SexpExpr() {
      @Override
      public void loadSexp(EmitContext context, InstructionAdapter mv) {

        CompiledSexp vectorEmitter = vector.getCompiledExpr(context);

        // Temp to hold length
        int lengthVar = context.getLocalVarAllocator().reserve(Type.INT_TYPE);
        vectorEmitter.loadLength(context, mv);
        mv.dup();
        mv.visitVarInsn(Opcodes.ISTORE, lengthVar);

        // Start by creating an array to hold the results
        int resultVar = context.getLocalVarAllocator().reserve(Type.getType("[Lorg/renjin/sexp/SEXP;"));
        // Length still on stack from above call to dup
        mv.newarray(BytecodeTypes.SEXP_TYPE);
        mv.visitVarInsn(Opcodes.ASTORE, resultVar);


        // Define a variable to hold the loop counter
        int counterVar = context.getLocalVarAllocator().reserve(Type.INT_TYPE);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitVarInsn(Opcodes.ISTORE, counterVar);

        // Now we need a loop over each of the elements
        Label loopHead = new Label();
        Label exitLabel = new Label();

        mv.visitLabel(loopHead);
        mv.visitVarInsn(Opcodes.ILOAD, counterVar);
        mv.visitVarInsn(Opcodes.ILOAD, lengthVar);
        mv.visitJumpInsn(Opcodes.IF_ICMPGE, exitLabel);

        // Loop body!
        CompiledSexp elementEmitter = vectorEmitter.elementAt(context, new ScalarExpr(VectorType.INT) {
          @Override
          public void loadScalar(EmitContext context, InstructionAdapter mv) {
            mv.visitVarInsn(Opcodes.ILOAD, counterVar);
          }
        });

        VariableStrategy functionValue = new VariableStrategy() {
          @Override
          public CompiledSexp getCompiledExpr() {
            throw new UnsupportedOperationException();
          }

          @Override
          public void store(EmitContext emitContext, InstructionAdapter mv, CompiledSexp compiledSexp) {
            mv.visitVarInsn(Opcodes.ALOAD, resultVar);
            mv.visitVarInsn(Opcodes.ILOAD, counterVar);
            compiledSexp.loadSexp(emitContext, mv);
            mv.visitInsn(Opcodes.AASTORE);
          }
        };

        function.emitInline(context, mv, Collections.singletonList(elementEmitter), functionValue);

        mv.visitIincInsn(counterVar, 1);
        mv.goTo(loopHead);

        mv.visitLabel(exitLabel);

        // Store the result as an SEXP expression
        mv.visitVarInsn(Opcodes.ALOAD, resultVar);
        mv.invokestatic("org/renjin/sexp/ListVector", "of",
            "([Lorg/renjin/sexp/SEXP;)Lorg/renjin/sexp/ListVector;", false);
      }
    };
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return vector;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      vector = child;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public String toString() {
    return "lapply(" + vector + ", fun())";
  }
}
