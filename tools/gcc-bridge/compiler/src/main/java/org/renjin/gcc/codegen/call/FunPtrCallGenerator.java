package org.renjin.gcc.codegen.call;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeOracle;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleFunctionType;

import java.util.List;

/**
 * Emits the bytecode to invoke a function pointer (MethodHandle)
 */
public class FunPtrCallGenerator implements CallGenerator {

  private TypeOracle typeOracle;
  private Value funPtrGenerator;
  private final ReturnStrategy returnStrategy;
  private final GimpleFunctionType functionType;

  public FunPtrCallGenerator(TypeOracle typeOracle, GimpleFunctionType type, Value funPtrGenerator) {
    this.typeOracle = typeOracle;
    this.funPtrGenerator = funPtrGenerator;
    functionType = type;
    returnStrategy = typeOracle.findReturnGenerator(functionType.getReturnType());
  }


  /**
   * Write the bytecode for invoking a call to a function pointer, which we represent
   * as a MethodHandle in the compiled code. Since GCC permits casting function pointer 
   * signatures willy-nill, we need to infer the actual types from the call.
   *
   * @see <a href="http://stackoverflow.com/questions/559581/casting-a-function-pointer-to-another-type">Stack Overflow discussion</a>
   */
  @Override
  public void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call) {
//
//    funPtrGenerator.load(mv);
//    
//    // Infer the parameters types from the arguments provided
//    List<Type> types = Lists.newArrayList();
//    for (ExprGenerator argumentGenerator : argumentGenerators) {
//      // TODO
//      //ParamStrategy paramStrategy = typeOracle.forParameter(argumentGenerator.getGimpleType());
////      paramStrategy.emitPushParameter(mv, argumentGenerator);
////      types.addAll(paramStrategy.getParameterTypes());
//      throw new UnsupportedOperationException("TODO");
//    }
    
    // Use invoke() rather than invokeExact() to smooth over any type differences
   // mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invoke", signature(types), false);
  
    throw new UnsupportedOperationException();
  }

  private String signature(List<Type> types) {
    return Type.getMethodDescriptor(returnType(), types.toArray(new Type[types.size()]));
  }

  private Type returnType() {
    return returnStrategy.getType();
  }

}
