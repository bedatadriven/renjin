package org.renjin.gcc.peephole;

import org.objectweb.asm.Opcodes;

import static org.renjin.gcc.peephole.Instructions.constantNode;
import static org.renjin.gcc.peephole.Instructions.getInteger;

/**
 * Replaces arithmetic on constant integers with the result
 */
public class ConstantMath implements PeepholeOptimization {
  @Override
  public boolean apply(NodeIt it) {
    if(it.matches(Pattern.ICONST, Pattern.ICONST, Pattern.I_ARITHMETIC)) {

      int x = getInteger(it.get(0));
      int y = getInteger(it.get(1));
      int op = it.get(2).getOpcode();

      it.remove(3);
      it.insert(constantNode(compute(op, x, y)));
    }
    return false;
  }

  private int compute(int op, int x, int y) {
    switch (op) {
      case Opcodes.IADD:
        return x + y;
      case Opcodes.ISUB:
        return x - y;
      case Opcodes.IDIV:
        return x / y;
      case Opcodes.IMUL:
        return x * y;
    }
    throw new IllegalArgumentException("opcode: " + op);
  }
}
