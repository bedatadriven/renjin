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

package org.renjin.compiler;

import org.renjin.compiler.ir.tac.RuntimeAssumption;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;

import java.util.List;

public class CachedBody {
  private final CompiledBody compiledBody;
  private final RuntimeAssumption[] assumptions;

  public CachedBody(CompiledBody compiledBody, List<RuntimeAssumption> assumptions) {
    this.compiledBody = compiledBody;
    this.assumptions = assumptions.toArray(new RuntimeAssumption[assumptions.size()]);
  }

  public CompiledBody getCompiledBody() {
    return compiledBody;
  }

  /**
   * Returns true if the assumptions used to compile the loop body are still met.
   */
  public boolean assumptionsStillMet(Context context, Environment rho) {
    for (int i = 0; i < assumptions.length; i++) {
      if(!assumptions[i].test(context, rho)) {
        System.err.println("Assumption " + assumptions[i] + " violated");
        return false;
      }
    }
    return true;
  }
}
