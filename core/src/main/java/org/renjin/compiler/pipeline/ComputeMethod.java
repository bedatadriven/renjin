package org.renjin.compiler.pipeline;

import org.objectweb.asm.MethodVisitor;

public class ComputeMethod {
  private int localCount = 2; // includes instance pointer and argument

  private MethodVisitor visitor;
  private int maxStackSize = 0;
  private int currentStack = 0;

  public ComputeMethod(MethodVisitor visitor) {
    this.visitor = visitor;
  }

  public MethodVisitor getVisitor() {
    return visitor;
  }

  public int reserveLocal(int size) {
    int pos = localCount;
    localCount += size;
    return pos;
  }

  public void stack(int change) {
    currentStack += change;
    if(currentStack > maxStackSize) {
      maxStackSize = currentStack;
    }
  }

  /**
   * @return the index of the operands array, containing
   * Vector[]
   */
  public int getOperandsLocalIndex() {
    return 1;
  }

  public int getMaxLocals() {
    return localCount;
  }
}
