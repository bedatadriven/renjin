package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.gimple.expr.GimplePrimitiveConstant;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import javax.annotation.Nonnull;

public class ConstantValue implements SimpleExpr {

  private Number value;
  private Type type;

  public ConstantValue(GimplePrimitiveConstant constant) {
    value = constant.getValue();
    type = ((GimplePrimitiveType) constant.getType()).jvmType();
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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ConstantValue that = (ConstantValue) o;

    if (!value.equals(that.value)) return false;
    return type.equals(that.type);

  }

  @Override
  public int hashCode() {
    int result = value.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}
