package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.complex.ComplexReturnStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitivePtrReturnStrategy;

import java.util.List;

/**
 * Provides a strategy for return values from methods.
 * 
 * <p>Because the JVM will only let us return a single value from a method,
 * we have to be sometimes creative in returning things like fat pointers, which
 * we represent using an array <i>and</i> and integer offset.</p>
 * 
 * @see PrimitivePtrReturnStrategy
 * @see ComplexReturnStrategy
 */
public interface ReturnStrategy {

  /**
   * 
   * @return the JVM return type
   */
  Type getType();

  /**
   * Generate the bytecode to return the given {@code valueGenerator} from the method.
   */
  void emitReturnValue(MethodGenerator mv, ExprGenerator valueGenerator);

  /**
   * Generate the bytecode to return a default or empty value for this type.
   * 
   * <p>Often required because GCC allows functions to return without explicitly
   * requiring a return value, while the JVM is not so lax.</p>
   */
  void emitReturnDefault(MethodGenerator mv);


  /**
   * Provides an {@link ExprGenerator} for accessing the return value of a call to a function
   * using this {@code ReturnStrategy}.
   */
  ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments);

}
