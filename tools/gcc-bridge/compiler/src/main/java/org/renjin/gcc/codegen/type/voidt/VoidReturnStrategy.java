package org.renjin.gcc.codegen.type.voidt;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.PointerTypeStrategy;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;
import org.renjin.gcc.codegen.type.primitive.PrimitiveTypeStrategy;
import org.renjin.repackaged.asm.Type;

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
  public JExpr marshall(GExpr expr) {
    throw new UnsupportedOperationException();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr returnValue, TypeStrategy lhsTypeStrategy) {
    if(lhsTypeStrategy instanceof PointerTypeStrategy) {
      return ((PointerTypeStrategy) lhsTypeStrategy).nullPointer();
    } else if(lhsTypeStrategy instanceof PrimitiveTypeStrategy) {
      return ((PrimitiveTypeStrategy) lhsTypeStrategy).zero();
    } else {
      throw new UnsupportedOperationException("No default value for " + lhsTypeStrategy.getClass().getSimpleName());
    }
  }

  @Override
  public JExpr getDefaultReturnValue() {
    return Expressions.voidValue();
  }
}
