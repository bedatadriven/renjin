package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Label;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.gimple.GimpleOp;

/**
 * Generates a conditional jump based on pointer equality
 */
public class FatPtrConditionGenerator implements ConditionGenerator {
  private GimpleOp op;
  private FatPtrExpr x;
  private FatPtrExpr y;

  public FatPtrConditionGenerator(GimpleOp op, FatPtrExpr x, FatPtrExpr y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    switch (op) {
      case EQ_EXPR:
        jump(mv, trueLabel, falseLabel);
        break;
      case NE_EXPR:
        jump(mv, falseLabel, trueLabel);
        break;
      default:
        throw new UnsupportedOperationException("op: " + op);
    }
  }

  private void jump(MethodGenerator mv, Label equalLabel, Label notEqualLabel) {

    // First compare the pointer arrays
    // jump immediately to the notEqualLabel
    x.getArray().load(mv);
    y.getArray().load(mv);
    mv.ifacmpne(notEqualLabel);

    // If they are equal
    // Compare the offsets
    x.getOffset().load(mv);
    y.getOffset().load(mv);
    mv.ificmpne(notEqualLabel);
    
    // If we get here, then the pointer is equal
    mv.goTo(equalLabel);
  }

}