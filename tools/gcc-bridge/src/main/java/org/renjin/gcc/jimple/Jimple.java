package org.renjin.gcc.jimple;


import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.gimple.type.PrimitiveType;

public class Jimple {

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

  public static JimpleType type(GimpleType gimpleType) {
    if(gimpleType instanceof PrimitiveType) {
      switch((PrimitiveType)gimpleType) {
      case DOUBLE_TYPE:
        return new JimpleType("double");
      case VOID_TYPE:
        return new JimpleType("void");
      case INT_TYPE :
        return new JimpleType("int");
      case BOOLEAN:
        return new JimpleType("boolean");
      }
    } else if(gimpleType instanceof PointerType) {
      return new JimpleType("org.renjin.gcc.runtime.Pointer");
    }
    throw new UnsupportedOperationException(gimpleType.toString());
  }

  public static String type(Class<?> type) {
    return type.toString();
  }
}
