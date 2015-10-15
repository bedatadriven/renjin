package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;

import java.util.List;

/**
 * Generates function call invocations
 */
public interface CallGenerator  {
  
  void emitCall(MethodVisitor visitor, List<ExprGenerator> argumentGenerators);

  Type returnType();
  
}

