package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.type.ParamStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.repackaged.asm.Handle;

import java.util.List;

/**
 * Defines a strategy for invoking a JVM method
 */
public interface InvocationStrategy {

  /**
   * 
   * @return a JVM method handle that can be used to construct a function pointer
   */
  Handle getMethodHandle();
  
  List<ParamStrategy> getParamStrategies();

  /**
   * If this function is variadic, get the {@code ParamStrategy} for the variadic argument.
   */
  boolean isVarArgs();
  
  ReturnStrategy getReturnStrategy();

  /**
   * Generates the bytecode instructions to invoke the method. 
   * 
   * <p>The method's parameters must already be on the stack, and after the generated instructions,
   * the method's return value, if any will be on the stack.</p>
   */
  void invoke(MethodGenerator mv);

}
