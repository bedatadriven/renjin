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

import java.io.IOException;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Unevaluated;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

public class Namespaces {

  @Internal
  public static SEXP getRegisteredNamespace(@Current NamespaceRegistry registry, Symbol name) {
    if(registry.isRegistered(name)) {
      return registry.getNamespace(name).getNamespaceEnvironment();
    } else {
      return Null.INSTANCE;
    }
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
  
  @Builtin
  public static StringVector getNamespaceName(@Current NamespaceRegistry registry, 
                                              final SEXP envExp) {
    if (isNamespace(registry, envExp)) {
      final FqPackageName packageName =
          registry.getNamespace((Environment)envExp).getFullyQualifiedName();
      if (packageName.getGroupId().equals(FqPackageName.CORE_GROUP_ID)) {
        return new StringArrayVector(packageName.getPackageName());
      } else {
        return new StringArrayVector(packageName.toString(':'));
      }
    } else {
      throw new EvalException("Error in argument " + envExp.toString() + " : not a namespace");
    }
  }
  
  @Builtin
  public static StringVector getNamespaceExports(@Current NamespaceRegistry registry, 
                                                 final SEXP envExp) {
    if ((envExp instanceof Environment) && isNamespace(registry, envExp)) {
      StringVector.Builder result = new StringVector.Builder();
      final Environment env = (Environment) envExp;
      final Namespace ns = registry.getNamespace(env);
      if (FqPackageName.BASE.equals(ns.getPackage().getName())) {
        // The base package's namespace is treated specially for historical reasons:
        // all symbols are considered to be exported.
        for (Symbol name : env.getSymbolNames()) {
          result.add(name.getPrintName());
        }
      } else {
        for (Symbol name : ns.exports) {
          result.add(name.getPrintName());
        }
      }
      return result.build();
    } else {
      throw new EvalException("Error in argument " + envExp.toString() + " : not a namespace");
    }
  }
  
  @Builtin
  public static StringVector getNamespaceImports(@Current NamespaceRegistry registry, 
                                                 final SEXP envExp) {
    if (isNamespace(registry, envExp)) {
      StringVector.Builder result = new StringVector.Builder();
      final Namespace ns = registry.getNamespace((Environment)envExp);
      for (final Symbol name : ns.getImportsEnvironment().getSymbolNames()) {
        result.add(name.getPrintName());
      }
      return result.build();
    } else {
      throw new EvalException("Error in argument " + envExp.toString() + " : not a namespace");
    }
  }
}
