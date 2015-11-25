package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.pointers.AddressOfPrimitiveValue;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleType;

public class TruncateExprGenerator extends AbstractExprGenerator implements ExprGenerator {

  
  private final ExprGenerator operandGenerator;

  public TruncateExprGenerator(ExprGenerator operandGenerator) {
    this.operandGenerator = operandGenerator;
  }
  

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    operandGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(Opcodes.D2I);
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimpleIntegerType(32);
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveValue(this);
  }
}
