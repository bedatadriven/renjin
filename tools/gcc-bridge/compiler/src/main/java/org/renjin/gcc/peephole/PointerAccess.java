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

import org.renjin.gcc.runtime.Ptr;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.tree.MethodInsnNode;

/**
 * Simplifies getInt(0) to getInt(), getDouble(0) to getDouble(), etc.
 */
public class PointerAccess implements PeepholeOptimization {
  @Override
  public boolean apply(NodeIt it) {
    if(it.matches(Pattern.ZERO, Pattern.POINTER_ACCESS_AT)) {
      MethodInsnNode accessNode = it.get(1);
      Type accessType = Type.getReturnType(accessNode.desc);

      it.remove(2);
      it.insert(new MethodInsnNode(Opcodes.INVOKEINTERFACE,
          Type.getInternalName(Ptr.class),
          accessNode.name,
          Type.getMethodDescriptor(accessType),
          true));
      return true;
    }

    if(it.matches(Pattern.EIGHT, Pattern.IMUL, Pattern.POINTER_ACCESS_AT)) {
      MethodInsnNode accessNode = it.get(2);
      if(accessNode.name.equals("getDouble")) {
        it.remove(3);
        it.insert(new MethodInsnNode(Opcodes.INVOKEINTERFACE,
            Type.getInternalName(Ptr.class),
            "getAlignedDouble",
            Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.INT_TYPE),
            true));
      }
    }

    return false;
  }
}
