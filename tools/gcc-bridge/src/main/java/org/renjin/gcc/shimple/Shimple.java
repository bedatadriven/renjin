package org.renjin.gcc.shimple;


import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.gimple.type.PrimitiveType;

public class Shimple {

  public static String id(GimpleParameter param) {
    return validIdentifier(param.getName());
  }

  public static String id(String varName) {
    return validIdentifier(varName);
  }

  private static String validIdentifier(String name) {
    return name.replace('.', '$');
  }

  public static String id(GimpleVar rhs) {
    return id(rhs.getName());
  }

  public static String constant(Object value) {
    if(value instanceof Number) {
      return value.toString();
    } else if(value instanceof Boolean) {
      return value.toString();
    } else {
      throw new UnsupportedOperationException("constant: " + value);
    }
  }

  public static String type(GimpleType gimpleType) {
    if(gimpleType instanceof PrimitiveType) {
      switch((PrimitiveType)gimpleType) {
      case DOUBLE_TYPE:
        return "double";
      case VOID_TYPE:
        return "void";
      case INT_TYPE :
        return "int";
      case BOOLEAN:
        return "boolean";
      }
    } else if(gimpleType instanceof PointerType) {
      return "org.renjin.gcc.runtime.Pointer";
    }
    throw new UnsupportedOperationException(gimpleType.toString());
  }

  public static String type(Class<?> type) {
    return type.toString();
  }
}
