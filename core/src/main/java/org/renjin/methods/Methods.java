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

package org.renjin.methods;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.methods.PrimitiveMethodTable.prim_methods_t;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.special.SubstituteFunction;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Logical;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.S4Object;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

import com.google.common.base.Strings;

public class Methods {

  public static Environment R_initMethodDispatch(Environment env) {
    System.out.println("methods init");
    return env;
  }
 

  public static boolean R_set_method_dispatch(@Current Context context, LogicalVector onOff) {
    MethodDispatch methodContext = context.getGlobals().getSingleton(MethodDispatch.class);
    boolean oldValue = methodContext.isEnabled();
    if(onOff.getElementAsLogical(0) == Logical.TRUE) {
      methodContext.setEnabled(true);
    } else if(onOff.getElementAsLogical(0) == Logical.FALSE) {
      methodContext.setEnabled(false);
    }
    System.out.println("methods enabled = " + methodContext.isEnabled());
    return oldValue;
  }

  public static S4Object Rf_allocS4Object() {
    return new S4Object();
  }


  public static S4Object R_externalptr_prototype_object() {
    return new S4Object();
  }

  public static SEXP R_set_slot(SEXP object, String name, SEXP value) {
    if(name.equals(".Data")) {
      // the .Data slot actually refers to the object value itself, for 
      // example the double values contained in a double vector
      // So we copy the slots from 'object' to the new value
      return value.setAttributes(object.getAttributes());
    } else {
      // When set via S4 methods, R attributes can contain
      // invalid values, for example the 'class' attribute
      // might contain a double vector of arbitrary length.
      // For this reason we have to be careful to avoid attribute
      // validation. 
      return object.setAttributes(object.getAttributes().copy().set(name, value).build());
    }
  }

  public static SEXP R_get_slot(SEXP object, String what) {
    if(what.equals(".Data")) {
      if(object instanceof S4Object) {
        throw new EvalException("Data part is undefined for general S4 object");
      }
      return object.setAttributes(AttributeMap.EMPTY);
    } else {
      SEXP value = object.getAttributes().get(what);
      if(value == Null.INSTANCE) {
        throw new EvalException("no slot of name \"%s\" for this object of class \"%s\"", what, 
            object.getAttributes().getClass());
  
      }
      return value;
    }
  }

  public static String R_methodsPackageMetaName(String prefix, String name, String packageName) {
    StringBuilder metaName = new StringBuilder()
    .append(".__")
    .append(prefix)
    .append("__")
    .append(name);
    if(!Strings.isNullOrEmpty(packageName)) {
      metaName.append(":").append(packageName);
    }
    return metaName.toString();
  }

  public static SEXP R_getClassFromCache(SEXP className, Environment table) {
    if(className instanceof StringVector) {
      String packageName = className.getAttributes().getPackage();
      SEXP cachedValue = table.getVariable(Symbol.get(((StringVector) className).getElementAsString(0)));

      if(cachedValue == Symbol.UNBOUND_VALUE) {
        return Null.INSTANCE;
      } else {
        String cachedPackage = cachedValue.getAttributes().getPackage();

        if(packageName == null || cachedPackage == null || 
            packageName.equals(cachedPackage)) {

          return cachedValue;

        } else {
          return Null.INSTANCE;
        }
      }

    } else if(!(className instanceof S4Object)) {
      throw new EvalException("Class should be either a character-string name or a class definition");
    } else {
      return className;
    }
  }


  /**
   * Seems to return true if e1 and e2 are character vectors
   * both of length 1 with equal string values.
   * 
   **/
  public static boolean R_identC(SEXP e1, SEXP e2) {
    if(e1 instanceof StringVector && e2 instanceof StringVector &&
        e1.length() == 1 && e2.length() == 1) {

      StringVector s1 = (StringVector) e1;
      StringVector s2 = (StringVector) e2;
      if(!s1.isElementNA(0)) {
        return s1.getElementAsString(0).equals(s2.getElementAsString(0));
      }

    }
    return false;
  }

  public static SEXP R_do_new_object(S4Object classRepresentation) {
    // TODO: check virtual flag

    SEXP classNameExp = classRepresentation.getAttributes().get(Symbols.CLASS_NAME);
    String className = ((StringVector)classNameExp).getElementAsString(0);
    SEXP prototype = classRepresentation.getAttribute(Symbols.PROTOTYPE);

    if(!(prototype instanceof S4Object)) {
      //  System.out.println(prototype.getClass().getSimpleName());
    }
    
    if(prototype instanceof S4Object || classNameExp.getAttributes().getPackage() != null) {
      return prototype.setAttribute(Symbols.CLASS, classNameExp);
    } else {
      return prototype;
    }
  }

  @Primitive(".cache_class")
  public static SEXP cacheClass(@Current Context context, String className) {
    return context
        .getGlobals()
        .getSingleton(MethodDispatch.class)
        .getExtends(className);
  }

  @Primitive(".cache_class")
  public static SEXP cacheClass(@Current Context context, String className, SEXP klass) {
    context
    .getGlobals()
    .getSingleton(MethodDispatch.class)
    .putExtends(className, klass);  
    return klass;
  }

  public static SEXP R_getGeneric(@Current Context context, String symbol, boolean mustFind, Environment rho, String pkg) {
    return R_getGeneric(context, Symbol.get(symbol), mustFind, rho, pkg);
  }
  
  public static SEXP R_getGeneric(@Current Context context, Symbol symbol, boolean mustFind, Environment rho, String pkg) {

    SEXP generic = getGeneric(context, symbol, rho, pkg);
    if(generic == Symbol.UNBOUND_VALUE) {
      if(mustFind) {
        throw new EvalException("No generic function definition found for '%s' in the supplied environment", symbol.getPrintName());
      }
      generic = Null.INSTANCE;
    }
    return generic;
  }

  protected static SEXP getGeneric(@Current Context context, Symbol symbol, Environment env, String pkg) {
    SEXP vl;
    SEXP generic = Symbol.UNBOUND_VALUE;
    String gpackage; 
    //const char *pkg; Rboolean ok;
    boolean ok;

    Environment rho = env;
    while (rho != Environment.EMPTY) {
      vl =  rho.getVariable(symbol);
      if (vl != Symbol.UNBOUND_VALUE) {
        vl = vl.force(context);

        ok = false;
        if(IS_GENERIC(vl)) {
          if(!Strings.isNullOrEmpty(pkg)) {
            gpackage = vl.getAttributes().getPackage();
            ok =  pkg.equals(gpackage);
          } else {
            ok = true;
          }
        }
        if(ok) {
          generic = vl;
          break;
        } else {
          vl = Symbol.UNBOUND_VALUE;
        }
      }
      rho = rho.getParent();
    }
    /* look in base if either generic is missing */
    if(generic == Symbol.UNBOUND_VALUE) {
      vl = env.getBaseEnvironment().getVariable(symbol);
      if(IS_GENERIC(vl)) {
        generic = vl;
        if(vl.getAttributes().getPackage() != null) {
          gpackage = vl.getAttributes().getPackage();
          if(!gpackage.equals(pkg)) {
            generic = Symbol.UNBOUND_VALUE;
          }
        }
      }
    }
    return generic;
  }

  private static boolean IS_GENERIC(SEXP value) {
    return value instanceof Closure && value.getAttributes().has(Symbols.GENERIC);
  }


  /**
   *  substitute in an _evaluated_ object, with an explicit list as
   *  second arg (although old-style lists and environments are allowed).
   */
  public static SEXP do_substitute_direct(SEXP f, SEXP env) {
    return SubstituteFunction.substitute(f, env);
  }
  
  
  public static SEXP R_M_setPrimitiveMethods(@Current Context context, SEXP fname, SEXP op, String code_vec,
      SEXP fundef, SEXP mlist) {
    
    return R_set_prim_method(context, fname, op, code_vec, fundef, mlist);
    
  }
  
  
  public static void do_set_prim_method(@Current Context context, PrimitiveFunction op, 
      String code_string, SEXP fundef, SEXP mlist) {

    prim_methods_t code = parseCode(code_string);

    PrimitiveMethodTable table = context.getGlobals().getSingleton(PrimitiveMethodTable.class);
    PrimitiveMethodTable.Entry entry = table.get(op);
    
    entry.setMethods(code);

    if(code != prim_methods_t.SUPPRESSED) {
      if(fundef != Null.INSTANCE) {
        entry.setGeneric((Closure)fundef);
      }
    }
    if(code == prim_methods_t.HAS_METHODS) {
      entry.setMethodList(mlist);
    }
  }
  
  public static SEXP R_set_prim_method(@Current Context context, SEXP fname, SEXP op, String code_string, SEXP fundef, SEXP mlist) {
    PrimitiveMethodTable table = context.getGlobals().getSingleton(PrimitiveMethodTable.class);
    
    /* with a NULL op, turns all primitive matching off or on (used to avoid possible infinite
    recursion in methods computations*/
    if(op == Null.INSTANCE) {
      SEXP value = LogicalVector.valueOf(table.isPrimitiveMethodsAllowed());
      switch(parseCode(code_string)) {
      case NO_METHODS:
        table.setPrimitiveMethodsAllowed(false);
        break;
      case HAS_METHODS:
        table.setPrimitiveMethodsAllowed(true);
        break;
      default: /* just report the current state */
        break;
      }
      return value;
    } else {
      do_set_prim_method(context, (PrimitiveFunction)op, code_string, fundef, mlist);
      return fname;
    }
  }


  private static prim_methods_t parseCode(String code_string) {
    prim_methods_t code = prim_methods_t.NO_METHODS;
    if(code_string.equalsIgnoreCase("clear")) {
      code = prim_methods_t.NO_METHODS;
    } else if(code_string.equalsIgnoreCase("reset")) {
      code = prim_methods_t.NEEDS_RESET;
    } else if(code_string.equalsIgnoreCase("set")) {
      code = prim_methods_t.HAS_METHODS;
    } else if(code_string.equalsIgnoreCase("suppress")) {
      code = prim_methods_t.SUPPRESSED;
    }  else {
      throw new EvalException("invalid primitive methods code (\"%s\"): should be \"clear\", \"reset\", \"set\", or \"suppress\"", code_string);
    }
    return code;
  }

}
