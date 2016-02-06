package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.gimple.statement.GimpleCall;

/**
 * Generates the bytecode for {@link GimpleCall} instructions.
 * 
 * <p>During code generation, we need to handle some function calls in a context-sensitive way, such
 * as calls to {@code malloc} or {@code free}, while others</p>
 */
public interface CallGenerator  {
  
  void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call);

}

