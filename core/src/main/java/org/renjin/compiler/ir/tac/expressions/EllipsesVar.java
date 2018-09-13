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
package org.renjin.compiler.ir.tac.expressions;

import java.util.Objects;

public class EllipsesVar extends Variable {

  public int index;

  /**
   *
   * @param index one-based index of the variable
   */
  public EllipsesVar(int index) {
    this.index = index;
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public String toString() {
    return ".." + index;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EllipsesVar that = (EllipsesVar) o;
    return index == that.index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index);
  }
}
