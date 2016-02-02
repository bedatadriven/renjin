package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.call.CallGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;

import java.util.List;

/**
 * Returns a Value
 */
public final class ValueReturnStrategy implements ReturnStrategy {
  
  private Type type;

  public ValueReturnStrategy(Type type) {
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void emitReturnValue(MethodGenerator mv, ExprGenerator valueGenerator) {
    Value value = (Value) valueGenerator;
    value.load(mv);
    mv.areturn(value.getType());
  }

  @Override
  public void emitReturnDefault(MethodGenerator mv) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator callExpression(CallGenerator callGenerator, List<ExprGenerator> arguments) {
    return null;
  }
}
