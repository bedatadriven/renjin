package org.renjin.gcc.peephole;

import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.renjin.gcc.peephole.Pattern.*;

/**
 * Replaces the sequences of instructions ILOAD, ICONST, IADD, ISTORE with a single IINC instruction
 */
public class IntegerIncrement implements PeepholeOptimization {
  @Override
  public boolean apply(NodeIt it) {
    if(it.matches(ILOAD, ICONST, IADD, ISTORE)) {
      VarInsnNode load = it.get(0);
      int increment = Instructions.getInteger(it.get(1));
      VarInsnNode store = it.get(3);
      
      if(load.var == store.var) {
        it.remove(4);
        if(increment != 0) {
          it.insert(new IincInsnNode(load.var, increment));
        }
        return true;
      }
    }
    return false;
  }
}
