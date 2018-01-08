/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.peephole;

import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.tree.AbstractInsnNode;
import org.renjin.repackaged.asm.tree.IincInsnNode;
import org.renjin.repackaged.asm.tree.IntInsnNode;
import org.renjin.repackaged.asm.tree.MethodInsnNode;

/**
 * Pattern-matching for byte-code instructions.
 */
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
  },

  IMUL {
    @Override
    public boolean match(AbstractInsnNode node) {
      return node.getOpcode() == Opcodes.IMUL;
    }
  },

  ZERO {
    @Override
    public boolean match(AbstractInsnNode node) {
      return node.getOpcode() == Opcodes.ICONST_0;
    }
  },

  EIGHT {
    @Override
    public boolean match(AbstractInsnNode node) {
      if (node instanceof IntInsnNode) {
        IntInsnNode intNode = (IntInsnNode) node;
        if(intNode.getOpcode() == Opcodes.BIPUSH &&
            intNode.operand == 8) {
          return true;
        }
      }
      return false;
    }
  },

  INT_COMPARISON {
    @Override
    public boolean match(AbstractInsnNode node) {

      switch (node.getOpcode()) {
        case Opcodes.IF_ICMPEQ:
        case Opcodes.IF_ICMPNE:
        case Opcodes.IF_ICMPLT:
        case Opcodes.IF_ICMPGE:
        case Opcodes.IF_ICMPGT:
        case Opcodes.IF_ICMPLE:
          return true;
        default:
          return false;
      }
    }
  },
  ZERO_COMPARISON {
    @Override
    public boolean match(AbstractInsnNode node) {

      switch (node.getOpcode()) {
        case Opcodes.IFEQ:
        case Opcodes.IFNE:
        case Opcodes.IFLT:
        case Opcodes.IFGE:
        case Opcodes.IFGT:
        case Opcodes.IFLE:
          return true;
        default:
          return false;
      }
    }
  },

  POINTER_PLUS {
    @Override
    public boolean match(AbstractInsnNode node) {
      if(node instanceof MethodInsnNode) {
        MethodInsnNode methodInsnNode = (MethodInsnNode) node;
        return methodInsnNode.owner.equals(Type.getType(Ptr.class).getInternalName()) &&
              methodInsnNode.name.equals("pointerPlus");
      }
      return false;
    }
  },

  POINTER_ACCESS_AT {
    @Override
    public boolean match(AbstractInsnNode node) {
      if(node instanceof MethodInsnNode) {
        MethodInsnNode methodInsnNode = (MethodInsnNode) node;
        return methodInsnNode.owner.equals(Type.getType(Ptr.class).getInternalName()) &&
            methodInsnNode.name.startsWith("get") &&
            methodInsnNode.desc.startsWith("(I)");
      }
      return false;
    }
  },

  IINC {
    @Override
    public boolean match(AbstractInsnNode node) {
      return node instanceof IincInsnNode;
    }
  };

  /**
   * Returns true if this Pattern matches the given instruction {@code node}.
   */
  public abstract boolean match(AbstractInsnNode node);


}
