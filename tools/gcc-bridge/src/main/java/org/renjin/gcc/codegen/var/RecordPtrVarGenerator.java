package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;

public class RecordPtrVarGenerator extends AbstractExprGenerator implements VarGenerator {
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

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushRecordRef(mv);
    mv.visitVarInsn(ASTORE, varIndex);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ACONST_NULL);
    mv.visitVarInsn(ASTORE, varIndex);
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