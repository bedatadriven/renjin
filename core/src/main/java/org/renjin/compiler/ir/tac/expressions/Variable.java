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


import org.renjin.compiler.ir.ssa.SsaVariable;
import org.renjin.repackaged.guava.collect.Maps;

import java.util.Map;

public abstract class Variable extends LValue {

  private Map<Integer, SsaVariable> versions = Maps.newHashMap();

  public SsaVariable getVersion(int versionNumber) {
    SsaVariable ssa = versions.get(versionNumber);
    if(ssa == null) {
      ssa = new SsaVariable(this, versionNumber);
      versions.put(versionNumber, ssa);
    }
    return ssa;
  }
}
