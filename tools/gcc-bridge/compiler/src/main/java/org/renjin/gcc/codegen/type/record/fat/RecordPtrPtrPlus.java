package org.renjin.gcc.codegen.type.record.fat;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.gimple.type.GimpleType;


public class RecordPtrPtrPlus extends AbstractExprGenerator {

  private RecordClassTypeStrategy strategy;
  private ExprGenerator basePointer;
  private ExprGenerator offsetInBytes;

  public RecordPtrPtrPlus(RecordClassTypeStrategy strategy, ExprGenerator basePointer, ExprGenerator offsetInBytes) {
    this.strategy = strategy;
    this.basePointer = basePointer;
    this.offsetInBytes = offsetInBytes;
  }

  @Override
  public GimpleType getGimpleType() {
    return basePointer.getGimpleType();
  }

  @Override
  public ExprGenerator valueOf() {
    return new DereferencedRecordPtr(strategy, this);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
    basePointer.emitPushPtrArrayAndOffset(mv);
    addOffsetInBytes(mv, offsetInBytes);
  }
}
