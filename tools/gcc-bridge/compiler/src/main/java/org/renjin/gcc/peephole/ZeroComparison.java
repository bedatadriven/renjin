/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
import org.renjin.repackaged.asm.tree.JumpInsnNode;

/**
 * Replaces a sequence of {@code iconst_0} and {@code if_icmpne} with {@code if_ne}
 */
public class ZeroComparison implements PeepholeOptimization {
  @Override
  public boolean apply(NodeIt it) {

    if(it.matches(Pattern.ZERO, Pattern.INT_COMPARISON)) {

      // Remove ICONST_0 node
      it.remove(1);

      // Update the Jump instruction to use IFEQ, IFNE, etc
      JumpInsnNode jumpNode = it.get(1);
      switch (jumpNode.getOpcode()) {
        case Opcodes.IF_ICMPEQ:
          jumpNode.setOpcode(Opcodes.IFEQ);
          break;

        case Opcodes.IF_ICMPNE:
          jumpNode.setOpcode(Opcodes.IFNE);
          break;

        case Opcodes.IF_ICMPLT:
          jumpNode.setOpcode(Opcodes.IFLT);
          break;

        case Opcodes.IF_ICMPGE:
          jumpNode.setOpcode(Opcodes.IFGE);
          break;

        case Opcodes.IF_ICMPGT:
          jumpNode.setOpcode(Opcodes.IFGT);
          break;

        case Opcodes.IF_ICMPLE:
          jumpNode.setOpcode(Opcodes.IFLE);
          break;

        default:
          throw new IllegalStateException("opcode: " + jumpNode.getOpcode());
      }
      return true;

    }
    return false;
  }

}
