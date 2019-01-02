/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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

import org.renjin.sexp.Symbol;


/**
 * A {@code Variable} that is bound to the R {@code Environment}.
 */
public class EnvironmentVariable extends Variable {

  private final Symbol name;
  
  public EnvironmentVariable(Symbol name) {
    this.name = name;
  }
  
  public EnvironmentVariable(String name) {
    this(Symbol.get(name));
  }
  
  public Symbol getName() {
    return name;
  }

  @Override
  public String toString() {
    return name.toString();
  }

  @Override
  public int hashCode() {
    return name.hashCode();
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
    EnvironmentVariable other = (EnvironmentVariable) obj;
    return name == other.name;
  }

  @Override
  public boolean isPure() {
    return false;
  }


}
