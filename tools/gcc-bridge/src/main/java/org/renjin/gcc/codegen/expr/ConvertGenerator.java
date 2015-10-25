package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates the bytecode to cast a primitive value to a new type
 */
public class ConvertGenerator extends AbstractExprGenerator implements ValueGenerator {
  
  private ValueGenerator valueGenerator;
  private GimplePrimitiveType destinationType;

  public ConvertGenerator(ExprGenerator valueGenerator, GimplePrimitiveType destinationType) {
    this.valueGenerator = (ValueGenerator) valueGenerator;
    this.destinationType = destinationType;
  }
  

  @Override
  public GimpleType getGimpleType() {
    return destinationType;
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    Type sourceType = valueGenerator.getJvmPrimitiveType();
    
    if(isIntType(sourceType) && isIntType(destinationType.jvmType())) {
      // no conversion necessary
      valueGenerator.emitPrimitiveValue(mv);
    
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
