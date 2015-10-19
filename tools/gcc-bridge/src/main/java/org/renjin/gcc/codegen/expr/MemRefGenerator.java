package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Generates the bytecode to dereference a pointer expression
 */
public class MemRefGenerator implements PrimitiveGenerator, LValueGenerator {
  
  private PtrGenerator ptrGenerator;

  public MemRefGenerator(ExprGenerator ptrGenerator) {
    this.ptrGenerator = (PtrGenerator) ptrGenerator;
  }

  @Override
  public Type primitiveType() {
    return ptrGenerator.baseType();
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    // push array onto stack
    ptrGenerator.emitPushArrayAndOffset(mv);
    mv.visitInsn(ptrGenerator.baseType().getOpcode(Opcodes.IALOAD));
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    
    // XASTORE = arrayref, index, value
    
    ptrGenerator.emitPushArrayAndOffset(mv);
    
    // push the value to assign
    PrimitiveGenerator primitiveGenerator = (PrimitiveGenerator) valueGenerator;
    primitiveGenerator.emitPush(mv);
    
    mv.visitInsn(primitiveType().getOpcode(Opcodes.IASTORE));
  }
}
