package org.renjin.gcc.codegen;

import org.objectweb.asm.Type;

/**
 * Allocates local variable slots
 */
public class LocalVarAllocator {
  
  private int slots = 0;

  
  public int reserve(Type type) {
    return reserve(type.getSize());
  }
  
  public int reserve(int numSlots) {
    int index = slots;
    slots += numSlots;
    return index;
  }

  public int reserveArrayRef() {
    return reserve(1);
  }
}
