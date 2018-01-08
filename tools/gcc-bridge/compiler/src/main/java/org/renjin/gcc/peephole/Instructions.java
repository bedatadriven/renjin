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

import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.tree.AbstractInsnNode;
import org.renjin.repackaged.asm.tree.InsnNode;
import org.renjin.repackaged.asm.tree.IntInsnNode;
import org.renjin.repackaged.asm.tree.LdcInsnNode;
import org.renjin.repackaged.asm.util.Textifier;

/**
 * Functions for manipulating {@link AbstractInsnNode} objects.
 */
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
