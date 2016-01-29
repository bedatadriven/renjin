package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.gimple.type.GimpleType;


public class DereferencedUnitRecordPtr extends AbstractExprGenerator implements RecordUnitPtrGenerator {
  private RecordClassTypeStrategy strategy;
  private ExprGenerator pointerPointer;

  public DereferencedUnitRecordPtr(RecordClassTypeStrategy strategy, ExprGenerator pointerPointer) {
    this.strategy = strategy;
    this.pointerPointer = pointerPointer;
  }

  @Override
  public GimpleType getGimpleType() {
    return pointerPointer.getGimpleType().getBaseType();
  }

  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    pointerPointer.emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.AALOAD);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    pointerPointer.emitPushPtrArrayAndOffset(mv);
    valueGenerator.emitPushRecordRef(mv);
    mv.visitInsn(Opcodes.AASTORE);
  }

  @Override
  public ExprGenerator addressOf() {
    return pointerPointer;
  }


  @Override
  public ExprGenerator memberOf(String memberName) {
    return strategy.getFieldGenerator(memberName).memberExprGenerator(this);
  }
}
