/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.compiler.codegen.var;

import org.renjin.repackaged.asm.Type;

public class LocalVarAllocator {

  private int nextIndex;

  public LocalVarAllocator(int parametersSize) {
    this.nextIndex = parametersSize;
  }

  public int reserve(Type type) {
    return reserveWithSize(type.getSize());
  }

  public int reserveArray() {
    return reserveWithSize(1);
  }

  private int reserveWithSize(int size) {
    int slotIndex = nextIndex;
    nextIndex += size;
    return slotIndex;
  }

  public int getCount() {
    return nextIndex;
  }

}
