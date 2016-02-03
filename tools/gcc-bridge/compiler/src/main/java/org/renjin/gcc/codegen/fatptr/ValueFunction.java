package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;

/**
 * Functor which can "unwrap" a fat ptr
 */
public interface ValueFunction {
  
  Type getValueType();
  
  ExprGenerator dereference(Value arrayElement);
  
}
