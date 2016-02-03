package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprFactory;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.statement.GimpleCall;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Generates function call invocations
 */
public interface CallGenerator  {
  
  void emitCall(MethodGenerator mv, ExprFactory exprFactory, GimpleCall call);
  
}

