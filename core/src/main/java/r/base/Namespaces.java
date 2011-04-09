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

import r.jvmi.annotations.Current;
import r.lang.*;
import r.lang.exception.EvalException;

public class Namespaces {

  public static SEXP getRegisteredNamespace(@Current Context context, String name) {
    return getRegisteredNamespace(context, new Symbol(name));

  }

  public static SEXP getRegisteredNamespace(@Current Context context, Symbol name) {
    SEXP value = context.getGlobals().namespaceRegistry.getVariable(name);
    if(value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    } else {
      return value;
    }
  }

  public static void registerNamespace(@Current Context context, String name, Environment env) {
    Frame registry = context.getGlobals().namespaceRegistry;
    Symbol symbol = new Symbol(name);
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
      SEXP info = env.getVariable(new Symbol(".__NAMESPACE__."));
      if(info instanceof Environment) {
        SEXP spec = ((Environment)info).getVariable(new Symbol("spec"));
        if(spec instanceof StringVector && spec.length() > 0) {
          return true;
        } else {
          return false;
        }
      }
    }
    return false;
  }
}
