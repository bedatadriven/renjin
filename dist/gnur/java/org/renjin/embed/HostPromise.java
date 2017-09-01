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
package org.renjin.embed;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.Promise;
import org.renjin.sexp.SEXP;

public class HostPromise extends Promise {

  private Wrapper wrapper;
  private final long expr;
  private final long rho;

  public HostPromise(Wrapper wrapper, long expr, long rho) {
    super(Environment.EMPTY, Null.INSTANCE);
    this.wrapper = wrapper;
    this.expr = expr;
    this.rho = rho;
  }

  @Override
  protected SEXP doEval(Context context) {

    long valuePtr = wrapper.getEngine().rniEval(expr, rho);
    SEXP value = wrapper.wrap(valuePtr);

    return value;
  }
}
