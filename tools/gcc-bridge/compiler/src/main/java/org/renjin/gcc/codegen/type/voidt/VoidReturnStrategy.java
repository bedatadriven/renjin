package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.GimpleVoidType;

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
  public GimpleType getGimpleType() {
    return new GimpleVoidType();
  }

  @Override
  public SimpleExpr marshall(Expr expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Expr unmarshall(MethodGenerator mv, SimpleExpr returnValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public SimpleExpr getDefaultReturnValue() {
    return Expressions.voidValue();
  }
}
