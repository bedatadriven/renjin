package org.renjin.gcc.codegen.type.record.unit;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.runtime.ObjectPtr;

import java.util.List;


public class RecordUnitPtrPtrReturnStrategy implements ReturnStrategy {
  
  private RecordClassGenerator recordClassGenerator;
  
  @Override
  public Type getType() {
    return Type.getType(ObjectPtr.class);
  }

  @Override
  public void emitReturnValue(MethodGenerator mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushPointerWrapper(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitReturnDefault(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    throw new UnsupportedOperationException();
  }
}
