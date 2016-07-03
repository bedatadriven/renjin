package org.renjin.gcc.codegen.type.primitive.op;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Generates the bytecode to negate a numeric value
 */
public class NegativeValue implements JExpr {
  
  private JExpr operand;

  public NegativeValue(JExpr operand) {
    this.operand = operand;
  }

  @Nonnull
  @Override
  public Type getType() {
    return operand.getType();
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    operand.load(mv);
    mv.neg(operand.getType());
  }
}
