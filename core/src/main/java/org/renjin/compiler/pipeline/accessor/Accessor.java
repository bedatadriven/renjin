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
package org.renjin.compiler.pipeline.accessor;

import org.renjin.compiler.pipeline.ComputeMethod;
import org.renjin.repackaged.asm.MethodVisitor;
import org.renjin.repackaged.asm.Opcodes;

import static org.renjin.repackaged.asm.Opcodes.D2I;

public abstract class Accessor {

  static boolean supportedType(Class<?> type) {
    return type.equals(double.class) ||
           type.equals(int.class);
  }

  public abstract void init(ComputeMethod method);

  /**
   * The index is on the stack, the method should
   * push the corresponding double on to the stack.
   * @param method
   */
  public abstract void pushDouble(ComputeMethod method);

  public abstract void pushLength(ComputeMethod method);

  protected final void pushOperandIndex(MethodVisitor mv, int operandIndex) {
    if (operandIndex >= -1 && operandIndex <= 5) {
      mv.visitInsn(Opcodes.ICONST_0 + operandIndex);
    } else if (operandIndex >= Byte.MIN_VALUE && operandIndex <= Byte.MAX_VALUE) {
      mv.visitIntInsn(Opcodes.BIPUSH, operandIndex);
    } else if (operandIndex >= Short.MIN_VALUE && operandIndex <= Short.MAX_VALUE) {
      mv.visitIntInsn(Opcodes.SIPUSH, operandIndex);
    } else {
      mv.visitLdcInsn(operandIndex);
    }
  }

  public void pushInt(ComputeMethod method) {
    pushDouble(method);
    method.getVisitor().visitInsn(D2I);
  }

}
