package org.renjin.gcc.codegen.type.primitive.op;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.var.Value;

import javax.annotation.Nonnull;

/**
 * Generates the bytecode to negate a numeric value
 */
public class NegateGenerator implements Value {
  
  private Value operand;

  public NegateGenerator(Value operand) {
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
