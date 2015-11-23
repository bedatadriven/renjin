package org.renjin.gcc.codegen.var;

import com.google.common.base.Optional;
import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import static org.objectweb.asm.Opcodes.*;

public class RecordVarGenerator extends AbstractExprGenerator implements VarGenerator {

  private int varIndex;
  private RecordClassGenerator recordGenerator;

  public RecordVarGenerator(RecordClassGenerator recordGenerator, int varIndex) {
    this.recordGenerator = recordGenerator;
    this.varIndex = varIndex;
  }

  @Override
  public void emitDefaultInit(MethodVisitor mv, Optional<ExprGenerator> initialValue) {
    mv.visitTypeInsn(NEW, recordGenerator.getClassName());
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, recordGenerator.getClassName(), "<init>", "()V", false);
    mv.visitVarInsn(ASTORE, varIndex);
    
    if(initialValue.isPresent()) {
      emitStore(mv, initialValue.get());
    }
  }

  @Override
  public ExprGenerator addressOf() {
    return new Pointer();
  }

  @Override
  public GimpleType getGimpleType() {
    return recordGenerator.getGimpleType();
  }

  public ExprGenerator memberOf(String name) {
    return recordGenerator.getFieldGenerator(name).memberExprGenerator(addressOf());
  }
  
  public class Pointer extends AbstractExprGenerator {

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(recordGenerator.getGimpleType());
    }

    @Override
    public void emitPushRecordRef(MethodVisitor mv) {
      mv.visitVarInsn(ALOAD, varIndex);
    }
  }
}
