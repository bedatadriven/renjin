package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.expr.GimpleConstant;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;

import static org.objectweb.asm.Opcodes.*;

public class ConstValueGenerator implements ValueGenerator {

  private Type type;
  private Number value;

  public ConstValueGenerator(GimpleConstant constant) {
    type = ((GimplePrimitiveType) constant.getType()).jvmType();
    value = constant.getNumberValue();
  }

  public ConstValueGenerator(Type type, Number value) {
    this.type = type;
    this.value = value;
  }

  @Override
  public Type primitiveType() {
    return type;
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    if(type.equals(Type.INT_TYPE) || type.equals(Type.BOOLEAN_TYPE)) {
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

  private void emitInt(MethodVisitor mv, int value) {
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

  private void emitLong(MethodVisitor mv, long l) {
    throw new UnsupportedOperationException();
  }

  private void emitFloat(MethodVisitor mv, float value) {
    throw new UnsupportedOperationException();
  }

  private void emitDouble(MethodVisitor mv, double v) {
    if(v == 0) {
      mv.visitInsn(DCONST_0);
    } else if(v == 1) {
      mv.visitInsn(DCONST_1);
    } else {
      mv.visitLdcInsn(v);
    }
  }
  
  public ConstValueGenerator divideBy(int divisor) {
    if (type.equals(Type.INT_TYPE)) {
      return new ConstValueGenerator(type, value.intValue() / divisor);
    } else {
      throw new UnsupportedOperationException();
    }
  }
}
