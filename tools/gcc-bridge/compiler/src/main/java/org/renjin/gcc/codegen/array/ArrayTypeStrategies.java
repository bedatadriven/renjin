package org.renjin.gcc.codegen.array;

import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.gimple.type.GimpleArrayType;

public class ArrayTypeStrategies {
  private ArrayTypeStrategies() {}
  
  
  public static ArrayTypeStrategy of(GimpleArrayType arrayType, ValueFunction function) {
    if(arrayType.isStatic()) {
      return new FixedArrayTypeStrategy(arrayType, function);
    } else {
      return new DynamicArrayStrategy(arrayType, function);
    }
  }
}
