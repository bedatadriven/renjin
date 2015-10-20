package org.renjin.gcc.codegen.call;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.expr.ValueGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.List;

/**
 * Generates the bytecode to invoke a function call
 */
public class ValueCallExprGenerator extends AbstractExprGenerator implements ValueGenerator {
  
  private CallGenerator callGenerator;
  private List<ExprGenerator> argumentGenerators;

  public ValueCallExprGenerator(CallGenerator callGenerator, List<ExprGenerator> argumentGenerators) {
    this.callGenerator = callGenerator;
    this.argumentGenerators = argumentGenerators;
  }

  @Override
  public Type getValueType() {
    return callGenerator.returnType();
  }

  @Override
  public void emitPushValue(MethodVisitor mv) {
    callGenerator.emitCall(mv, argumentGenerators);
  }

  @Override
  public GimpleType getGimpleType() {
    return callGenerator.getGimpleReturnType();
  }
}
