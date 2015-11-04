package org.renjin.gcc.codegen.call;

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
    
    if(paramGenerators == null) {
      paramGenerators = factory.forParameterTypes(method.getParameterTypes());
    }
    
    if(paramGenerators.size() != argumentGenerators.size()) {
      throw new InternalCompilerException("Arity mismatch: " + 
          paramGenerators.size() + " != " + argumentGenerators.size());
    }

    // Push all parameters on the stack
    for (int i = 0; i < paramGenerators.size(); i++) {
      ParamGenerator paramGenerator = paramGenerators.get(i);
      paramGenerator.emitPushParameter(mv, argumentGenerators.get(i));
    }

    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(method.getDeclaringClass()),
        method.getName(), Type.getMethodDescriptor(method), false);
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
  public ExprGenerator expressionGenerator(List<ExprGenerator> argumentGenerators) {
    return returnGenerator().callExpression(this, argumentGenerators);
  }
}
