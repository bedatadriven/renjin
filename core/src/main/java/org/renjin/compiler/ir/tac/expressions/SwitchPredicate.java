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

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.codegen.expr.ConditionalExpr;
import org.renjin.compiler.codegen.expr.VectorType;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.primitives.special.SwitchFunction;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.sexp.*;

import java.util.Map;
import java.util.Objects;

/**
 * A boolean-valued expression that supports the evaluation of R switch statements.
 *
 * <p>The R "switch" function is special in that can either switch on a string value, or on
 * an integer value, and we won't know which until runtime. So this expression matches either
 * an integer or a string, depending on the type found at runtime.</p>
 */
public class SwitchPredicate implements Expression {

  private static final ValueBounds LOWER_BOUNDS = ValueBounds.builder()
    .setTypeSet(TypeSet.LOGICAL)
    .addFlags(ValueBounds.FLAG_NO_NA | ValueBounds.LENGTH_ONE)
    .build();

  private final int branchNumber;
  private final String branchName;
  private final boolean finalBranch;

  private Expression expression;
  private ValueBounds expressionBounds;

  private ValueBounds bounds = LOWER_BOUNDS;

  /**
   * @param branchNumber Zero-based index of the branch within the switch statement
   */
  public SwitchPredicate(Expression expression, int branchNumber, SEXP tag) {
    assert branchNumber > 0;

    this.expression = expression;
    this.branchNumber = branchNumber;
    this.finalBranch = false;
    if(tag instanceof Symbol) {
      this.branchName = ((Symbol) tag).getPrintName();
    } else {
      this.branchName = null;
    }
  }

  private SwitchPredicate(Expression expression, int branchNumber) {
    this.expression = expression;
    this.branchNumber = branchNumber;
    this.branchName = null;
    this.finalBranch = true;
  }

  public static SwitchPredicate finalUnnamedBranch(Expression expression, int branchNumber) {
    return new SwitchPredicate(expression, branchNumber);
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {

    expressionBounds = expression.updateTypeBounds(typeMap);

    if(expressionBounds.isConstant()) {
      bounds = evaluate(expressionBounds.getConstantValue());

    } else if(expressionBounds.getTypeSet() == TypeSet.STRING && branchName == null) {
      if(finalBranch) {
        bounds = ValueBounds.constantValue(LogicalVector.TRUE);
      } else {
        bounds = ValueBounds.constantValue(LogicalVector.FALSE);
      }

    } else {
      bounds = LOWER_BOUNDS;
    }
    return bounds;
  }

  private ValueBounds evaluate(SEXP expr) {
    if(expr instanceof StringVector) {
      String name = SwitchFunction.branchName(expr);
      return ValueBounds.constantValue(LogicalVector.valueOf(name.equals(branchName)));
    } else if(expr instanceof AtomicVector) {
      int number = SwitchFunction.branchNumber((AtomicVector) expr);
      return ValueBounds.constantValue(LogicalVector.valueOf(number == branchNumber));
    } else {
      return ValueBounds.UNBOUNDED;
    }
  }

  @Override
  public ValueBounds getValueBounds() {
    return bounds;
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return new ConditionalExpr() {
      @Override
      public void jumpIfTrue(EmitContext context, InstructionAdapter mv, Label trueLabel) {

        CompiledSexp compiledExpr = expression.getCompiledExpr(context);

        int exprTypeSet = expressionBounds.getTypeSet();

        if(exprTypeSet == TypeSet.STRING) {

          // Match by name
          compiledExpr.loadScalar(context, mv, VectorType.STRING);
          if(branchName == null) {
            mv.aconst(null);
          } else {
            mv.visitLdcInsn(branchName);
          }

          mv.invokestatic(Type.getInternalName(Objects.class), "equals",
              "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);

          mv.visitJumpInsn(Opcodes.IFNE, trueLabel);

        } else if(!TypeSet.mightBe(exprTypeSet, TypeSet.STRING) &&
                  TypeSet.isDefinitelyAtomic(exprTypeSet)) {

          // Match by branch number

          compiledExpr.loadScalar(context, mv, VectorType.INT);
          mv.visitLdcInsn(branchNumber);

          mv.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);

        } else {

          // We don't know whether we're matching against a branch number, a name,
          // or whether expr is valid at all, so defer to runtime support in SwitchFunction

          compiledExpr.loadSexp(context, mv);
          mv.visitLdcInsn(branchNumber);

          if(finalBranch) {
            mv.invokestatic(Type.getInternalName(SwitchFunction.class), "testFinal",
                "(Lorg/renjin/sexp/SEXP;I)Z", false);
          } else if(branchName == null) {
            mv.invokestatic(Type.getInternalName(SwitchFunction.class), "test",
                "(Lorg/renjin/sexp/SEXP;I)Z", false);
          } else {
            mv.visitLdcInsn(branchName);
            mv.invokestatic(Type.getInternalName(SwitchFunction.class), "test",
                "(Lorg/renjin/sexp/SEXP;ILjava/lang/String;)Z", false);
          }

          mv.visitJumpInsn(Opcodes.IFNE, trueLabel);
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
    assert index == 0;
    return expression;
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    this.expression = child;
  }

  @Override
  public String toString() {
    if(branchName == null) {
      return "switchP(" + expression + " == " + branchNumber + ")";
    } else {
      return "switchP(" + expression + " == " + branchNumber + " or '" + branchName + "')";
    }
  }
}
