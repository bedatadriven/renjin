/**
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

import static org.renjin.gcc.peephole.Instructions.constantNode;
import static org.renjin.gcc.peephole.Instructions.getInteger;

/**
 * Replaces arithmetic on constant integers with the result
 */
public class ConstantMath implements PeepholeOptimization {
  @Override
  public boolean apply(NodeIt it) {
    if(it.matches(Pattern.ICONST, Pattern.ICONST, Pattern.I_ARITHMETIC)) {

      int x = getInteger(it.get(0));
      int y = getInteger(it.get(1));
      int op = it.get(2).getOpcode();

      it.remove(3);
      it.insert(constantNode(compute(op, x, y)));
      return true;
    }
    return false;
  }

  private int compute(int op, int x, int y) {
    switch (op) {
      case Opcodes.IADD:
        return x + y;
      case Opcodes.ISUB:
        return x - y;
      case Opcodes.IDIV:
        return x / y;
      case Opcodes.IMUL:
        return x * y;
    }
    throw new IllegalArgumentException("opcode: " + op);
  }
}
