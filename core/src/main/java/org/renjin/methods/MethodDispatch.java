package org.renjin.methods;

import java.util.HashMap;

import org.renjin.eval.Calls;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.Context.Type;
import org.renjin.primitives.Evaluation;
import org.renjin.primitives.annotations.SessionScoped;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.ListVector.Builder;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Symbols;

import com.google.common.collect.Maps;

@SessionScoped
public class MethodDispatch {
  

  public static final Symbol DOT_METHOD = Symbol.get(".Method");
  public static final Symbol DOT_METHODS = Symbol.get(".Methods");
  public static final Symbol DOT_DEFINED = Symbol.get(".defined");
  public static final Symbol DOT_TARGET = Symbol.get(".target");

  
  public static final Symbol dot_Generic =  Symbol.get(".Generic");
  public static final Symbol GENERIC = Symbol.get("generic");


  public static final Symbol R_target = Symbol.get("target");
  public static final Symbol R_defined = Symbol.get("defined");
  public static final Symbol R_nextMethod = Symbol.get("nextMethod");
  public static final Symbol R_loadMethod_name = Symbol.get("loadMethod");
  public static final Symbol R_dot_target = Symbol.get(".target");
  public static final Symbol R_dot_defined = Symbol.get(".defined");
  public static final Symbol R_dot_nextMethod = Symbol.get(".nextMethod");
  public static final Symbol R_dot_Method = Symbol.get(".Method");

  public static final Symbol s_dot_Methods = Symbol.get(".Methods");
  public static final Symbol s_skeleton = Symbol.get("skeleton");
  public static final Symbol s_expression = Symbol.get("expression");
  public static final Symbol s_function = Symbol.get("function");
  public static final Symbol s_getAllMethods = Symbol.get("getAllMethods");
  public static final Symbol s_objectsEnv = Symbol.get("objectsEnv");
  public static final Symbol s_MethodsListSelect = Symbol.get("MethodsListSelect");
  public static final Symbol s_sys_dot_frame = Symbol.get("sys.frame");
  public static final Symbol s_sys_dot_call = Symbol.get("sys.call");
  public static final Symbol s_sys_dot_function = Symbol.get("sys.function");
  public static final Symbol s_generic = Symbol.get("generic");
  public static final Symbol s_generic_dot_skeleton = Symbol.get("generic.skeleton");
  public static final Symbol s_subset_gets = Symbol.get("[<-");
  public static final Symbol s_element_gets = Symbol.get("[[<-");
  public static final Symbol s_argument = Symbol.get("argument");
  public static final Symbol s_allMethods = Symbol.get("allMethods");
  public static final Symbol s_dot_Data = Symbol.get(".Data");
  public static final Symbol s_dot_S3Class = Symbol.get(".S3Class");
  public static final Symbol s_getDataPart = Symbol.get("getDataPart");
  public static final Symbol s_setDataPart = Symbol.get("setDataPart");
  
  public static final Symbol s_xData = Symbol.get(".xData");
  public static final Symbol s_dotData = Symbol.get(".Data");

  public static final Symbol R_mtable = Symbol.get(".MTable");
  public static final Symbol R_allmtable = Symbol.get(".AllMTable");
  public static final Symbol R_sigargs = Symbol.get(".SigArgs");
  public static final Symbol R_siglength = Symbol.get(".SigLength");

  public static final StringVector s_missing = StringVector.valueOf("missing");

  /* create and preserve an object that is NOT R_NilValue, and is used
     to represent slots that are NULL (which an attribute can not
     be).  The point is not just to store NULL as a slot, but also to
     provide a check on invalid slot names (see get_slot below).

     The object has to be a symbol if we're going to check identity by
     just looking at referential equality. */
  public static final Symbol pseudo_NULL = Symbol.get("\001NULL\001");

  
  
  private boolean enabled = false;
  private HashMap<String, SEXP> extendsTable = Maps.newHashMap();
  private Environment methodsNamespace;
  private boolean tableDispatchEnabled = true;
  
  
  
  public void init(Environment environment) {
    methodsNamespace = environment;
  }
  
  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  public SEXP getExtends(String className) {
    SEXP value = extendsTable.get(className);
    if(value == null) {
      return Null.INSTANCE;
    } else {
      return value;
    }
  }
  
  public void putExtends(String className, SEXP klass) {
    extendsTable.put(className, klass);
  }

  public Environment getMethodsNamespace() {
    return methodsNamespace;
  }

  public SEXP standardGeneric(Context context, Symbol fname, Environment ev,
      SEXP fdef) {
    if(tableDispatchEnabled) {
      return R_dispatchGeneric(context, fname, ev, fdef);
    } else {
      throw new UnsupportedOperationException();
    }
  }
  
  public SEXP R_dispatchGeneric(Context context, Symbol fname, Environment ev, SEXP fdef)   {
    SEXP method;
    SEXP f;
    SEXP val=Null.INSTANCE;
    // char *buf, *bufptr;
    int   lwidth = 0;
    boolean prim_case = false;

    Environment f_env;
    if(fdef instanceof Closure) {
      f_env = ((Closure) fdef).getEnclosingEnvironment();
    } else if(fdef instanceof PrimitiveFunction) {
      fdef = R_primitive_generic(fdef);
      if(!(fdef instanceof Closure)) {
        throw new EvalException("Failed to get the generic for the primitive \"%s\"", fname.asString());
      }
      f_env = ((Closure) fdef).getEnclosingEnvironment();
      prim_case = true;        
    } else {
      throw new EvalException("Expected a generic function or a primitive for dispatch, " +
          "got an object of class \"%s\"", fdef.getImplicitClass());
    }
    SEXP mtable = f_env.getVariable(R_allmtable);
    if(mtable == Symbol.UNBOUND_VALUE) {
      do_mtable(fdef, ev); /* Should initialize the generic */        
      mtable = f_env.getVariable(R_allmtable);
    }
    SEXP sigargs = f_env.getVariable(R_sigargs);
    SEXP siglength = f_env.getVariable(R_siglength);

    if(sigargs == Symbol.UNBOUND_VALUE || siglength == Symbol.UNBOUND_VALUE ||
        mtable == Symbol.UNBOUND_VALUE) {        
      throw new EvalException("Generic \"%s\" seems not to have been initialized for table dispatch---need to have .SigArgs and .AllMtable assigned in its environment",
          fname.asString());
    }
    int nargs =  (int)siglength.asReal();
    ListVector.Builder classListBuilder = ListVector.newBuilder();
    StringVector thisClass;
    StringBuilder buf = new StringBuilder();
    
    for(int i = 0; i < nargs; i++) {
      Symbol arg_sym = sigargs.getElementAsSEXP(i);
      if(is_missing_arg(context, arg_sym, ev)) {
        thisClass = s_missing;
      } else {
        /*  get its class */
        SEXP arg;
        try {
          arg = context.evaluate(arg_sym, ev);
        } catch(EvalException e) {
          throw new EvalException(String.format("error in evaluating the argument '%s' in selecting a " +
              "method for function '%s'",
              arg_sym.getPrintName(), fname.asString()), e);
        }
        thisClass = Methods.R_data_class(arg, true);
      }
      classListBuilder.set(i, thisClass);
      if(i > 0) {
        buf.append("#");
      }
      buf.append(thisClass.asString());
    }
    ListVector classes = classListBuilder.build();
    method = ((Environment)mtable).getVariable(buf.toString());
    if(method == Symbol.UNBOUND_VALUE) {
      method = do_inherited_table(context, classes, fdef, mtable, (Environment)ev);
    }
    /* the rest of this is identical to R_standardGeneric;
         hence the f=method to remind us  */
    f = method;
    if(f.isObject())
      f = R_loadMethod(context, f, fname.getPrintName(), ev);

    if(f instanceof Closure) {
      val = R_execMethod(context, (Closure)f, ev);
    } else if(f instanceof PrimitiveFunction) {
      /* primitives  can't be methods; they arise only as the
      default method when a primitive is made generic.  In this
      case, return a special marker telling the C code to go on
      with the internal computations. */
      //val = R_deferred_default_method();
      throw new UnsupportedOperationException();
    } else {
      throw new EvalException("invalid object (non-function) used as method");

    }
    return val;  
  }



  /* C version of the standardGeneric R function. */
  public SEXP R_standardGeneric(Context context, Symbol fsym, Environment ev, SEXP fdef) {
    String fname = fsym.getPrintName();
    Environment f_env = context.getGlobalEnvironment().getBaseEnvironment();
    SEXP mlist = Null.INSTANCE;
    SEXP f;
    SEXP val = Null.INSTANCE;
    int nprotect = 0;

    //    if(!initialized)
    //      R_initMethodDispatch(NULL);

    if(fdef instanceof Closure) {
      f_env = ((Closure) fdef).getEnclosingEnvironment();
      mlist = f_env.getVariable(".Methods");
      if(mlist == Symbol.UNBOUND_VALUE) {
        mlist = Null.INSTANCE;
      }
    } else if(fdef instanceof PrimitiveFunction ) {
      f_env = context.getBaseEnvironment();
      //mlist = R_primitive_methods((PrimitiveFunction)fdef);
      throw new UnsupportedOperationException();
    } else {
      throw new EvalException("invalid generic function object for method selection for function '%s': expected a function or a primitive, got an object of class \"%s\"", 
          fsym.getPrintName(), fdef.getAttributes().getClassVector());
    }
    if(mlist instanceof Null || mlist instanceof Closure || mlist instanceof PrimitiveFunction) {
      f = mlist;
    } else {
      //f = do_dispatch(fname, ev, mlist, TRUE, TRUE);
      throw new UnsupportedOperationException();
    }
    if(f == Null.INSTANCE) {
      SEXP value = R_S_MethodsListSelect(context, StringArrayVector.valueOf(fname), ev, mlist, f_env);
      if(value == Null.INSTANCE) {
        throw new EvalException("no direct or inherited method for function '%s' for this call",
            fname);
      }
      mlist = value;
      /* now look again.  This time the necessary method should
       have been inserted in the MethodsList object */
      f = do_dispatch(context, fname, (Environment)ev, mlist, false, true);
    }
    //    /* loadMethod methods */
    if(f.isObject()) {
      f = R_loadMethod(context, f, fsym.getPrintName(), ev);
    }
    if(f instanceof Closure) {
      return R_execMethod(context, (Closure)f, ev);  
    } else if(f instanceof PrimitiveFunction) {
      /* primitives  can't be methods; they arise only as the
      default method when a primitive is made generic.  In this
      case, return a special marker telling the C code to go on
      with the internal computations. */
      // val = R_deferred_default_method();
      throw new UnsupportedOperationException();
    } else {
      throw new EvalException("invalid object (non-function) used as method");
    }
    //    return val;
  }

  private SEXP R_S_MethodsListSelect(Context context, SEXP fname, SEXP ev, SEXP mlist, SEXP f_env) {
    PairList.Builder args = new PairList.Builder();
    args.add(fname);
    args.add(ev);
    args.add(mlist);
    if(f_env != Null.INSTANCE) {
      args.add(f_env);
    }

    try {
      return context.evaluate(new FunctionCall(s_MethodsListSelect, args.build()), methodsNamespace);
    } catch(EvalException e) {
      throw new EvalException(String.format("S language method selection got an error when called from" +
          " internal dispatch for function '%s'", fname), e);
    }
  }
  
  private static SEXP R_loadMethod(Context context, SEXP def, String fname, Environment ev) {

    /* since this is called every time a method is dispatched with a
       definition that has a class, it should be as efficient as
       possible => we build in knowledge of the standard
       MethodDefinition and MethodWithNext slots.  If these (+ the
       class slot) don't account for all the attributes, regular
       dispatch is done. */
    int found = 1; /* we "know" the class attribute is there */

    found++; // we also have our fake __S4_BIt for renjin

    PairList attrib = def.getAttributes().asPairList();
    for(PairList.Node s : attrib.nodes()) {
      SEXP t = s.getTag();
      if(t == R_target) {
        ev.setVariable(R_dot_target, s.getValue());
        found++;
      }
      else if(t == R_defined) {
        ev.setVariable(R_dot_defined, s.getValue());
        found++;
      }
      else if(t == R_nextMethod)  {
        ev.setVariable(R_dot_nextMethod, s.getValue());
        found++;
      }
      else if(t == Symbols.SOURCE)  {
        /* ignore */ found++;
      }
    }
    ev.setVariable(R_dot_Method, def);

    /* this shouldn't be needed but check the generic being
       "loadMethod", which would produce a recursive loop */
    if(fname.equals("loadMethod")) {
      return def;
    }
    if(found < attrib.length()) {
      FunctionCall call = FunctionCall.newCall(R_loadMethod_name, def, StringArrayVector.valueOf(fname), ev);
      return context.evaluate(call, ev);

      //      SEXP e, val;
      //      PROTECT(e = allocVector(LANGSXP, 4));
      //      SETCAR(e, R_loadMethod_name); val = CDR(e);
      //      SETCAR(val, def); val = CDR(val);
      //      SETCAR(val, fname); val = CDR(val);
      //      SETCAR(val, ev);
      //      val = eval(e, ev);
      //      return val;
    } else {
      return def;
    }
  }


  private SEXP do_dispatch(Context context, String fname, SEXP ev, SEXP mlist, boolean firstTry, boolean evalArgs) {
    String klass;
    SEXP arg_slot;
    Symbol arg_sym;
    SEXP method, value = Null.INSTANCE;
    int nprotect = 0;
    /* check for dispatch turned off inside MethodsListSelect */
    if(mlist instanceof Function) {
      return mlist;
    }
    arg_slot = Methods.R_do_slot(context, mlist, s_argument);
    if(arg_slot == Null.INSTANCE) {
      throw new EvalException("object of class \"%s\" used as methods list for function '%s' " +
          "( no 'argument' slot)",
          mlist.toString(), fname);
    }
    if(arg_slot instanceof Symbol) {
      arg_sym = (Symbol) arg_slot;
    } else {
      /* shouldn't happen, since argument in class MethodsList has class
       "name" */
      arg_sym = Symbol.get(arg_slot.asString());
    }
    //    if(arg_sym == Symbols.ELLIPSES || DDVAL(arg_sym) > 0)
    //  error(_("(in selecting a method for function '%s') '...' and related variables cannot be used for methods dispatch"),
    //        CHAR(asChar(fname)));
    //    if(TYPEOF(ev) != ENVSXP) {
    //  error(_("(in selecting a method for function '%s') the 'environment' argument for dispatch must be an R environment; got an object of class \"%s\""),
    //      CHAR(asChar(fname)), class_string(ev));
    //  return(R_NilValue); /* -Wall */
    //    }
    /* find the symbol in the frame, but don't use eval, yet, because
       missing arguments are ok & don't require defaults */
    if(evalArgs) {
      if(is_missing_arg(context, arg_sym, (Environment)ev)) {
        klass = "missing";
      } else {
        /*  get its class */
        SEXP arg, class_obj; 
        try {
          arg = context.evaluate(arg_sym, (Environment)ev);
        } catch(EvalException e) {
          throw new EvalException(String.format("error in evaluating the argument '%s' in selecting a method for function '%s'",
              arg_sym.getPrintName(), fname), e);
        }

        class_obj = Methods.R_data_class(arg, true);
        klass = class_obj.asString();
      }
    } else {
      /* the arg contains the class as a string */
      SEXP arg; int check_err;
      try {
        arg = context.evaluate(arg_sym, (Environment)ev);
      } catch(Exception e) {
        throw new EvalException(String.format("error in evaluating the argument '%s' in selecting a method for function '%s'",
            arg_sym.getPrintName(), fname));
      }  
      klass = arg.asString();
    }
    method = R_find_method(mlist, klass, fname);
    if(method == Null.INSTANCE) {
      if(!firstTry) {
        throw new EvalException("no matching method for function '%s' (argument '%s', with class \"%s\")",
            fname, arg_sym.getPrintName(), klass);
      }
    }
    if(value == Symbol.MISSING_ARG) {/* the check put in before calling
        function  MethodListSelect in R */
      throw new EvalException("recursive use of function '%s' in method selection, with no default method",
          fname);
    }
    if(!(method instanceof Function)) {
      /* assumes method is a methods list itself.  */
      /* call do_dispatch recursively.  Note the NULL for fname; this is
     passed on to the S language search function for inherited
     methods, to indicate a recursive call, not one to be stored in
     the methods metadata */
      method = do_dispatch(context, null, ev, method, firstTry, evalArgs);
    }
    return method;
  }


  private SEXP R_find_method(SEXP mlist, String klass, String fname) {
    throw new UnsupportedOperationException();
  }


  private static SEXP R_primitive_generic(SEXP fdef) {
    throw new UnsupportedOperationException();
  }


  private static SEXP do_inherited_table(StringVector classes, SEXP fdef,
      SEXP mtable, Environment ev) {
    throw new UnsupportedOperationException();
  }


  private static void do_mtable(SEXP fdef, Environment ev) {
    throw new UnsupportedOperationException();
  }


  private static boolean is_missing_arg(Context context, Symbol arg_sym, Environment ev) {
    return Evaluation.missing(context, ev, arg_sym);
  }


  public static SEXP R_execMethod(Context context, Closure op, Environment rho)  {

    /* create a new environment frame enclosed by the lexical
       environment of the method */
    Environment newrho = Environment.createChildEnvironment(op.getEnclosingEnvironment());

    /* copy the bindings for the formal environment from the top frame
       of the internal environment of the generic call to the new
       frame.  need to make sure missingness information is preserved
       and the environments for any default expression promises are
       set to the new environment.  should move this to envir.c where
       it can be done more efficiently. */
    for(PairList.Node next : op.getFormals().nodes()) {
      //R_varloc_t loc;
      //int missing;
      // TODO(alex): redo missingness handling
      //      loc = R_findVarLocInFrame(rho,symbol);
      //      if(loc == NULL)
      //       throw new EvalException("could not find symbol \"%s\" in environment of the generic function"),
      //            CHAR(PRINTNAME(symbol)));
      //      missing = R_GetVarLocMISSING(loc);
      //      val = R_GetVarLocValue(loc);

      if(!next.hasTag()) {
        throw new EvalException("closure formal has no tag! op = " + op);
      }
      
      Symbol symbol = next.getTag();
      SEXP val = rho.findVariable(symbol);
      if(val == Symbol.UNBOUND_VALUE) {
        throw new EvalException("could not find symbol \"%s\" in the environment of the generic function", symbol.getPrintName());
      }

      //      SET_FRAME(newrho, CONS(val, FRAME(newrho)));
      //      SET_TAG(FRAME(newrho), symbol);

      newrho.setVariable(symbol, val);

      //      if (missing) {
      //        SET_MISSING(FRAME(newrho), missing);
      //        if (TYPEOF(val) == PROMSXP && PRENV(val) == rho) {
      //          SEXP deflt;
      //          SET_PRENV(val, newrho);
      //          /* find the symbol in the method, copy its expression
      //           * to the promise */
      //          for(deflt = CAR(op); deflt != R_NilValue; deflt = CDR(deflt)) {
      //            if(TAG(deflt) == symbol)
      //              break;
      //          }
      //          if(deflt == R_NilValue)
      //            error(_("symbol \"%s\" not in environment of method"),
      //                CHAR(PRINTNAME(symbol)));
      //          SET_PRCODE(val, CAR(deflt));
      //        }
      //      }
    }

    /* copy the bindings of the spacial dispatch variables in the top
       frame of the generic call to the new frame */
    newrho.setVariable(DOT_DEFINED, rho.getVariable(DOT_DEFINED));
    newrho.setVariable(DOT_METHOD, rho.getVariable(DOT_METHOD));
    newrho.setVariable(DOT_TARGET, rho.getVariable(DOT_TARGET));

    /* copy the bindings for .Generic and .Methods.  We know (I think)
       that they are in the second frame, so we could use that. */
    newrho.setVariable(Symbols.GENERIC, newrho.getVariable(".Generic"));
    newrho.setVariable(DOT_METHODS, newrho.getVariable(DOT_METHODS));

    /* Find the calling context.  Should be R_GlobalContext unless
       profiling has inserted a CTXT_BUILTIN frame. */
    Context cptr = context;
    //    cptr = R_GlobalContext;
    //    if (cptr->callflag & CTXT_BUILTIN)
    //      cptr = cptr->nextcontext;

    /* The calling environment should either be the environment of the
       generic, rho, or the environment of the caller of the generic,
       the current sysparent. */
    Environment callerenv = cptr.getCallingEnvironment(); /* or rho? */

    /* get the rest of the stuff we need from the current context,
       execute the method, and return the result */
    FunctionCall call = cptr.getCall();
    PairList arglist = cptr.getArguments();
    SEXP val = R_execClosure(context, call, op, arglist, callerenv, newrho);
    return val;
  }


  private static SEXP R_execClosure(Context context, FunctionCall call, Closure op, PairList arglist,
      Environment callerenv, Environment newrho) {
    return Calls.applyClosure(op, context, callerenv, call, arglist, newrho, new HashFrame());
  }
  
  
  private  SEXP do_inherited_table(Context context, SEXP class_objs, SEXP fdef, SEXP mtable, Environment ev) {
    SEXP fun = methodsNamespace.findFunction(context, Symbol.get(".InheritForDispatch"));
    
    return context.evaluate(FunctionCall.newCall(fun, class_objs, fdef, mtable), ev);
  }
//
//  static SEXP do_mtable(SEXP fdef, SEXP ev)
//  {
//      static SEXP dotFind = NULL, f; SEXP  e, ee;
//      if(dotFind == NULL) {
//    dotFind = install(".getMethodsTable");
//    f = findFun(dotFind, R_MethodsNamespace);
//    R_PreserveObject(f);
//      }
//      PROTECT(e = allocVector(LANGSXP, 2));
//      SETCAR(e, f); ee = CDR(e);
//      SETCAR(ee, fdef);
//      ee = eval(e, ev);
//      UNPROTECT(1);
//      return ee;
//  }



}
