package org.renjin.gcc.codegen.type;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.Expr;
import org.renjin.gcc.codegen.expr.Expressions;
import org.renjin.gcc.codegen.expr.SimpleExpr;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Strategy for returning types whose values can be represented as a {@link SimpleExpr}
 */
public final class SimpleReturnStrategy implements ReturnStrategy {

  private GimpleType gimpleType;
  private Type type;

  public SimpleReturnStrategy(GimpleType gimpleType, Type type) {
    this.gimpleType = gimpleType;
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public GimpleType getGimpleType() {
    return gimpleType;
  }

  @Override
  public SimpleExpr marshall(Expr expr) {
    return (SimpleExpr)expr;
  }

  @Override
  public SimpleExpr unmarshall(MethodGenerator mv, SimpleExpr returnValue) {
    return returnValue;
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
