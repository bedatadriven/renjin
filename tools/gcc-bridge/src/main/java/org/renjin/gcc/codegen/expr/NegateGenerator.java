package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Generates the bytecode to negate a numeric value
 */
public class NegateGenerator implements PrimitiveGenerator {
  
  private PrimitiveGenerator operand;

  public NegateGenerator(ExprGenerator operand) {
    this.operand = (PrimitiveGenerator) operand;
  }

  @Override
  public Type primitiveType() {
    return operand.primitiveType();
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    operand.emitPush(mv);
    mv.visitInsn(operand.primitiveType().getOpcode(Opcodes.INEG));
  }
}
