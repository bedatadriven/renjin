package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;


public class DereferencedRecordPtr extends AbstractExprGenerator {
  
  private RecordClassGenerator recordClassGenerator;
  private ExprGenerator pointerPointer;

  public DereferencedRecordPtr(RecordClassGenerator recordClassGenerator, ExprGenerator pointerPointer) {
    this.recordClassGenerator = recordClassGenerator;
    this.pointerPointer = pointerPointer;
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(recordClassGenerator.getGimpleType());
  }
  
  @Override
  public ExprGenerator addressOf() {
    return pointerPointer;
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    pointerPointer.emitPushPtrArrayAndOffset(mv);
    valueGenerator.emitPushPointerWrapper(mv);
    mv.visitInsn(Opcodes.AASTORE);
  }

  @Override
  public void emitPushPointerWrapper(MethodVisitor mv) {
    pointerPointer.emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.AALOAD);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    emitPushPointerWrapper(mv);
    WrapperType.OBJECT_PTR.emitUnpackArrayAndOffset(mv);
  }

  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.AALOAD);
    mv.visitTypeInsn(Opcodes.CHECKCAST, recordClassGenerator.getType().getInternalName());
  }
}
