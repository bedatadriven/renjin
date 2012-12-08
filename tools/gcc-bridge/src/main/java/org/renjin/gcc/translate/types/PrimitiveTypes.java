package org.renjin.gcc.translate.types;

import org.renjin.gcc.gimple.type.PrimitiveType;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.jimple.RealJimpleType;
import org.renjin.gcc.runtime.CharPtr;
import org.renjin.gcc.runtime.DoublePtr;
import org.renjin.gcc.runtime.IntPtr;

public class PrimitiveTypes {

  
  public static JimpleType get(PrimitiveType type) {
    switch(type) {
    case DOUBLE_TYPE:
      return JimpleType.DOUBLE;
    case INT_TYPE:
      return JimpleType.INT;
    case FLOAT_TYPE:
      return JimpleType.FLOAT;
    case BOOLEAN:
      return JimpleType.BOOLEAN;
    case LONG:
      return JimpleType.LONG;
    case VOID_TYPE:
      return JimpleType.VOID;
    case CHAR:
      return JimpleType.CHAR;
    }
    throw new UnsupportedOperationException(type.name());
  }
  
  public static JimpleType getArrayType(PrimitiveType type) {
    switch(type) {
    case DOUBLE_TYPE:
      return new RealJimpleType(double[].class);
    case INT_TYPE:
      return new RealJimpleType(int[].class);
    case CHAR:
      return new RealJimpleType(char[].class);
    }
    throw new UnsupportedOperationException(type.name());  
  }
  
  public static JimpleType getWrapperType(PrimitiveType type) {
    switch(type) {
    case DOUBLE_TYPE:
      return new RealJimpleType(DoublePtr.class);
    case INT_TYPE:
      return new RealJimpleType(IntPtr.class);
    case CHAR:
      return new RealJimpleType(CharPtr.class);
    }
    throw new UnsupportedOperationException(type.name());  
  }
}
