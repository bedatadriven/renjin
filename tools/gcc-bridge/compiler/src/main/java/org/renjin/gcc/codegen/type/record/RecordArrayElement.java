package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;


public class RecordArrayElement extends AbstractExprGenerator {
  private RecordClassGenerator record;
  private ExprGenerator array;
  private GimpleArrayType arrayType;
  private ExprGenerator indexGenerator;

  public RecordArrayElement(RecordClassGenerator record, ExprGenerator array, ExprGenerator indexGenerator) {
    this.record = record;
    this.array = array;
    this.arrayType = (GimpleArrayType) array.getGimpleType();
    this.indexGenerator = indexGenerator;
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType.getComponentType();
  }

  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    array.emitPushArray(mv);
    indexGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(Opcodes.AALOAD);
  }

  @Override
  public ExprGenerator memberOf(String memberName) {
    return record.getFieldGenerator(memberName).memberExprGenerator(this);
  }
}
