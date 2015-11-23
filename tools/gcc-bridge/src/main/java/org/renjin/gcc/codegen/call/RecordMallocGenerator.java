package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Generates malloc() calls for Record pointers
 */
public class RecordMallocGenerator extends AbstractExprGenerator {

  private RecordClassGenerator generator;
  private final ExprGenerator sizeGenerator;

  public RecordMallocGenerator(RecordClassGenerator generator, ExprGenerator sizeGenerator) {
    this.generator = generator;
    this.sizeGenerator = sizeGenerator;
  }

  @Override
  public GimpleType getGimpleType() {
    return generator.getGimpleType();
  }

  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    assertUnitLength();
  
    mv.visitTypeInsn(Opcodes.NEW, generator.getType().getInternalName());
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, generator.getType().getInternalName(), "<init>", "()V", false);
  }

  private void assertUnitLength() {
    if(sizeGenerator.isConstantIntEqualTo(generator.getGimpleType().sizeOf())) {
      // we can be sure we're allocating exactly one record
      return;
    }
    throw new InternalCompilerException("Size cannot be determined to be 1");
  }
}
