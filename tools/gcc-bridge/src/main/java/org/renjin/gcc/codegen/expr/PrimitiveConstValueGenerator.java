package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.gimple.expr.GimplePrimitiveConstant;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

public class PrimitiveConstValueGenerator extends AbstractExprGenerator implements ValueGenerator {

  private GimplePrimitiveType gimpleType;
  private Number value;

  public PrimitiveConstValueGenerator(GimplePrimitiveConstant constant) {
    value = constant.getValue();
    gimpleType = (GimplePrimitiveType) constant.getType();
  }

  public PrimitiveConstValueGenerator(GimplePrimitiveType gimpleType, Number value) {
    this.gimpleType = gimpleType;
    this.value = value;
  }

  @Override
  public Type getValueType() {
    return gimpleType.jvmType();
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    Type type = this.gimpleType.jvmType();
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

  private void emitLong(MethodVisitor mv, long value) {
    if(value == 0) {
      mv.visitInsn(LCONST_0);
    } else if(value == 1) {
      mv.visitInsn(LCONST_1);
    } else {
      mv.visitLdcInsn(value);
    }
  }

  private void emitFloat(MethodVisitor mv, float value) {
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

  private void emitDouble(MethodVisitor mv, double v) {
    if(v == 0) {
      mv.visitInsn(DCONST_0);
    } else if(v == 1) {
      mv.visitInsn(DCONST_1);
    } else {
      mv.visitLdcInsn(v);
    }
  }
  
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
  public GimpleType getGimpleType() {
    return gimpleType;
  }

  @Override
  public ExprGenerator addressOf() {
    return new AddressOf();
  }

  private class AddressOf extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(gimpleType);
    }

    @Override
    public WrapperType getPointerType() {
      return WrapperType.of(gimpleType.jvmType());
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      Type type = gimpleType.jvmType();

      // Allocate a new array of size 1 and push to the stack
      mv.visitInsn(ICONST_1);
      MallocGenerator.emitNewArray(mv, type);
      
      // Initialize first and only element
      // IASTORE: (array, index, value) 
      mv.visitInsn(DUP);
      mv.visitInsn(ICONST_0);
      PrimitiveConstValueGenerator.this.emitPushValue(mv);
      mv.visitInsn(type.getOpcode(IASTORE));
      
      // should still have the array on the stack
      
      // now push the offset
      mv.visitInsn(ICONST_0);
    }
  }
}
