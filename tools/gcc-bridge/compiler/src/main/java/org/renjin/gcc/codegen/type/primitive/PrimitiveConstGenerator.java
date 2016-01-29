package org.renjin.gcc.codegen.type.primitive;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.expr.GimplePrimitiveConstant;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import static org.objectweb.asm.Opcodes.*;

public class PrimitiveConstGenerator extends AbstractExprGenerator implements ExprGenerator {

  private GimplePrimitiveType gimpleType;
  private Number value;

  public PrimitiveConstGenerator(GimplePrimitiveConstant constant) {
    value = constant.getValue();
    if(constant.getType() instanceof GimpleIndirectType) {
      gimpleType = new GimpleIntegerType(32);
    } else {
      gimpleType = (GimplePrimitiveType) constant.getType();
    }
  }

  public PrimitiveConstGenerator(GimplePrimitiveType gimpleType, Number value) {
    this.gimpleType = gimpleType;
    this.value = value;
  }

  public Number getValue() {
    return value;
  }

  @Override
  public void emitPrimitiveValue(MethodGenerator mv) {
    Type type = this.gimpleType.jvmType();
    if(type.equals(Type.LONG_TYPE)) {
      emitLong(mv, value.longValue());
    
    } else if(type.equals(Type.FLOAT_TYPE)) {
      emitFloat(mv, value.floatValue());
    
    } else if(type.equals(Type.DOUBLE_TYPE)) {
      emitDouble(mv, value.doubleValue());

    } else {
      emitInt(mv, value.intValue());
    }
  }

  public static void emitInt(MethodGenerator mv, int value) {
    if(value == -1) {
      mv.visitInsn(ICONST_M1);
    } else if(value >= 0 && value <= 5) {
      mv.visitInsn(ICONST_0 + value);
    } else if(value >= -128 && value <= 127) {
      mv.visitIntInsn(BIPUSH, value); 
    } else {
      mv.visitLdcInsn(value);
    }
  }

  public static void emitLong(MethodGenerator mv, long value) {
    if(value == 0) {
      mv.visitInsn(LCONST_0);
    } else if(value == 1) {
      mv.visitInsn(LCONST_1);
    } else {
      mv.visitLdcInsn(value);
    }
  }

  public static void emitFloat(MethodGenerator mv, float value) {
    if(value == 0) {
      mv.visitInsn(FCONST_0);
    } else if(value == 1) {
      mv.visitInsn(FCONST_1);
    } else if(value == 2) {
      mv.visitInsn(FCONST_2);
    } else {
      mv.visitLdcInsn(value);
    }
  }

  public static void emitDouble(MethodGenerator mv, double v) {
    if(v == 0) {
      mv.visitInsn(DCONST_0);
    } else if(v == 1) {
      mv.visitInsn(DCONST_1);
    } else {
      mv.visitLdcInsn(v);
    }
  }

  @Override
  public boolean isConstantIntEqualTo(int value) {
    return gimpleType.jvmType().equals(Type.INT_TYPE) && this.value.intValue() == value;
  }

  @Override
  public GimplePrimitiveType getGimpleType() {
    return gimpleType;
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveValue(this);
  }

}
