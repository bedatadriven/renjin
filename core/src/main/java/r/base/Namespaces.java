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
    return context.findNamespace(name);
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

  public static void importIntoEnv(Environment impenv, StringVector impnames, Environment expenv, StringVector expnames) {

    /* This function copies values of variables from one environment
       to another environment, possibly with different names.
       Promises are not forced and active bindings are preserved. */

    SEXP binding, env, val;
    int  n;

    if(impnames.length() != expnames.length()) {
      throw new EvalException("length of import and export names must match");
    }

    for(int i=0;i!=impnames.length();++i) {
      if(impnames.isElementNA(i) || expnames.isElementNA(i)) {
        throw new EvalException("Import/export name cannot be NA");
      }
      Symbol impsym = new Symbol(impnames.getElementAsString(i));
      Symbol expsym = new Symbol(expnames.getElementAsString(i));

      SEXP value = expenv.findVariable(impsym);
      expenv.setVariable(expsym, value);
    }

  }


//
//  public static boolean importIntoEnv() {
//    SEXP attribute_hidden do_importIntoEnv(SEXP call, SEXP op, SEXP args, SEXP rho)
//{
//    /* This function copies values of variables from one environment
//       to another environment, possibly with different names.
//       Promises are not forced and active bindings are preserved. */
//    SEXP impenv, impnames, expenv, expnames;
//    SEXP impsym, expsym, binding, env, val;
//    int i, n;
//
//    checkArity(op, args);
//
//    impenv = CAR(args); args = CDR(args);
//    impnames = CAR(args); args = CDR(args);
//    expenv = CAR(args); args = CDR(args);
//    expnames = CAR(args); args = CDR(args);
//
//    if (TYPEOF(impenv) == NILSXP)
//        error(_("use of NULL environment is defunct"));
//    if (TYPEOF(impenv) != ENVSXP)
//        error(_("bad import environment argument"));
//    if (TYPEOF(expenv) == NILSXP)
//        error(_("use of NULL environment is defunct"));
//    if (TYPEOF(expenv) != ENVSXP)
//        error(_("bad export environment argument"));
//    if (TYPEOF(impnames) != STRSXP || TYPEOF(expnames) != STRSXP)
//        error(_("invalid '%s' argument"), "names");
//    if (LENGTH(impnames) != LENGTH(expnames))
//        error(_("length of import and export names must match"));
//
//    n = LENGTH(impnames);
//    for (i = 0; i < n; i++) {
//        impsym = install(translateChar(STRING_ELT(impnames, i)));
//        expsym = install(translateChar(STRING_ELT(expnames, i)));
//
//        /* find the binding--may be a CONS cell or a symbol */
//        for (env = expenv, binding = R_NilValue;
//             env != R_EmptyEnv && binding == R_NilValue;
//             env = ENCLOS(env))
//            if (env == R_BaseNamespace) {
//                if (SYMVALUE(expsym) != R_UnboundValue)
//                    binding = expsym;
//            }
//            else
//                binding = findVarLocInFrame(env, expsym, NULL);
//        if (binding == R_NilValue)
//            binding = expsym;
//
//        /* get value of the binding; do not force promises */
//        if (TYPEOF(binding) == SYMSXP) {
//            if (SYMVALUE(expsym) == R_UnboundValue)
//                error(_("exported symbol '%s' has no value"),
//                      CHAR(PRINTNAME(expsym)));
//            val = SYMVALUE(expsym);
//        }
//        else val = CAR(binding);
//
//        /* import the binding */
//        if (IS_ACTIVE_BINDING(binding))
//            R_MakeActiveBinding(impsym, val, impenv);
//        /* This is just a tiny optimization */
//        else if (impenv == R_BaseNamespace || impenv == R_BaseEnv)
//            gsetVar(impsym, val, impenv);
//        else
//            defineVar(impsym, val, impenv);
//    }
//    return R_NilValue;
//
//  }
}
