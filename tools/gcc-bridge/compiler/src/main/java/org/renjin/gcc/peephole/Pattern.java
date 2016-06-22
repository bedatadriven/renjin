package org.renjin.gcc.peephole;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;


public enum Pattern {
  
  LOAD {
    @Override
    public boolean match(AbstractInsnNode node) {
      switch (node.getOpcode()) {
        case Opcodes.ALOAD:
        case Opcodes.ILOAD:
        case Opcodes.LLOAD:
        case Opcodes.FLOAD:
        case Opcodes.DLOAD:
          return true;
        default:
          return false;
      }
    }
  },
  
  ILOAD {
    @Override
    public boolean match(AbstractInsnNode node) {
      return node.getOpcode() == Opcodes.ILOAD;
    }
  },
  
  STORE {
    @Override
    public boolean match(AbstractInsnNode node) {
      switch (node.getOpcode()) {
        case Opcodes.ASTORE:
        case Opcodes.ISTORE:
        case Opcodes.LSTORE:
        case Opcodes.FSTORE:
        case Opcodes.DSTORE:
          return true;
        default:
          return false;
      }
    }
  },
  
  ISTORE {
    @Override
    public boolean match(AbstractInsnNode node) {
      return node.getOpcode() == Opcodes.ISTORE;
    }
  },
  
  ICONST {
    @Override
    public boolean match(AbstractInsnNode node) {
      switch (node.getOpcode()) {
        case Opcodes.ICONST_0:
        case Opcodes.ICONST_1:
        case Opcodes.ICONST_2:
        case Opcodes.ICONST_3:
        case Opcodes.ICONST_4:
        case Opcodes.ICONST_5:
        case Opcodes.ICONST_M1:
        case Opcodes.BIPUSH:
        case Opcodes.SIPUSH:
          return true;
        
        default:
          return false;
      }
    }
  },
  
  I_ARITHMETIC {
    @Override
    public boolean match(AbstractInsnNode node) {
      switch (node.getOpcode()) {
        case Opcodes.IADD:
        case Opcodes.ISUB:
        case Opcodes.IDIV:
        case Opcodes.IMUL:
          return true;
        
        default:
          return false;
      }
    }
  },
  
  
  IADD {
    @Override
    public boolean match(AbstractInsnNode node) {
      return node.getOpcode() == Opcodes.IADD;
    }
  }
  ;
  
  public abstract boolean match(AbstractInsnNode node);
  
  
}
