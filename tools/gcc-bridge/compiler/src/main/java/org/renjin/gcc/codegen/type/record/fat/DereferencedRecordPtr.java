package org.renjin.gcc.codegen.type.record.fat;

import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.gimple.type.GimpleType;


public class DereferencedRecordPtr extends AbstractExprGenerator {
  
  private RecordClassTypeStrategy strategy;
  private ExprGenerator pointerPointer;

  public DereferencedRecordPtr(RecordClassTypeStrategy strategy, ExprGenerator pointerPointer) {
    this.strategy = strategy;
    this.pointerPointer = pointerPointer;
  }

  @Override
  public GimpleType getGimpleType() {
    return strategy.getRecordType().pointerTo();
  }
  
  @Override
  public ExprGenerator addressOf() {
    return pointerPointer;
  }

  @Override
  public void emitStore(MethodGenerator mv, ExprGenerator valueGenerator) {
    pointerPointer.emitPushPtrArrayAndOffset(mv);
    valueGenerator.emitPushPointerWrapper(mv);
    mv.visitInsn(Opcodes.AASTORE);
  }

  @Override
  public void emitPushPointerWrapper(MethodGenerator mv) {
    pointerPointer.emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.AALOAD);
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
    emitPushPointerWrapper(mv);
    WrapperType.OBJECT_PTR.emitUnpackArrayAndOffset(mv);
  }

  @Override
  public void emitPushRecordRef(MethodGenerator mv) {
    emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.AALOAD);
    mv.visitTypeInsn(Opcodes.CHECKCAST, strategy.getJvmType().getInternalName());
  }
}
