package org.renjin.gcc.codegen.condition;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.NullPtrGenerator;
import org.renjin.gcc.gimple.GimpleOp;

/**
 * Generates a conditional jump based on pointer equality
 */
public class PointerCmpGenerator implements ConditionGenerator {
  private GimpleOp op;
  private ExprGenerator x;
  private ExprGenerator y;

  public PointerCmpGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodVisitor mv, Label trueLabel, Label falseLabel) {
    if(x instanceof NullPtrGenerator) {
      emitJumpOnNullCondition(mv, y, trueLabel, falseLabel);
    } else if(y instanceof NullPtrGenerator) {
      emitJumpOnNullCondition(mv, x, trueLabel, falseLabel);
    } else {
      switch (op) {
        case EQ_EXPR:
          emitJumpOnPointerComparison(mv, trueLabel, falseLabel);
          break;
        case NE_EXPR:
          emitJumpOnPointerComparison(mv, falseLabel, trueLabel);
          break;
        default:
          throw new UnsupportedOperationException("op: " + op);
      }
      
    }
  }

  private void emitJumpOnNullCondition(MethodVisitor mv, ExprGenerator ptr, Label trueLabel, Label falseLabel) {
    
    ptr.emitPushPtrArray(mv);
    
    switch (op) {
      case NE_EXPR:
        // TRUE: not equal to null
        mv.visitJumpInsn(Opcodes.IFNONNULL, trueLabel);
        mv.visitJumpInsn(Opcodes.GOTO, falseLabel);
        break;
      
      case EQ_EXPR:
        // TRUE: equal to null
        mv.visitJumpInsn(Opcodes.IFNULL, trueLabel);
        mv.visitJumpInsn(Opcodes.GOTO, falseLabel);
        break;
    
      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }

  private void emitJumpOnPointerComparison(MethodVisitor mv, Label equalLabel, Label notEqualLabel) {


    x.emitPushPtrArrayAndOffset(mv);
    y.emitPushPtrArrayAndOffset(mv);

    // first compare offsets, which are on top of the stack
    Label offsetsEqual = new Label();
    mv.visitJumpInsn(Opcodes.IF_ICMPEQ, offsetsEqual);

    // in the case the offsets are unequal, we need to pop the arrays off the stack
    // before jumping to equalLabel
    mv.visitInsn(Opcodes.POP2);
    mv.visitJumpInsn(Opcodes.GOTO, notEqualLabel);

    // if the offsets are equal, we need to compare the pointers, which
    // are the next two words on the stack
    mv.visitJumpInsn(Opcodes.IF_ACMPEQ, equalLabel);
    mv.visitJumpInsn(Opcodes.GOTO, notEqualLabel);
  }
  
}
