package org.renjin.gcc.codegen.call;

import com.google.common.collect.Lists;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.GeneratorFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.gimple.type.GimpleFunctionType;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Emits the bytecode to invoke a function pointer (MethodHandle)
 */
public class FunPtrCallGenerator implements CallGenerator {

  private GeneratorFactory factory;
  private ExprGenerator funPtrGenerator;
  private final List<ParamGenerator> parameters;
  private final ReturnGenerator returnGenerator;
  private final GimpleFunctionType functionType;

  public FunPtrCallGenerator(GeneratorFactory factory, ExprGenerator funPtrGenerator) {
    this.factory = factory;
    this.funPtrGenerator = funPtrGenerator;
    functionType = funPtrGenerator.getGimpleType().getBaseType();
    parameters = factory.forParameterTypes(functionType.getArgumentTypes());
    returnGenerator = factory.findReturnGenerator(functionType.getReturnType());
  }

  @Override
  public List<GimpleType> getGimpleParameterTypes() {
    return functionType.getArgumentTypes();
  }

  /**
   * Write the bytecode for invoking a call to a function pointer, which we represent
   * as a MethodHandle in the compiled code. Since GCC permits casting function pointer 
   * signatures willy-nill, we need to infer the actual types from the call.
   *
   * @see <a href="http://stackoverflow.com/questions/559581/casting-a-function-pointer-to-another-type">Stack Overflow discussion</a>
   */
  @Override
  public void emitCall(MethodVisitor mv, List<ExprGenerator> argumentGenerators) {

    funPtrGenerator.emitPushMethodHandle(mv);
    
    // Infer the parameters types from the arguments provided
    List<Type> types = Lists.newArrayList();
    for (ExprGenerator argumentGenerator : argumentGenerators) {
      ParamGenerator paramGenerator = factory.forParameter(argumentGenerator.getGimpleType());
      paramGenerator.emitPushParameter(mv, argumentGenerator);
      types.addAll(paramGenerator.getParameterTypes());
    }
    
    // Use invoke() rather than invokeExact() to smooth over any type differences
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invoke", signature(types), false);
  }

  private String signature(List<Type> types) {
    return Type.getMethodDescriptor(returnType(), types.toArray(new Type[types.size()]));
  }

  @Override
  public ExprGenerator expressionGenerator(List<ExprGenerator> argumentGenerators) {
    return returnGenerator.callExpression(this, argumentGenerators);
  }
  
  @Override
  public Type returnType() {
    return returnGenerator.getType();
  }

  @Override
  public GimpleType getGimpleReturnType() {
    return functionType.getReturnType();
  }

}
