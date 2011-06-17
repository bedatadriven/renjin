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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import r.base.special.ReturnException;
import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Evaluate;
import r.jvmi.annotations.Primitive;
import r.jvmi.binding.JvmMethod;
import r.jvmi.binding.RuntimeInvoker;
import r.lang.*;
import r.lang.exception.EvalException;
import r.parser.RParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class Evaluation {



  /**
   * There are no restrictions on name: it can be a non-syntactic name (see make.names).
   *
   * The pos argument can specify the environment in which to assign the object in any
   * of several ways: as an integer (the position in the search list); as the character
   * string name of an element in the search list; or as an environment (including using
   * sys.frame to access the currently active function calls). The envir argument is an
   *  alternative way to specify an environment, but is primarily there for back compatibility.
   *
   * assign does not dispatch assignment methods, so it cannot be used to
   *  set elements of vectors, names, attributes, etc.
   *
   * Note that assignment to an attached list or data frame changes the attached copy
   *  and not the original object: see attach and with.
   */
  public static EvalResult assign(@Current Context context, String name, SEXP value, Environment environ, boolean inherits) {

    Symbol symbol = new Symbol(name);
    if(!inherits) {
      environ.setVariable(symbol, value);
    } else {
      while(environ != Environment.EMPTY && !environ.hasVariable(symbol)) {
        environ = environ.getParent();
      }
      if(environ == Environment.EMPTY) {
        context.getGlobalEnvironment().setVariable(symbol, value);
      } else {
        environ.setVariable(symbol, value);
      }
    }
    return EvalResult.invisible(value);
  }

  public static void delayedAssign(@Current Context context, String x, SEXP expr, Environment evalEnv, Environment assignEnv) {
    assignEnv.setVariable( new Symbol(x), new Promise(context, evalEnv, expr));
  }


  /**
   * This is the so-called complex assignment, such as:
   *  class(x) <- "foo" or
   *  length(x) <- 3
   *
   *
   */

  @Primitive("on.exit")
  public static void onExit( @Current Context context, @Evaluate(false) SEXP exp, boolean add ) {
    if(add) {
      context.addOnExit(exp);
    } else {
      context.setOnExit(exp);
    }
  }

  public static ListVector lapply(@Current Context context, @Current Environment rho, FunctionCall call) {
    Vector vector = (Vector) call.evalArgument(context, rho, 0);
    Function function = (Function) call.evalArgument(context, rho, 1);

    ListVector.Builder builder = ListVector.newBuilder();
    for(int i=0;i!=vector.length();++i) {
      // For historical reasons, the calls created by lapply are unevaluated, and code has
      // been written (e.g. bquote) that relies on this.
      FunctionCall getElementCall = FunctionCall.newCall(new Symbol("[["), (SEXP)vector, new IntVector(i+1));
      FunctionCall applyFunctionCall = new FunctionCall((SEXP)function, new PairList.Node(getElementCall,
          new PairList.Node(Symbol.ELLIPSES, Null.INSTANCE)));
      builder.add( applyFunctionCall.evalToExp(context, rho) );
    }
    return builder.build();
  }


  public static void stop(boolean call, String message) {
    throw new EvalException(message);
  }

  public static void warning(boolean call, boolean immediate, String message) {
    Warning.warning(message);
  }

  @Primitive("return")
  public static EvalResult doReturn(@Current Environment rho, SEXP value) {
    throw new ReturnException(rho, value);
  }

  @Primitive("do.call")
  public static EvalResult doCall(@Current Context context, Function what, ListVector arguments, Environment environment) {
    PairList argumentPairList = new PairList.Builder().addAll(arguments).build();
    FunctionCall call = new FunctionCall(what, argumentPairList);
    return call.evaluate(context, environment);
  }

  @Primitive("do.call")
  public static EvalResult doCall(@Current Context context, @Current Environment rho, String what, ListVector arguments, Environment environment) {
    SEXP function = environment.findVariable(new Symbol(what));
    if(function instanceof Promise) {
      function = ((Promise) function).force().getExpression();
    }
    return doCall(context, (Function) function, arguments, environment);
  }

  @Primitive("call")
  public static EvalResult call(@Current Context context, @Current Environment rho, FunctionCall call) {
    if(call.length() < 1) {
      throw new EvalException("first argument must be character string");
    }
    SEXP name = call.evalArgument(context, rho, 0);
    if(!(name instanceof StringVector) || name.length() != 1) {
      throw new EvalException("first argument must be character string");
    }

    FunctionCall newCall = new FunctionCall(new Symbol(((StringVector) name).getElementAsString(0)),
        ((PairList.Node)call.getArguments()).getNextNode());
    return newCall.evaluate(context, rho);
  }

  public static EvalResult eval(@Current Context context,
                                SEXP expression, SEXP environment,
                                SEXP enclosing) {

    Environment rho;
    if(environment instanceof Environment) {
      rho = (Environment) environment;
    } else {

      /* If envir is a list (such as a data frame) or pairlist, it is copied into a temporary environment
       * (with enclosure enclos), and the temporary environment is used for evaluation. So if expr
       * changes any of the components named in the (pair)list, the changes are lost.
       */
      Environment parent = enclosing == Null.INSTANCE ? context.getEnvironment().getBaseEnvironment() :
          EvalException.<Environment>checkedCast(enclosing);

      rho = Environment.createChildEnvironment(parent);

      if(environment instanceof ListVector) {
        for(NamedValue namedValue : ((ListVector) environment).namedValues()) {
          if(!StringVector.isNA(namedValue.getName())) {
            rho.setVariable(new Symbol(namedValue.getName()), namedValue.getValue());
          }
        }
      } else {
        throw new EvalException("invalid 'environ' argument");
      }
    }

    return expression.evaluate(context, rho);
  }

  public static SEXP quote(@Evaluate(false) SEXP exp) {
    return exp;
  }

  public static boolean missing(@Current Context context, @Current Environment rho, @Evaluate(false) Symbol symbol) {
    SEXP value = rho.findVariable(symbol);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("'missing' can only be used for arguments");

    } else if(value == Symbol.MISSING_ARG) {
      return true;
    } else {
      // we need to rematch the arguments to determine whether the value was actually provided
      // or whether 'value' contains the default value
      //
      // this seems quite expensive, perhaps there's a faster way?
      PairList rematched = Calls.matchArguments(
          Calls.stripDefaultValues(context.getClosure().getFormals()),
          context.getCall().getArguments());
      SEXP providedValue = rematched.findByTag(symbol);

      return providedValue == Symbol.MISSING_ARG;
      //return false;
    }
  }

  @Primitive(".C")
  public static EvalResult dotC(@Current Context context,
                                @Current Environment rho,
                                @ArgumentList ListVector arguments) {
    
    String methodName = arguments.getElementAsString(0);
    String packageName = null;
    boolean naOK = false;
    
    ListVector.Builder callArguments = ListVector.newBuilder();
    for(int i=1;i<arguments.length();++i) {
      if(arguments.getName(i).equals("PACKAGE")) {
        packageName = arguments.getElementAsString(i);
      } else if(arguments.getName(i).equals("NAOK")) {
        naOK = (arguments.asLogical() == Logical.TRUE);
      } else if(arguments.getName(i).equals("DUP")) {
        // ignore
      } else if(arguments.getName(i).equals("ENCODING")) {
        // ignore
      } else if(arguments.getElementAsSEXP(i) != Null.INSTANCE) {
        callArguments.add(arguments.getElementAsSEXP(i));
      }
    }
    
    return doNativeCall(context, rho, methodName, packageName, callArguments);
    
  }
  
  @Primitive(".Call")
  public static EvalResult dotCall(@Current Context context,
                                   @Current Environment rho,
                                   @ArgumentList ListVector arguments) {

    String methodName = arguments.getElementAsString(0);
    String packageName = null;
    ListVector.Builder callArguments = ListVector.newBuilder();
    for(int i=1;i<arguments.length();++i) {
      if(arguments.getName(i).equals("PACKAGE")) {
        packageName = arguments.getElementAsString(i);  
      } else if(arguments.getElementAsSEXP(i) != Null.INSTANCE) {
        callArguments.add(arguments.getElementAsSEXP(i));
      }
    }

    return doNativeCall(context, rho, methodName, packageName, callArguments);
  }

  private static EvalResult doNativeCall(Context context, Environment rho,
      String methodName, String packageName, ListVector.Builder callArguments) {
    Class packageClass;
    if(packageName.equals("base")) {
      packageClass = Base.class;
    } else {
      String packageClassName = "r.library." + packageName + "." +
          packageName.substring(0, 1).toUpperCase() + packageName.substring(1);
      try {
        packageClass = Class.forName(packageClassName);
      } catch (ClassNotFoundException e) {
        throw new EvalException("Could not find class for 'native' methods for package '%s' (className='%s')",
            packageName, packageClassName);
      }
    }

    List<JvmMethod> overloads = JvmMethod.findOverloads(packageClass, methodName, methodName);
    if(overloads.isEmpty()) {
      throw new EvalException("No C method '%s' defined in package %s", methodName, packageName);
    }
    return RuntimeInvoker.INSTANCE.invoke(context, rho, callArguments.build(), overloads);


//    throw new EvalException(
//        String.format("Call to native function '%s' in package '%s'",
//            methodName, packageName));
  }

  public static EvalResult UseMethod(Context context, Environment rho, FunctionCall call) {
    SEXP generic = call.evalArgument(context, rho, 0);
    EvalException.check(generic.length() == 1 && generic instanceof StringVector,
        "first argument must be a character string");

    String genericName = ((StringVector)generic).getElementAsString(0);

    Preconditions.checkArgument(context.getType() == Context.Type.FUNCTION);
    SEXP object;
    if(call.getArguments().length() >= 2) {
      object = call.evalArgument(context, rho, 1);
    } else {
      object = context.getArguments().getElementAsSEXP(0).evalToExp(context,
          context.getParent().getEnvironment());
    }


    StringVector classes = object.getClassAttribute();
    DispatchGeneric(context, rho, call, genericName, object, classes);

    throw new UnsupportedOperationException();
  }

  public static EvalResult NextMethod(Context context, Environment env, FunctionCall call) {

    if(call.getArguments().length() < 2) {
      throw new UnsupportedOperationException(".Internal(NextMethod()) must be called with at least 2 arguments");
    }

//    char buf[512], b[512], bb[512], tbuf[10];
//    const char *sb, *sg, *sk;
//    SEXP ans, s, t, klass, method, matchedarg, generic, nextfun;
//    SEXP sysp, m, formals, actuals, tmp, newcall;
//    SEXP a, group, basename;
//    SEXP callenv, defenv;
//    RCNTXT *cptr;
//    int i, j, cftmp;

//    cptr = R_GlobalContext;
//    cftmp = cptr->callflag;
//    cptr->callflag = CTXT_GENERIC;

    /* get the env NextMethod was called from */
//    sysp = R_GlobalContext->sysparent;
//    while (cptr != NULL) {
//        if (cptr->callflag & CTXT_FUNCTION && cptr->cloenv == sysp) break;
//        cptr = cptr->nextcontext;
//    }
//    if (cptr == NULL)
//        error(_("'NextMethod' called from outside a function"));

    Context sysp = context.getParent();

    /* eg get("print.ts")(1) */
//    if (TYPEOF(CAR(cptr->call)) == LANGSXP)
//       error(_("'NextMethod' called from an anonymous function"));

    /* Find dispatching environments. Promises shouldn't occur, but
       check to be on the safe side.  If the variables are not in the
       environment (the method was called outside a method dispatch)
       then chose reasonable defaults. */
    SEXP callenv = env.getVariable(".GenericCallEnv");

    /*if (TYPEOF(callenv) == PROMSXP)
        callenv = eval(callenv, R_BaseEnv);
    else */
    if (callenv == Symbol.UNBOUND_VALUE) {
      callenv = env;
    }

    SEXP defenv = env.getVariable(".GenericDefEnv");
    /*if (TYPEOF(defenv) == PROMSXP) defenv = eval(defenv, R_BaseEnv);
    else */
    if (defenv == Symbol.UNBOUND_VALUE) {
      defenv = context.getGlobalEnvironment();
    }

    /* set up the arglist */
    SEXP s = Calls.lookupMethod((Symbol)sysp.getCall().getFunction(), (Environment)env,
        (Environment)callenv, (Environment)defenv);

    if(s == Symbol.UNBOUND_VALUE) {
      throw new EvalException("no calling generic was found: was a method called directly?");
    }
    if (!(s instanceof Closure)){ /* R_LookupMethod looked for a function */
      throw new EvalException("function' is not a function, but of type %s", s.getTypeName());
    }
    /* get formals and actuals; attach the names of the formals to
       the actuals, expanding any ... that occurs */

    PairList formals = ((Closure) s).getFormals();
    PairList actuals = context.getParent().getArguments();
    actuals = Calls.matchArguments(formals, actuals);
    actuals = expandDotDotDot(actuals);

//    /* we can't duplicate because it would force the promises */
//    /* so we do our own duplication of the promargs */
//

    PairList.Builder updatedArgs = new PairList.Builder();
    for(PairList.Node actual : actuals.nodes()) {
      SEXP temp = context.getParent().getEnvironment().findVariable(actual.getTag());
      if(temp != Symbol.UNBOUND_VALUE) {
        updatedArgs.add(actual.getTag(), new Promise(context.getParent(), context.getParent().getEnvironment(), temp));
      } else {
        updatedArgs.add(actual.getTag(), actual.getValue());
      }
    }
    actuals = updatedArgs.build();




//    PROTECT(matchedarg = allocList(length(cptr->promargs)));
//    for (t = matchedarg, s = cptr->promargs; t != R_NilValue;
//         s = CDR(s), t = CDR(t)) {
//        SETCAR(t, CAR(s));
//        SET_TAG(t, TAG(s));
//    }
//    for (t = matchedarg; t != R_NilValue; t = CDR(t)) {
//        for (m = actuals; m != R_NilValue; m = CDR(m))
//            if (CAR(m) == CAR(t))  {
//                if (CAR(m) == R_MissingArg) {
//                    tmp = findVarInFrame3(cptr->cloenv, TAG(m), TRUE);
//                    if (tmp == R_MissingArg) break;
//                }
//                SETCAR(t, mkPROMISE(TAG(m), cptr->cloenv));
//                break;
//           }
//    }
//    /*
//      Now see if there were any other arguments passed in
//      Currently we seem to only allow named args to change
//      or to be added, this is at variance with p. 470 of the
//      White Book
//    */
//
//    s = CADDR(args); /* this is ... and we need to see if it's bound */
//    if (s == R_DotsSymbol) {
//        t = findVarInFrame3(env, s, TRUE);
//        if (t != R_NilValue && t != R_MissingArg) {
//            SET_TYPEOF(t, LISTSXP); /* a safe mutation */
//            s = matchmethargs(matchedarg, t);
//            UNPROTECT(1);
//            PROTECT(matchedarg = s);
//            newcall = fixcall(newcall, matchedarg);
//        }
//    }
//    else
//        error(_("wrong argument ..."));

    /*
      .Class is used to determine the next method; if it doesn't
      exist the first argument to the current method is used
      the second argument to NextMethod is another option but
      isn't currently used).
    */
    SEXP klass = sysp.getEnvironment().getVariable(".Class");


    if (klass == Symbol.UNBOUND_VALUE) {
//        s = GetObject(cptr);
//        if (!isObject(s)) error(_("object not specified"));
//        klass = getAttrib(s, R_ClassSymbol);
      throw new EvalException(".Class attribute not present");
    }

    /* the generic comes from either the sysparent or it's named */
    SEXP generic = sysp.getEnvironment().getVariable(".Generic");
    if (generic == Symbol.UNBOUND_VALUE) {
      //  generic = eval(CAR(args), env);
      throw new EvalException(".Generic not present");
    }
    if( generic == Null.INSTANCE ) {
      throw new EvalException("generic function not specified");
    }

    if ( !(generic instanceof StringVector) || generic.length() > 1) {
      throw new EvalException("invalid generic argument to NextMethod");
    }

//    if (CHAR(STRING_ELT(generic, 0))[0] == '\0')
//        error(_("generic function not specified"));

    /* determine whether we are in a Group dispatch */

    SEXP groupExp = context.getParent().getEnvironment().getVariable(".Group");
    String group;
    if (groupExp == Symbol.UNBOUND_VALUE) {
      group = "";
    } else {
      group = ((StringVector)groupExp).getElementAsString(0);
    }

//    if (!isString(group) || length(group) > 1)
//        error(_("invalid 'group' argument found in NextMethod"));

    /* determine the root: either the group or the generic will be it */

    String basename;
    if (group.isEmpty()) {
      basename = ((StringVector)generic).getElementAsString(0);
    } else {
      basename = group;
    }

    SEXP nextfun = Null.INSTANCE;

    /*
       Find the method currently being invoked and jump over the current call
       If t is R_UnboundValue then we called the current method directly
    */

    SEXP method = context.getParent().getEnvironment().getVariable(".Method");

    String b=null;
    if( method != Symbol.UNBOUND_VALUE) {
      if( !(method instanceof StringVector) ) {
        throw new EvalException("wrong value for .Method");
      }
      for(String ss : (StringVector)method) {
        if(!ss.isEmpty()) {
          b = ss;
          break;
        }
      }
//        for(i = 0; i < length(method); i++) {
//            ss = translateChar(STRING_ELT(method, i));
//            if(strlen(ss) >= 512)
//                error(_("method name too long in '%s'"), ss);
//            sprintf(b, "%s", ss);
//            if(strlen(b)) break;
//        }
//        /* for binary operators check that the second argument's method
//           is the same or absent */
//        for(j = i; j < length(method); j++){
//            const char *ss = translateChar(STRING_ELT(method, j));
//            if(strlen(ss) >= 512)
//                error(_("method name too long in '%s'"), ss);
//          sprintf(bb, "%s", ss);
//          if (strlen(bb) && strcmp(b,bb))
//              warning(_("Incompatible methods ignored"));
//        }
    }
    else {
//        if(strlen(CHAR(PRINTNAME(CAR(cptr->call)))) >= 512)
//           error(_("call name too long in '%s'"),
//                 CHAR(PRINTNAME(CAR(cptr->call))));
//        sprintf(b, "%s", CHAR(PRINTNAME(CAR(cptr->call))));
      throw new EvalException(".Method is not set, not sure what to do");
    }

    String sb = basename;
    String buf = null;
    int j;
    for (j = 0; j < klass.length(); j++) {
      String sk = ((StringVector)klass).getElementAsString(j);
      buf = sb + "." + sk;
      if (!buf.equals(b)) {
        break;
      }
    }

    if (buf != null && buf.equals(b)) { /* we found a match and start from there */
      j++;
    } else {
      j = 0;  /*no match so start with the first element of .Class */
    }
    /* we need the value of i on exit from the for loop to figure out
           how many classes to drop. */

    String sg = ((StringVector)generic).getElementAsString(0);
    int i;
    for (i = j ; i < klass.length(); i++) {
      String sk = ((StringVector)klass).getElementAsString(0);
      buf = sg + "." + sk;
      nextfun = Calls.lookupMethod(new Symbol(buf), env, (Environment)callenv, (Environment)defenv);
      if (nextfun instanceof Function) {
        break;
      }
      if (groupExp != Symbol.UNBOUND_VALUE) {
        /* if not Generic.foo, look for Group.foo */
        buf = sb + "." + sk;
        nextfun = Calls.lookupMethod(new Symbol(buf), env, (Environment)callenv, (Environment)defenv);
        if(nextfun instanceof Function) {
          break;
        }
      }
      if (nextfun instanceof Function) {
        break;
      }
    }
    if (!(nextfun instanceof Function)) {
      buf = sg + ".default";
      nextfun = Calls.lookupMethod(new Symbol(buf), env, (Environment)callenv, (Environment)defenv);
      /* If there is no default method, try the generic itself,
        provided it is primitive or a wrapper for a .Internal
        function of the same name.
      */
      if (!(nextfun instanceof Function)) {
        Symbol t = new Symbol(sg);
        nextfun = env.findVariable(t);
        if ( nextfun instanceof Promise) {
          nextfun = nextfun.evalToExp(context, env);
        }
        if (!(nextfun instanceof Function)) {
          throw new EvalException("no method to invoke");
        }
        if (nextfun instanceof Closure) {
          if (env.getBaseEnvironment().findInternal(t) != Symbol.UNBOUND_VALUE)
            nextfun = env.getBaseEnvironment().findInternal(t);
          else {
            throw new EvalException("no method to invoke");
          }
        }
      }
    }

    StringVector.Builder newklass = new StringVector.Builder();
    for(j=0;j< newklass.length();++j) {
      newklass.add(((StringVector)klass).getElementAsString(i++));
    }
    newklass.setAttribute("previous", klass);

   // PROTECT(m = allocSExp(ENVSXP));
    Frame m = new HashFrame();
    m.setVariable(new Symbol(".Class"), newklass.build());


    /* It is possible that if a method was called directly that
        'method' is unset */
//    if (method != Symbol.UNBOUND_VALUE) {
//      /* for Ops we need `method' to be a vector */
//
//      PROTECT(method = duplicate(method));
//      for(j = 0; j < length(method); j++) {
//        if (strlen(CHAR(STRING_ELT(method,j))))
//          SET_STRING_ELT(method, j,  mkChar(buf));
//      }
//    } else {
    method = new Symbol(buf);
//    }
    m.setVariable(new Symbol(".Method"), method);
//    defineVar(install(".GenericCallEnv"), callenv, m);
//    defineVar(install(".GenericDefEnv"), defenv, m);


    m.setVariable(new Symbol(".Generic"), generic);
    m.setVariable(new Symbol(".Group"), groupExp);

    FunctionCall newcall = new FunctionCall(method, actuals);


    if(nextfun instanceof Closure) {
      return Calls.applyClosure((Closure)nextfun, context, newcall, actuals, env, m);
    } else {
      return ((Function)nextfun).apply(context, env, newcall, actuals);
    }

//
//
//    SETCAR(newcall, method);
//    ans = applyMethod(newcall, nextfun, matchedarg, env, m);
//    UNPROTECT(10);
//    return(ans);
//    throw new EvalException("this and no further");
  }

  private static PairList expandDotDotDot(PairList actuals) {
    PairList.Builder result = new PairList.Builder();
    for(PairList.Node node : actuals.nodes()) {
      if(node.getValue() instanceof DotExp) {
        for(PairList.Node dotNode : ((DotExp) node.getValue()).getPromises().nodes()) {
          result.add(dotNode.getRawTag(), dotNode.getValue());
        }
      } else {
        result.add(node.getRawTag(), node.getValue());
      }
    }
    return result.build();
  }


  private static void DispatchGeneric(Context context, Environment rho, FunctionCall call, String genericName, SEXP object, StringVector classes) {
    for(String className : Iterables.concat(classes, Arrays.asList("default"))) {
      Symbol method = new Symbol(genericName + "." + className);
      SEXP function = rho.findVariable(method);
      if(function != Symbol.UNBOUND_VALUE) {
        function = function.evalToExp(context, rho);

        Frame extra = new HashFrame();
        extra.setVariable(new Symbol(".Class"), object.getClassAttribute());
        extra.setVariable(new Symbol(".Method"), method);
        extra.setVariable(new Symbol(".Generic"), new StringVector(genericName));

        PairList repromisedArgs = Calls.promiseArgs(context.getArguments(), context, rho);
        FunctionCall newCall = new FunctionCall(method,repromisedArgs);


        if(function instanceof Closure) {
          EvalResult result = Calls.applyClosure((Closure) function, context, newCall,
              repromisedArgs, rho, extra);
          throw new ReturnException(context.getEnvironment(), result.getExpression());
        } else {
          throw new UnsupportedOperationException("target of UseMethod is not a closure, it is a " +
              function.getClass().getName() );
        }
      }
    }
  }

  /**
   * @return  TRUE when R is being used interactively and FALSE otherwise.
   */
  public static boolean interactive() {
    return false;
  }

  public static ExpressionVector parse(SEXP file, SEXP maxExpressions, Vector text, String prompt, String sourceFile, String encoding) throws IOException {
    List<SEXP> expressions = Lists.newArrayList();
    if(text != Null.INSTANCE) {
      for(int i=0;i!=text.length();++i) {
        String line = text.getElementAsString(i);
        try {
          ExpressionVector result = RParser.parseSource(new StringReader(line + "\n"));
          Iterables.addAll(expressions, result);
        } catch (IOException e) {
          throw new EvalException("I/O Exception occurred during parse: " + e.getMessage());
        }
      }
    } else if(file instanceof ExternalExp) {
      ExternalExp<Connection> conn = EvalException.checkedCast(file);
      Reader reader = new InputStreamReader(conn.getValue().getInputStream());
      ExpressionVector result = RParser.parseSource(reader);
      Iterables.addAll(expressions, result);
    }

    return new ExpressionVector(expressions);
  }

  public static int nargs(@Current Context context) {
    return context.getArguments().length();
  }
}
