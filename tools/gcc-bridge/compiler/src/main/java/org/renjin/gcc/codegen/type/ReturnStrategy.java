package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.complex.ComplexReturnStrategy;
import org.renjin.gcc.codegen.var.Value;

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
   * Converts if necessary the expression to be returned to a single value.
   */
  Value marshall(ExprGenerator expr);


  /**
   * Converts a function call return value to an expression if necessary.
   */
  ExprGenerator unmarshall(MethodGenerator mv, Value returnValue);

}
