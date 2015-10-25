package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates the bytecode to negate a numeric value
 */
public class NegateGenerator extends AbstractExprGenerator implements ValueGenerator {
  
  private ValueGenerator operand;

  public NegateGenerator(ExprGenerator operand) {
    this.operand = (ValueGenerator) operand;
  }
  

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    operand.emitPrimitiveValue(mv);
    mv.visitInsn(operand.getJvmPrimitiveType().getOpcode(Opcodes.INEG));
  }

  @Override
  public GimpleType getGimpleType() {
    return operand.getGimpleType();
  }
}
