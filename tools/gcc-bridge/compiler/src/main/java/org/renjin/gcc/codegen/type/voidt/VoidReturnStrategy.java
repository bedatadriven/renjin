package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.var.Value;
import org.renjin.gcc.codegen.var.Values;

/**
 * Strategy for returning from a void-typed function.
 *
 */
public class VoidReturnStrategy implements ReturnStrategy {

  @Override
  public Type getType() {
    return Type.VOID_TYPE;
  }

  @Override
  public Value marshall(ExprGenerator expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ExprGenerator unmarshall(MethodGenerator mv, Value returnValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Value getDefaultReturnValue() {
    return Values.voidValue();
  }
}
