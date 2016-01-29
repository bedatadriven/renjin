package org.renjin.gcc.codegen.type.record.fat;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.gimple.type.GimpleType;

public class DereferencedRecordFatPtr extends AbstractExprGenerator {
  
  private RecordClassTypeStrategy recordTypeStrategy;
  private ExprGenerator fatPointer;

  public DereferencedRecordFatPtr(RecordClassTypeStrategy recordTypeStrategy, ExprGenerator fatPointer) {
    this.recordTypeStrategy = recordTypeStrategy;
    this.fatPointer = fatPointer;
  }

  @Override
  public GimpleType getGimpleType() {
    return recordTypeStrategy.getRecordType();
  }

  @Override
  public ExprGenerator memberOf(String memberName) {
    return recordTypeStrategy.getFieldGenerator(memberName).memberExprGenerator(this);
  }

  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    fatPointer.emitPushPtrArrayAndOffset(mv);
    mv.visitInsn(Opcodes.AALOAD);
  }
}
