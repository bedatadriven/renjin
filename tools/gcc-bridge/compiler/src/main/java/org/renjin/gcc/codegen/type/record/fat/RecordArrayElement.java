package org.renjin.gcc.codegen.type.record.fat;

import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.gimple.type.GimpleArrayType;
import org.renjin.gcc.gimple.type.GimpleType;


public class RecordArrayElement extends AbstractExprGenerator {
  private RecordClassTypeStrategy strategy;
  private ExprGenerator array;
  private GimpleArrayType arrayType;
  private ExprGenerator indexGenerator;

  public RecordArrayElement(RecordClassTypeStrategy strategy, ExprGenerator array, ExprGenerator indexGenerator) {
    this.strategy = strategy;
    this.array = array;
    this.arrayType = (GimpleArrayType) array.getGimpleType();
    this.indexGenerator = indexGenerator;
  }

  @Override
  public GimpleType getGimpleType() {
    return arrayType.getComponentType();
  }

  @Override
  public void emitPushRecordRef(MethodGenerator mv) {
    array.emitPushArray(mv);
    indexGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(Opcodes.AALOAD);
  }

  @Override
  public ExprGenerator memberOf(String memberName) {
    return strategy.getFieldGenerator(memberName).memberExprGenerator(this);
  }
}
