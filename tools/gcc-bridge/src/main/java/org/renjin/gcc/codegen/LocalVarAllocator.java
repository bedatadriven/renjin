package org.renjin.gcc.codegen;

import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.param.ParamGenerator;

import java.util.List;

/**
 * Allocates local variable slots
 */
public class LocalVarAllocator {
  
  private int slots = 0;

  public LocalVarAllocator(List<ParamGenerator> params) {
    // the first local variable slots are reserved for parameters
    for (ParamGenerator param : params) {
      slots += param.numSlots();
    }
  }
  
  public int reserve(Type type) {
    return reserve(type.getSize());
  }
  
  public int reserve(int numSlots) {
    int index = slots;
    slots += numSlots;
    return index;
  }
}
