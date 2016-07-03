package org.renjin.gcc.gimple.type;

import org.renjin.repackaged.asm.Type;

public abstract class GimplePrimitiveType extends AbstractGimpleType {

  /**
   * @return the number of slots required to store this type on the stack or 
   * in the local variable table in the JVM.
   */
  public abstract int localVariableSlots();

  /**
   * 
   * @return the equivalent JVM type
   */
  public abstract Type jvmType();


  public static GimplePrimitiveType fromJvmType(Type type) {
    if(type.equals(Type.BOOLEAN_TYPE)) {
      return new GimpleBooleanType();

    } else if(type.equals(Type.DOUBLE_TYPE)) {
      return new GimpleRealType(64);

    } else if(type.equals(Type.FLOAT_TYPE)) {
      return new GimpleRealType(32);

    } else if(type.equals(Type.INT_TYPE)) {
      return new GimpleIntegerType(32);

    } else if(type.equals(Type.LONG_TYPE)) {
      return new GimpleIntegerType(64);

    } else if(type.equals(Type.CHAR_TYPE)) {
      return GimpleIntegerType.unsigned(16);

    } else if(type.equals(Type.SHORT_TYPE)) {
      return new GimpleIntegerType(16);

    } else if(type.equals(Type.BYTE_TYPE)) {
      return new GimpleIntegerType(8);

    } else {
      throw new UnsupportedOperationException("type: " + type);
    }
  }

  public static GimpleType fromJvmType(Class<?> primitiveClass) {
    return fromJvmType(Type.getType(primitiveClass));
  }
}
