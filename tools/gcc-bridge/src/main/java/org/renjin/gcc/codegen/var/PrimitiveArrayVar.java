package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.arrays.PrimitiveArrayElement;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.pointers.AddressOfPrimitiveArray;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.NEWARRAY;

/**
 * Emits bytecode for loading / storing array variables.
 * 
 */
public class PrimitiveArrayVar extends AbstractExprGenerator implements VarGenerator {

  /**
   * The local variable index of the array
   */
  private final int arrayIndex;
  private Type componentType;
  private GimpleArrayType gimpleType;

  public PrimitiveArrayVar(GimpleArrayType gimpleType, int arrayIndex) {
    this.gimpleType = gimpleType;
    this.arrayIndex = arrayIndex;

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
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {

    mv.visitLdcInsn(gimpleType.getUbound() - gimpleType.getLbound() + 1);

    if(componentType.equals(Type.DOUBLE_TYPE)) {
      mv.visitIntInsn(NEWARRAY, Opcodes.T_DOUBLE);
      
    } else if(componentType.equals(Type.INT_TYPE)) {
      mv.visitIntInsn(NEWARRAY, Opcodes.T_INT);

    } else if(componentType.equals(Type.BYTE_TYPE)) {
      mv.visitIntInsn(NEWARRAY, Opcodes.T_BYTE);
      
    } else {
      throw new UnsupportedOperationException("componentType: " + componentType);
    }
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
   
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    mv.visitVarInsn(ALOAD, arrayIndex);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GimpleArrayType getGimpleType() {
    return gimpleType;
  }
  
  @Override
  public ExprGenerator addressOf() {
    return new AddressOfPrimitiveArray(this);
  }

  @Override
  public ExprGenerator elementAt(ExprGenerator indexGenerator) {
    return new PrimitiveArrayElement(this, indexGenerator);
  }

}
