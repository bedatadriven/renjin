package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.*;
import org.renjin.gcc.gimple.type.GimpleIndirectType;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates loads and stores from a pointer variable
 */
public class PtrVarGenerator extends AbstractExprGenerator implements PtrGenerator, VarGenerator, LValueGenerator {

  private GimpleIndirectType type;
  
  /**
   * The local variable index storing the array backing the pointer
   */
  private int arrayVariableIndex;

  /**
   * The local varaible index storing the offset within the array
   */
  private int offsetVariableIndex;
  
  public PtrVarGenerator(GimpleType type, int arrayVariableIndex, int offsetVariableIndex) {
    this.type = (GimpleIndirectType) type;
    this.arrayVariableIndex = arrayVariableIndex;
    this.offsetVariableIndex = offsetVariableIndex;
  }

  public PtrVarGenerator(GimpleType type, LocalVarAllocator localVarAllocator) {
    this.type = (GimpleIndirectType) type;
    this.arrayVariableIndex = localVarAllocator.reserve(1);
    this.offsetVariableIndex = localVarAllocator.reserve(Type.INT_TYPE);
  }

  @Override
  public ExprGenerator valueOf() {
    return new MemRefGenerator(this);
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public void emitPushArrayAndOffset(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, arrayVariableIndex);
    mv.visitVarInsn(Opcodes.ILOAD, offsetVariableIndex);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    PtrGenerator ptrGenerator = (PtrGenerator) valueGenerator;
    
    ptrGenerator.emitPushArrayAndOffset(mv);

    mv.visitVarInsn(Opcodes.ISTORE, offsetVariableIndex);
    mv.visitVarInsn(Opcodes.ASTORE, arrayVariableIndex);
  }

  @Override
  public WrapperType getPointerType() {
    return WrapperType.forPointerType(this.type);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    
  }

}

