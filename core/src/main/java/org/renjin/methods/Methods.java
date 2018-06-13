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
package org.renjin.methods;

import org.renjin.eval.ClosureDispatcher;
import org.renjin.eval.Context;
import org.renjin.eval.Context.Type;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.methods.PrimitiveMethodTable.prim_methods_t;
import org.renjin.primitives.Types;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.primitives.special.SubstituteFunction;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.s4.*;
import org.renjin.sexp.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.renjin.s4.S4.generateCallMetaData;

public class Methods {


  public static SEXP R_initMethodDispatch(@Current Context context, SEXP environ) {
    context.getSession().getSingleton(MethodDispatch.class)
    .init(environ == Null.INSTANCE ? context.getGlobalEnvironment() : (Environment)environ);
    return environ;
  }

  @Builtin(".isMethodsDispatchOn")
  public static boolean isMethodsDispatchOn(@Current MethodDispatch methodDispatch) {
    return methodDispatch.isEnabled();
  }

  @Builtin(".isMethodsDispatchOn")
  public static void setMethodsDispatchOn(@Current MethodDispatch methodDispatch, boolean enabled) {
    methodDispatch.setEnabled(enabled);
  }

  public static boolean R_set_method_dispatch(@Current Context context, LogicalVector onOff) {
    MethodDispatch methodContext = context.getSession().getSingleton(MethodDispatch.class);
    boolean oldValue = methodContext.isEnabled();
    if(onOff.getElementAsLogical(0) == Logical.TRUE) {
      methodContext.setEnabled(true);
    } else if(onOff.getElementAsLogical(0) == Logical.FALSE) {
      methodContext.setEnabled(false);
    }
    return oldValue;
  }

  public static S4Object Rf_allocS4Object() {
    return new S4Object();
  }


  public static ExternalPtr R_externalptr_prototype_object() {
    return new ExternalPtr(null);
  }

  public static SEXP R_set_slot(@Current Context context, SEXP object, String name, SEXP value) {
    if(name.equals(".Data")) {
      // the .Data slot actually refers to the object value itself, for 
      // example the double values contained in a double vector
      // So we copy the slots from 'object' to the new value
      return context.evaluate(FunctionCall.newCall(Symbol.get("setDataPart"), object, value),
          context.getSingleton(MethodDispatch.class).getMethodsNamespace());
    } else {
      // When set via S4 methods, R attributes can contain
      // invalid values, for example the 'class' attribute
      // might contain a double vector of arbitrary length.
      // For this reason we have to be careful to avoid attribute
      // validation. 
      SEXP slotValue = value == Null.INSTANCE ? Symbols.S4_NULL : value;
      return object.setAttributes(object.getAttributes().copy().set(name, slotValue));
    }
  }


  public static SEXP R_get_slot(@Current Context context, SEXP object, String what) {
    return R_do_slot(context, object, StringArrayVector.valueOf(what));
  }

  public static int R_has_slot(SEXP obj, SEXP name) {
    Symbol slot = Symbol.get(name.asString());
    Map<Symbol, SEXP> objSlots = obj.getAttributes().toMap();
    if(objSlots.containsKey(slot)) {
      return 1;
    }
    return 0;
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

  public static SEXP R_getClassFromCache(@Current Context context, SEXP className, Environment table) {
    if(className instanceof StringVector) {
      String packageName = className.getAttributes().getPackage();
      SEXP cachedValue = table.getVariable(context, Symbol.get(((StringVector) className).getElementAsString(0)));

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
    
    if(prototype instanceof S4Object || classNameExp.getAttributes().getPackage() != null) {
      return prototype.setAttribute(Symbols.CLASS, classNameExp);
    } else {
      return prototype;
    }
  }

  @Builtin(".cache_class")
  public static SEXP cacheClass(@Current Context context, String className) {
    return context
        .getSession()
        .getSingleton(MethodDispatch.class)
        .getExtends(className);
  }

  @Builtin(".cache_class")
  public static SEXP cacheClass(@Current Context context, String className, SEXP klass) {
    context
        .getSession()
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
      vl =  rho.getVariable(context, symbol);
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
      vl = context.getBaseEnvironment().getVariable(context, symbol);
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
  public static SEXP do_substitute_direct(@Current Context context, SEXP f, SEXP env) {
    return SubstituteFunction.substitute(context, f, env);
  }
  
  public static SEXP R_M_setPrimitiveMethods(@Current Context context, SEXP fname, SEXP op, String code_vec,
      SEXP fundef, SEXP mlist) {

    return R_set_prim_method(context, fname, op, code_vec, fundef, mlist);

  }


  public static void do_set_prim_method(@Current Context context, PrimitiveFunction op, 
      String code_string, SEXP fundef, SEXP mlist) {

    prim_methods_t code = parseCode(code_string);

    PrimitiveMethodTable table = context.getSession().getSingleton(PrimitiveMethodTable.class);
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
    PrimitiveMethodTable table = context.getSession().getSingleton(PrimitiveMethodTable.class);

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

  @Internal

  @Internal
  public static SEXP getClass(@Current Context context, SEXP className, boolean dotForce, SEXP where) {

    if(className instanceof S4Object) {
      SEXP classes = className.getAttribute(Symbol.get("class"));
      if(classes instanceof StringVector) {
        if("classRepresentation".equals(((StringVector)classes).getElementAsString(0))) {
          return className;
        }
      }
    }

    SEXP classDef = getClassDef(context, ((StringVector)className), Null.INSTANCE, Null.INSTANCE, true);

    if(dotForce && (classDef == Null.INSTANCE || classDef == Symbol.UNBOUND_VALUE)) {
      if(dotForce) {
        System.out.println("getClass(" + ((StringVector)className).getElementAsString(0) + ", .Force = TRUE)");
        SEXP env;
        if(where == Null.INSTANCE) {
          env = context.getCallingEnvironment();
        } else {
          env = where;
        }
        PairList.Builder args = new PairList.Builder();
        args.add(className);
        args.add(Symbol.get("package"), StringVector.valueOf("base"));
        args.add(Symbol.get("virtual"), LogicalVector.TRUE);
        args.add(Symbol.get("where"), env);
        classDef = context.evaluate(FunctionCall.newCall(Symbol.get("makeClassRepresentation"), args.build()));
      } else {
        throw new EvalException("'" + ((StringVector)className).getElementAsString(0) + "' is not a defined class");
      }
    }
    return classDef;
  }

  @Internal
  public static SEXP getClassDef(@Current Context context, StringVector className, SEXP where, SEXP packageName, boolean inherits) {
    SEXP classDef = Symbol.UNBOUND_VALUE;
    String providedPackage = null;

    if(inherits) {
      S4Cache s4Cache = context.getSession().getS4Cache();
      S4Class s4Class = s4Cache.getS4ClassCache().lookupClass(context, className.getElementAsString(0));
      if(s4Class != null) {
        classDef = s4Class.getDefinition();
      }
    }

    if(classDef == Symbol.UNBOUND_VALUE) {
      Symbol metadataName = Symbol.get(S4.CLASS_PREFIX + className);

      if(packageName == Null.INSTANCE) {
        SEXP packageSlot = className.getAttribute(Symbols.PACKAGE);
        if(packageSlot != Null.INSTANCE) {
          providedPackage = ((StringArrayVector) packageSlot).getElementAsString(0);
        }
      } else if(packageName instanceof StringArrayVector) {
        providedPackage = ((StringArrayVector) packageName).getElementAsString(0);
      }

      if(!Strings.isNullOrEmpty(providedPackage)) {
        Optional<Namespace> namespace = context.getNamespaceRegistry().getNamespaceIfPresent(Symbol.get(providedPackage));
        if(!namespace.isPresent()) {
          throw new EvalException("Package " + providedPackage + " is not loaded");
        }
        classDef = namespace.get().getNamespaceEnvironment().findVariable(context, metadataName, x -> true, inherits);
      } else {
        // the default value of where getClassDef where argument topenv(parent.frame()) was replaced with NULL
        if(where == Null.INSTANCE) {
          SEXP parentFrame = context.evaluate(FunctionCall.newCall(Symbol.get("parent.frame"), IntVector.valueOf(1)));
          where = context.evaluate(FunctionCall.newCall(Symbol.get("topenv"), parentFrame));
        }
        classDef = ((Environment) where).findVariable(context, metadataName, x -> true, inherits);
      }
    }

    if(classDef == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }

    classDef = classDef.force(context);

    if(!Types.isS4(classDef)) {
      throw new EvalException("ClassDefinition " + className + " is corrupted. Please rebuild package: " + classDef.getAttribute(Symbol.get("package")));
    }
    return classDef;
  }

  @Internal
  public static SEXP selectMethod(@Current Context context, StringArrayVector functionName, StringArrayVector args,
                                  LogicalArrayVector opt, LogicalArrayVector useInherited, SEXP mlist, SEXP fdef, SEXP verbose, SEXP doCache) {

    boolean optional = opt.isElementTrue(0);
    String fname = functionName.getElementAsString(0);

    String packageName = getPackageName(context, fdef);

    Generic generic = Generic.standardGeneric(context, fname, packageName);

    S4MethodCache methodCache = context.getSession().getS4Cache().getS4MethodCache();
    S4MethodTable methodTable = methodCache.getMethod(context, generic, fname);

    if(methodTable == null || methodTable.isEmpty()) {
      if (optional) {
        return Null.INSTANCE;
      } else {
        throw new EvalException("selectMethod(" + fname + "): No methods found!");
      }
    }

    Signature signature = new Signature(args.toArray());

    boolean[] inheritance = computeUseInheritance(args, useInherited, generic, methodTable);

    RankedMethod selectedMethod = methodTable.selectMethod(context, generic, signature, inheritance);

    if(selectedMethod == null) {
      if(optional) {
        return Null.INSTANCE;
      } else {
        throw new EvalException("selectMethod(" + fname + "): No matching methods found! 'optional' is set to FALSE.");
      }
    }

    return selectedMethod.getMethodDefinition();
  }

  public static boolean[] computeUseInheritance(StringArrayVector args, LogicalArrayVector useInherited, Generic generic, S4MethodTable methodTable) {
    // useInherited argument provided to selectMethod() is used to indicate
    // if inherited methods can be used for each given argument. if the length
    // of useInherited is shorter than the number of arguments, it is repeated.
    // Inheritance is not used in case of "ANY".
    boolean[] inheritance = new boolean[methodTable.getMaximumSignatureLength()];
    int useInheritedLength = useInherited.length();

    if(useInheritedLength == 1) {
      Arrays.fill(inheritance, useInherited.isElementTrue(0));
    } else {
      int j = 0;
      for(int i = 0; i < args.length(); i++, j++) {
        if(j == useInheritedLength) {
          j = 0;
        }
        inheritance[i] = useInherited.isElementTrue(j)
            && !("ANY".equals(args.getElementAsString(i)));
      }
    }


    // "coerce" is a special case. It always has two arguments and inheritance
    // might be used only for the first argument.
    if("coerce".equals(generic.getName())) {
      inheritance = new boolean[]{inheritance[0], false};
    }
    return inheritance;
  }

  public static String getPackageName(@Current Context context, SEXP fdef) {
    String packageName;
    if(fdef instanceof Closure) {
      packageName = fdef.getAttribute(S4.PACKAGE).asString();
    } else {
      packageName = context.getFunction().getAttribute(S4.PACKAGE).asString();
    }
    return packageName;
  }

  @Builtin
  public static SEXP standardGeneric(@Current Context context, Symbol fname, SEXP fdef) {
    return standardGeneric(context, Environment.EMPTY, fname.getPrintName());
  }

  @Builtin
  public static SEXP standardGeneric(@Current Context context, @Current Environment ev, String fname) {

  
    if(Strings.isNullOrEmpty(fname)) {
      throw new EvalException("argument to 'standardGeneric' must be a non-empty character string");
    }

    String packageName = context.getFunction().getAttribute(S4.PACKAGE).asString();
    Generic generic = Generic.standardGeneric(context, fname, packageName);

    S4MethodCache methodCache = context.getSession().getS4Cache().getS4MethodCache();
    S4MethodTable methodTable = methodCache.getMethod(context, generic, fname);

    if(methodTable == null || methodTable.isEmpty()) {
      throw new EvalException("standardGeneric(" + fname + "): No methods found!");
    }

    CallingArguments arguments = CallingArguments.standardGenericArguments(context, methodTable.getArgumentMatcher());

    Signature signature = arguments.getSignature(methodTable.getMaximumSignatureLength(), generic.getSignatureArgumentNames());

    boolean[] useInheritance = new boolean[methodTable.getMaximumSignatureLength()];
    Arrays.fill(useInheritance, Boolean.TRUE);

    RankedMethod selectedMethod = methodTable.selectMethod(context, generic, signature, useInheritance);

    if(selectedMethod == null) {
      throw new EvalException("unable to find an inherited method for function '" + fname +
          "' for signature " + arguments.getFullSignatureString(methodTable.getMaximumSignatureLength()));
    }

    Closure function = selectedMethod.getMethodDefinition();

    Map<Symbol, SEXP> metadata = generateCallMetaData(context, selectedMethod, signature, fname);

    PairList coercedArgs = coerce(context, arguments, selectedMethod);

    FunctionCall call = new FunctionCall(function, coercedArgs);

    return ClosureDispatcher.apply(context, context.getCallingEnvironment(), call, function, coercedArgs, metadata);
  }

  public static PairList coerce(Context context, CallingArguments arguments, RankedMethod method) {

    int signatureLength = method.getMethodSignatureLength();

    Set<String> argNames = method.getMethod().getGeneric().getSignatureArgumentNames();

    S4ClassCache classCache = context.getSession().getS4Cache().getS4ClassCache();

    PairList.Builder coercedArgs = new PairList.Builder();

    int step = 0;

    for(PairList.Node arg : arguments.getPromisedArgs().nodes()) {
      SEXP value = arg.getValue();
      SEXP tag = arg.getRawTag();
      if(step < signatureLength && (tag != Null.INSTANCE && argNames.contains(arg.getTag().getPrintName()))) {
        String from = arguments.getArgumentClass(step);
        String to = method.getArgumentClass(step);
        if(to.equals(from) || to.equals("ANY") || classCache.isSimple(from, to)) {
          coercedArgs.add(tag, value);
        } else {
          SEXP coercedArg = classCache.coerceComplex(context, value, from, to);
          coercedArgs.add(tag, coercedArg);
        }
        step += 1;
      } else {
        if(value != Symbol.MISSING_ARG) {
          coercedArgs.add(tag, value);
        }
      }
    }
    return coercedArgs.build();
  }


  /* get the generic function, defined to be the function definition for
   * the call to standardGeneric(), or for primitives, passed as the second
   * argument to standardGeneric.
   */
  public static SEXP get_this_generic(Context context, String fname) {

    SEXP value = Null.INSTANCE;

    //    /* a second argument to the call, if any, is taken as the function */
    //    if(args.length() >= 2) {
    //      return args.getElementAsSEXP(1);
    //    }
    /* else use sys.function (this is fairly expensive-- would be good
     * to force a second argument if possible) */

    Context cptr = context;
    while(!cptr.isTopLevel()) {
      SEXP function = cptr.getFunction();
      if(function.isObject()) {
        SEXP generic = function.getAttribute(MethodDispatch.GENERIC);
        if(generic instanceof StringVector && generic.asString().equals(fname)) {
          value = function;
          break;
        }
      }
      cptr = cptr.getParent();
    }
    return value;
  }


  private static Symbol checkSlotName(SEXP name) {
    if(name instanceof Symbol) {
      return (Symbol) name;
    }
    if(name instanceof StringVector && name.length() == 1) {
      return Symbol.get(name.asString());
    }
    throw new EvalException("Invalid type or length for a slot name");
  }

  static SEXP R_do_slot(Context context, SEXP obj, SEXP slotName) {
    Symbol name = checkSlotName(slotName);

    if(name == MethodDispatch.s_dot_Data) {
      return data_part(context, obj);
    } else {
      SEXP value = obj.getAttribute(name);
      if(value == Null.INSTANCE) {
        String input = name.getPrintName();
        SEXP classString;
        if(name == MethodDispatch.s_dot_S3Class) {
          /* defaults to class(obj) */
          //return R_data_class(obj, false);
          throw new UnsupportedOperationException();
        }
        input = name.getPrintName();
        classString = obj.getAttribute(Symbols.CLASS);
        if(classString == Null.INSTANCE) {
          throw new EvalException("cannot get a slot (\"%s\") from an object of type \"%s\"",
              input, obj.getTypeName());
        }

        /* not there.  But since even NULL really does get stored, this
         implies that there is no slot of this name.  Or somebody
         screwed up by using attr(..) <- NULL */

        throw new EvalException("no slot of name \"%s\" for this object of class \"%s\"",
            input, classString.asString());
      }
      else if(value == MethodDispatch.pseudo_NULL) {
        value = Null.INSTANCE;
      }
      return value;
    }
  }



  public static SEXP data_part(Context context, SEXP obj) {
    SEXP val = context.evaluate(FunctionCall.newCall(MethodDispatch.s_getDataPart, obj),
        context.getSession().getSingleton(MethodDispatch.class).getMethodsNamespace());

    // Clear S4 object
    return Types.setS4Object(val, false, false);
  }



  /* the S4-style class: for dispatch required to be a single string;
   for the new class() function;
   if(!singleString) , keeps S3-style multiple classes.
   Called from the methods package, so exposed.
   */
  public static StringVector R_data_class(SEXP obj, boolean singleString) {
    SEXP value;
    SEXP klass = obj.getAttribute(Symbols.CLASS);
    int n = klass.length();
    if(n == 1 || (n > 0 && !singleString)) {
      return (StringVector) (klass);
    }
    if(n == 0) {
      SEXP dim = obj.getAttribute(Symbols.DIM);
      int nd = dim.length();
      if(nd > 0) {
        if(nd == 2) {
          return StringVector.valueOf("matrix");
        } else {
          return StringVector.valueOf("array");
        }
      } else {
        if(obj instanceof Function) {
          return StringVector.valueOf("function");
        } else if(obj instanceof DoubleVector) {
          return StringVector.valueOf("numeric");
        } else if(obj instanceof Symbol) {
          return StringVector.valueOf("name");
        }       
      }
    }
    return StringVector.valueOf(obj.getImplicitClass());
  }
  

  private static SEXP dispatchNonGeneric(Context context, String name, Environment env, SEXP fdef) {
    /* dispatch the non-generic definition of `name'.  Used to trap
         calls to standardGeneric during the loading of the methods package */
    SEXP e, value, fun;
    /* find a non-generic function */
    
    Symbol symbol = Symbol.get(name);
    for(Environment rho = env.getParent(); rho != Environment.EMPTY;
        rho = rho.getParent()) {
      fun = rho.getVariable(context, symbol);
      if(fun instanceof Closure) {
        if(!isGenericFunction(context, fun)) {
          break;
        }
      } 
      fun = Symbol.UNBOUND_VALUE;
    }
    fun = symbol;
    if(fun == Symbol.UNBOUND_VALUE) {
      throw new EvalException("unable to find a non-generic version of function \"%s\"",
         name);
    }
    
    Context cptr = context;
    /* check this is the right context */
    while (!cptr.isTopLevel()) {
      if (cptr.getType() == Type.FUNCTION) {
        if (cptr.getEnvironment() == env) {
          break;
        }
      }
      cptr = cptr.getParent();
    }

//    PROTECT(e = duplicate(R_syscall(0, cptr)));
//    SETCAR(e, fun);
    /* evaluate a call the non-generic with the same arguments and from
         the same environment as the call to the generic version */
    return context.evaluate(FunctionCall.newCall(fun, cptr.getArguments(), cptr.getCallingEnvironment()));
  }


  private static boolean isGenericFunction(@Current Context context, SEXP fun) {
    SEXP value = ((Closure) fun).getEnclosingEnvironment().getVariable(context, MethodDispatch.DOT_GENERIC);
    return value != Symbol.UNBOUND_VALUE;
  }

  
  public static String R_get_primname(PrimitiveFunction function) {
    return function.getName();
  }

  public static SEXP R_nextMethod(@Current Context context, FunctionCall matched_call, Environment ev) {
    SEXP val, this_sym, op;
    int i, nargs = matched_call.length()-1;
    boolean prim_case;
    
    /* for primitive .nextMethod's, suppress further dispatch to avoid
     * going into an infinite loop of method calls
    */
    op =  ev.findVariable(context, MethodDispatch.R_dot_nextMethod);

    if(op == Symbol.UNBOUND_VALUE) {
      throw new EvalException(
          "internal error in 'callNextMethod': '.nextMethod' was not assigned in the frame of the method call");
    }

    PairList.Node e = (PairList.Node) matched_call.newCopyBuilder().build();

    prim_case = (op instanceof PrimitiveFunction);
    if(prim_case) {
      /* retain call to primitive function, suppress method
         dispatch for it */
      // todo: do_set_prim_method(op, "suppress", R_NilValue, R_NilValue);
      throw new UnsupportedOperationException();
    } else {
      e.setValue(MethodDispatch.R_dot_nextMethod); /* call .nextMethod instead */
    }
    PairList args = e.getNext();
    /* e is a copy of a match.call, with expand.dots=FALSE.  Turn each
    <TAG>=value into <TAG> = <TAG>, except  ...= is skipped (if it
    appears) in which case ... was appended. */
    for(i=0; i<nargs; i++) {
      PairList.Node argsNode = (PairList.Node) args;
      this_sym = args.getRawTag();
      if(argsNode.getValue() != Symbol.MISSING_ARG) { /* "missing" only possible in primitive */
        argsNode.setValue(this_sym);
      }
      args = argsNode.getNext();
    }

    if(prim_case) {
      throw new UnsupportedOperationException("todo: do_set_prim_method");
//
//      try {
//        val = context.evaluate(e, ev);
//      } catch (EvalException ex) {
//        throw new EvalException("error in evaluating a 'primitive' next method: %s", ex.getMessage());
//      } finally {
//        /* reset the methods:  R_NilValue for the mlist argument
//           leaves the previous function, methods list unchanged */
//        //do_set_prim_method(op, "set", R_NilValue, R_NilValue);
//      
//      }

    } else {
      val = context.evaluate(e, ev);
    }
    return val;
  }
}
