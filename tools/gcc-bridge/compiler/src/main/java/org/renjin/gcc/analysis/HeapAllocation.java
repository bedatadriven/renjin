/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${year} BeDataDriven Groep B.V. and contributors
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

package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.statement.GimpleStatement;

public class HeapAllocation extends Allocation {

  private static int NEXT_ID = 1;

  private int id;
  private GimpleStatement mallocStatement;

  public HeapAllocation(GimpleStatement mallocStatement) {
    this.id = (NEXT_ID++);
    this.mallocStatement = mallocStatement;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    HeapAllocation that = (HeapAllocation) o;

    return mallocStatement == that.mallocStatement;
  }

  @Override
  public int hashCode() {
    return mallocStatement.hashCode();
  }

  @Override
  public String toString() {
    return "heap@" + id;
  }
}
