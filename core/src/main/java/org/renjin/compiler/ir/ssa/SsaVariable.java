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
package org.renjin.compiler.ir.ssa;

import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.expr.CompiledSexp;
import org.renjin.compiler.ir.IRFormatting;
import org.renjin.compiler.ir.tac.expressions.Variable;


public class SsaVariable extends Variable {
  private final Variable inner;
  private final int version;
  
  public SsaVariable(Variable inner, int version) {
    super();
    if(inner instanceof SsaVariable) {
      throw new IllegalArgumentException("SSA variables should not be nested");
    }
    this.inner = inner;
    this.version = version;
  }
  
  public Variable getInner() {
    return inner;
  }

  public int getVersion() {
    return version;
  }

  @Override
  public boolean isPure() {
    if(version == 0) {
      // version zero is fetched from the environment, which may force
      // a promise with side effects, so we can't trust it.
      return false;
    } else {
      // otherwise we know that we're not dealing with promises
      return true;
    }
  }

  @Override
  public CompiledSexp getCompiledExpr(EmitContext emitContext) {
    return emitContext.getVariable(this).getCompiledExpr();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(inner.toString());
    
    IRFormatting.appendSubscript(sb, version);
    
    return sb.toString();
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + inner.hashCode();
    result = prime * result + version;
    return result;
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
    SsaVariable other = (SsaVariable) obj;
    return inner.equals(other.inner) && version == other.version;
  }
}
