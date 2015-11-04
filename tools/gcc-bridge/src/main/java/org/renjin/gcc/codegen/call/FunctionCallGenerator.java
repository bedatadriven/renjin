package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.gimple.type.GimpleBooleanType;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleRealType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Generates a call to a soon-to-be compiled Gimple function
 */
public class FunctionCallGenerator implements CallGenerator {
  
  private String className;
  private String methodName;
  private List<ParamGenerator> paramGenerators;
  private ReturnGenerator returnGenerator;


  public FunctionCallGenerator(String className, String methodName,
                               List<ParamGenerator> paramGenerators,
                               ReturnGenerator returnGenerator) {
    this.className = className;
    this.methodName = methodName;
    this.paramGenerators = paramGenerators;
    this.returnGenerator = returnGenerator;
  }

  public Handle getHandle() {
    return new Handle(Opcodes.H_INVOKESTATIC, className, methodName, descriptor());
  }

  @Override
  public void emitCall(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {

    // Push all parameters on the stack
    for (int i = 0; i < paramGenerators.size(); i++) {
      ParamGenerator paramGenerator = paramGenerators.get(i);
      paramGenerator.emitPushParameter(mv, argumentGenerators.get(i));
    }
    
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, className, methodName, descriptor(), false);
  }

  @Override
  public Type returnType() {
    return returnGenerator.getType();
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
    if(WrapperType.is(returnType())) {
      return new PtrCallExprGenerator(WrapperType.valueOf(returnType()), this, argumentGenerators);
    } else {
      return new ValueCallExprGenerator(this, argumentGenerators);
    }
  }
  
  private String descriptor() {
    return Type.getMethodDescriptor(returnType(), ParamGenerator.parameterTypes(paramGenerators));
  }
}
