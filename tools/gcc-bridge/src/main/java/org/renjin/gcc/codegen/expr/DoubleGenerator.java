package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Generates the code to cast an integer to a double value
 */
public class DoubleGenerator implements PrimitiveGenerator {
  
  private PrimitiveGenerator valueGenerator;
  
  public DoubleGenerator(ExprGenerator valueGenerator) {
    this.valueGenerator = (PrimitiveGenerator) valueGenerator;
  }

  @Override
  public Type primitiveType() {
    return Type.DOUBLE_TYPE;
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    valueGenerator.emitPush(mv);
    
    if(valueGenerator.primitiveType().equals(Type.INT_TYPE)) {
      mv.visitInsn(Opcodes.I2D);
    } else {
      throw new UnsupportedOperationException("from type: " + valueGenerator.primitiveType());
    }
  }
}
