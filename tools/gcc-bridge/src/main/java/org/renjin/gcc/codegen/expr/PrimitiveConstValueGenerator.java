package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.pointers.AddressOfPrimitiveValue;
import org.renjin.gcc.gimple.expr.GimplePrimitiveConstant;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import static org.objectweb.asm.Opcodes.*;

public class PrimitiveConstValueGenerator extends AbstractExprGenerator implements ExprGenerator {

  private GimplePrimitiveType gimpleType;
  private Number value;

  public PrimitiveConstValueGenerator(GimplePrimitiveConstant constant) {
    value = constant.getValue();
    if(constant.getType() instanceof GimpleIndirectType) {
      gimpleType = new GimpleIntegerType(32);
    } else {
      gimpleType = (GimplePrimitiveType) constant.getType();
    }
  }

  public PrimitiveConstValueGenerator(GimplePrimitiveType gimpleType, Number value) {
    this.gimpleType = gimpleType;
    this.value = value;
  }

  public Number getValue() {
    return value;
  }

  @Override
  public void emitPrimitiveValue(MethodVisitor mv) {
    Type type = this.gimpleType.jvmType();
    if(type.equals(Type.INT_TYPE) || type.equals(Type.BOOLEAN_TYPE) || type.equals(Type.BYTE_TYPE)) {
      emitInt(mv, value.intValue());
    
    } else if(type.equals(Type.LONG_TYPE)) {
      emitLong(mv, value.longValue());
    
    } else if(type.equals(Type.FLOAT_TYPE)) {
      emitFloat(mv, value.floatValue());
    
    } else if(type.equals(Type.DOUBLE_TYPE)) {
      emitDouble(mv, value.doubleValue());
    
    } else {
      throw new UnsupportedOperationException(type.toString());
    }
  }

  public static void emitInt(MethodVisitor mv, int value) {
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

  public static void emitLong(MethodVisitor mv, long value) {
    if(value == 0) {
      mv.visitInsn(LCONST_0);
    } else if(value == 1) {
      mv.visitInsn(LCONST_1);
    } else {
      mv.visitLdcInsn(value);
    }
  }

  public static void emitFloat(MethodVisitor mv, float value) {
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

  public static void emitDouble(MethodVisitor mv, double v) {
    if(v == 0) {
      mv.visitInsn(DCONST_0);
    } else if(v == 1) {
      mv.visitInsn(DCONST_1);
    } else {
      mv.visitLdcInsn(v);
    }
  }
  
  @Override
  public PrimitiveConstValueGenerator divideBy(int divisor) {
    if (gimpleType.jvmType().equals(Type.INT_TYPE)) {
      return new PrimitiveConstValueGenerator(gimpleType, value.intValue() / divisor);
    } else {
      throw new UnsupportedOperationException();
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
