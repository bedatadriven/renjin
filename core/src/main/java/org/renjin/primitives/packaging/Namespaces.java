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
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Evaluate;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;

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
  public static boolean isNamespaceEnv(@Current Context context, SEXP envExp) {
    if(envExp == context.getSession().baseNamespaceEnv) {
      return true;
    } else if(envExp instanceof Environment) {
      Environment env = (Environment)envExp;
      SEXP info = env.getVariable(Symbol.get(".__NAMESPACE__."));
      if(info instanceof Environment) {
        SEXP spec = ((Environment)info).getVariable(Symbol.get("spec"));
        if(spec instanceof StringVector && spec.length() > 0) {
          return true;
        } else {
          return false;
        }
      }
    }
    return false;
  }

  @Primitive
  public static void importIntoEnv(Environment impenv, Vector impnames, Environment expenv, Vector expnames) {

    /* This function copies values of variables from one environment
       to another environment, possibly with different names.
       Promises are not forced and active bindings are preserved. */

    if(impnames.length() != expnames.length()) {
      throw new EvalException("length of import and export names must match");
    }

    for(int i=0;i!=impnames.length();++i) {
      if(impnames.isElementNA(i) || expnames.isElementNA(i)) {
        throw new EvalException("Import/export name cannot be NA");
      }
      Symbol impsym = Symbol.get(impnames.getElementAsString(i));
      Symbol expsym = Symbol.get(expnames.getElementAsString(i));

      SEXP value = expenv.findVariable(impsym);
      impenv.setVariable(expsym, value);
    }
  }

  @Primitive(":::")
  public static SEXP getNamespaceValue(@Current NamespaceRegistry registry, @Evaluate(false) Symbol namespace, @Evaluate(false) Symbol entry) {
    return registry.getNamespace(namespace).getEntry(entry);  
  }
  
  @Primitive("::")
  public static SEXP getExportedNamespaceValue(@Current NamespaceRegistry registry, @Evaluate(false) Symbol namespace, @Evaluate(false) Symbol entry) {
    return registry.getNamespace(namespace).getExport(entry);  
  }
}