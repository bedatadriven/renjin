package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleType;

public class TruncateExprGenerator extends AbstractExprGenerator implements ValueGenerator {

  
  private final ValueGenerator operandGenerator;

  public TruncateExprGenerator(ExprGenerator operandGenerator) {
    this.operandGenerator = (ValueGenerator) operandGenerator;
  }

  @Override
  public Type getValueType() {
    return Type.INT_TYPE;
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    operandGenerator.emitPushValue(mv);
    mv.visitInsn(Opcodes.D2I);
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimpleIntegerType(32);
  }
}
