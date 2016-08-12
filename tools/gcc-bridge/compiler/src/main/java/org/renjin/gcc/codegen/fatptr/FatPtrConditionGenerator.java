package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.repackaged.asm.Label;

/**
 * Generates a conditional jump based on pointer equality
 */
public class FatPtrConditionGenerator implements ConditionGenerator {
  private GimpleOp op;
  private FatPtrPair x;
  private FatPtrPair y;

  public FatPtrConditionGenerator(GimpleOp op, FatPtrPair x, FatPtrPair y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    switch (op) {
      case EQ_EXPR:
        jumpOnEqual(mv, trueLabel, falseLabel);
        break;
      case NE_EXPR:
        jumpOnEqual(mv, falseLabel, trueLabel);
        break;
      default:
        jumpOnComparison(mv, trueLabel, falseLabel);
        break;
    }
  }

  private void jumpOnEqual(MethodGenerator mv, Label equalLabel, Label notEqualLabel) {

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
  
  private void jumpOnComparison(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    
    Label arraysEqual = new Label();

    // First push the address of the two arrays on the stack
    // and check if they are equal
    x.getArray().load(mv);
    y.getArray().load(mv);
    mv.ifacmpeq(arraysEqual);
    
    // If the arrays are not the same, compare the address of the array
    x.getArray().load(mv);
    mv.invokeIdentityHashCode();
    y.getArray().load(mv);
    mv.invokeIdentityHashCode();
    switch (op) {
      case LT_EXPR:
      case LE_EXPR:
        mv.ificmplt(trueLabel);
        break;
      case GT_EXPR:
      case GE_EXPR:
        mv.ificmpgt(trueLabel);
        break;
    }
    mv.goTo(falseLabel);
    
    // Arrays are the same, compare the offsets
    mv.mark(arraysEqual);
    x.getOffset().load(mv);
    y.getOffset().load(mv);

    switch (op) {
      case LT_EXPR:
        mv.ificmplt(trueLabel);
        break;
      case LE_EXPR:
        mv.ificmple(trueLabel);
        break;
      case GT_EXPR:
        mv.ificmpgt(trueLabel);
        break;
      case GE_EXPR:
        mv.ificmpge(trueLabel);
        break;
    }
    
    mv.goTo(falseLabel);
  }

}