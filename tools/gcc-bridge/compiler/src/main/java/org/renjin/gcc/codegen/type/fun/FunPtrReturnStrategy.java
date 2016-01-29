package org.renjin.gcc.codegen.type.fun;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.lang.invoke.MethodHandle;
import java.util.List;

/**
 * Strategy for returning a pointer to a function using a {@link MethodHandle}.
 */
public class FunPtrReturnStrategy implements ReturnStrategy {
  
  private GimpleFunctionType functionType;
  
  @Override
  public Type getType() {
    return Type.getType(MethodHandle.class);
  }

  @Override
  public void emitReturnValue(MethodGenerator mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushMethodHandle(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitReturnDefault(MethodGenerator mv) {
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

    @Override
    public void emitPushMethodHandle(MethodGenerator mv) {
      callGenerator.emitCall(mv, arguments);
    }
  }
  
  
}
