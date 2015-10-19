package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Generates the bytecode to cast a primitive value to a new type
 */
public class ConvertGenerator implements ValueGenerator {
  
  private ValueGenerator valueGenerator;
  private Type destinationType;

  public ConvertGenerator(ExprGenerator valueGenerator, Type destinationType) {
    this.valueGenerator = (ValueGenerator) valueGenerator;
    this.destinationType = destinationType;
  }

  @Override
  public Type primitiveType() {
    return destinationType;
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    Type sourceType = valueGenerator.primitiveType();
    
    if(isIntType(sourceType) && isIntType(destinationType)) {
      // no conversion necessary
      valueGenerator.emitPush(mv);
    
    } else {
      throw new UnsupportedOperationException(String.format("Cannot convert from %s to %s",
          sourceType, destinationType));
    }
  }
  
  private boolean isIntType(Type type) {
    return type.equals(Type.BOOLEAN_TYPE) ||
           type.equals(Type.INT_TYPE) ||
           type.equals(Type.SHORT_TYPE) ||
           type.equals(Type.CHAR_TYPE);
  }
}
