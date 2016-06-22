package org.renjin.gcc.codegen.type.primitive;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.GSimpleExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.expr.JLValue;


public class PrimitiveValue implements GSimpleExpr {

  private JExpr expr;
  private GExpr address;

  public PrimitiveValue(JExpr expr) {
    this.expr = expr;
  }

  public PrimitiveValue(JExpr expr, GExpr address) {
    this.expr = expr;
    this.address = address;
  }

  public JExpr getExpr() {
    return expr;
  }

  @Override
  public GExpr addressOf() {
    if(address == null) {
      throw new UnsupportedOperationException("Not addressable");
    }
    return address;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    
    PrimitiveValue primitiveRhs = (PrimitiveValue) rhs;
    
    ((JLValue) expr).store(mv, primitiveRhs.getExpr());
  }

  @Override
  public JExpr unwrap() {
    return expr;
  }
}
