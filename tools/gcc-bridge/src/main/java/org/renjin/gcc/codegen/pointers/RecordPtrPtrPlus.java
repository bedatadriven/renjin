package org.renjin.gcc.codegen.pointers;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;


public class RecordPtrPtrPlus extends AbstractExprGenerator {

  private RecordClassGenerator recordClassGenerator;
  private ExprGenerator basePointer;
  private ExprGenerator offsetInBytes;

  public RecordPtrPtrPlus(RecordClassGenerator recordClassGenerator, ExprGenerator basePointer, ExprGenerator offsetInBytes) {
    this.recordClassGenerator = recordClassGenerator;
    this.basePointer = basePointer;
    this.offsetInBytes = offsetInBytes;
  }

  @Override
  public GimpleType getGimpleType() {
    return basePointer.getGimpleType();
  }

  @Override
  public ExprGenerator valueOf() {
    return new DereferencedRecordPtr(recordClassGenerator, this);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    basePointer.emitPushPtrArrayAndOffset(mv);
    addOffsetInBytes(mv, offsetInBytes);
  }
}
