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

package r.lang.primitive;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import r.base.Base;
import r.lang.*;
import r.lang.exception.ControlFlowException;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.Current;
import r.lang.primitive.annotations.Evaluate;
import r.lang.primitive.annotations.Primitive;
import r.lang.primitive.binding.PrimitiveMethod;
import r.lang.primitive.binding.RuntimeInvoker;

import java.util.List;

public class Evaluation {


  /**
   * Evaluates a list of statements. (The '{' function)
   */
  @Primitive("{")
  public static EvalResult begin(Context context, Environment rho, FunctionCall call) {

    EvalResult lastResult = new EvalResult(Null.INSTANCE, true);
    for (SEXP sexp : call.getArguments().values()) {
      lastResult = sexp.evaluate(context, rho);
    }
    return lastResult;
  }

  @Primitive("(")
  public static SEXP paren(SEXP value) {
    return value;
  }

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

    SymbolExp symbol = new SymbolExp(name);
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


  /**
   * This is the so-called complex assignment, such as:
   *  class(x) <- "foo" or
   *  length(x) <- 3
   *
   *
   */

  /*  It's important that the rhs get evaluated first because
   assignment is right associative i.e.  a <- b <- c is parsed as
   a <- (b <- c).  */
  @Primitive("<-")
  public static EvalResult assignLeft(@Current Context context, @Current Environment rho, @Evaluate(false) SEXP lhs, SEXP rhs) {

    // this loop handles nested, complex assignments, such as:
    // class(x) <- "foo"
    // x$a[3] <- 4
    // class(x$a[3]) <- "foo"

    while(lhs instanceof FunctionCall) {
      FunctionCall call = (FunctionCall) lhs;
      SymbolExp getter = (SymbolExp) call.getFunction();
      SymbolExp setter = new SymbolExp(getter.getPrintName() + "<-");

      rhs = new FunctionCall(setter,
          PairList.Node.newBuilder()
            .addAll(call.getArguments())
            .add(rhs)
            .build()).evalToExp(context, rho);

      lhs = call.getArgument(0);
    }

    SymbolExp target;
    if( lhs instanceof SymbolExp ) {
      target = (SymbolExp) lhs;
    } else if(lhs instanceof StringVector) {
      target = new SymbolExp(((StringVector) lhs).getElement(0));
    } else {
      throw new EvalException("cannot assign to value of type " + lhs.getTypeName());
    }

    // make the final assignment to the target symbol
    rho.setVariable(target, rhs);

    return EvalResult.invisible(rhs);
  }


  @Primitive("on.exit")
  public static void onExit( @Current Environment rho, @Evaluate(false) SEXP exp, boolean add ) {
    if(add) {
      rho.addOnExit(exp);
    } else {
      rho.setOnExit(exp);
    }
  }

  @Primitive("on.exit")
  public static void onExit( @Current Environment rho, @Evaluate(false) SEXP exp) {
    rho.setOnExit(exp);
  }

  /**
   * for ( x in elements ) { statement }
   */
  @Primitive("for")
  public static void forLoop(Context context, Environment rho, FunctionCall call) {
    PairList args = call.getArguments();
    SymbolExp symbol = (SymbolExp) args.get(0);
    Vector elements = (Vector) args.get(1).evalToExp(context, rho);
    SEXP statement = args.get(2);

    for(int i=0; i!=elements.length(); ++i) {
      try {
        rho.setVariable(symbol, elements.getElementAsSEXP(i));
        statement.evaluate(context, rho);
      } catch (BreakException e) {
        break;
      } catch (NextException e) {
        // next iteration
      }
    }
  }

  public static ListVector lapply(@Current Context context, @Current Environment rho, FunctionCall call) {
    Vector vector = (Vector) call.evalArgument(context, rho, 0);
    Function function = (Function) call.evalArgument(context, rho, 1);

    PairList remainingArguments =  call.getArguments().length() > 2 ?
        call.getNextNode().getNextNode().getNextNode() : Null.INSTANCE;

    ListVector.Builder builder = ListVector.newBuilder();
    for(int i=0;i!=vector.length();++i) {
      // For historical reasons, the calls created by lapply are unevaluated, and code has
      // been written (e.g. bquote) that relies on this.
      FunctionCall getElementCall = FunctionCall.newCall(new SymbolExp("[["), (SEXP)vector, new IntVector(i+1));
      FunctionCall applyFunctionCall = new FunctionCall((SEXP)function, new PairList.Node(getElementCall, remainingArguments));
      builder.add( applyFunctionCall.evalToExp(context, rho) );
    }
    return builder.build();
  }

  @Primitive("while")
  public static void whileLoop(Context context, Environment rho, FunctionCall call) {
    PairList args = call.getArguments();
    SEXP condition = args.get(0);
    SEXP statement = args.get(1);

    while(asLogicalNoNA(call, condition.evaluate(context, rho).getExpression(), rho)) {

      try {

        statement.evaluate(context, rho);

      } catch(BreakException e) {
        break;
      } catch(NextException e) {
        // next loop iteration
      }
    }
  }

  public static Closure function( @Current Environment rho,
                                  @Evaluate(false) PairList formals,
                                  @Evaluate(false) SEXP body,
                                  SEXP source)
  {
    return new Closure(rho,formals, body);
  }

  @Primitive("if")
  public static EvalResult doIf(Context context, Environment rho, FunctionCall call) {
    SEXP condition = call.getArguments().get(0).evalToExp(context, rho);

    if (asLogicalNoNA(call, condition, rho)) {
      return call.getArguments().get(1).evaluate(context, rho); /* true value */

    } else {
      if (call.getArguments().length() == 3) {
        return call.getArguments().get(2).evaluate(context, rho); /* else value */
      } else {
        return EvalResult.NON_PRINTING_NULL;   /* no else, evaluates to NULL */
      }
    }
  }

  @Primitive("switch")
  public static EvalResult doSwitch(Context context, Environment rho, FunctionCall call) {
    EvalException.check(call.length() > 1, "argument \"EXPR\" is missing");

    SEXP expr = call.evalArgument(context, rho, 0);
    EvalException.check(expr.length() == 1, "EXPR must return a length 1 vector");

    DotExp branchPromises  = (DotExp) call.getArgument(1).evalToExp(context, rho);
    Iterable<PairList.Node> branches = branchPromises.getPromises().nodes();

    if(expr instanceof StringVector) {
      String name = ((StringVector) expr).getElementAsString(0);
      SEXP partialMatch = null;
      int partialMatchCount = 0;
      for(PairList.Node node : branches) {
        if(node.hasTag()) {
          String branchName = node.getTag().getPrintName();
          if(branchName.equals(name)) {
            return node.getValue().evaluate(context, rho);
          } else if(branchName.startsWith(name)) {
            partialMatch = node.getValue();
            partialMatchCount ++;
          }
        }
      }
      if(partialMatchCount == 1) {
        return partialMatch.evaluate(context, rho);
      } else if(Iterables.size(branches) > 0) {
        PairList.Node last = Iterables.getLast(branches);
        if(!last.hasTag()) {
          return last.getValue().evaluate(context, rho);
        }
      }

    } else if(expr instanceof AtomicVector) {
      int branchIndex = ((AtomicVector) expr).getElementAsInt(0);
      if(branchIndex >= 1 && branchIndex <= Iterables.size(branches)) {
        return Iterables.get(branches, branchIndex-1).getValue().evaluate(context, rho);
      }
    }

    return EvalResult.visible( Null.INSTANCE );
  }

  @Primitive(".Internal")
  public static EvalResult internal(Context context, Environment rho, FunctionCall call) {
    SEXP arg = call.getArguments().get(0);
    if(!(arg instanceof FunctionCall)) {
      throw new EvalException("invalid .Internal() argument");
    }
    FunctionCall internalCall = (FunctionCall) arg;
    SymbolExp internalName = (SymbolExp)internalCall.getFunction();
    SEXP function = rho.findInternal(internalName);

    if(function == Null.INSTANCE) {
      throw new EvalException(String.format("no internal function \"%s\"", internalName.getPrintName()));
    }
    return ((Function)function).apply(context, rho, internalCall, internalCall.getArguments());
  }

  public static EvalResult next() {
    throw new NextException();
  }

  public static void stop(boolean call, String message) {
    throw new EvalException(message);
  }

  /**
   * break;
   */
  @Primitive("break")
  public static void doBreak() {
    throw new BreakException();
  }

  @Primitive("return")
  public static EvalResult doReturn(@Current Environment rho, SEXP value) {
    throw new ReturnException(rho, value);
  }

  public static EvalResult eval(@Current Context context,
                                SEXP expression, Environment environment,
                                SEXP enclosing /* ignored */) {

    return expression.evaluate(context, environment);
  }

  public static SEXP quote(@Evaluate(false) SEXP exp) {
    return exp;
  }

  public static SEXP substitute(@Current Environment rho, @Evaluate(false) SEXP exp) {
    // this seems pretty arbitrary but its documented!
//    if(rho == rho.getGlobalEnvironment()) {
//      return exp;
//    }
    return substitute(exp, rho);
  }

  public static SEXP substitute(@Evaluate(false) SEXP exp, Environment environment) {
    SubstitutingVisitor visitor = new SubstitutingVisitor(environment);
    exp.accept(visitor);
    return visitor.getResult();
  }

  private static class SubstitutingVisitor extends SexpVisitor<SEXP> {
    private final Environment environment;
    private SEXP result;

    public SubstitutingVisitor(Environment environment) {
      this.environment = environment;
    }

    @Override
    public void visit(FunctionCall langExp) {
      result = new FunctionCall(
          substitute(langExp.getFunction()),
          (PairList) substitute((SEXP) langExp.getArguments()),
          langExp.getAttributes(),
          langExp.getRawTag());
    }

    @Override
    public void visit(PairList.Node listExp) {
      PairList.Node.Builder builder = PairList.Node.newBuilder();
      for(PairList.Node node : listExp.nodes()) {
        builder.add(node.getRawTag(), substitute(node.getValue()));
      }
      result = builder.build();
    }

    @Override
    public void visit(ListVector listExp) {
      ListVector.Builder builder = ListVector.newBuilder();
      for(SEXP exp : listExp) {
        builder.add(substitute(exp));
      }
      builder.setAttributes(listExp.getAttributes());
      result = builder.build();
    }

    @Override
    public void visit(ExpressionVector expSexp) {
      List<SEXP> list = Lists.newArrayList();
      for(SEXP exp : expSexp) {
        list.add( substitute(exp ));
      }
      result = new ExpressionVector(list, expSexp.getAttributes());
    }

    @Override
    public void visit(SymbolExp symbolExp) {
      if(environment.hasVariable(symbolExp)) {
        result = environment.getVariable(symbolExp);
        if(result instanceof Promise) {
          result = ((Promise) result).getExpression();
        }
      } else {
        result = symbolExp;
      }
    }

    @Override
    protected void unhandled(SEXP exp) {
      result = exp;
    }

    @Override
    public SEXP getResult() {
      return result;
    }

    private SEXP substitute(SEXP exp) {
      return Evaluation.substitute(exp, environment);
    }
  }

  public static boolean asLogicalNoNA(FunctionCall call, SEXP s, Environment rho) {

    if (s.length() > 1) {
      Warning.warning(call, "the condition has length > 1 and only the first element will be used");
    }

    Logical logical = s.asLogical();
    if (logical == Logical.NA) {
      throw new EvalException("missing value where TRUE/FALSE needed");
    }

    return logical == Logical.TRUE;
  }

  public static boolean missing(@Current Environment rho, @Evaluate(false) SymbolExp symbol) {
    SEXP value = rho.findVariable(symbol);
    if(value == SymbolExp.UNBOUND_VALUE) {
      throw new EvalException("'missing' can only be used for arguments");

    } else {
      return value == SymbolExp.MISSING_ARG;
    }
  }

  @Primitive(".Call")
  public static EvalResult call(@Current Context context,
                            @Current Environment rho,
                            String methodName,
                            PairList arguments,
                            String packageName) {

    if(packageName.equals("base")) {
      List<PrimitiveMethod> overloads = PrimitiveMethod.findOverloads(Base.class, methodName, methodName);
      return RuntimeInvoker.INSTANCE.invoke(context, rho, arguments.values(), overloads);
    }

    throw new EvalException(
        String.format("Call to native function '%s' in package '%s'",
            methodName, packageName));
  }

  /**
   * @return  TRUE when R is being used interactively and FALSE otherwise.
   */
  public static boolean interactive() {
    return false;
  }

  public static class BreakException extends ControlFlowException {   }

  public static class NextException extends ControlFlowException {   }

  public static class ReturnException extends ControlFlowException {

    private final Environment environment;
    private final SEXP value;

    public ReturnException(Environment environment, SEXP value) {
      this.environment = environment;
      this.value = value;
    }

    public SEXP getValue() {
      return value;
    }

    public Environment getEnvironment() {
      return environment;
    }
  }
}
