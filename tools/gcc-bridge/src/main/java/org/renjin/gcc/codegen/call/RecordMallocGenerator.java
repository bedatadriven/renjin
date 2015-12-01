package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PrimitiveConstValueGenerator;
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
    return generator.getGimpleType().pointerTo();
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    int constantCount = isConstantCount(sizeGenerator);
    if(constantCount < 0) {
      throw new UnsupportedOperationException("TODO: dynamic record allocation size");
    } else {
      PrimitiveConstValueGenerator.emitInt(mv, constantCount);
      mv.visitTypeInsn(Opcodes.ANEWARRAY, generator.getType().getInternalName());
      
      for(int i=0; i< constantCount; ++i) {
        mv.visitInsn(Opcodes.DUP);
        PrimitiveConstValueGenerator.emitInt(mv, i);
        mv.visitTypeInsn(Opcodes.NEW, generator.getType().getInternalName());
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, generator.getType().getInternalName(), "<init>", "()V", false);
        mv.visitInsn(Opcodes.AASTORE);
      }
    }
    mv.visitInsn(Opcodes.ICONST_0);
  }

  private int isConstantCount(ExprGenerator sizeGenerator) {
    if(sizeGenerator instanceof PrimitiveConstValueGenerator) {
      return ((PrimitiveConstValueGenerator) sizeGenerator).getValue().intValue() /
          generator.getGimpleType().getSize();
    } else {
      return -1;
    }
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
