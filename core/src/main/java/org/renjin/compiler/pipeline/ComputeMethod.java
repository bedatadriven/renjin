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
package org.renjin.compiler.pipeline;

import org.renjin.repackaged.asm.MethodVisitor;

public class ComputeMethod {
  private int localCount = 2; // includes instance pointer and argument

  private MethodVisitor visitor;
  private int maxStackSize = 0;
  private int currentStack = 0;

  public ComputeMethod(MethodVisitor visitor) {
    this.visitor = visitor;
  }

  public MethodVisitor getVisitor() {
    return visitor;
  }

  public int reserveLocal(int size) {
    int pos = localCount;
    localCount += size;
    return pos;
  }

  public void stack(int change) {
    currentStack += change;
    if(currentStack > maxStackSize) {
      maxStackSize = currentStack;
    }
  }

  /**
   * @return the index of the operands array, containing
   * Vector[]
   */
  public int getOperandsLocalIndex() {
    return 1;
  }

  public int getMaxLocals() {
    return localCount;
  }
}
