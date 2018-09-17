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
import org.renjin.sexp.Vector;

import java.util.Map;

import static java.util.Collections.singletonList;

public class ApplyExpression implements Expression {

  private Expression vector;
  private InlinedFunction function;
  private final boolean simplify;
  private final boolean useNames = true;
  private ValueBounds resultBounds;

  public ApplyExpression(Expression vector, InlinedFunction function, boolean simplify) {
    this.vector = vector;
    this.function = function;
    this.simplify = simplify;
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

    } else if(vectorBounds.getShape() != null) {
      elementBounds = vectorBounds.getShape().getElementBounds();

    } else {
      elementBounds = ValueBounds.UNBOUNDED;
    }

    ValueBounds functionResultBounds = function.updateBounds(singletonList(new ArgumentBounds(elementBounds)));

    resultBounds = maybeSimplify(vectorBounds, functionResultBounds);

    return resultBounds;
  }

  private ValueBounds maybeSimplify(ValueBounds vectorBounds, ValueBounds functionBounds) {

    if(simplify) {
      if(canBeSimplified(functionBounds)) {

        return ValueBounds.builder()
            .setTypeSet(functionBounds.getTypeSet())
            .addFlagsFrom(vectorBounds, ValueBounds.LENGTH_NON_ZERO)
            .build();
      }
    }

    return ValueBounds.builder()
        .setTypeSet(TypeSet.LIST)
        .addFlagsFrom(vectorBounds, ValueBounds.LENGTH_NON_ZERO)
        .build();
  }

  private boolean canBeSimplified(ValueBounds functionBounds) {
    return functionBounds.isFlagSet(ValueBounds.LENGTH_ONE) &&
        TypeSet.isDefinitelyAtomic(functionBounds.getTypeSet()) &&
        !TypeSet.mightBe(functionBounds.getTypeSet(), TypeSet.NULL);
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

        // If this is an sapply() call, and we know that all results will
        // yield scalars, then we don't have to box the individual results and
        // can store them directly to a primitive array.

        final VectorType scalarType;
        if(TypeSet.isSpecificAtomic(resultBounds.getTypeSet())) {
          scalarType = VectorType.of(resultBounds.getTypeSet());
        } else {
          scalarType = null;
        }

        CompiledSexp vectorEmitter = vector.getCompiledExpr(context);

        // Temp to hold length
        int lengthVar = context.getLocalVarAllocator().reserve(Type.INT_TYPE);
        vectorEmitter.loadLength(context, mv);
        mv.dup();
        mv.visitVarInsn(Opcodes.ISTORE, lengthVar);

        // Start by creating an array to hold the results
        // Length still on stack from above call to dup
        int resultVar;
        if(scalarType == null) {
          resultVar = context.getLocalVarAllocator().reserve(Type.getType("[Lorg/renjin/sexp/SEXP;"));
          mv.newarray(BytecodeTypes.SEXP_TYPE);
        } else {
          resultVar = context.getLocalVarAllocator().reserve(scalarType.getJvmArrayType());
          mv.newarray(scalarType.getJvmType());
        }

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
            if(scalarType == null) {
              compiledSexp.loadSexp(emitContext, mv);
              mv.visitInsn(Opcodes.AASTORE);
            } else {
              compiledSexp.loadScalar(context, mv, scalarType);
              mv.astore(scalarType.getJvmType());
            }
          }
        };

        function.emitInline(context, mv, singletonList(elementEmitter), functionValue);

        mv.visitIincInsn(counterVar, 1);
        mv.goTo(loopHead);

        mv.visitLabel(exitLabel);

        // Store the result as an SEXP expression
        if(scalarType == null) {
          vectorEmitter.loadSexp(context, mv);
          mv.visitVarInsn(Opcodes.ALOAD, resultVar);
          mv.visitLdcInsn(simplify ? 1 : 0);
          mv.visitLdcInsn(useNames ? 1 : 0);
          mv.invokestatic("org/renjin/primitives/special/ApplyFunction", "build",
              Type.getMethodDescriptor(Type.getType(Vector.class),
                  BytecodeTypes.SEXP_TYPE,
                  Type.getType("[Lorg/renjin/sexp/SEXP;"),
                  Type.BOOLEAN_TYPE,
                  Type.BOOLEAN_TYPE), false);
        } else {
          vectorEmitter.loadSexp(context, mv);
          mv.visitVarInsn(Opcodes.ALOAD, resultVar);
          mv.visitLdcInsn(useNames ? 1 : 0);
          mv.invokestatic("org/renjin/primitives/special/ApplyFunction", "build",
              Type.getMethodDescriptor(Type.getType(Vector.class),
                  BytecodeTypes.SEXP_TYPE,
                  scalarType.getJvmArrayType(),
                  Type.BOOLEAN_TYPE), false);
        }
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
