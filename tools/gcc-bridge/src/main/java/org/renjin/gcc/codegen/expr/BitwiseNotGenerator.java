package org.renjin.gcc.codegen.expr;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class BitwiseNotGenerator implements ValueGenerator {

  private final ValueGenerator valueGenerator;

  public BitwiseNotGenerator(ExprGenerator valueGenerator) {
    this.valueGenerator = (ValueGenerator) valueGenerator;
  }

  @Override
  public Type primitiveType() {
    return Type.INT_TYPE;
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    
    if(!valueGenerator.primitiveType().equals(Type.INT_TYPE)) {
      throw new UnsupportedOperationException("Bitwise not only supported for int32 operands.");
    }
    
    // Unary bitwise complement operator is implemented
    // as an XOR operation with -1 (all bits set)
    valueGenerator.emitPush(mv);
    mv.visitInsn(Opcodes.ICONST_M1);
    mv.visitInsn(Opcodes.IXOR);
  }
}
