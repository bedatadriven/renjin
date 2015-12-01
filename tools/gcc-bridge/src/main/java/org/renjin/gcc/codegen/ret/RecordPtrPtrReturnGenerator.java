package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.runtime.ObjectPtr;

import java.util.List;


public class RecordPtrPtrReturnGenerator implements ReturnGenerator {
  
  private RecordClassGenerator recordClassGenerator;
  
  @Override
  public Type getType() {
    return Type.getType(ObjectPtr.class);
  }

  @Override
  public GimpleType getGimpleType() {
    return new GimplePointerType(new GimplePointerType(recordClassGenerator.getGimpleType()));
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushPointerWrapper(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitVoidReturn(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    throw new UnsupportedOperationException();
  }
}
