package org.renjin.gcc.peephole;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
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
  
  public static AbstractInsnNode constantNode(int cst) {
    if (cst >= -1 && cst <= 5) {
      return new InsnNode(Opcodes.ICONST_0 + cst);
      
    } else if (cst >= Byte.MIN_VALUE && cst <= Byte.MAX_VALUE) {
      return new IntInsnNode(Opcodes.BIPUSH, cst);
      
    } else if (cst >= Short.MIN_VALUE && cst <= Short.MAX_VALUE) {
      return new IntInsnNode(Opcodes.SIPUSH, cst);

    } else {
      return new LdcInsnNode(cst);
    }
  }
}
