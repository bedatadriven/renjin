package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates the code to cast an integer to a double value
 */
public class DoubleGenerator extends AbstractExprGenerator implements ValueGenerator {
  
  private ValueGenerator valueGenerator;
  
  public DoubleGenerator(ExprGenerator valueGenerator) {
    this.valueGenerator = (ValueGenerator) valueGenerator;
  }


  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    valueGenerator.emitPrimitiveValue(mv);
    
    if(valueGenerator.getJvmPrimitiveType().equals(Type.INT_TYPE)) {
      mv.visitInsn(Opcodes.I2D);
    } else {
      throw new UnsupportedOperationException("from type: " + valueGenerator.getJvmPrimitiveType());
    }
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimpleRealType(64);
  }
}
