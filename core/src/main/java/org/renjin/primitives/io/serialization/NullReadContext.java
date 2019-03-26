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
package org.renjin.primitives.io.serialization;

import org.renjin.sexp.*;

/**
 * A "null" read context which will deserialize references
 * to special environments like base and global with the
 * empty environment.
 *
 * <p>This is useful if you just want to deserialize R data
 * outside of an R {@link org.renjin.eval.Session}</p>
 */
public class NullReadContext implements ReadContext {

  @Override
  public Environment getBaseEnvironment() {
    return Environment.EMPTY;
  }

  @Override
  public Promise createPromise(SEXP expr, Environment environment) {
    return Promise.repromise(Null.INSTANCE);
  }

  @Override
  public Environment findNamespace(Symbol symbol) {
    return Environment.EMPTY;
  }

  @Override
  public Environment getBaseNamespaceEnvironment() {
    return Environment.EMPTY;
  }

  @Override
  public Environment getGlobalEnvironment() {
    return Environment.EMPTY;
  }
}
