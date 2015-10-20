package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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
  public Type getValueType() {
    return Type.DOUBLE_TYPE;
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    valueGenerator.emitPushValue(mv);
    
    if(valueGenerator.getValueType().equals(Type.INT_TYPE)) {
      mv.visitInsn(Opcodes.I2D);
    } else {
      throw new UnsupportedOperationException("from type: " + valueGenerator.getValueType());
    }
  }

  @Override
  public GimpleType getGimpleType() {
    throw new UnsupportedOperationException();
  }
}
