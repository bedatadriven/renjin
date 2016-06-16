package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.condition.ConditionGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.runtime.VoidPtr;

/**
 * Compares two pointers.
 */
public class VoidPtrComparison implements ConditionGenerator {
  private final GimpleOp op;
  private final JExpr x;
  private final JExpr y;

  public VoidPtrComparison(GimpleOp op, JExpr x, JExpr y) {
    this.op = op;
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodGenerator mv, Label trueLabel, Label falseLabel) {
    // x and y might actually be references to Fat Pointers,
    // which cannot be directly compared, as we actually have to unwrap
    // them to check to see whether they point to the same array.
    
    // To avoid too much complexity and type checking here, we delegate to a runtime function
    
    x.load(mv);
    y.load(mv);
    
    mv.invokestatic(VoidPtr.class, "compare", 
        Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(Object.class), Type.getType(Object.class)));

    // The compare method leaves a result of -1, 0, +1 on the stack.
    switch (op) {
      case LT_EXPR:
        mv.iflt(trueLabel);
        break;
      case LE_EXPR:
        mv.ifle(trueLabel);
        break;
      case EQ_EXPR:
        mv.ifeq(trueLabel);
        break;
      case NE_EXPR:
        mv.ifne(trueLabel);
        break;
      case GT_EXPR:
        mv.ifgt(trueLabel);
        break;
      case GE_EXPR:
        mv.ifge(trueLabel);
        break;
    }
    mv.goTo(falseLabel);
  }
}
