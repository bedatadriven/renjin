package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.GSimpleExpr;
import org.renjin.gcc.codegen.expr.JExpr;

/**
 * Strategy for returning types whose values can be represented as a {@link JExpr}
 */
public final class SimpleReturnStrategy implements ReturnStrategy {

  private final SimpleTypeStrategy strategy;
  private Type type;

  public SimpleReturnStrategy(SimpleTypeStrategy strategy) {
    this.type = strategy.getJvmType();
    this.strategy = strategy;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public JExpr marshall(GExpr expr) {
    return ((GSimpleExpr) expr).unwrap();
  }

  @Override
  public GExpr unmarshall(MethodGenerator mv, JExpr returnValue, TypeStrategy lhsTypeStrategy) {
    return strategy.wrap(Expressions.cast(returnValue, type));
  }

  @Override
  public JExpr getDefaultReturnValue() {
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
