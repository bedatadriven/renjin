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

import org.renjin.repackaged.asm.tree.IincInsnNode;
import org.renjin.repackaged.asm.tree.VarInsnNode;

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
