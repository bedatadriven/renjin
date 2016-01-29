package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.record.RecordClassTypeStrategy;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Strategy for returning a pointer to a single record as a simple JVM object reference.
 */
public class RecordUnitPtrReturnStrategy implements ReturnStrategy {

  private RecordClassTypeStrategy strategy;

  public RecordUnitPtrReturnStrategy(RecordClassTypeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public Type getType() {
    return strategy.getJvmType();
  }

  @Override
  public void emitReturnValue(MethodGenerator mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushRecordRef(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitReturnDefault(MethodGenerator mv) {
    mv.visitInsn(Opcodes.ACONST_NULL);
    mv.visitInsn(Opcodes.ARETURN);
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
      return new GimplePointerType(strategy.getRecordType());
    }

    @Override
    public void emitPushRecordRef(MethodGenerator mv) {
      callGenerator.emitCall(mv, arguments);
      // Record ref is now on stack
    }

    @Override
    public void emitPushPtrArrayAndOffset(MethodGenerator mv) {
      mv.visitInsn(Opcodes.ICONST_1);
      mv.visitTypeInsn(Opcodes.ANEWARRAY, strategy.getJvmType().getInternalName());
      mv.visitInsn(Opcodes.DUP);
      mv.visitInsn(Opcodes.ICONST_0);
      emitPushRecordRef(mv);
      mv.visitInsn(Opcodes.AASTORE);
      mv.visitInsn(Opcodes.ICONST_0);
    }
  }

}
