package org.renjin.gcc.codegen.call;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Generates function call invocations
 */
public interface CallGenerator  {
  
  void emitCall(MethodGenerator visitor, List<ExprGenerator> argumentGenerators);
  
  void emitCallAndPopResult(MethodGenerator visitor, List<ExprGenerator> argumentGenerators);

  ExprGenerator expressionGenerator(GimpleType returnType, List<ExprGenerator> argumentGenerators);
  
}

