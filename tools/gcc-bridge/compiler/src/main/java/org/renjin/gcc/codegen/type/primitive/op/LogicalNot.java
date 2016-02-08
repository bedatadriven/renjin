package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;

import javax.annotation.Nonnull;


public class LogicalNot implements SimpleExpr {
  
  private SimpleExpr operand;

  public LogicalNot(SimpleExpr operand) {
    this.operand = operand;
  }

  @Nonnull
  @Override
  public Type getType() {
    return Type.BOOLEAN_TYPE;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    Label trueLabel = new Label();
    Label exit = new Label();

    operand.load(mv);
    mv.ifne(trueLabel);

    // operand is FALSE, push TRUE onto stack
    mv.iconst(1);
    mv.goTo(exit);

    // operand is TRUE, push FALSE onto stack
    mv.mark(trueLabel);
    mv.iconst(0);

    // Exit point
    mv.mark(exit);
  }
}
