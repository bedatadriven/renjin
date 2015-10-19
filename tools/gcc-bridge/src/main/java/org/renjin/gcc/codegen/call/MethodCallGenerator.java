package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.WrapperType;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.ValueGenerator;

import java.util.List;

/**
 * Generates a call to a static JVM method
 */
public class MethodCallGenerator implements CallGenerator {
  
  private String className;
  private String methodName;
  private Type[] parameterTypes;
  private Type returnType;

  public MethodCallGenerator(String className, String methodName, Type[] parameterTypes, Type returnType) {
    this.className = className;
    this.methodName = methodName;
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
  }

  @Override
  public void emitCall(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {

    // Push all parameters on the stack
    for (int i = 0; i < parameterTypes.length; i++) {
      Type paramType = parameterTypes[i];
      ExprGenerator generator = argumentGenerators.get(i);
      
      ParamConverter converter = findConverter(generator, paramType);
      converter.emitPushParam(mv);
    }
    
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, className, methodName, 
        Type.getMethodDescriptor(returnType, parameterTypes), false);
  }

  @Override
  public Type returnType() {
    return returnType;
  }

  @Override
  public ExprGenerator expressionGenerator(List<ExprGenerator> argumentGenerators) {
    if(WrapperType.is(returnType)) {
      return new PtrCallExprGenerator(WrapperType.valueOf(returnType), this, argumentGenerators);
    } else {
      return new ValueCallExprGenerator(this, argumentGenerators);
    }
  }

  private ParamConverter findConverter(ExprGenerator generator, Type paramType) {
    if(generator instanceof ValueGenerator) {
      ValueGenerator valueGenerator = (ValueGenerator) generator;
      if(valueGenerator.primitiveType().equals(paramType)) {
        return new ValueParamConverter(valueGenerator);
      }
    }
    if(WrapperType.is(paramType)) {
      return new WrappedPtrConverter(WrapperType.valueOf(paramType), generator);
    }
    throw new UnsupportedOperationException(String.format("Cannot convert from %s to %s", generator, paramType));
  }
}
