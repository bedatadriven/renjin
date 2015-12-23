package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Generates a call to a soon-to-be compiled Gimple function
 */
public class FunctionCallGenerator implements CallGenerator {

  private final FunctionGenerator functionGenerator;


  public FunctionCallGenerator(FunctionGenerator functionGenerator) {
    this.functionGenerator = functionGenerator;
  }

  public FunctionGenerator getFunctionGenerator() {
    return functionGenerator;
  }

  public Handle getHandle() {
    return functionGenerator.getMethodHandle();
  }

  @Override
  public void emitCall(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {

    // Push all parameters on the stack
    List<ParamStrategy> paramStrategies = functionGenerator.getParamGenerators();
    for (int i = 0; i < paramStrategies.size(); i++) {
      ParamStrategy paramStrategy = paramStrategies.get(i);
      paramStrategy.emitPushParameter(mv, argumentGenerators.get(i));
    }
    
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
        functionGenerator.getClassName(), 
        functionGenerator.getMangledName(), descriptor(), false);
  }

  @Override
  public void emitCallAndPopResult(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {
    emitCall(mv, argumentGenerators);
    switch (functionGenerator.getReturnStrategy().getType().getSize()) {
      case 0:
        // NOOP
        break;
      case 1:
        mv.visitInsn(Opcodes.POP);
        break;
      case 2:
        mv.visitInsn(Opcodes.POP2);
        break;
      default:
        throw new UnsupportedOperationException();
    }
    
  }

  @Override
  public ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators) {
    return functionGenerator.getReturnStrategy().callExpression(this, argumentGenerators);
  }

  public String getClassName() {
    return functionGenerator.getClassName();
  }

  private String descriptor() {
    return functionGenerator.getFunctionDescriptor();
  }
}
