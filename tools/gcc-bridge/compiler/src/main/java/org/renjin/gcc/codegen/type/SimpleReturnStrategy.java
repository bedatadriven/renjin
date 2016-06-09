package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;

/**
 * Strategy for returning types whose values can be represented as a {@link SimpleExpr}
 */
public final class SimpleReturnStrategy implements ReturnStrategy {

  private Type type;

  public SimpleReturnStrategy(Type type) {
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public SimpleExpr marshall(Expr expr) {
    return (SimpleExpr)expr;
  }

  @Override
  public SimpleExpr unmarshall(MethodGenerator mv, SimpleExpr returnValue, TypeStrategy lhsTypeStrategy) {
    return Expressions.cast(returnValue, type);
  }

  @Override
  public SimpleExpr getDefaultReturnValue() {
    switch (type.getSort()) {
      case Type.OBJECT:
      case Type.ARRAY:
      case Type.METHOD:
        return Expressions.nullRef(type); 
      
      default:
        return Expressions.zero(type);
    }
  }
}
