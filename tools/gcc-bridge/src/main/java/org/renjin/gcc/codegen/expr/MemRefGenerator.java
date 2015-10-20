package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates the bytecode to dereference a pointer expression
 */
public class MemRefGenerator extends AbstractExprGenerator implements ValueGenerator, LValueGenerator {
  
  private PtrGenerator ptrGenerator;

  public MemRefGenerator(ExprGenerator ptrGenerator) {
    this.ptrGenerator = (PtrGenerator) ptrGenerator;
  }

  @Override
  public Type getValueType() {
    // not quite right probably...
    return ptrGenerator.getPointerType().getBaseType();
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    // push array onto stack
    ptrGenerator.emitPushArrayAndOffset(mv);
    mv.visitInsn(typeOpcode(Opcodes.IALOAD));
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    
    // XASTORE = arrayref, index, value
    
    ptrGenerator.emitPushArrayAndOffset(mv);
    valueGenerator.emitPushValue(mv);
    mv.visitInsn(typeOpcode(Opcodes.IASTORE));
  }

  @Override
  public GimpleType getGimpleType() {
    return ptrGenerator.getGimpleType().getBaseType();
  }

  private int typeOpcode(int opcode) {
    Type baseType = ptrGenerator.getPointerType().getBaseType();
    return baseType.getOpcode(opcode);
  }
}
