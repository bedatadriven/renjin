package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.PointerTypeStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;

/**
 * Strategy for returning and receiving void pointers.
 */
public class VoidPtrReturnStrategy implements ReturnStrategy {
  @Override
  public Type getType() {
    return Type.getType(Object.class);
  }

  @Override
  public SimpleExpr marshall(Expr expr) {
    return (SimpleExpr) expr;
  }

  @Override
  public Expr unmarshall(MethodGenerator mv, SimpleExpr returnValue, TypeStrategy lhsTypeStrategy) {
    return ((PointerTypeStrategy) lhsTypeStrategy).unmarshallVoidPtrReturnValue(mv, returnValue);
  }

  @Override
  public SimpleExpr getDefaultReturnValue() {
    return Expressions.nullRef(Type.getType(Object.class));
  }
}
