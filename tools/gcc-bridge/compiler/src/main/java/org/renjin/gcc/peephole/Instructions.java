package org.renjin.gcc.peephole;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.util.Textifier;


public class Instructions {
  
  public static int getInteger(AbstractInsnNode node) {
    switch (node.getOpcode()) {
      case Opcodes.ICONST_M1:
        return -1;
      case Opcodes.ICONST_0:
        return 0;
      case Opcodes.ICONST_1:
        return 1;
      case Opcodes.ICONST_2:
        return 2;
      case Opcodes.ICONST_3:
        return 3;
      case Opcodes.ICONST_4:
        return 4;
      case Opcodes.ICONST_5:
        return 5;
      case Opcodes.SIPUSH:
      case Opcodes.BIPUSH:
        return ((IntInsnNode) node).operand;
    }
    throw new IllegalArgumentException("node: " + Textifier.OPCODES[node.getOpcode()]);
  }
}
