package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleOp;


public class RecordUnitPtrCmpGenerator implements ConditionGenerator {
  private final GimpleOp op;
  private final ExprGenerator x;
  private final ExprGenerator y;

  public RecordUnitPtrCmpGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    // push both refs on the stack
    x.emitPushRecordRef(mv);
    y.emitPushRecordRef(mv);
    switch (op) {
      case EQ_EXPR:
        mv.visitJumpInsn(Opcodes.IF_ACMPEQ, trueLabel);
        mv.visitJumpInsn(Opcodes.GOTO, falseLabel);
        break;
      case NE_EXPR:
        mv.visitJumpInsn(Opcodes.IF_ACMPNE, trueLabel);
        mv.visitJumpInsn(Opcodes.GOTO, falseLabel);
        break;
      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }
}
