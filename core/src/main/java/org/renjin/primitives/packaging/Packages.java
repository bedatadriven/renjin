/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.packaging;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.invoke.annotations.Invisible;
import org.renjin.sexp.*;

import java.io.IOException;

public class Packages {

  public static final FqPackageName METHODS_NAMESPACE = new FqPackageName("org.renjin", "methods");

  @Internal
  public static void library(
      @Current Context context,
      @Current NamespaceRegistry namespaceRegistry, 
      String packageName) throws IOException {


    Namespace namespace = namespaceRegistry.getNamespace(context, packageName);
    
    // Check to see if already on the search path...
    if(isAttached(context, namespace)) {
      return;
    }
    
    // Add "Depends" packages to the global search path
    // (But not "Imports" !)
    for(String dependencyName : namespace.getPackage().getPackageDependencies()) {
      context.getSession().getStdOut().println("Loading required package: " + dependencyName);
      library(context, namespaceRegistry, dependencyName);
    }
    
    // Create the package environment
    Environment packageEnv = context.getGlobalEnvironment().insertAbove(new HashFrame());
    packageEnv.setAttribute(Symbols.NAME,  StringVector.valueOf("package:" + namespace.getFullyQualifiedName().getPackageName()));
    packageEnv.setAttribute(Symbols.FQNAME,  StringVector.valueOf("package:" + namespace.getFullyQualifiedName().toString()));

    // Copy in the namespace's exports
    namespace.copyExportsTo(context, packageEnv);
    
    context.setInvisibleFlag();
  }

  private static boolean isAttached(Context context, Namespace namespace) {
    
    String expected = "package:" + namespace.getFullyQualifiedName().toString();
    
    Environment env = context.getGlobalEnvironment();
    while(env != Environment.EMPTY) {
      SEXP fqNameSexp = env.getAttribute(Symbols.FQNAME);
      if(fqNameSexp instanceof StringVector && fqNameSexp.length() == 1) {
        String fqName = ((StringVector) fqNameSexp).getElementAsString(0);
        if(expected.equals(fqName)) {
          return true;
        }
      }
      env = env.getParent();
    }
    return false;
  }


  @Internal
  @Invisible
  public static boolean require(@Current Context context,
                                @Current NamespaceRegistry registry, 
                                String packageName) {
    try {
      library(context, registry, packageName);
      return true;
    } catch(Exception e) {
      return false;
    }
  }
}
