package org.renjin.gcc.codegen.condition;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.GimpleOp;

/**
 * Generates a conditional jump based on pointer equality
 */
public class PointerCmpGenerator implements ConditionGenerator {
  private ExprGenerator x;
  private ExprGenerator y;

  public PointerCmpGenerator(GimpleOp op, ExprGenerator x, ExprGenerator y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public void emitJump(MethodVisitor mv, Label trueLabel, Label falseLabel) {
    throw new UnsupportedOperationException();
  }
}
