package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.arrays.PrimitiveArrayElement;
import org.renjin.gcc.codegen.call.MallocGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PrimitiveConstValueGenerator;
import org.renjin.gcc.codegen.pointers.AddressOfPrimitiveArray;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ALOAD;

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

    if(initialValue.isPresent()) {
      // provided an initial value for this array
      initialValue.get().emitPushArray(mv);
    } else {
      // allocate a new, empty array
      PrimitiveConstValueGenerator.emitInt(mv, gimpleType.getElementCount());
      MallocGenerator.emitNewArray(mv, componentType);

    }
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    mv.visitVarInsn(ALOAD, arrayIndex);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushArray(mv);
    mv.visitVarInsn(Opcodes.ASTORE, arrayIndex);
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
