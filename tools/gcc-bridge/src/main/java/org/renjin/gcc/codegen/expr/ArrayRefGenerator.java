package org.renjin.gcc.codegen.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Generates a reference to an array element
 */
public class ArrayRefGenerator implements ValueGenerator, LValueGenerator {
  
  private final ArrayValueGenerator arrayGenerator;
  private final ValueGenerator indexGenerator;

  public ArrayRefGenerator(ExprGenerator arrayGenerator, ExprGenerator indexGenerator) {
    this.arrayGenerator = (ArrayValueGenerator) arrayGenerator;
    this.indexGenerator = (ValueGenerator) indexGenerator;
  }

  @Override
  public Type primitiveType() {
    return arrayGenerator.getComponentType();
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    arrayGenerator.emitPush(mv);
    indexGenerator.emitPush(mv);
    int opcode = arrayGenerator.getComponentType().getOpcode(Opcodes.IALOAD);
    mv.visitInsn(opcode);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator exprGenerator) {
    arrayGenerator.emitPush(mv);
    indexGenerator.emitPush(mv);
    
    ValueGenerator generator = (ValueGenerator) exprGenerator;
    generator.emitPush(mv);
    
    int opcode = arrayGenerator.getComponentType().getOpcode(Opcodes.IASTORE);
    mv.visitInsn(opcode);
  }

}
