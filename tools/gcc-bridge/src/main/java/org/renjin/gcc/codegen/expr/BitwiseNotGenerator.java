package org.renjin.gcc.codegen.expr;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleType;

public class BitwiseNotGenerator extends AbstractExprGenerator implements ValueGenerator {

  private final ValueGenerator valueGenerator;

  public BitwiseNotGenerator(ExprGenerator valueGenerator) {
    this.valueGenerator = (ValueGenerator) valueGenerator;
  }
  

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    
    if(!valueGenerator.getJvmPrimitiveType().equals(Type.INT_TYPE)) {
      throw new UnsupportedOperationException("Bitwise not only supported for int32 operands.");
    }
    
    // Unary bitwise complement operator is implemented
    // as an XOR operation with -1 (all bits set)
    valueGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(Opcodes.ICONST_M1);
    mv.visitInsn(Opcodes.IXOR);
  }

  @Override
  public GimpleType getGimpleType() {
    return valueGenerator.getGimpleType();
  }
}
