package org.renjin.gcc.codegen.type;

import org.renjin.gcc.InternalCompilerException;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.GSimpleExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.repackaged.asm.Type;

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
    GExpr result = strategy.wrap(Expressions.cast(returnValue, type));
    try {
      return lhsTypeStrategy.cast(mv, result, strategy);
    } catch (UnsupportedCastException e) {
      throw new InternalCompilerException("Cannot cast from " + strategy + " to " + lhsTypeStrategy, e);
    }
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
