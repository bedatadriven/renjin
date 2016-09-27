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
import org.renjin.repackaged.asm.tree.AbstractInsnNode;
import org.renjin.repackaged.asm.tree.InsnNode;
import org.renjin.repackaged.asm.tree.VarInsnNode;

/**
 * Replaces two consecutive LOAD instructions with a LOAD, DUP pair
 * 
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
        return new InsnNode(Opcodes.DUP2);
      default:
        return new InsnNode(Opcodes.DUP);
    }
  }
}
