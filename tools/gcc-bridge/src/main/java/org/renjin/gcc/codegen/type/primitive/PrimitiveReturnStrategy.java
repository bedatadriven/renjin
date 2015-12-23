package org.renjin.gcc.codegen.type.primitive;

import com.google.common.base.Preconditions;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.gimple.type.GimplePrimitiveType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

import static org.objectweb.asm.Opcodes.IRETURN;


/**
 * Strategy for returning primitive value using {@code IRETURN}, {@code DRETURN}, etc
 */
public class PrimitiveReturnStrategy implements ReturnStrategy {
  
  private GimplePrimitiveType gimpleType;
  private Type type;
  
  public PrimitiveReturnStrategy(GimpleType gimpleType) {
    Preconditions.checkNotNull(gimpleType);
    this.gimpleType = (GimplePrimitiveType) gimpleType;
    this.type = ((GimplePrimitiveType) gimpleType).jvmType();
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void emitReturnValue(MethodVisitor mv, ExprGenerator valueGenerator) {
    valueGenerator.emitPrimitiveValue(mv);
    mv.visitInsn(type.getOpcode(IRETURN));
  }

  @Override
  public void emitReturnDefault(MethodVisitor mv) {
    // GCC allows not returning a value from a method.
    // we need to return the default for this type to satifisfy the JVM
    if(type.equals(Type.DOUBLE_TYPE)) {
      mv.visitInsn(Opcodes.DCONST_0);
    } else if(type.equals(Type.FLOAT_TYPE)) {
      mv.visitInsn(Opcodes.FCONST_0);
    } else if(type.equals(Type.LONG_TYPE)) {
      mv.visitInsn(Opcodes.LCONST_0);
    } else {
      mv.visitInsn(Opcodes.ICONST_0);
    }
    mv.visitInsn(type.getOpcode(IRETURN));
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
