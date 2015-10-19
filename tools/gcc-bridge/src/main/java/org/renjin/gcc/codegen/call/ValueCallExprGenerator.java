package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.PrimitiveGenerator;

import java.util.List;

/**
 * Generates the bytecode to invoke a function call
 */
public class ValueCallExprGenerator implements PrimitiveGenerator {
  
  private CallGenerator callGenerator;
  private List<ExprGenerator> argumentGenerators;

  public ValueCallExprGenerator(CallGenerator callGenerator, List<ExprGenerator> argumentGenerators) {
    this.callGenerator = callGenerator;
    this.argumentGenerators = argumentGenerators;
  }

  @Override
  public Type primitiveType() {
    return callGenerator.returnType();
  }

  @Override
  public void emitPush(MethodVisitor mv) {
    callGenerator.emitCall(mv, argumentGenerators);
  }
}
