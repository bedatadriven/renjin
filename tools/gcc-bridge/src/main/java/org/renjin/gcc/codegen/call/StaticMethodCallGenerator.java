package org.renjin.gcc.codegen.call;

import com.google.common.collect.Lists;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.GeneratorFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Generates a call to an existing JVM method.
 */
public class StaticMethodCallGenerator implements CallGenerator {
  
  private GeneratorFactory factory;
  private Method method;

  private List<ParamGenerator> paramGenerators = null;
  private ReturnGenerator returnGenerator = null;
  
  public StaticMethodCallGenerator(GeneratorFactory factory, Method method) {
    this.factory = factory;
    this.method = method;
  }

  private ReturnGenerator returnGenerator() {
    if(returnGenerator == null) {
      returnGenerator = factory.forReturnValue(method);
    }
    return returnGenerator;
  }

  @Override
  public void emitCall(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {

    getParamGenerators();

    if(getParamGenerators().size() != argumentGenerators.size()) {
      throw new InternalCompilerException(String.format(
        "Arity mismatch: expected %d args to method '%s', called with %d" ,
          paramGenerators.size(),
          method.getName(), 
          argumentGenerators.size()));
    }

    // Push all parameters on the stack
    for (int i = 0; i < getParamGenerators().size(); i++) {
      ParamGenerator paramGenerator = getParamGenerators().get(i);
      paramGenerator.emitPushParameter(mv, argumentGenerators.get(i));
    }

    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()),
        method.getName(), Type.getMethodDescriptor(method), false);
  }

  private List<ParamGenerator> getParamGenerators() {
    if(paramGenerators == null) {
      paramGenerators = factory.forParameterTypesOf(method);
    }
    return paramGenerators;
  }

  @Override
  public Type returnType() {
    return returnGenerator().getType();
  }

  @Override
  public GimpleType getGimpleReturnType() {
    return returnGenerator().getGimpleType();
  }

  @Override
  public List<GimpleType> getGimpleParameterTypes() {
    List<GimpleType> types = Lists.newArrayList();
    for (ParamGenerator generator : getParamGenerators()) {
      types.add(generator.getGimpleType());
    }
    return types;
  }

  @Override
  public ExprGenerator expressionGenerator(List<ExprGenerator> argumentGenerators) {
    return returnGenerator().callExpression(this, argumentGenerators);
  }
}
