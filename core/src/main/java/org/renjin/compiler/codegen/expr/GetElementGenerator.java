package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.ir.ValueBounds;

public class GetElementGenerator implements ExprGenerator {
  
  private ValueBounds valueBounds;

  public GetElementGenerator(ValueBounds valueBounds) {
    this.valueBounds = valueBounds;
  }

  @Override
  public ValueBounds getBounds() {
    return null;
  }
}
