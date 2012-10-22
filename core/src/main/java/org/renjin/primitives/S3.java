package org.renjin.primitives;

import com.google.common.collect.Lists;
import org.renjin.eval.*;
import org.renjin.primitives.annotations.ArgumentList;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.*;

import java.util.Collections;
import java.util.List;

import static org.renjin.util.CDefines.*;

/**
 * Primitives used in the implementation of the S3 object system
 */
public class S3 {


  public static SEXP UseMethod(@Current Context context, String genericMethodName) {
    /*
     * When object is not provided, it defaults to the first argument
     * of the calling function
     */
    if(context.getArguments().length() == 0) {
      return UseMethod(context, genericMethodName, Null.INSTANCE);

    } else {
      SEXP object = context.evaluate( context.getArguments().getElementAsSEXP(0),
              context.getParent().getEnvironment());

      return UseMethod(context, genericMethodName, object);
    }
  }

  public static SEXP UseMethod(@Current Context context, String genericMethodName, SEXP object) {


    return Resolver
              .start(context, genericMethodName, object)
              .withDefinitionEnvironment(context.getClosure().getEnclosingEnvironment())
              .next()
              .apply(context, context.getEnvironment());
  }

  @Primitive
 public static SEXP NextMethod(@Current Context context, @Current Environment env,
      SEXP generic, SEXP object, @ArgumentList ListVector extraArgs) {

    return Resolver
           .resume(context)
           .withGenericArgument(generic)
           .withObjectArgument(object)
           .next()
           .applyNext(context, context.getEnvironment());
  }

//
//
////    char buf[512], b[512], bb[512], tbuf[10];
////    const char *sb, *sg, *sk;
////    SEXP ans, s, t, klass, method, matchedarg, generic, nextfun;
////    SEXP sysp, m, formals, actuals, tmp, newcall;
////    SEXP a, group, basename;
////    SEXP callenv, defenv;
////    RCNTXT *cptr;
////    int i, j, cftmp;
//
////    cptr = R_GlobalContext;
////    cftmp = cptr->callflag;
////    cptr->callflag = CTXT_GENERIC;
//
//    /* get the env NextMethod was called from */
////    sysp = R_GlobalContext->sysparent;
////    while (cptr != NULL) {
////        if (cptr->callflag & CTXT_FUNCTION && cptr->cloenv == sysp) break;
////        cptr = cptr->nextcontext;
////    }
////    if (cptr == NULL)
////        error(_("'NextMethod' called from outside a function"));
//
//    Context sysp = context.getParent();
//
//    /* eg get("print.ts")(1) */
////    if (TYPEOF(CAR(cptr->call)) == LANGSXP)
////       error(_("'NextMethod' called from an anonymous function"));
//
//    /* Find dispatching environments. Promises shouldn't occur, but
//       check to be on the safe side.  If the variables are not in the
//       environment (the method was called outside a method dispatch)
//       then chose reasonable defaults. */
//    SEXP callenv = env.getVariable(".GenericCallEnv");
//
//    /*if (TYPEOF(callenv) == PROMSXP)
//        callenv = eval(callenv, R_BaseEnv);
//    else */
//    if (callenv == Symbol.UNBOUND_VALUE) {
//      callenv = env;
//    }
//
//    SEXP defenv = env.getVariable(".GenericDefEnv");
//    /*if (TYPEOF(defenv) == PROMSXP) defenv = eval(defenv, R_BaseEnv);
//    else */
//    if (defenv == Symbol.UNBOUND_VALUE) {
//      defenv = context.getGlobalEnvironment();
//    }
//
//    /* set up the arglist */
//    SEXP s = lookupMethod(context, (Symbol)sysp.getCall().getFunction(), (Environment)env,
//        (Environment)callenv, (Environment)defenv);
//
//    if(s == Symbol.UNBOUND_VALUE) {
//      throw new EvalException("no calling generic was found: was a method called directly?");
//    }
//    if (!(s instanceof Closure)){ /* R_LookupMethod looked for a function */
//      throw new EvalException("function' is not a function, but of type %s", s.getTypeName());
//    }
//    /* get formals and actuals; attach the names of the formals to
//       the actuals, expanding any ... that occurs */
//    PairList formals = ((Closure) s).getFormals();
//    PairList actuals = context.getParent().getArguments();
//    actuals = Calls.matchArguments(formals, actuals);
//    actuals = expandDotDotDot(actuals);
//
//
//    // strip out arguments with missing values. These named arguments may not
//    // be defined in calls further down the chain and we don't want to "generate"
//    // new arguments
//    PairList.Builder noMissing = new PairList.Builder();
//    for(PairList.Node node : actuals.nodes()) {
//      if(node.getValue() != Symbol.MISSING_ARG) {
//        noMissing.add(node.getRawTag(), node.getValue());
//      }
//    }
//    actuals = noMissing.build();
//
////    /* we can't duplicate because it would force the promises */
////    /* so we do our own duplication of the promargs */
////
//
//    PairList.Builder updatedArgs = new PairList.Builder();
//    for(PairList.Node actual : actuals.nodes()) {
//      SEXP temp;
//      if(actual.hasTag()) {
//        // an argument may not have a tag even at this point if was
//        // part of a ... expansion
//        temp = context.getParent().getEnvironment().findVariable(actual.getTag());
//      } else {
//        temp = Symbol.UNBOUND_VALUE;
//      }
//      if(temp != Symbol.UNBOUND_VALUE &&
//          !isDefaultArgValue(temp) &&
//          temp != Symbol.MISSING_ARG) {
//        updatedArgs.add(actual.getRawTag(), Promise.repromise(context.getParent().getEnvironment(), temp));
//      } else {
//        updatedArgs.add(actual.getRawTag(), actual.getValue());
//      }
//    }
//    actuals = updatedArgs.build();
//
//
//
//
////    PROTECT(matchedarg = allocList(length(cptr->promargs)));
////    for (t = matchedarg, s = cptr->promargs; t != R_NilValue;
////         s = CDR(s), t = CDR(t)) {
////        SETCAR(t, CAR(s));
////        SET_TAG(t, TAG(s));
////    }
////    for (t = matchedarg; t != R_NilValue; t = CDR(t)) {
////        for (m = actuals; m != R_NilValue; m = CDR(m))
////            if (CAR(m) == CAR(t))  {
////                if (CAR(m) == R_MissingArg) {
////                    tmp = findVarInFrame3(cptr->cloenv, TAG(m), TRUE);
////                    if (tmp == R_MissingArg) break;
////                }
////                SETCAR(t, mkPROMISE(TAG(m), cptr->cloenv));
////                break;
////           }
////    }
////    /*
////      Now see if there were any other arguments passed in
////      Currently we seem to only allow named args to change
////      or to be added, this is at variance with p. 470 of the
////      White Book
////    */
////
////    s = CADDR(args); /* this is ... and we need to see if it's bound */
////    if (s == R_DotsSymbol) {
////        t = findVarInFrame3(env, s, TRUE);
////        if (t != R_NilValue && t != R_MissingArg) {
////            SET_TYPEOF(t, LISTSXP); /* a safe mutation */
////            s = matchmethargs(matchedarg, t);
////            UNPROTECT(1);
////            PROTECT(matchedarg = s);
////            newcall = fixcall(newcall, matchedarg);
////        }
////    }
////    else
////        error(_("wrong argument ..."));
//
//    /*
//      .Class is used to determine the next method; if it doesn't
//      exist the first argument to the current method is used
//      the second argument to NextMethod is another option but
//      isn't currently used).
//    */
//    SEXP klass = sysp.getEnvironment().getVariable(".Class");
//
//
//    if (klass == Symbol.UNBOUND_VALUE) {
//        s = GetObject(context.getParent());
//        if (!s.isObject()) {
//          throw new EvalException("object not specified");
//        }
//        klass = s.getAttributes().getClassVector();
////      throw new EvalException(".Class attribute not present");
//    }
//
//    /* the generic comes from either the sysparent or it's named */
//    if(generic == Null.INSTANCE) {
//      generic = sysp.getEnvironment().getVariable(".Generic");
//    }
//    if (generic == Symbol.UNBOUND_VALUE) {
//      //  generic = eval(CAR(args), env);
//      throw new EvalException(".Generic not present");
//    }
//    if( generic == Null.INSTANCE ) {
//      throw new EvalException("generic function not specified");
//    }
//
//    if ( !(generic instanceof StringVector) || generic.length() > 1) {
//      throw new EvalException("invalid generic argument to NextMethod");
//    }
//
////    if (CHAR(STRING_ELT(generic, 0))[0] == '\0')
////        error(_("generic function not specified"));
//
//    /* determine whether we are in a Group dispatch */
//
//    SEXP groupExp = context.getParent().getEnvironment().getVariable(".Group");
//    String group;
//    if (groupExp == Symbol.UNBOUND_VALUE) {
//      group = "";
//    } else {
//      group = ((StringVector)groupExp).getElementAsString(0);
//    }
//
////    if (!isString(group) || length(group) > 1)
////        error(_("invalid 'group' argument found in NextMethod"));
//
//    /* determine the root: either the group or the generic will be it */
//
//    String basename;
//    if (group.isEmpty()) {
//      basename = ((StringVector)generic).getElementAsString(0);
//    } else {
//      basename = group;
//    }
//
//    SEXP nextfun = Null.INSTANCE;
//
//    /*
//       Find the method currently being invoked and jump over the current call
//       If t is R_UnboundValue then we called the current method directly
//    */
//
//    SEXP dotMethod = context.getParent().getEnvironment().getVariable(".Method");
//
//    String b=null;
//    if( dotMethod != Symbol.UNBOUND_VALUE) {
//      if( !(dotMethod instanceof StringVector) ) {
//        throw new EvalException("wrong value for .Method");
//      }
//      for(String ss : (StringVector)dotMethod) {
//        if(!ss.isEmpty()) {
//          b = ss;
//          break;
//        }
//      }
////        for(i = 0; i < length(method); i++) {
////            ss = translateChar(STRING_ELT(method, i));
////            if(strlen(ss) >= 512)
////                error(_("method name too long in '%s'"), ss);
////            sprintf(b, "%s", ss);
////            if(strlen(b)) break;
////        }
////        /* for binary operators check that the second argument's method
////           is the same or absent */
////        for(j = i; j < length(method); j++){
////            const char *ss = translateChar(STRING_ELT(method, j));
////            if(strlen(ss) >= 512)
////                error(_("method name too long in '%s'"), ss);
////          sprintf(bb, "%s", ss);
////          if (strlen(bb) && strcmp(b,bb))
////              warning(_("Incompatible methods ignored"));
////        }
//    }
//    else {
////        if(strlen(CHAR(PRINTNAME(CAR(cptr->call)))) >= 512)
////           error(_("call name too long in '%s'"),
////                 CHAR(PRINTNAME(CAR(cptr->call))));
////        sprintf(b, "%s", CHAR(PRINTNAME(CAR(cptr->call))));
//      throw new EvalException(".Method is not set, not sure what to do");
//    }
//
//    String sb = basename;
//    String buf = null;
//    int j;
//    for (j = 0; j < klass.length(); j++) {
//      String sk = ((StringVector)klass).getElementAsString(j);
//      buf = sb + "." + sk;
//      if (!buf.equals(b)) {
//        break;
//      }
//    }
//
//    if (buf != null && buf.equals(b)) { /* we found a match and start from there */
//      j++;
//    } else {
//      j = 0;  /*no match so start with the first element of .Class */
//    }
//    /* we need the value of i on exit from the for loop to figure out
//           how many classes to drop. */
//
//    String sg = ((StringVector)generic).getElementAsString(0);
//    int i;
//    for (i = j ; i < klass.length(); i++) {
//      String sk = ((StringVector)klass).getElementAsString(0);
//      buf = sg + "." + sk;
//      nextfun = lookupMethod(context, Symbol.get(buf), env, (Environment)callenv, (Environment)defenv);
//      if (nextfun instanceof Function) {
//        break;
//      }
//      if (groupExp != Symbol.UNBOUND_VALUE) {
//        /* if not Generic.foo, look for Group.foo */
//        buf = sb + "." + sk;
//        nextfun = lookupMethod(context, Symbol.get(buf), env, (Environment)callenv, (Environment)defenv);
//        if(nextfun instanceof Function) {
//          break;
//        }
//      }
//      if (nextfun instanceof Function) {
//        break;
//      }
//    }
//    if (!(nextfun instanceof Function)) {
//      buf = sg + ".default";
//      nextfun = lookupMethod(context, Symbol.get(buf), env, (Environment)callenv, (Environment)defenv);
//      /* If there is no default method, try the generic itself,
//        provided it is primitive or a wrapper for a .Internal
//        function of the same name.
//      */
//      if (!(nextfun instanceof Function)) {
//        Symbol t = Symbol.get(sg);
//        nextfun = env.findFunction(context, t);
//        if ( nextfun instanceof Promise) {
//          nextfun = context.evaluate( nextfun, env);
//        }
//        if (!(nextfun instanceof Function)) {
//          throw new EvalException("no method to invoke");
//        }
//        if (nextfun instanceof Closure) {
//          PrimitiveFunction internal = Primitives.getInternal(t);
//          if (internal != null)
//            nextfun = internal;
//          else {
//            throw new EvalException("no method to invoke");
//          }
//        }
//      }
//    }
//
//    StringArrayVector.Builder newklass = new StringArrayVector.Builder();
//    for(j=0;j< newklass.length();++j) {
//      newklass.add(((StringVector)klass).getElementAsString(i++));
//    }
//    newklass.setAttribute("previous", klass);
//
//   // PROTECT(m = allocSExp(ENVSXP));
//    Frame m = new HashFrame();
//    m.setVariable(Symbol.get(".Class"), newklass.build());
//
//
//    /* It is possible that if a method was called directly that
//        'method' is unset */
////    if (dotMethod != Symbol.UNBOUND_VALUE) {
////      /* for Ops we need `method' to be a vector */
////
////      PROTECT(method = duplicate(dotMethod));
////      for(j = 0; j < length(method); j++) {
////        if (strlen(CHAR(STRING_ELT(dotMethod,j))))
////          SET_STRING_ELT(dotMethod, j,  mkChar(buf));
////      }
////    } else {
////    dotMethod = Symbol.get(buf);
////    }
//    m.setVariable(Symbol.get(".Method"), StringVector.valueOf(buf));
////    defineVar(install(".GenericCallEnv"), callenv, m);
////    defineVar(install(".GenericDefEnv"), defenv, m);
//
//
//    m.setVariable(Symbol.get(".Generic"), generic);
//    m.setVariable(Symbol.get(".Group"), groupExp);
//
//    FunctionCall newcall = new FunctionCall(Symbol.get(buf), actuals);
//
//
//    if(nextfun instanceof Closure) {
//      return Calls.applyClosure((Closure)nextfun, context, newcall, actuals, env, m);
//    } else {
//      return ((Function)nextfun).apply(context, env, newcall, actuals);
//    }

//
//
//    SETCAR(newcall, method);
//    ans = applyMethod(newcall, nextfun, matchedarg, env, m);
//    UNPROTECT(10);
//    return(ans);
//    throw new EvalException("this and no further");
 // }

  public static boolean isDefaultArgValue(SEXP temp) {
    return temp instanceof Promise;
  }

  public static PairList expandDotDotDot(PairList actuals) {
    PairList.Builder result = new PairList.Builder();
    for(PairList.Node node : actuals.nodes()) {
      if(node.getValue() instanceof PromisePairList) {
        for(PairList.Node dotNode : ((PromisePairList) node.getValue()).nodes()) {
          result.add(dotNode.getRawTag(), dotNode.getValue());
        }
      } else {
        result.add(node.getRawTag(), node.getValue());
      }
    }
    return result.build();
  }

  /* gr needs to be protected on return from this function */
  public static FindResult findmethod(Context context, Vector Class, String group, String generic, Environment rho) {
    int len, whichclass;
    FindResult result = new FindResult();

    len = Class.length();

    /* Need to interleave looking for group and generic methods
     e.g. if class(x) is c("foo", "bar)" then x > 3 should invoke
     "Ops.foo" rather than ">.bar"
     */
    for (whichclass = 0; whichclass < len; whichclass++) {
      String ss = Class.getElementAsString(whichclass);
      result.buf = generic + "." + ss;

      result.meth = Symbol.get(result.buf);
      result.sxp = lookupMethod(context, result.meth, rho, rho, rho.getBaseEnvironment());
      if (result.sxp instanceof Function) {
        result.gr = StringVector.valueOf("");
        break;
      }
      result.buf = group + "." + ss;
      result.meth = Symbol.get(result.buf);
      result.sxp = lookupMethod(context, result.meth, rho, rho, rho.getBaseEnvironment());
      if (result.sxp instanceof Function) {
        result.gr = StringVector.valueOf(group);
        break;
      }
    }
    result.which = whichclass;
    return result;
  }


  public static SEXP lookupMethod(Context context, Symbol method, SEXP rho, Environment callrho, Environment defrho)
  {
//
//    if (callrho == Null.INSTANCE) {
//      throw new EvalException("use of NULL environment is defunct");
//    } else if( callrho instanceof Environment) {
//      throw new EvalException("bad generic call environment");
//    }
//    if (defrho == Null.INSTANCE) {
//      throw new EvalException("use of NULL environment is defunct");
//    } else if(!(defrho instanceof Environment)) {
//      throw new EvalException("bad generic definition environment");
//    }
//    if (defrho == R_BaseEnv)
//      defrho = R_BaseNamespace;

    /* This evaluates promises */
    SEXP val = callrho.findVariable(context, method, CollectionUtils.IS_FUNCTION, true);
    if (val instanceof Function) {
      return val;
    } else {
//      /* We assume here that no one registered a non-function */
//      SEXP table = findVarInFrame3(defrho,
//          install(".__S3MethodsTable__."),
//          TRUE);
//      if (TYPEOF(table) == PROMSXP) table = eval(table, R_BaseEnv);
//      if (TYPEOF(table) == ENVSXP) {
//        val = findVarInFrame3(table, method, TRUE);
//        if (TYPEOF(val) == PROMSXP) val = eval(val, rho);
//        if (val != R_UnboundValue) return val;
//      }
      return R_UnboundValue;
    }
  }

  /**
   * Computes the class list used for normal S3 Dispatch. Note that this
   * is different than the class() function
   */
  public static StringVector computeDataClasses(Context context, SEXP exp) {

    /*
     * Make sure we're dealing with the evaluated expression
     */
    exp = exp.force(context);

    SEXP classAttribute = exp.getAttribute(Symbols.CLASS);

    if(classAttribute.length() > 0) {
      /*
       * S3 Class has been explicitly defined
       */
      return (StringVector)classAttribute;
    } else {
      /*
       * Compute implicit class based on DIM attribute and type
       */
      StringArrayVector.Builder dataClass = new StringArrayVector.Builder();
      SEXP dim = exp.getAttribute(Symbols.DIM);
      if(dim.length() == 2) {
        dataClass.add("matrix");
      } else if(dim.length() == 1) {
        dataClass.add("array");
      }
      if(exp instanceof IntVector || exp instanceof DoubleVector) {
        dataClass.add(exp.getTypeName());
        dataClass.add("numeric");
      } else {
         dataClass.add(exp.getImplicitClass());
      }
      return dataClass.build();
    }
  }

  public static SEXP dispatchGroup(String group, FunctionCall call, String opName, PairList args, Context context, Environment rho) {
    int i, j, nargs;

    if(call.getFunction() instanceof Symbol && ((Symbol) call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }



    boolean useS4 = true, isOps = false;

    /* pre-test to avoid string computations when there is nothing to
     dispatch on because either there is only one argument and it
     isn't an object or there are two or more arguments but neither
     of the first two is an object -- both of these cases would be
     rejected by the code following the string examination code
     below */
//	if (args != R_NilValue && !isObject(CAR(args)) && (CDR(args) == R_NilValue
//			|| !isObject(CADR(args))))
//		return 0;

    //isOps = strcmp(group, "Ops") == 0;
    isOps = group.equals("Ops");

//    /* try for formal method */
//    if (length(args) == 1 && !IS_S4_OBJECT(CAR(args)))
//      useS4 = FALSE;
//    if (length(args) == 2 && !IS_S4_OBJECT(CAR(args)) && !IS_S4_OBJECT(CADR(
//        args)))
//      useS4 = FALSE;
//    if (useS4) {
//      /* Remove argument names to ensure positional matching */
//      if (isOps)
//        for (s = args; s != R_NilValue; s = CDR(s))
//          SET_TAG(s, R_NilValue);
//      if (R_has_methods(op) && (value = R_possible_dispatch(call, op, args,
//          rho, FALSE))) {
//        *ans = value;
//        return 1;
//      }
//      /* else go on to look for S3 methods */
//    }

    /* check whether we are processing the default method */
    if (isSymbol(CAR(call))) {
//      if (strlen(CHAR(PRINTNAME(CAR(call)))) >= 512)
//        error(_("call name too long in '%s'"), CHAR(PRINTNAME(CAR(call))));

      String symbolName = ((Symbol)CAR(call)).getPrintName();
      //sprintf(lbuf, "%s", CHAR(PRINTNAME(CAR(call))) );
      int pt = symbolName.indexOf('.');
      pt = symbolName.indexOf('.', pt);

      if (pt != -1 && symbolName.substring(pt).equals("default")) {
        return null;
      }
    }

    if (isOps)
      nargs = args.length();
    else
      nargs = 1;

//    if (nargs == 1 && !isObject(CAR(args)))
//      return 0;
//
//    if (!isObject(CAR(args)) && !isObject(CADR(args)))
//      return 0;

    String generic = opName;

//    lclass = IS_S4_OBJECT(CAR(args)) ? R_data_class2(CAR(args)) : getAttrib(
//        CAR(args), R_ClassSymbol);

    Vector lclass = computeDataClasses(context, CAR(args));
    Vector rclass;
    if (nargs == 2) {
//      rclass = IS_S4_OBJECT(CADR(args)) ? R_data_class2(CADR(args))
//          : getAttrib(CADR(args), R_ClassSymbol);
      rclass = computeDataClasses(context, args.getElementAsSEXP(1));
    } else {
      rclass = Null.INSTANCE;
    }

//
//    lsxp = R_NilValue;
//    lgr = R_NilValue;
//    lmeth = R_NilValue;
//    rsxp = R_NilValue;
//    rgr = R_NilValue;
//    rmeth = R_NilValue;

    FindResult left = findmethod(context, lclass, group, generic, rho);
//    if (fm.sxp instanceof Function && IS_S4_OBJECT(CAR(args)) && lwhich > 0
//        && isBasicClass(translateChar(STRING_ELT(lclass, lwhich)))) {
//      /* This and the similar test below implement the strategy
//        for S3 methods selected for S4 objects.  See ?Methods */
//      value = CAR(args);
//      if (NAMED(value))
//        SET_NAMED(value, 2);
//      value = R_getS4DataSlot(value, S4SXP); /* the .S3Class obj. or NULL*/
//      if (value != R_NilValue) /* use the S3Part as the inherited object */
//        SETCAR(args, value);
//    }

    FindResult right;
    if (nargs == 2) {
      right = findmethod(context, rclass, group, generic, rho);
    } else {
      right = new FindResult();
      right.which = 0;
    }

//    if (isFunction(rsxp) && IS_S4_OBJECT(CADR(args)) && rwhich > 0
//        && isBasicClass(translateChar(STRING_ELT(rclass, rwhich)))) {
//      value = CADR(args);
//      if (NAMED(value))
//        SET_NAMED(value, 2);
//      value = R_getS4DataSlot(value, S4SXP);
//      if (value != R_NilValue)
//        SETCADR(args, value);
//    }


    if (!(left.sxp instanceof Function) &&!(right.sxp instanceof Function)) {
      UNPROTECT(2);
      return null; /* no generic or group method so use default*/
    }

    if (!left.sxp.equals(right.sxp)) {
//      if (isFunction(lsxp) && isFunction(rsxp)) {
//        /* special-case some methods involving difftime */
//        const char *lname = CHAR(PRINTNAME(lmeth)), *rname =
//            CHAR(PRINTNAME(rmeth));
//        if (streql(rname, "Ops.difftime") && (streql(lname, "+.POSIXt")
//            || streql(lname, "-.POSIXt") || streql(lname, "+.Date")
//            || streql(lname, "-.Date")))
//          rsxp = R_NilValue;
//        else if (streql(lname, "Ops.difftime")
//            && (streql(rname, "+.POSIXt") || streql(rname, "+.Date")))
//          lsxp = R_NilValue;
//        else {
//          warning(_("Incompatible methods (\"%s\", \"%s\") for \"%s\""),
//              lname, rname, generic);
//          UNPROTECT(2);
//          return 0;
//        }
//      }
      /* if the right hand side is the one */
      if (!(left.sxp instanceof Function)) { /* copy over the righthand stuff */
        lclass = rclass;
        left = right;
      }
    }

    /* we either have a group method or a class method */

//    PROTECT(newrho = allocSExp(ENVSXP));
    Frame newrho = new HashFrame();
    String[] m = new String[nargs];
    PairList.Node s = (PairList.Node)args;
    for (i = 0; i < nargs; i++) {
      StringVector t = computeDataClasses(context, args.getElementAsSEXP(i));

//      t = IS_S4_OBJECT(CAR(s)) ? R_data_class2(CAR(s)) : getAttrib(CAR(s),
//          R_ClassSymbol);

      boolean set = false;
      for (j = 0; j < t.length(); j++) {
        if ( t.getElementAsString(j).equals(lclass.getElementAsString(left.which))) {
          m[i] = left.buf;
          set = true;
          break;
        }
      }
      if (!set) {
        m[i] = "";
      }
    }

    newrho.setVariable(Symbol.get(".Method"), new StringArrayVector(m));
    newrho.setVariable(Symbol.get(".Generic"), StringVector.valueOf(generic));
    newrho.setVariable(Symbol.get(".Group"), left.gr);

    StringArrayVector.Builder dotClass = StringVector.newBuilder();
    for(j=left.which;j<lclass.length();++j) {
      dotClass.add(lclass.getElementAsString(j));
    }

    newrho.setVariable(Symbol.get(".Class"), dotClass.build());
    newrho.setVariable(Symbol.get(".GenericCallEnv"), rho);
    newrho.setVariable(Symbol.get(".GenericDefEnv"), rho.getBaseEnvironment());

    FunctionCall newCall = FunctionCall.newCall(left.meth, call.getArguments());

    /* the arguments have been evaluated; since we are passing them */
    /* out to a closure we need to wrap them in promises so that */
    /* they get duplicated and things like missing/substitute work. */


    PairList promisedArgs = Calls.promiseArgs(call.getArguments(), context, rho);
    if (promisedArgs.length() != args.length()) {
      throw new EvalException("dispatch error in group dispatch");
    }
    if(promisedArgs != Null.INSTANCE) {
      PairList.Node promised = (PairList.Node)promisedArgs;
      PairList.Node evaluated = (PairList.Node)args;

      while(true) {

        ((Promise)promised.getValue()).setResult(evaluated.getValue());
        /* ensure positional matching for operators */
        if (isOps) {
          promised.setTag(Null.INSTANCE);
        }
        if(!promised.hasNextNode()) {
          break;
        }
        promised = promised.getNextNode();
        evaluated = evaluated.getNextNode();
      }
    }

    return Calls.applyClosure((Closure) left.sxp, context, newCall, promisedArgs, rho, newrho);
  }

  /**
   * There are a few primitive functions (`[[` among them) which are proper builtins, but attempt
   * to dispatch on the class of their first argument before going ahead with the default implementation.
   *
   * @param context
   * @param rho
   * @param name the name of the function
   * @param args the original args from the FunctionCall
   * @param object evaluated first argument
   * @return
   */
  public static SEXP tryDispatchFromPrimitive(Context context, Environment rho, FunctionCall call,
      String name, SEXP object, PairList args) {

    if(call.getFunction() instanceof Symbol &&
        ((Symbol)call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }

    GenericMethod method = Resolver
      .start(context, name, object)
      .withBaseDefinitionEnvironment()
      .withObjectArgument(object)
      .withGenericArgument(name)
      .findNext();

    if(method == null) {
      return null;
    }

    return method.doApply(context, rho,
            reassembleAndEvaluateArgs(object, args, context, rho));
  }

  public static SEXP tryDispatchFromPrimitive(Context context, Environment rho, FunctionCall call,
      String name, String[] argumentNames, SEXP[] arguments) {

    if(call.getFunction() instanceof Symbol &&
        ((Symbol)call.getFunction()).getPrintName().endsWith(".default")) {
      return null;
    }

    Vector classVector = (Vector)arguments[0].getAttribute(Symbols.CLASS);
    if(classVector.length() == 0) {
      return null;
    }

    DispatchChain chain = DispatchChain.newChain(context, rho, name, classVector);
    if(chain == null) {
      return null;
    }

    PairList.Builder newArgsBuilder = new PairList.Builder();
    for(int i=0;i!=arguments.length;++i) {
      newArgsBuilder.add(argumentNames[i], arguments[i]);
    }
    PairList newArgs = newArgsBuilder.build();

    FunctionCall newCall = new FunctionCall(chain.getMethodSymbol(), newArgs);

    ClosureDispatcher dispatcher = new ClosureDispatcher(context, rho, newCall);
    return dispatcher.apply(chain, newArgs);
  }

  static PairList reassembleAndEvaluateArgs(SEXP object, PairList args, Context context, Environment rho) {
    PairList.Builder newArgs = new PairList.Builder();
    PairList.Node firstArg = (PairList.Node)args;
    newArgs.add(firstArg.getRawTag(), object);

    args = firstArg.getNext();

    for(PairList.Node node : args.nodes()) {
      newArgs.add(node.getRawTag(), context.evaluate( node.getValue(), rho));
    }

    return newArgs.build();
  }

  public static SEXP tryDispatchGroupFromPrimitive(Context context, Environment rho, FunctionCall call,
      String group, String name, SEXP s0) {

    PairList newArgs = new PairList.Node(s0, Null.INSTANCE);

    return dispatchGroup(group, call, name, newArgs, context, rho);
  }

  public static SEXP tryDispatchGroupFromPrimitive(Context context, Environment rho, FunctionCall call,
      String group, String name, SEXP s0, SEXP s1) {

    PairList newArgs = new PairList.Node(s0, new PairList.Node(s1, Null.INSTANCE));

    return dispatchGroup(group, call, name, newArgs, context, rho);
  }

  public static SEXP tryDispatchSummaryFromPrimitive(Context context, Environment rho, FunctionCall call,
      String name, ListVector evaluatedArguments, boolean naRm) {

    // REpackage the evaluated arguments.
    // this is ghastly but i don't think it will
    // get better until Calls is refactored

    PairList.Builder newArgs = new PairList.Builder();
    int varArgIndex = 0;
    Symbol naRmName = Symbol.get("na.rm");
    for(PairList.Node node : call.getArguments().nodes()) {
      if(node.getRawTag() == naRmName) {
        newArgs.add(node.getTag(), new LogicalArrayVector(naRm));
      } else {
        newArgs.add(node.getRawTag(), evaluatedArguments.get(varArgIndex++));
      }
    }

    return dispatchGroup("Summary", call, name, newArgs.build(), context, rho);
  }

  private static class Resolver {

    /**
     * The environment of the call to the generic method.
     */
    private Environment callingEnvironment;

    /**
     * The environment in which the generic was defined. This will be the
     * enclosing environment of the function that calls UseMethod().
     */
    private Environment definitionEnvironment;

    /**
     * The name of the generic: for example "print" or "summary" or
     * "as.character".
     */
    private String genericMethodName;

    /**
     * The S3 classes associated with this object, each to be tried
     * in turn
     */
    private List<String> classes;

    private Context context;

    private SEXP object;

    private static Resolver start(Context context, String genericMethodName, SEXP object) {
      Resolver resolver = new Resolver();
      resolver.callingEnvironment = context.getEnvironment();
      resolver.genericMethodName = genericMethodName;
      resolver.classes = Lists.newArrayList(computeDataClasses(context, object));
      resolver.context = context;
      resolver.object = object;
      return resolver;
    }


    /**
     * Resumes a dispatch chain (called by NextMethod)
     */
    public static Resolver resume(Context context) {
      Context parentContext = findParentContext(context);
      GenericMethod method = parentContext.getState(GenericMethod.class);

      Resolver resolver = new Resolver();
      resolver.context = context;
      resolver.callingEnvironment = context.getEnvironment();
      resolver.definitionEnvironment = method.resolver.definitionEnvironment;
      resolver.genericMethodName = method.resolver.genericMethodName;
      resolver.classes = method.nextClasses();
      resolver.object = method.resolver.object;
      return resolver;
    }


    public Resolver withObjectArgument(SEXP object) {
      if(object != Null.INSTANCE) {
        this.object = object;
      }
      return this;
    }

    public Resolver withGenericArgument(SEXP generic) {
      if(generic != Null.INSTANCE) {
        this.genericMethodName = generic.asString();
      }
      return this;
    }

    public Resolver withGenericArgument(String genericName) {
      this.genericMethodName = genericName;
      return this;
    }

    public Resolver withDefinitionEnvironment(Environment rho) {
      this.definitionEnvironment = rho;
      return this;
    }

    public Resolver withBaseDefinitionEnvironment() {
      this.definitionEnvironment = context.getEnvironment().getBaseEnvironment();
      return this;
    }

    private static Context findParentContext(Context context) {
      for( ; context != null; context = context.getParent() ) {
        if(context.getState(GenericMethod.class) != null) {
          return context;
        }
      }
      throw new EvalException("NextMethod called out of context");
    }

    private static Environment retrieveDefEnv(Context context, Environment env) {
      SEXP defEnv = env.getVariable(".GenericDefEnv");
      if(defEnv == Symbol.UNBOUND_VALUE) {
        return context.getGlobalEnvironment();
      } else if(defEnv instanceof Environment) {
        return (Environment)defEnv;
      } else {
        throw new EvalException("Unexpected value for .GenericalDefEnv: " + defEnv);
      }
    }

    public GenericMethod next() {

      GenericMethod next = findNextOrDefault();

      if(next == null) {
        throw new EvalException("no applicable method for '%s' applied to an object of class \"%s\"",
          genericMethodName, classes.toString());
      }

      return next;
    }

    private GenericMethod findNextOrDefault() {
      GenericMethod next = findNext();

      if(next != null) {
        return next;
      } else {

        Symbol defaultMethod = Symbol.get(genericMethodName + ".default");
        SEXP function = callingEnvironment.findFunction(context, defaultMethod);
        if(function != null) {
          return new GenericMethod(this, defaultMethod, null, (Function) function);
        }

        // as a last step, we call BACK into the primitive
        // to get the default implementation  - ~ YECK ~
        PrimitiveFunction primitive = Primitives.getBuiltin(genericMethodName);
        if(primitive != null) {
          return new GenericMethod(this, defaultMethod, null, primitive);
        }

        return null;
      }
    }

    private GenericMethod findNext() {
      Environment methodTable = getMethodTable();

      for(String className : classes) {
        Symbol method = Symbol.get(genericMethodName + "." + className);
        SEXP function = callingEnvironment.findFunction(context, method);
        if(function != null) {
          return new GenericMethod(this, method, className, (Function) function);
        } else if(methodTable != null && methodTable.hasVariable(method)) {
          return new GenericMethod(this, method, className, (Function) methodTable.getVariable(method).force(context));
        }
      }
      return null;
    }

    private Environment getMethodTable() {
      SEXP table = definitionEnvironment.getVariable(".__S3MethodsTable__.").force(context);
      if(table instanceof Environment) {
        return (Environment) table;
      } else if(table == Symbol.UNBOUND_VALUE) {
        return null;
      } else {
        throw new EvalException("Unexpected value for .__S3MethodsTable__. in " + definitionEnvironment.getName());
      }
    }
  }

  public static class GenericMethod {
    private Resolver resolver;

    /**
     * The name of the selected function
     */
    private Symbol method;
    private Function function;
    private String className;

    public GenericMethod(Resolver resolver, Symbol method, String className, Function function) {
      assert function != null;
      this.resolver = resolver;
      this.method = method;
      this.className = className;
      this.function = function;
    }

    public SEXP apply(Context callContext, Environment callEnvironment) {
      PairList rePromisedArgs = Calls.promiseArgs(callContext.getArguments(), callContext, callEnvironment);
      return doApply(callContext, callEnvironment, rePromisedArgs);
    }

    public SEXP applyNext(Context context, Environment environment) {
      return doApply(context, environment, nextArguments(context, environment));
    }

    public SEXP doApply(Context callContext, Environment callEnvironment, PairList args) {
      FunctionCall newCall = new FunctionCall(method,args);

      callContext.setState(GenericMethod.class, this);

      if(function instanceof Closure) {
        return Calls.applyClosure((Closure) function, callContext, newCall,
                args, callEnvironment, persistChain());
      } else {
        // primitive
        return function.apply(callContext, callEnvironment, newCall, args);
      }
    }

    public PairList nextArguments(Context callContext, Environment callEnvironment) {

        Closure closure = (Closure) this.function;

      /* get formals and actuals; attach the names of the formals to
         the actuals, expanding any ... that occurs */
      PairList formals = closure.getFormals();
      PairList actuals = resolver.context.getArguments();
      actuals = Calls.matchArguments(formals, actuals);
      actuals = expandDotDotDot(actuals);


      // strip out arguments with missing values. These named arguments may not
      // be defined in calls further down the chain and we don't want to "generate"
      // new arguments
      PairList.Builder noMissing = new PairList.Builder();
      for(PairList.Node node : actuals.nodes()) {
        if(node.getValue() != Symbol.MISSING_ARG) {
          noMissing.add(node.getRawTag(), node.getValue());
        }
      }
      actuals = noMissing.build();

//    /* we can't duplicate because it would force the promises */
//    /* so we do our own duplication of the promargs */
//

      PairList.Builder updatedArgs = new PairList.Builder();
      for(PairList.Node actual : actuals.nodes()) {
        SEXP temp;
        if(actual.hasTag()) {
          // an argument may not have a tag even at this point if was
          // part of a ... expansion
          temp = callContext.getParent().getEnvironment().findVariable(actual.getTag());
        } else {
          temp = Symbol.UNBOUND_VALUE;
        }
        if(temp != Symbol.UNBOUND_VALUE &&
                !isDefaultArgValue(temp) &&
                temp != Symbol.MISSING_ARG) {
          updatedArgs.add(actual.getRawTag(), Promise.repromise(callContext.getParent().getEnvironment(), temp));
        } else {
          updatedArgs.add(actual.getRawTag(), actual.getValue());
        }
      }
      return updatedArgs.build();
    }

    private Frame persistChain() {
      HashFrame frame = new HashFrame();
      frame.setVariable(Symbol.get(".Class"), new StringArrayVector(resolver.classes));
      frame.setVariable(Symbol.get(".Method"), StringVector.valueOf(method.getPrintName()));
      frame.setVariable(Symbol.get(".Generic"), StringVector.valueOf(resolver.genericMethodName));
      frame.setVariable(Symbol.get(".GenericCallEnv"), resolver.callingEnvironment);
      frame.setVariable(Symbol.get(".GenericDefEnv"), resolver.definitionEnvironment);
      return frame;
    }

    /**
     *
     * @return remaining classes to be tried after this method
     */
    public List<String> nextClasses() {
      if(className == null) {
        return Collections.emptyList();
      }
      int myIndex = resolver.classes.indexOf(className);
      return resolver.classes.subList(myIndex+1, resolver.classes.size());
    }

  }

  public static class FindResult {
    SEXP sxp;
    SEXP gr;
    Symbol meth;
    int which;
    String buf;
  }
}
