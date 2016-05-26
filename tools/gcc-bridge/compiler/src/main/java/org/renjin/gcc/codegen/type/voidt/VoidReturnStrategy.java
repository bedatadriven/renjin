package org.renjin.gcc.codegen.type.voidt;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.PointerTypeStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;

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
  public SimpleExpr marshall(Expr expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Expr unmarshall(MethodGenerator mv, SimpleExpr returnValue, TypeStrategy lhsTypeStrategy) {
    if(lhsTypeStrategy instanceof PointerTypeStrategy) {
      return ((PointerTypeStrategy) lhsTypeStrategy).nullPointer();
    } else if(lhsTypeStrategy instanceof PrimitiveTypeStrategy) {
      return ((PrimitiveTypeStrategy) lhsTypeStrategy).zero();
    } else {
      throw new UnsupportedOperationException("No default value for " + lhsTypeStrategy.getClass().getSimpleName());
    }
  }

  @Override
  public SimpleExpr getDefaultReturnValue() {
    return Expressions.voidValue();
  }
}
