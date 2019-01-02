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

import org.renjin.sexp.Environment;

/**
 * Context for writing SEXP without an environment or session. Referencing
 * namespaces or global environments is likely to fail.
 */
public enum  HeadlessWriteContext implements WriteContext {

  INSTANCE;

  @Override
  public boolean isBaseEnvironment(Environment exp) {
    return false;
  }

  @Override
  public boolean isNamespaceEnvironment(Environment exp) {
    return false;
  }

  @Override
  public boolean isBaseNamespaceEnvironment(Environment ns) {
    return false;
  }

  @Override
  public boolean isGlobalEnvironment(Environment env) {
    return false;
  }

  @Override
  public String getNamespaceName(Environment ns) {
    throw new UnsupportedOperationException();
  }
}
