package org.renjin.compiler.emit;

import org.objectweb.asm.Type;

public class VariableStorage {
  private int slotIndex;
  private Type type;

  public VariableStorage(int slotIndex, Type type) {
    this.slotIndex = slotIndex;
    this.type = type;
  }

  public int getSlotIndex() {
    return slotIndex;
  }

  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return "VariableStorage{" +
        "slotIndex=" + slotIndex +
        ", type=" + type +
        '}';
  }
}
