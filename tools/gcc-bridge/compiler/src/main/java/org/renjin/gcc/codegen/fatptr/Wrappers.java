package org.renjin.gcc.codegen.fatptr;

import com.google.common.base.Preconditions;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.runtime.*;
import org.renjin.repackaged.asm.Type;

import javax.annotation.Nonnull;

/**
 * Constructs expression generators related to FatPointer wrappers.
 */
public class Wrappers {


  public static Type valueType(Class<?> wrapperClass) {
    return valueType(Type.getType(wrapperClass));

  }
  
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
  
  public static JExpr arrayField(JExpr wrapperInstance) {
    return Expressions.field(wrapperInstance, fieldArrayType(wrapperInstance.getType()), "array");
  }

  public static JExpr arrayField(JExpr instance, Type valueType) {
    JExpr array = arrayField(instance);
    Type arrayType = arrayType(valueType);
    if(!array.getType().equals(arrayType)) {
      array = Expressions.cast(array, arrayType);
    }
    return array;
  }


  private static Type arrayType(Type valueType) {
    return Type.getType("[" + valueType.getDescriptor());
  }

  public static JExpr offsetField(JExpr wrapperInstance) {
    return Expressions.field(wrapperInstance, Type.INT_TYPE, "offset");
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

  public static WrapperType valueOf(Class<?> wrapperClass) {
    return WrapperType.valueOf(Type.getType(wrapperClass));
  }
  
  public static JExpr cast(final Type valueType, final JExpr pointer) {
    final Type wrapperType = wrapperType(valueType);

    return new JExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return wrapperType;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        pointer.load(mv);
        mv.invokestatic(wrapperType, "cast", Type.getMethodDescriptor(wrapperType, Type.getType(Object.class)));
      }
    };
  }

  public static Type componentType(Type arrayType) {
    Preconditions.checkArgument(arrayType.getSort() == Type.ARRAY, "arrayType: " + arrayType);

    String arrayDescriptor = arrayType.getDescriptor();
    assert arrayDescriptor.startsWith("[");
    
    String componentDescriptor = arrayDescriptor.substring(1);
    return Type.getType(componentDescriptor);
  }
}
