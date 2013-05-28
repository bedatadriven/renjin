package org.renjin.gcc.translate.type;


import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleRealType;

public enum PrimitiveType {
  DOUBLE,
  FLOAT,
  INT,
  LONG,
  BOOLEAN,
  CHAR;

  public static PrimitiveType fromGimple(GimplePrimitiveType gimpleType) {
    if(gimpleType instanceof GimpleIntegerType) {
      switch(((GimpleIntegerType) gimpleType).getPrecision()) {
        case 8:
          return PrimitiveType.CHAR;
        case 32:
          return PrimitiveType.INT;
        case 64:
          return PrimitiveType.LONG;
      }
    } else if(gimpleType instanceof GimpleRealType) {
      switch(((GimpleRealType) gimpleType).getPrecision()) {
        case 32:
          return PrimitiveType.FLOAT;
        case 64:
          return PrimitiveType.DOUBLE;
      }
    } else if(gimpleType instanceof GimpleBooleanType) {
      return PrimitiveType.BOOLEAN;
    }

    throw new UnsupportedOperationException(gimpleType.toString());
  }
}
