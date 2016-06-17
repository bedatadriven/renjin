package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;

import javax.annotation.Nonnull;

/**
 * Logical binary operator, such as TRUTH_OR, TRUTH_AND
 */
public class LogicalAnd implements JExpr {
  
  private JExpr x;
  private JExpr y;

  public LogicalAnd(JExpr x, JExpr y) {
    this.x = x;
    this.y = y;
  }


  @Nonnull
  @Override
  public Type getType() {
    return x.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    Label falseLabel = new Label();
    Label exitLabel = new Label();

    // if x is false, then can jump right away to false
    x.load(mv);
    jumpIfFalse(mv, falseLabel);

    // Otherwise need to check y
    y.load(mv);
    jumpIfFalse(mv, falseLabel);
    
    // TRUE: emit 1
    mv.iconst(1);
    mv.goTo(exitLabel);
    
    // FALSE: emit 0
    mv.mark(falseLabel);
    mv.iconst(0);
    
    mv.mark(exitLabel);
  }

  private void jumpIfFalse(MethodGenerator mv, Label trueLabel) {
    mv.visitJumpInsn(Opcodes.IFEQ, trueLabel);
  }
}
