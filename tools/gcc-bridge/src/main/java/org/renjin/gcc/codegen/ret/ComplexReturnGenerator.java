package org.renjin.gcc.codegen.ret;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleComplexType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

public class ComplexReturnGenerator implements ReturnGenerator {
  
  private GimpleComplexType type;

  public ComplexReturnGenerator(GimpleComplexType type) {
    this.type = type;
  }

  @Override
  public Type getType() {
    return type.getJvmPartArrayType();
  }

  @Override
  public GimpleType getGimpleType() {
    return type;
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPushComplexAsArray(mv);
    mv.visitInsn(Opcodes.ARETURN);
  }

  @Override
  public void emitVoidReturn(MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    return new CallExpr(callGenerator, arguments);
  }

  private class CallExpr extends AbstractExprGenerator {
    private CallGenerator callGenerator;
    private List<ExprGenerator> argumentGenerators;

    public CallExpr(CallGenerator callGenerator, List<ExprGenerator> argumentGenerators) {
      this.callGenerator = callGenerator;
      this.argumentGenerators = argumentGenerators;
    }

    @Override
    public GimpleType getGimpleType() {
      return type;
    }

    @Override
    public void emitPushComplexAsArray(MethodVisitor mv) {
      callGenerator.emitCall(mv, argumentGenerators);
    }
  }
}
