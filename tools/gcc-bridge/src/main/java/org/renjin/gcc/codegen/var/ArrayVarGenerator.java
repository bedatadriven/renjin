package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Emits bytecode for generating a byte cod
 */
public class ArrayVarGenerator implements ArrayValueGenerator, LValueGenerator, VarGenerator,
    AddressableGenerator {

  /**
   * The local variable index of the array
   */
  private final int index;
  private Type componentType;
  private GimpleArrayType gimpleType;

  public ArrayVarGenerator(GimpleArrayType gimpleType, LocalVarAllocator localVarAllocator) {
    this.gimpleType = gimpleType;
    index = localVarAllocator.reserve(1);

    GimpleType componentType = gimpleType.getComponentType();
    if(componentType instanceof GimplePrimitiveType) {
      this.componentType = ((GimplePrimitiveType) componentType).jvmType();
    } else if(componentType instanceof GimplePointerType) {
      this.componentType = WrapperType.wrapperType(componentType.getBaseType());
    } else {
      throw new UnsupportedOperationException("componentType: " + this.componentType);
    }
  }

  @Override
  public Type primitiveType() {
    return Type.getType("[" + componentType.getDescriptor());
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, index);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Type getComponentType() {
    return componentType;
  }

  @Override
  public ExprGenerator addressOf() {
    return new ArrayVarPtrGenerator(this);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    
    mv.visitLdcInsn(gimpleType.getUbound() - gimpleType.getLbound() + 1);
    
    if(componentType.equals(Type.DOUBLE_TYPE)) {
      mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_DOUBLE);
    } else if(componentType.equals(Type.INT_TYPE)) {
      mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
    } else {
      throw new UnsupportedOperationException("componentType: " + componentType);
    }
    mv.visitVarInsn(Opcodes.ASTORE, index);
  }
}
