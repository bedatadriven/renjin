package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Invocation strategy for existing JVM static methods.
 */
public class StaticMethodStrategy implements InvocationStrategy {

  private final TypeOracle typeOracle;
  private Method method;

  /**
   * List of ParamStrategies, constructed lazily.
   */
  private List<ParamStrategy> paramStrategies;

  /**
   * Strategy for dealing with the method's return value, constructed lazily.
   */
  private ReturnStrategy returnStrategy;

  public StaticMethodStrategy(TypeOracle typeOracle, Method method) {
    this.typeOracle = typeOracle;
    this.method = method;
  }

  @Override
  public Handle getMethodHandle() {
    return new Handle(Opcodes.H_INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()), 
        method.getName(), Type.getMethodDescriptor(method));
  }

  @Override
  public List<ParamStrategy> getParamStrategies() {
    if(paramStrategies == null) {
      paramStrategies = typeOracle.forParameterTypesOf(method);
    }
    return paramStrategies;
  }

  @Override
  public boolean isVarArgs() {
    return method.isVarArgs();
  }

  @Override
  public ReturnStrategy getReturnStrategy() {
    if (returnStrategy == null) {
      returnStrategy = typeOracle.forReturnValue(method);
    }
    return returnStrategy;
  }
  
  @Override
  public void invoke(MethodGenerator mv) {
    mv.invokestatic(method.getDeclaringClass(), method.getName(), Type.getMethodDescriptor(method));
  }
}
