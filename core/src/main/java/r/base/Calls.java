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

import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import r.base.special.ReturnException;
import r.lang.*;
import r.lang.exception.EvalException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static r.util.CDefines.*;

/**
 * Routines for dispatching and generally organizing function calls.
 * Much of this code is a pretty literal port of portions of eval.c and
 * object.c
 */
public class Calls {

  public static PairList evaluateList(Context context, Environment rho, PairList args) {
    PairList.Builder evaled = new PairList.Builder();
    for(PairList.Node node : args.nodes()) {
      evaled.add(node.getRawTag(), node.getValue().evalToExp(context, rho));
    }
    return evaled.build();
  }


  public static EvalResult DispatchGroup(String group, FunctionCall call, PrimitiveFunction op, PairList args, Context context, Environment rho) {
    int i, j, nargs;


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
    isOps = true;

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

    String generic = PRIMNAME(op);

//    lclass = IS_S4_OBJECT(CAR(args)) ? R_data_class2(CAR(args)) : getAttrib(
//        CAR(args), R_ClassSymbol);

    Vector lclass = CAR(args).getClassAttribute();
    Vector rclass;
    if (nargs == 2) {
//      rclass = IS_S4_OBJECT(CADR(args)) ? R_data_class2(CADR(args))
//          : getAttrib(CADR(args), R_ClassSymbol);
      rclass = args.getElementAsSEXP(1).getClassAttribute();
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
    Environment newrho = Environment.createOrphanEnvironment();
    String[] m = new String[nargs];
    PairList.Node s = (PairList.Node)args;
    for (i = 0; i < nargs; i++) {
      StringVector t = args.getElementAsSEXP(i).getClassAttribute();

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

    newrho.setVariable(new Symbol(".Method"), new StringVector(m));
    newrho.setVariable(new Symbol(".Generic"), new StringVector(generic));
    newrho.setVariable(new Symbol(".Group"), left.gr);

    StringVector.Builder dotClass = StringVector.newBuilder();
    for(j=left.which;j<lclass.length();++j) {
      dotClass.add(lclass.getElementAsString(j));
    }

    newrho.setVariable(new Symbol(".Class"), dotClass.build());
    newrho.setVariable(new Symbol(".GenericCallEnv"), rho);
    newrho.setVariable(new Symbol(".GenericDefEnv"), rho.getBaseEnvironment());

    FunctionCall newCall = FunctionCall.newCall(left.meth, call.getArguments());

    /* the arguments have been evaluated; since we are passing them */
    /* out to a closure we need to wrap them in promises so that */
    /* they get duplicated and things like missing/substitute work. */


    PairList promisedArgs = promiseArgs(call.getArguments(), rho);
    if (promisedArgs.length() != args.length())
      throw new EvalException("dispatch error in group dispatch");

    if(promisedArgs != Null.INSTANCE) {
      for (PairList.Node promised = (PairList.Node)promisedArgs,
               evaluated = (PairList.Node)args; promised.hasNextNode();
           promised=promised.getNextNode(), evaluated = evaluated.getNextNode()) {

        ((Promise)promised.getValue()).setResult(evaluated.getValue());
        /* ensure positional matching for operators */
        if (isOps) {
          promised.setTag(Null.INSTANCE);
        }
      }
    }

    return applyClosure((Closure)left.sxp, context, promisedArgs, rho, newrho);
  }

  public static EvalResult applyClosure(Closure closure, Context context, PairList promisedArgs, Environment rho,
                                        Environment suppliedEnvironment) {

    PairList formals = closure.getFormals();
    SEXP body = closure.getBody();
    Environment savedrho = closure.getEnclosingEnvironment();


    Context functionContext = context.beginFunction(closure.getEnclosingEnvironment(), promisedArgs);
    Environment functionEnvironment = functionContext.getEnvironment();

    try {
      matchArgumentsInto(closure.getFormals(), promisedArgs, functionEnvironment);

      // copy supplied environment values into the function environment
      for(Symbol name : suppliedEnvironment.getSymbolNames()) {
        functionEnvironment.setVariable(name, suppliedEnvironment.getVariable(name));
      }

      EvalResult result = closure.getBody().evaluate(functionContext, functionEnvironment);

      functionContext.exit();

      return result;
    } catch(ReturnException e) {
      if(e.getEnvironment() != functionEnvironment) {
        throw e;
      }
      return EvalResult.visible(e.getValue());
    }
  }

  private static class FindResult {
    SEXP sxp;
    SEXP gr;
    Symbol meth;
    int which;
    String buf;
  }

  /* Create a promise to evaluate each argument.  Although this is most */
/* naturally attacked with a recursive algorithm, we use the iterative */
/* form below because it is does not cause growth of the pointer */
/* protection stack, and because it is a little more efficient. */

  public static PairList promiseArgs(PairList el, Environment rho)
  {
    PairList.Builder list = new PairList.Builder();

    for(PairList.Node node : el.nodes()) {

      /* If we have a ... symbol, we look to see what it is bound to.
      * If its binding is Null (i.e. zero length)
      * we just ignore it and return the cdr with all its
      * expressions promised; if it is bound to a ... list
      * of promises, we repromise all the promises and then splice
      * the list of resulting values into the return value.
      * Anything else bound to a ... symbol is an error
      */

      /* Is this double promise mechanism really needed? */

      if (node.getValue().equals(Symbol.ELLIPSES)) {
        DotExp dotExp = DotExp.cast(rho.findVariable(Symbol.ELLIPSES));
        for(PairList.Node dotNode : dotExp.getPromises().nodes()) {
          list.add(dotNode.getRawTag(), dotNode.getValue());
        }
      } else if (node.getValue() == Symbol.MISSING_ARG) {
        list.add(node.getRawTag(), node.getValue());
      } else {
        list.add(node.getRawTag(), new Promise(rho, node.getValue()));
      }
    }
    return list.build();
  }

  /* gr needs to be protected on return from this function */
  static FindResult findmethod(Context context, Vector Class, String group,  String generic, Environment rho) {
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

      result.meth = new Symbol(result.buf);
      result.sxp = R_LookupMethod(context, result.meth, rho, rho, rho.getBaseEnvironment());
      if (result.sxp instanceof Function) {
        result.gr = new StringVector("");
        break;
      }
      result.buf = group + "." + ss;
      result.meth = new Symbol(result.buf);
      result.sxp = R_LookupMethod(context, result.meth, rho, rho, rho.getBaseEnvironment());
      if (result.sxp instanceof Function) {
        result.gr = new StringVector(group);
        break;
      }
    }
    result.which = whichclass;
    return result;
  }

/*  usemethod  -  calling functions need to evaluate the object
 *  (== 2nd argument).  They also need to ensure that the
 *  argument list is set up in the correct manner.
 *
 *    1. find the context for the calling function (i.e. the generic)
 *       this gives us the unevaluated arguments for the original call
 *
 *    2. create an environment for evaluating the method and insert
 *       a handful of variables (.Generic, .Class and .Method) into
 *       that environment. Also copy any variables in the env of the
 *       generic that are not formal (or actual) arguments.
 *
 *    3. fix up the argument list; it should be the arguments to the
 *       generic matched to the formals of the method to be invoked */

  private static SEXP R_LookupMethod(Context context, Symbol method, SEXP rho, Environment callrho, Environment defrho)
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
   * Argument matching is done by a three-pass process:
   * <ol>
   * <li><strong>Exact matching on tags.</strong> For each named supplied argument the list of formal arguments
   *  is searched for an item whose name matches exactly. It is an error to have the same formal
   * argument match several actuals or vice versa.</li>
   *
   * <li><strong>Partial matching on tags.</strong> Each remaining named supplied argument is compared to the
   * remaining formal arguments using partial matching. If the name of the supplied argument
   * matches exactly with the first part of a formal argument then the two arguments are considered
   * to be matched. It is an error to have multiple partial matches.
   *  Notice that if f <- function(fumble, fooey) fbody, then f(f = 1, fo = 2) is illegal,
   * even though the 2nd actual argument only matches fooey. f(f = 1, fooey = 2) is legal
   * though since the second argument matches exactly and is removed from consideration for
   * partial matching. If the formal arguments contain ‘...’ then partial matching is only applied to
   * arguments that precede it.
   *
   * <li><strong>Positional matching.</strong> Any unmatched formal arguments are bound to unnamed supplied arguments,
   * in order. If there is a ‘...’ argument, it will take up the remaining arguments, tagged or not.
   * If any arguments remain unmatched an error is declared.
   *
   * @param actuals the actual arguments supplied to the list
   * @param innerEnv the environment in which to resolve the arguments;
   */
  public static void matchArgumentsInto(PairList formals, PairList actuals, Environment innerEnv) {

    List<PairList.Node> unmatchedActuals = Lists.newArrayList();
    for(PairList.Node argNode : actuals.nodes()) {
      unmatchedActuals.add(argNode);
    }

    List<PairList.Node> unmatchedFormals = Lists.newArrayList(formals.nodes());

    // do exact matching
    for(ListIterator<PairList.Node> formalIt = unmatchedFormals.listIterator(); formalIt.hasNext(); ) {
      PairList.Node formal = formalIt.next();
      if(formal.hasTag()) {
        Symbol name = (Symbol) formal.getTag();
        Collection<PairList.Node> matches = Collections2.filter(unmatchedActuals, PairList.Predicates.matches(name));

        if(matches.size() == 1) {
          PairList.Node match = first(matches);
          innerEnv.setVariable(name, match.getValue());
          formalIt.remove();
          unmatchedActuals.remove(match);

        } else if(matches.size() > 1) {
          throw new EvalException(String.format("Multiple named values provided for argument '%s'", name.getPrintName()));
        }
      }
    }

    // do partial matching
    Collection<PairList.Node> remainingNamedFormals = filter(unmatchedFormals, PairList.Predicates.hasTag());
    for(Iterator<PairList.Node> actualIt = unmatchedActuals.iterator(); actualIt.hasNext(); ) {
      PairList.Node actual = actualIt.next();
      if(actual.hasTag()) {
        Collection<PairList.Node> matches = Collections2.filter(remainingNamedFormals,
            PairList.Predicates.startsWith(actual.getTag()));

        if(matches.size() == 1) {
          PairList.Node match = first(matches);
          innerEnv.setVariable(match.getTag(), actual.getValue());
          actualIt.remove();
          unmatchedFormals.remove(match);

        } else if(matches.size() > 1) {
          throw new EvalException(String.format("Provided argument '%s' matches multiple named formal arguments: %s",
              actual.getTag().getPrintName(), argumentTagList(matches)));
        }
      }
    }

    // match any unnamed args positionally

    Iterator<PairList.Node> formalIt = unmatchedFormals.iterator();
    PeekingIterator<PairList.Node> actualIt = Iterators.peekingIterator(unmatchedActuals.iterator());
    while( formalIt.hasNext()) {
      PairList.Node formal = formalIt.next();
      if(Symbol.ELLIPSES.equals(formal.getTag())) {
        PairList.Node.Builder promises = PairList.Node.newBuilder();
        while(actualIt.hasNext()) {
          PairList.Node actual = actualIt.next();
          promises.add( actual.getRawTag(),  actual.getValue() );
        }
        innerEnv.setVariable(formal.getTag(), new DotExp( promises.build() ));

      } else if( hasNextUnTagged(actualIt) ) {
        innerEnv.setVariable(formal.getTag(), nextUnTagged(actualIt).getValue() );

      } else if( formal.getValue() == Symbol.MISSING_ARG ) {
        innerEnv.setVariable(formal.getTag(), Symbol.MISSING_ARG);

      } else {
        innerEnv.setVariable(formal.getTag(), new Promise(innerEnv, formal.getValue())); // default
      }
    }
    if(actualIt.hasNext()) {
      throw new EvalException(String.format("Unmatched positional arguments"));
    }
  }

  private static boolean hasNextUnTagged(PeekingIterator<PairList.Node> it) {
    return it.hasNext() && !it.peek().hasTag();
  }

  private static PairList.Node nextUnTagged(Iterator<PairList.Node> it) {
    PairList.Node arg = it.next() ;
    while( arg.hasTag() ) {
      arg = it.next();
    }
    return arg;
  }

  private static String argumentTagList(Collection<PairList.Node> matches) {
    return Joiner.on(", ").join(transform(matches, new CollectionUtils.TagName()));
  }

  private static <X> X first(Iterable<X> values) {
    return values.iterator().next();
  }

}
