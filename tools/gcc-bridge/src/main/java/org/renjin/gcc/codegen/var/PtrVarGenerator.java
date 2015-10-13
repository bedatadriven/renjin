package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.LValueGenerator;
import org.renjin.gcc.codegen.expr.PtrGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates loads and stores from a pointer variable
 */
public class PtrVarGenerator implements PtrGenerator, VarGenerator, LValueGenerator {

  private GimpleType baseType;
  
  /**
   * The local variable index storing the array backing the pointer
   */
  private int arrayVariableIndex;

  /**
   * The local varaible index storing the offset within the array
   */
  private int offsetVariableIndex;


  public PtrVarGenerator(GimpleType baseType, int arrayVariableIndex, int offsetVariableIndex) {
    this.baseType = baseType;
    this.arrayVariableIndex = arrayVariableIndex;
    this.offsetVariableIndex = offsetVariableIndex;
  }

  public PtrVarGenerator(GimpleType baseType, LocalVarAllocator localVarAllocator) {
    this.baseType = baseType;
    this.arrayVariableIndex = localVarAllocator.reserve(1);
    this.offsetVariableIndex = localVarAllocator.reserve(Type.INT_TYPE);
  }


  @Override
  public GimpleType gimpleBaseType() {
    return baseType;
  }

  @Override
  public Type baseType() {
    if(baseType instanceof GimplePrimitiveType) {
      return ((GimplePrimitiveType) baseType).jvmType();
    } else {
      throw new UnsupportedOperationException("baseType: " + baseType);
    }
  }

  @Override
  public boolean isSameArray(PtrGenerator other) {
    return this == other;
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    PtrGenerator ptrGenerator = (PtrGenerator) valueGenerator;
    
    // Check types
    // TODO
    
    // If we are only updating the pointer's offset, no need to update the 
    // array part of the pointer 
    if(!ptrGenerator.isSameArray(this)) {
      ptrGenerator.emitPushArray(mv);
      mv.visitVarInsn(Opcodes.ASTORE, arrayVariableIndex);
    }
    
    ptrGenerator.emitPushOffset(mv);
    mv.visitVarInsn(Opcodes.ISTORE, offsetVariableIndex);
  }

  @Override
  public void emitPushArray(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, arrayVariableIndex);
  }

  @Override
  public void emitPushOffset(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ILOAD, offsetVariableIndex);
  }
}

