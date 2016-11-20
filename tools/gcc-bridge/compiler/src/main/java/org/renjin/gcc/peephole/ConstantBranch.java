/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import static org.renjin.gcc.peephole.Instructions.getInteger;

/**
 * Replaces constant
 */
public class ConstantBranch implements PeepholeOptimization {
  @Override
  public boolean apply(NodeIt it) {
    if(it.matches(Pattern.ICONST, Pattern.ICONST, Pattern.INT_COMPARISON)) {
      int x = getInteger(it.get(0));
      int y = getInteger(it.get(1));
      JumpInsnNode branchNode = it.get(2);

      boolean branch = evaluate(branchNode.getOpcode(), x, y);

      if(branch) {
        // We will always branch, so replace with a goto
        it.remove(3);
        it.insert(new JumpInsnNode(Opcodes.GOTO, branchNode.label));

      } else {
        // We will never branch, so just continue on
        it.remove(3);
      }
      return true;
    }
    return false;
  }

  private boolean evaluate(int opcode, int x, int y) {
    switch (opcode) {
      case Opcodes.IF_ICMPEQ:
        return x == y;

      case Opcodes.IF_ICMPNE:
        return x != y;

      case Opcodes.IF_ICMPLT:
        return x < y;

      case Opcodes.IF_ICMPGE:
        return x >= y;

      case Opcodes.IF_ICMPGT:
        return x > y;

      case Opcodes.IF_ICMPLE:
        return x <= y;

      default:
        throw new IllegalArgumentException("opcode: " + opcode);
    }
  }
}
