package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.ir.ValueBounds;
import org.renjin.invoke.model.JvmMethod;
import sun.awt.SunHints;

public class BuiltinCallGenerator implements ExprGenerator {
  
  private JvmMethod method;
  private ValueBounds valueBounds;

  public BuiltinCallGenerator(JvmMethod method, ValueBounds valueBounds) {
    this.method = method;
    this.valueBounds = valueBounds;
  }

  @Override
  public ValueBounds getBounds() {
    return valueBounds;
  }
}
