package org.renjin.gcc.codegen.fatptr;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.Value;

import java.util.List;

/**
 * Functor which can "unwrap" a fat ptr
 */
public interface ValueFunction {
  
  Type getValueType();

  /**
   * Returns the number of array elements required for each value.
   * 
   * <p>For an array of doubles, for example, the length is 1. By contrast, a complex number value requires
   * two array elements per value.</p>
   */
  int getElementLength();

  /**
   * Returns the size of the value in bytes, as understood by GCC/Gimple. 
   * 
   */
  int getElementSize();

  ExprGenerator dereference(Value array, Value offset);

  List<Value> getDefaultValue();
}
