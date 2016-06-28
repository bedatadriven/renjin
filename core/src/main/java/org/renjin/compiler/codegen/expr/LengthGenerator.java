package org.renjin.compiler.codegen.expr;


import org.renjin.compiler.ir.ValueBounds;

public class LengthGenerator implements ExprGenerator {


  @Override
  public ValueBounds getBounds() {
    return ValueBounds.INT_PRIMITIVE;
  }
}
