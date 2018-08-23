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

import org.renjin.compiler.ir.IRFormatting;


/**
 * A slot for a temporary value. 
 * A temporary value can only be assigned once, so it is not
 * have to be processed by the SSA transformation
 */
public class Temp extends LValue {
  
  private static final String TAO = "τ";
  
  private final int index;
  
  public Temp(int index) {
    this.index = index;
  }

  @Override 
  public String toString() {
    StringBuilder sb = new StringBuilder(TAO);
    IRFormatting.appendSubscript(sb, index+1);
    return sb.toString();
  }

  @Override
  public boolean isPure() {
    return true;
  }

  @Override
  public int hashCode() {
    return index;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Temp other = (Temp) obj;
    return index == other.index;
  }
}
