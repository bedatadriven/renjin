package org.renjin.gcc.codegen;

import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Finds the JVM type to use for a Gimple value type
 */
public class ValueTypes {
  
  public static Type typeOf(GimpleType type) {
    if(type instanceof GimplePrimitiveType) {
      GimplePrimitiveType primitiveType = (GimplePrimitiveType) type;
      return primitiveType.jvmType();
    } else if(type instanceof GimpleArrayType) {
      Type componentType = typeOf(((GimpleArrayType) type).getComponentType());
      return Type.getType("[" + componentType.getDescriptor());
    } else {
      throw new UnsupportedOperationException("type: " + type + " [" + type.getClass().getSimpleName() + "]");
    }
  }
  
}
