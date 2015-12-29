package org.renjin.gcc.peephole;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Replaces two consecutive LOAD instructions with a LOAD, DUP pair
 */
public class LoadLoad implements PeepholeOptimization {
  @Override
  public boolean apply(NodeIt it) {
    if(it.matches(Pattern.LOAD, Pattern.LOAD)) {
      VarInsnNode load1 = it.get(0);
      VarInsnNode load2 = it.get(1);
      if(load1.var == load2.var) {
        it.getList().set(load2, dup(load2));
        return true;
      }
    }
    return false;
  }

  private AbstractInsnNode dup(VarInsnNode load) {
    switch (load.getOpcode()) {
      case Opcodes.DLOAD:
      case Opcodes.LLOAD:
        return new InsnNode(Opcodes.DUP);
      default:
        return new InsnNode(Opcodes.DUP2);
    }
  }
}
