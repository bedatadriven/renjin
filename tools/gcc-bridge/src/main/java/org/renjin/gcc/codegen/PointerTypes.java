package org.renjin.gcc.codegen;

import org.objectweb.asm.Type;
import org.renjin.gcc.runtime.*;


public class PointerTypes {
  
  public static Type wrapperType(Type type) {
    if(type.equals(Type.BOOLEAN_TYPE)) {
      return Type.getType(BooleanPtr.class);
    
    } else if(type.equals(Type.DOUBLE_TYPE)) {
      return Type.getType(DoublePtr.class);

    } else if(type.equals(Type.FLOAT_TYPE)) {
      throw new UnsupportedOperationException("todo: FloatPtr");
    
    } else if(type.equals(Type.INT_TYPE)) {
      return Type.getType(IntPtr.class);
    
    } else if(type.equals(Type.LONG_TYPE)) {
      return Type.getType(LongPtr.class);
      
    } else if(type.equals(Type.CHAR_TYPE)) {
      return Type.getType(CharPtr.class);
      
    } else if(type.equals(Type.BYTE_TYPE)) {
      throw new UnsupportedOperationException("todo: BytePtr");
    
    } else {
      return Type.getType(ObjectPtr.class);
    }
  }

  public static Type wrapperArrayType(Type baseType) {
    if(baseType.equals(Type.BOOLEAN_TYPE)) {
      return Type.getType(boolean[].class);

    } else if(baseType.equals(Type.DOUBLE_TYPE)) {
      return Type.getType(double[].class);

    } else if(baseType.equals(Type.FLOAT_TYPE)) {
      throw new UnsupportedOperationException("todo: FloatPtr");

    } else if(baseType.equals(Type.INT_TYPE)) {
      return Type.getType(int[].class);

    } else if(baseType.equals(Type.LONG_TYPE)) {
      return Type.getType(long[].class);

    } else if(baseType.equals(Type.CHAR_TYPE)) {
      return Type.getType(char[].class);

    } else if(baseType.equals(Type.BYTE_TYPE)) {
      throw new UnsupportedOperationException("todo: BytePtr");

    } else {
      return Type.getType(Object[].class);
    }
  }
}
