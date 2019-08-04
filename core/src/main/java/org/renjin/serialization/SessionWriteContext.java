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
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.sexp.Environment;

public class SessionWriteContext implements WriteContext {
  private Context context;

  public SessionWriteContext(Context context) {
    this.context = context;
  }

  @Override
  public boolean isBaseEnvironment(Environment exp) {
    return exp == context.getBaseEnvironment();
  }

  @Override
  public boolean isNamespaceEnvironment(Environment environment) {
    return context.getNamespaceRegistry().isNamespaceEnv(environment);
  }

  @Override
  public boolean isBaseNamespaceEnvironment(Environment ns) {
    return ns == context
        .getNamespaceRegistry()
        .getBaseNamespaceEnv();
  }

  @Override
  public boolean isGlobalEnvironment(Environment env) {
    return env == context.getGlobalEnvironment();
  }

  @Override
  public String getNamespaceName(Environment ns) {
    // we want to use fully qualified names as much as possible throughout Renjin, but
    // also maintain compatibility with GNU R as much as possible.

    // here we will write out only the local name for the "core" packages such as base, stats, etc
    // but use the fully qualified name (e.g. 'com.acme.xy:foobar') otherwise, which will
    // fail to load in GNU R.

    // if we get to the point of needing to exchange ASTs between Renjin and R then we can always
    // provide an alternate implementation of the WriteContext interface

    FqPackageName packageName = context.getNamespaceRegistry().getNamespace(ns).getFullyQualifiedName();
    if(packageName.getGroupId().equals(FqPackageName.CORE_GROUP_ID)) {
      return packageName.getPackageName();

    } else {
      return packageName.toString(':');
    }
  }
}
