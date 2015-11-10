package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.FunctionGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleRealType;
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
    List<ParamGenerator> paramGenerators = functionGenerator.getParamGenerators();
    for (int i = 0; i < paramGenerators.size(); i++) {
      ParamGenerator paramGenerator = paramGenerators.get(i);
      paramGenerator.emitPushParameter(mv, argumentGenerators.get(i));
    }
    
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
        functionGenerator.getClassName(), 
        functionGenerator.getMangledName(), descriptor(), false);
  }

  @Override
  public Type returnType() {
    return functionGenerator.getReturnGenerator().getType();
  }

  @Override
  public GimpleType getGimpleReturnType() {
    Type returnType = returnType();    
    if(returnType.equals(Type.INT_TYPE)) {
      return new GimpleIntegerType(32);
    } else if(returnType.equals(Type.LONG_TYPE)) {
      return new GimpleIntegerType(64);
    } else if(returnType.equals(Type.FLOAT_TYPE)) {
      return new GimpleRealType(32);
    } else if(returnType.equals(Type.DOUBLE_TYPE)) {
      return new GimpleRealType(64);
    } else if(returnType.equals(Type.BOOLEAN_TYPE)) {
      return new GimpleBooleanType();
    } else {
      throw new UnsupportedOperationException("returnType: " + returnType);
    }
  } 

  @Override
  public ExprGenerator expressionGenerator(List<ExprGenerator> argumentGenerators) {
    return functionGenerator.getReturnGenerator().callExpression(this, argumentGenerators);
  }

  public String getClassName() {
    return functionGenerator.getClassName();
  }

  private String descriptor() {
    return functionGenerator.getFunctionDescriptor();
  }
}
