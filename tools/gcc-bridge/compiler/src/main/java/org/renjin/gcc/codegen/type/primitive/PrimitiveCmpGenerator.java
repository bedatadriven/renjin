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
package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;

import static org.renjin.gcc.codegen.expr.Expressions.flip;
import static org.renjin.repackaged.asm.Opcodes.*;

/**
 * Generates codes for binary comparisons
 */
public class PrimitiveCmpGenerator implements ConditionGenerator {
  
  private GimpleOp op;
  private JExpr x;
  private JExpr y;

  
  public static PrimitiveCmpGenerator unsigned(GimpleOp op, JExpr x, JExpr y) {
    Preconditions.checkArgument(x.getType().equals(y.getType()));
    
    // For straight-up equality checks, nothing special is needed for unsigned numbers
    if(op == GimpleOp.EQ_EXPR || op == GimpleOp.NE_EXPR) {
      return new PrimitiveCmpGenerator(op, x, y);
    }
    
    // The JVM has exactly ONE unsigned integer type: char
    // So nothing special needed here.
    if (x.getType().equals(Type.CHAR_TYPE)) {
      return new PrimitiveCmpGenerator(op, x, y);
    }

    // Otherwise, map unsigned space to signed one [2^63,2^63-1] before the comparison
    return new PrimitiveCmpGenerator(op, flip(x), flip(y));
  }
  
  
  public PrimitiveCmpGenerator(GimpleOp op, JExpr x, JExpr y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {

    Type tx = x.getType();
    Type ty = y.getType();
    
    if(!tx.equals(ty)) {
      throw new UnsupportedOperationException("Type mismatch: " + tx + " != " + ty);
    }

    if(op == GimpleOp.ORDERED_EXPR) {
      emitOrderedJump(mv, trueLabel, falseLabel);
      return;
    }
    if(op == GimpleOp.UNORDERED_EXPR) {
      emitOrderedJump(mv, falseLabel, trueLabel);
      return;
    }

    
    // Push two operands on the stack
    x.load(mv);
    y.load(mv);

    if(tx.equals(Type.DOUBLE_TYPE) ||
        ty.equals(Type.FLOAT_TYPE)) {


      emitRealJump(mv, trueLabel);

    } else {
      
      mv.visitJumpInsn(integerComparison(), trueLabel);
    } 
    
    mv.visitJumpInsn(GOTO, falseLabel);
  }

  private void emitOrderedJump(MethodGenerator mv, Label orderedLabel, Label unorderedLabel) {
    x.load(mv);
    mv.invokestatic(Double.class, "isNaN", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, x.getType()));
    mv.ifne(unorderedLabel);
    y.load(mv);
    mv.invokestatic(Double.class, "isNaN", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, y.getType()));
    mv.ifne(unorderedLabel);
    mv.goTo(orderedLabel);
  }

  private int integerComparison() {
    switch (op) {
      case LT_EXPR:
        return IF_ICMPLT;
      case LE_EXPR:
        return IF_ICMPLE;
      case EQ_EXPR:
        return IF_ICMPEQ;
      case NE_EXPR:
        return IF_ICMPNE;
      case GT_EXPR:
        return IF_ICMPGT;
      case GE_EXPR:
        return IF_ICMPGE;
    }
    throw new UnsupportedOperationException("op: " + op);
  }


  private void emitRealJump(MethodGenerator mv, Label trueLabel) {


    // Branching on floating point comparisons requires two steps:
    // First we have to do the actual comparison, using DCMPG/DCMPL/FCMPL/FCMPG,
    // which compares the two operands and pushes -1, 0, or 1 onto the stack

    // Then we can compare this value to zero and branch on the result.

    // But because we have floating points, we need to be mindful of NaN values.

    //            CMPG:     CMPL
    // x <  y       -1        -1
    // y == 0        0         0 
    // x >  y        1         1
    // NaN           1        -1

    // So if we're interested in whether x is less than y, we need to use
    // CMPL to make sure that our condition is false if either x or y is NaN
    switch (op) {
      case UNGT_EXPR:
      case LT_EXPR:
      case LE_EXPR:
        mv.visitInsn(isDouble() ? DCMPG : FCMPG);
        break;
      default:
        mv.visitInsn(isDouble() ? DCMPL : FCMPL);
        break;
    }

    // Now we jump based on the comparison of the above result to zero
    switch (op) {
      case LT_EXPR:
        // 1: x < y
        mv.visitJumpInsn(IFLT, trueLabel);
        break;
      case LE_EXPR:
        // 1 : x < y
        // 0 : x == y
        mv.visitJumpInsn(IFLE, trueLabel);
        break;
      case EQ_EXPR:
        // 0 : x == y
        mv.visitJumpInsn(IFEQ, trueLabel);
        break;

      case NE_EXPR:
        mv.visitJumpInsn(IFNE, trueLabel);
        break;

      case UNGT_EXPR:
      case GT_EXPR:
        mv.visitJumpInsn(IFGT, trueLabel);
        break;

      case GE_EXPR:
        mv.visitJumpInsn(IFGE, trueLabel);
        break;

      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }

  private boolean isDouble() {
    return x.getType().equals(Type.DOUBLE_TYPE);
  }
}
