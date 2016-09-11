package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.gimple.expr.GimplePrimitiveConstant;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Preconditions;
import org.renjin.repackaged.guava.primitives.UnsignedBytes;

import javax.annotation.Nonnull;

public class ConstantValue implements JExpr {

  private Number value;
  private Type type;

  public ConstantValue(GimplePrimitiveConstant constant) {
    GimplePrimitiveType primitiveType = (GimplePrimitiveType) constant.getType();
    this.type =  primitiveType.jvmType();
    value = computeValue(constant);
  }
  
  private static Number computeValue(GimplePrimitiveConstant constant) {

    if(constant.getType() instanceof GimpleIntegerType) {
      GimpleIntegerType integerType = (GimpleIntegerType) constant.getType();
      if(integerType.isUnsigned()) {
        switch (integerType.getSize()) {
          case 8:
            return UnsignedBytes.checkedCast(constant.getNumberValue().longValue());
          case 16:
            return constant.getNumberValue().intValue();
          case 32:
            if(constant.getNumberValue().longValue() <= Integer.MAX_VALUE) {
              return constant.getNumberValue().intValue();
            } else {
              throw new UnsupportedOperationException("TODO: " + constant.getNumberValue());
            }
          case 64:
            return constant.getNumberValue().longValue();
          
          default:
            throw new UnsupportedOperationException("TODO: " + constant.getType());
        }
      }
    }

    // For signed integers and floats, no special handling is required
    return constant.getNumberValue();
  }

  public ConstantValue(Type type, Number value) {
    this.type = type;
    this.value = value;
  }

  public Number getValue() {
    return value;
  }

  public int getIntValue() {
    Preconditions.checkState(type.equals(Type.INT_TYPE));
    
    return value.intValue();
  }
  
  @Nonnull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void load(@Nonnull MethodGenerator mv) {
    switch (type.getSort()) {
      case Type.BOOLEAN:
      case Type.BYTE:
      case Type.CHAR:
      case Type.SHORT:
      case Type.INT:
        mv.iconst(value.intValue());
        break;
      case Type.LONG:
        mv.lconst(value.longValue());
        break;
      case Type.FLOAT:
        mv.fconst(value.floatValue());
        break;
      case Type.DOUBLE:
        mv.dconst(value.doubleValue());
        break;
      default:
        throw new IllegalStateException("type: " + type);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConstantValue that = (ConstantValue) o;

    if (!value.equals(that.value)) {
      return false;
    }
    return type.equals(that.type);

  }

  @Override
  public int hashCode() {
    int result = value.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }

  public static boolean isZero(GExpr expr) {
    if(expr instanceof ConstantValue) {
      ConstantValue constantValue = (ConstantValue) expr;
      if(constantValue.getIntValue() == 0) {
        return true;
      }
    }
    return false;
  }
}
