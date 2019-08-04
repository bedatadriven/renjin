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
package org.renjin.serialization;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

public class SessionReadContext implements ReadContext {
  private Context context;

  public SessionReadContext(Context context) {
    this.context = context;
  }

  @Override
  public Environment getBaseEnvironment() {
    return context.getBaseEnvironment();
  }

  @Override
  public SEXP createPromise(SEXP expr, Environment env) {
    return expr.promise(env);
  }

  @Override
  public Environment findNamespace(Symbol symbol) {
    return context.getNamespaceRegistry().getNamespace(context, symbol).getNamespaceEnvironment();
  }

  @Override
  public Environment getBaseNamespaceEnvironment() {
    return context.getSession().getBaseNamespaceEnv();
  }

  @Override
  public Environment getGlobalEnvironment() {
    return context.getGlobalEnvironment();
  }
}
