package org.renjin.gcc.translate.types;

import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

public class PrimitiveTypes {

  
  public static JimpleType get(PrimitiveType type) {
    switch(type) {
    case DOUBLE_TYPE:
      return new JimpleType("double");
    case INT_TYPE:
      return new JimpleType("int");
    case FLOAT_TYPE:
      return JimpleType.FLOAT;
    case BOOLEAN:
      return JimpleType.BOOLEAN;
    case LONG:
      return JimpleType.LONG;
    case VOID_TYPE:
      return JimpleType.VOID;
    }
    throw new UnsupportedOperationException(type.name());
  }
  
  public static JimpleType getArrayType(PrimitiveType type) {
    switch(type) {
    case DOUBLE_TYPE:
      return new JimpleType("double[]");
    case INT_TYPE:
      return new JimpleType("int[]");
    }
    throw new UnsupportedOperationException(type.name());  
  }
  
  public static JimpleType getWrapperType(PrimitiveType type) {
    switch(type) {
    case DOUBLE_TYPE:
      return new JimpleType(DoublePtr.class);
    case INT_TYPE:
      return new JimpleType(IntPtr.class);
    }
    throw new UnsupportedOperationException(type.name());  
  }
}
