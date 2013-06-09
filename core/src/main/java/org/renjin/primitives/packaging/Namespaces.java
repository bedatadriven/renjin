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

import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Evaluate;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

public class Namespaces {

  @Primitive
  public static SEXP getRegisteredNamespace(@Current NamespaceRegistry registry, Symbol name) {
    if(registry.isRegistered(name)) {
      return registry.getNamespace(name).getNamespaceEnvironment();
    } else {
      return Null.INSTANCE;
    }
  }
  
  @Primitive
  public static SEXP getNamespace(@Current NamespaceRegistry registry, Symbol name) {
    return registry.getNamespace(name).getNamespaceEnvironment();
  }

  @Primitive
  public static SEXP getNamespace(@Current NamespaceRegistry registry, String name) {
    return registry.getNamespace(name).getNamespaceEnvironment();
  }
  
  @Primitive
  public static boolean isNamespace(@Current NamespaceRegistry registry, SEXP envExp) {
    if(envExp instanceof Environment) {
      return registry.isNamespaceEnv((Environment)envExp);
    } else {
      return false;
    }
  }
  
  @Primitive
  public static StringVector loadedNamespaces(@Current NamespaceRegistry registry) {
    StringVector.Builder result = new StringVector.Builder();
    for(Symbol name : registry.getLoadedNamespaces()) {
      result.add(name.getPrintName());
    }
    return result.build();
  }

  @Primitive(":::")
  public static SEXP getNamespaceValue(@Current NamespaceRegistry registry, @Evaluate(false) Symbol namespace, @Evaluate(false) Symbol entry) {
    return registry.getNamespace(namespace).getEntry(entry);  
  }
  
  @Primitive("::")
  public static SEXP getExportedNamespaceValue(@Current NamespaceRegistry registry, @Evaluate(false) Symbol namespace, @Evaluate(false) Symbol entry) {
    return registry.getNamespace(namespace).getExport(entry);  
  }

  @Primitive
  public static SEXP getDataset(@Current NamespaceRegistry registry, String namespaceName, String datasetName) throws IOException {
    return registry.getNamespace(namespaceName).getPackage().loadDataset(datasetName);
  }
  
}