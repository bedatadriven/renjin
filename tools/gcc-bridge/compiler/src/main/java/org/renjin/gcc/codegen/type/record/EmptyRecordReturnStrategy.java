package org.renjin.gcc.codegen.type.record;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.codegen.type.ReturnStrategy;
import org.renjin.gcc.codegen.type.TypeStrategy;

import javax.annotation.Nonnull;


public class EmptyRecordReturnStrategy implements ReturnStrategy {
  @Override
  public Type getType() {
    return Type.VOID_TYPE;
  }

  @Override
  public SimpleExpr marshall(Expr expr) {
    return getDefaultReturnValue();
  }

  @Override
  public Expr unmarshall(MethodGenerator mv, SimpleExpr returnValue, TypeStrategy lhsTypeStrategy) {
    return Expressions.nullRef(Type.getType(Object.class));
  }

  @Override
  public SimpleExpr getDefaultReturnValue() {
    return new SimpleExpr() {
      @Nonnull
      @Override
      public Type getType() {
        return Type.VOID_TYPE;
      }

      @Override
      public void load(@Nonnull MethodGenerator mv) {
        // NOOP
      }
    };
  }
}
