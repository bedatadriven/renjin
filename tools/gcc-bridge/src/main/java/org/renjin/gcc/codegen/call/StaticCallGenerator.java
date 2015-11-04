package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
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
public class StaticCallGenerator implements CallGenerator {
  
  private GeneratorFactory factory;
  private Method method;

  private List<ParamGenerator> paramGenerators = null;
  private ReturnGenerator returnGenerator = null;
  
  
  public StaticCallGenerator(GeneratorFactory factory, Method method) {
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
  public void emitCall(MethodVisitor visitor, List<ExprGenerator> argumentGenerators) {
    throw new InternalCompilerException("todo");
  }

  @Override
  public Type returnType() {
    return returnGenerator().getType();
  }

  @Override
  public GimpleType getGimpleReturnType() {
    return returnGenerator.getGimpleType();
  }

  @Override
  public ExprGenerator expressionGenerator(List<ExprGenerator> argumentGenerators) {
    return new ValueCallExprGenerator(this, argumentGenerators);
  }
}
