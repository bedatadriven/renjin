package org.renjin.gcc.codegen;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleType;

public class Types {
  
  public static boolean isInt(GimpleType type) {
    return type instanceof GimpleIntegerType && ((GimpleIntegerType) type).getPrecision() <= 32;
  }
  
  public static boolean isInt(ExprGenerator expr) {
    return isInt(expr.getGimpleType());
  }
  
  public static boolean isLong(GimpleType type) {
    return type instanceof GimpleIntegerType && ((GimpleIntegerType) type).getPrecision() > 32;
  }
  
  public static boolean isLong(ExprGenerator expr) {
    return isLong(expr.getGimpleType());
  }

  public static Type getComponentType(Type arrayType) {
    String descriptor = arrayType.getDescriptor();
    if(!descriptor.startsWith("[")) {
      throw new IllegalArgumentException(descriptor + " is not an array type");
    }
    return Type.getType(descriptor.substring(1));
  }
  
}
