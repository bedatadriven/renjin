package org.renjin.gcc.codegen.ret;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.ValueGenerator;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

import static org.objectweb.asm.Opcodes.IRETURN;


public class PrimitiveReturnGenerator implements ReturnGenerator {
  
  private GimplePrimitiveType gimpleType;
  private Type type;
  
  public PrimitiveReturnGenerator(GimpleType gimpleType) {
    Preconditions.checkNotNull(gimpleType);
    this.gimpleType = (GimplePrimitiveType) gimpleType;
    this.type = ((GimplePrimitiveType) gimpleType).jvmType();
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public GimpleType getGimpleType() {
    return gimpleType;
  }

  @Override
  public void emitReturn(MethodVisitor mv, ExprGenerator valueGenerator) {
    ValueGenerator primitiveGenerator = (ValueGenerator) valueGenerator;
    primitiveGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(type.getOpcode(IRETURN));
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
      return gimpleType;
    }

    @Override
    public void emitPrimitiveValue(MethodVisitor mv) {
      callGenerator.emitCall(mv, argumentGenerators);
    }
  }
}
