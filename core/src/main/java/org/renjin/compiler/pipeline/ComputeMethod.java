package org.renjin.compiler.pipeline;

import org.renjin.repackaged.asm.MethodVisitor;

import static org.renjin.repackaged.asm.Opcodes.*;

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

  /**
   * Reserves a local variable slot for an integer counter and 
   * initializes it to zero.
   * 
   * @return the local variable index
   */
  public int declareCounter() {
    int localVar = reserveLocal(1);
    visitor.visitInsn(ICONST_0);
    visitor.visitVarInsn(ISTORE, localVar);
    return localVar;
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
