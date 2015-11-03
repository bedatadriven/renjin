package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ALOAD;

public class RecordPtrVarGenerator extends AbstractExprGenerator implements ExprGenerator {
  private int varIndex;
  private RecordClassGenerator recordGenerator;
  private GimpleType pointerType;

  public RecordPtrVarGenerator(RecordClassGenerator recordGenerator, int varIndex) {
    this.recordGenerator = recordGenerator;
    this.varIndex = varIndex;
    this.pointerType = new GimplePointerType(recordGenerator.getGimpleType());
  }
  
  @Override
  public GimpleType getGimpleType() {
    return pointerType;
  }

  @Override
  public ExprGenerator valueOf() {
    return new ValueOf();
  }


  @Override
  public void emitPushRecordRef(MethodVisitor mv) {
    mv.visitVarInsn(ALOAD, varIndex);
  }

  private class ValueOf extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return pointerType.getBaseType();
    }

    @Override
    public ExprGenerator addressOf() {
      return RecordPtrVarGenerator.this;
    }

    @Override
    public ExprGenerator memberOf(String memberName) {
      return recordGenerator.getFieldGenerator(memberName).memberExprGenerator(RecordPtrVarGenerator.this);
    }
  }
}