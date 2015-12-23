package org.renjin.gcc.codegen.type.record;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.VarGenerator;
import org.renjin.gcc.codegen.var.Var;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

public class RecordPtrVarGenerator extends AbstractExprGenerator implements VarGenerator, RecordUnitPtrGenerator {
  private Var var;
  private RecordClassGenerator recordGenerator;
  private GimpleType pointerType;

  public RecordPtrVarGenerator(RecordClassGenerator recordGenerator, Var var) {
    this.recordGenerator = recordGenerator;
    this.var = var;
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
    var.load(mv);
  }

  @Override
  public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushRecordRef(mv);
    var.store(mv);
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitTypeInsn(Opcodes.ANEWARRAY, recordGenerator.getType().getInternalName());
    mv.visitInsn(Opcodes.ICONST_0);
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