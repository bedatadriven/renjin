package org.renjin.gcc.codegen.fatptr;

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
  
  public static Type fieldArrayType(Type wrapperType) {
    if (wrapperType.equals(Type.getType(ObjectPtr.class))) {
      return Type.getType("[Ljava/lang/Object;");
    }
    Type valueType = valueType(wrapperType);
    Type arrayType = Type.getType("[" + valueType.getDescriptor());
    return arrayType;
  }
  
  public static Type valueArrayType(Type valueType) {
    return Type.getType("[" + valueType.getDescriptor());
  }
  
  public static Value arrayField(Value wrapperInstance) {
    return Values.field(wrapperInstance, fieldArrayType(wrapperInstance.getType()), "array");
  }

  public static Value arrayField(Value instance, Type valueType) {
    Value array = arrayField(instance);
    Type arrayType = arrayType(valueType);
    if(!array.getType().equals(arrayType)) {
      array = Values.cast(array, arrayType);
    }
    return array;
  }

  private static Type arrayType(Type valueType) {
    return Type.getType("[" + valueType.getDescriptor());
  }

  public static Value offsetField(Value wrapperInstance) {
    return Values.field(wrapperInstance, Type.INT_TYPE, "offset");
  }

  public static Type wrapperType(Type valueType) {
    switch (valueType.getSort()) {
      case Type.BOOLEAN:
        return Type.getType(BooleanPtr.class);
      case Type.SHORT:
        return Type.getType(ShortPtr.class);
      case Type.BYTE:
        return Type.getType(BytePtr.class);
      case Type.CHAR:
        return Type.getType(CharPtr.class);
      case Type.INT:
        return Type.getType(IntPtr.class);
      case Type.LONG:
        return Type.getType(LongPtr.class);
      case Type.FLOAT:
        return Type.getType(FloatPtr.class);
      case Type.DOUBLE:
        return Type.getType(DoublePtr.class);
      case Type.OBJECT:
        return Type.getType(ObjectPtr.class);
    }
    throw new UnsupportedOperationException("No wrapper for type: " + valueType);
  }

}
