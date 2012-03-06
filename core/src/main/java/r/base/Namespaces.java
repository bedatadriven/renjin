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

package r.base;

import org.renjin.primitives.annotations.Current;

import r.lang.*;
import r.lang.exception.EvalException;

public class Namespaces {

  public static SEXP getRegisteredNamespace(@Current Context context, String name) {
    return getRegisteredNamespace(context, Symbol.get(name));
  }

  public static SEXP getRegisteredNamespace(@Current Context context, Symbol name) {
    return context.findNamespace(name);
  }

  public static void registerNamespace(@Current Context context, String name, Environment env) {
    Frame registry = context.getGlobals().namespaceRegistry;
    Symbol symbol = Symbol.get(name);
    if(registry.getVariable(symbol) != Symbol.UNBOUND_VALUE) {
      throw new EvalException("name space already registered");
    }
    registry.setVariable(symbol, env);
  }

  public static boolean isNamespaceEnv(@Current Context context, SEXP envExp) {
    if(envExp == context.getGlobals().baseNamespaceEnv) {
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

}
