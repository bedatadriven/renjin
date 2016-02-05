package org.renjin.gcc.codegen.array;

import org.renjin.gcc.codegen.fatptr.ValueFunction;
import org.renjin.gcc.codegen.var.VarAllocator;
import org.renjin.gcc.gimple.GimpleVarDecl;

/**
 * When values need to be addressable, we have to allocate a unit-length array
 * so that it's value can  be addressed
 */
public class AddressableStrategy {
  
  private ValueFunction valueFunction;

  public AddressableStrategy(ValueFunction valueFunction) {
    this.valueFunction = valueFunction;
  }

  public static void variable(GimpleVarDecl decl, VarAllocator allocator, ValueFunction valueFunction) {
    
  } 
}
