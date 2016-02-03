package org.renjin.gcc.codegen.var;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.type.primitive.ConstantValue;
import org.renjin.gcc.codegen.type.primitive.op.PrimitiveBinOpGenerator;
import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.gimple.expr.GimpleImPartExpr;

/**
 * Functions to create value instances
 */
public class Values {
  
  public static Value newArray(final Type componentType, final int constantSize) {
    return new Value() {
      @Override
      public Type getType() {
        return Type.getType("[" + componentType.getDescriptor());
      }

      @Override
      public void load(MethodGenerator mv) {
        mv.iconst(constantSize);
        mv.newarray(componentType);
      }
    };
  }
  
  public static Value newArray(final WrapperType componentType, final int constantSize) {
    return newArray(componentType.getWrapperType(), constantSize);
  }

  public static Value newArray(Class<?> componentClass, int constantSize) {
    return newArray(Type.getType(componentClass), constantSize);
  }

  public static Value nullRef() {
    return new Value() {
      @Override
      public Type getType() {
        return null;
      }

      @Override
      public void load(MethodGenerator mv) {
        mv.aconst(null);
      }
    };
  }
  
  public static Value constantInt(final int value) {
    return new ConstantValue(Type.INT_TYPE, value);
  }
  
  public static Value zero() {
    return constantInt(0);
  }
  
  public static Value arrayValue(final Value array, final Value offset) {

    String arrayType = array.getType().getDescriptor();
    if(!arrayType.startsWith("[")) {
      throw new IllegalArgumentException("array is not of type array: " + arrayType);
    }
    final Type componentType = Type.getType(arrayType.substring(1));
    
    return new Value() {
      @Override
      public Type getType() {
        return componentType;
      }

      @Override
      public void load(MethodGenerator mv) {
        array.load(mv);
        offset.load(mv);
        mv.aload(componentType);
      }
    };
  }

  public static Value zero(final Type type) {
    return new ConstantValue(type, 0);
  }
  
  public static Value add(final Value x, final Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.PLUS_EXPR, x, y);
  }

  public static Value difference(Value x, Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.MINUS_EXPR, x, y);
  }

  public static Value product(Value x, Value y) {
    return new PrimitiveBinOpGenerator(GimpleOp.MULT_EXPR, x, y);
  }
}
