package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.expr.GimplePrimitiveConstant;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import static org.objectweb.asm.Opcodes.*;

public class ConstantValue extends AbstractExprGenerator implements Value {

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

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void load(MethodGenerator mv) {
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
}
