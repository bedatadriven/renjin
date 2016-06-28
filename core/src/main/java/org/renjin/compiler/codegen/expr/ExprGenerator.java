package org.renjin.compiler.codegen.expr;

import org.renjin.compiler.ir.ValueBounds;

/**
 * Created by alex on 27-6-16.
 */
public interface ExprGenerator {
  
  ValueBounds getBounds();
}
