package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;
import org.renjin.gcc.runtime.*;

/**
 * Constructs expression generators related to FatPointer wrappers.
 */
public class Wrappers {
  
  public static Type valueType(Type wrapperType) {
    if(wrapperType.equals(Type.getType(BooleanPtr.class))) {
      return Type.BOOLEAN_TYPE;
    } else if(wrapperType.equals(Type.getType(BytePtr.class))) {
      return Type.BYTE_TYPE;
    } else if(wrapperType.equals(Type.getType(ShortPtr.class))) {
      return Type.SHORT_TYPE;
    } else if(wrapperType.equals(Type.getType(CharPtr.class))) {
      return Type.CHAR_TYPE;
    } else if(wrapperType.equals(Type.getType(IntPtr.class))) {
      return Type.INT_TYPE;
    } else if(wrapperType.equals(Type.getType(LongPtr.class))) {
      return Type.LONG_TYPE;
    } else if(wrapperType.equals(Type.getType(FloatPtr.class))) {
      return Type.FLOAT_TYPE;
    } else if(wrapperType.equals(Type.getType(DoublePtr.class))) {
      return Type.DOUBLE_TYPE;
    } else if(wrapperType.equals(Type.getType(ObjectPtr.class))) {
      return Type.getType(Object.class);
    }
    throw new IllegalArgumentException("not a wrapper type: " + wrapperType);
  }
  
  private static Type arrayType(Type wrapperType) {
    Type valueType = valueType(wrapperType);
    Type arrayType = Type.getType("[" + valueType.getDescriptor());
    return arrayType;
  }
  
  public static Type valueArrayType(Type valueType) {
    return Type.getType("[" + valueType.getDescriptor());
  }
  
  public static Value getArray(Value wrapperInstance) {
    return Values.field(wrapperInstance, arrayType(wrapperInstance.getType()), "array");
  }
  
  public static Value getOffset(Value wrapperInstance) {
    return Values.field(wrapperInstance, Type.INT_TYPE, "offset");
  }
}
