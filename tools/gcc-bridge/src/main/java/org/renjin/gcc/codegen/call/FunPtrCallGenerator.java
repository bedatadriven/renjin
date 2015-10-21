package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.GeneratorFactory;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.ArrayList;
import java.util.List;

/**
 * Emits the bytecode to invoke a function pointer (MethodHandle)
 */
public class FunPtrCallGenerator implements CallGenerator {
  
  private ExprGenerator funPtrGenerator;
  private final List<ParamGenerator> parameters;
  private final ReturnGenerator returnGenerator;
  private final GimpleFunctionType functionType;

  public FunPtrCallGenerator(GeneratorFactory factory, ExprGenerator funPtrGenerator) {
    this.funPtrGenerator = funPtrGenerator;
    functionType = funPtrGenerator.getGimpleType().getBaseType();
    parameters = factory.forParameterTypes(functionType.getArgumentTypes());
    returnGenerator = factory.findReturnGenerator(functionType.getReturnType());
    
  }

  @Override
  public void emitCall(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {

    funPtrGenerator.emitPushMethodHandle(mv);

    // Push all parameters on the stack
    for (int i = 0; i < parameters.size(); i++) {
      parameters.get(i).emitPushParameter(mv, argumentGenerators.get(i));
    }
    
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", signature(), false);
  }

  private String signature() {
    List<Type> parameterTypes = new ArrayList<Type>();
    for (ParamGenerator parameter : parameters) {
      parameterTypes.addAll(parameter.getParameterTypes());
    }
    return Type.getMethodDescriptor(returnType(), parameterTypes.toArray(new Type[0]));
  }

  @Override
  public ExprGenerator expressionGenerator(List<ExprGenerator> argumentGenerators) {
    if(WrapperType.is(returnType())) {
      return new PtrCallExprGenerator(WrapperType.valueOf(returnType()), this, argumentGenerators);
    } else {
      return new ValueCallExprGenerator(this, argumentGenerators);
    }
  }
  
  @Override
  public Type returnType() {
    return returnGenerator.type();
  }

  @Override
  public GimpleType getGimpleReturnType() {
    return functionType.getReturnType();
  }

}
