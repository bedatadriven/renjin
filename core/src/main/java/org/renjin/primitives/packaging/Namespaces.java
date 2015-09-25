/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Unevaluated;
import org.renjin.sexp.*;

import java.io.IOException;

public class Namespaces {

  @Internal
  public static SEXP getRegisteredNamespace(@Current NamespaceRegistry registry, Symbol name) {
    if(registry.isRegistered(name)) {
      return registry.getNamespace(name).getNamespaceEnvironment();
    } else {
      return Null.INSTANCE;
    }
  }
  
  @Internal
  public static Environment getNamespaceRegistry(@Current NamespaceRegistry registry) {
    return Environment.createChildEnvironment(Environment.EMPTY, new NamespaceFrame(registry));
  }

  @Builtin
  public static SEXP getNamespace(@Current NamespaceRegistry registry, Symbol name) {
    return registry.getNamespace(name).getNamespaceEnvironment();
  }

  @Builtin
  public static SEXP getNamespace(@Current NamespaceRegistry registry, String name) {
    return registry.getNamespace(name).getNamespaceEnvironment();
  }

  @Builtin
  public static boolean isNamespace(@Current NamespaceRegistry registry, SEXP envExp) {
    if(envExp instanceof Environment) {
      return registry.isNamespaceEnv((Environment)envExp);
    } else {
      return false;
    }
  }

  @Builtin
  public static StringVector loadedNamespaces(@Current NamespaceRegistry registry) {
    StringVector.Builder result = new StringVector.Builder();
    for(Symbol name : registry.getLoadedNamespaces()) {
      result.add(name.getPrintName());
    }
    return result.build();
  }

  @Builtin(":::")
  public static SEXP getNamespaceValue(@Current Context context,
                                       @Current NamespaceRegistry registry,
                                       @Unevaluated Symbol namespace,
                                       @Unevaluated Symbol entry) {

    return registry.getNamespace(namespace).getEntry(entry).force(context);
  }

  @Builtin("::")
  public static SEXP getExportedNamespaceValue(@Current Context context,
                                               @Current NamespaceRegistry registry,
                                               @Unevaluated Symbol namespace,
                                               @Unevaluated Symbol entry) {

    return registry.getNamespace(namespace).getExport(entry).force(context);
  }

  @Internal
  public static SEXP getDataset(@Current NamespaceRegistry registry,
                                String namespaceName,
                                String datasetName) throws IOException {
    return registry.getNamespace(namespaceName).getPackage().getDataset(datasetName);
  }

  private static Namespace resolveNamespace(NamespaceRegistry registry, SEXP sexp) {

    if (sexp instanceof Environment) {
      Environment environment = (Environment) sexp;
      if (registry.isNamespaceEnv(environment)) {
        return registry.getNamespace(environment);
      }
    } else if(sexp instanceof StringVector && sexp.length() == 1) {
      return registry.getNamespace(((StringVector) sexp).getElementAsString(0));
    }
    throw new EvalException("Error in argument " + sexp + " : not a namespace");
  }

  @Builtin
  public static StringVector getNamespaceName(@Current NamespaceRegistry registry,
                                              final SEXP envExp) {

    Namespace namespace = resolveNamespace(registry, envExp);

    if(namespace == registry.getBaseNamespace()) {
      // For whatever reason R3.2.0 returns a simple character vector without attributes
      return new StringArrayVector("base");

    } else {
      // All other package names result in a named vector
      StringVector.Builder builder = StringArrayVector.newBuilder();
      builder.add(namespace.getCompatibleName());
      builder.setAttribute(Symbols.NAMES, StringArrayVector.valueOf("name"));
      return builder.build();
    }
  }

  @Builtin
  public static StringVector getNamespaceExports(@Current NamespaceRegistry registry, final SEXP sexp) {
    final Namespace ns = resolveNamespace(registry, sexp);

    StringVector.Builder result = new StringVector.Builder();
    for (Symbol name : ns.getExports()) {
      result.add(name.getPrintName());
    }
    return result.build();
  }

  @Builtin
  public static StringVector getNamespaceImports(@Current NamespaceRegistry registry, final SEXP sexp) {
    Namespace ns = resolveNamespace(registry, sexp);
    throw new UnsupportedOperationException("TODO: implement getNamespaceImports!");
  }
}
