package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;

import javax.annotation.Nonnull;

/**
 * Generates the bytecode to negate a numeric value
 */
public class NegativeValue implements SimpleExpr {
  
  private SimpleExpr operand;

  public NegativeValue(SimpleExpr operand) {
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
