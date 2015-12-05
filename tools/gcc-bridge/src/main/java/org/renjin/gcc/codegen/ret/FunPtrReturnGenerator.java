package org.renjin.gcc.codegen.ret;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class FunPtrReturnGenerator implements ReturnGenerator {
  
  private GimpleFunctionType functionType;
  
  @Override
  public Type getType() {
    return Type.getType(MethodHandle.class);
  }

  @Override
  public GimpleType getGimpleType() {
    return functionType.pointerTo();
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushMethodHandle(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitVoidReturn(MethodVisitor mv) {
    mv.visitInsn(Opcodes.ACONST_NULL);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    return new CallExpr(callGenerator, arguments);
  }

  private class CallExpr extends AbstractExprGenerator {
    private final CallGenerator callGenerator;
    private final List<ExprGenerator> arguments;

    public CallExpr(CallGenerator callGenerator, List<ExprGenerator> arguments) {
      this.callGenerator = callGenerator;
      this.arguments = arguments;
    }


    @Override
    public GimpleType getGimpleType() {
      return functionType.pointerTo();
    }
  }
}
