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
  public static EvalResult assign(String name, SEXP value, Environment environ, boolean inherits) {

    Symbol symbol = new Symbol(name);
    if(!inherits) {
      environ.setVariable(symbol, value);
    } else {
      while(environ != Environment.EMPTY && !environ.hasVariable(symbol)) {
        environ = environ.getParent();
      }
      if(environ == Environment.EMPTY) {
        environ.getGlobalEnvironment().setVariable(symbol, value);
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
                                SEXP expression, Environment environment,
                                SEXP enclosing /* ignored */) {

    return expression.evaluate(context, environment);
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

  public static EvalResult NextMethod(Context context, Environment rho, FunctionCall call) {

    if(call.getArguments().length() < 2) {
      throw new UnsupportedOperationException(".Internal(NextMethod()) must be called with at least 2 arguments");
    }

    SEXP generic = call.evalArgument(context, rho, 0);
    if(generic == Null.INSTANCE) {
      generic = rho.getVariable(".Generic");
    }
    String genericName = ((StringVector)generic).getElementAsString(0);

    SEXP object = call.evalArgument(context, rho, 1);

    Context callingContext = context.getParent();
    Environment callingEnvironment = callingContext.getEnvironment();

    StringVector classes = (StringVector) callingEnvironment.getVariable(".Class");

    String groupName = null;
    SEXP group = callingEnvironment.getVariable(".Group");
    if(group != Symbol.UNBOUND_VALUE) {
      groupName = ((StringVector)group).getElementAsString(0);
    }

    // Build new list of arguments
    PairList.Builder newArgs = new PairList.Builder();
    for(PairList.Node node : callingContext.getClosure().getFormals().nodes()) {
      // check to see if the arg has been updated
      SEXP newValue = callingEnvironment.getVariable(node.getTag());
      if(newValue == Symbol.UNBOUND_VALUE) {
        newArgs.add(node.getTag(), node.getValue());
      } else {
        newArgs.add(node.getTag(), newValue);
      }
    }

    EvalResult result = new FunctionCall(new Symbol(genericName), newArgs.build()).evaluate(context, rho);
    throw new ReturnException(rho, result.getExpression());
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

        if(function instanceof Closure) {
          EvalResult result = Calls.applyClosure((Closure) function, context, call,
              Calls.promiseArgs(context.getArguments(), context, rho), rho, extra);
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
