package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class TruncateExprGenerator implements PrimitiveGenerator {

  
  private final PrimitiveGenerator operandGenerator;

  public TruncateExprGenerator(ExprGenerator operandGenerator) {
    this.operandGenerator = (PrimitiveGenerator) operandGenerator;
  }

  @Override
  public Type primitiveType() {
    return Type.INT_TYPE;
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    operandGenerator.emitPush(mv);
    mv.visitInsn(Opcodes.D2I);
  }
}
