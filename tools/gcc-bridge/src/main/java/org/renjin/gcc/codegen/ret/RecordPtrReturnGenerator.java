package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Handles Recorded-typed returning
 */
public class RecordPtrReturnGenerator implements ReturnGenerator {
  
  private RecordClassGenerator recordClassGenerator;

  public RecordPtrReturnGenerator(RecordClassGenerator recordClassGenerator) {
    this.recordClassGenerator = recordClassGenerator;
  }

  @Override
  public Type getType() {
    return recordClassGenerator.getType();
  }

  @Override
  public GimpleType getGimpleType() {
    return recordClassGenerator.getGimpleType();
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushRecordRef(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitVoidReturn(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    return new ReturnExpr(callGenerator, arguments);
  }
  
  private class ReturnExpr extends AbstractExprGenerator {


    private final CallGenerator callGenerator;
    private final List<ExprGenerator> arguments;

    public ReturnExpr(CallGenerator callGenerator, List<ExprGenerator> arguments) {
      this.callGenerator = callGenerator;
      this.arguments = arguments;
    }

    @Override
    public GimpleType getGimpleType() {
      return new GimplePointerType(recordClassGenerator.getGimpleType());
    }

    @Override
    public void emitPushRecordRef(MethodVisitor mv) {
      callGenerator.emitCall(mv, arguments);
      // Record ref is now on stack
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodVisitor mv) {
      mv.visitInsn(Opcodes.ICONST_0);
      mv.visitTypeInsn(Opcodes.ANEWARRAY, recordClassGenerator.getType().getInternalName());
      mv.visitInsn(Opcodes.DUP);
      mv.visitInsn(Opcodes.ICONST_0);
      mv.visitTypeInsn(Opcodes.NEW, recordClassGenerator.getType().getInternalName());
      mv.visitInsn(Opcodes.DUP);
      mv.visitMethodInsn(Opcodes.INVOKESPECIAL, recordClassGenerator.getType().getInternalName(), "<init>", "()V", false);
      mv.visitInsn(Opcodes.AASTORE);
      mv.visitInsn(Opcodes.ICONST_0);
    }
  }
  
}
