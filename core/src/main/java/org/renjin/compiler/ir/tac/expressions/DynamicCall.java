package org.renjin.compiler.ir.tac.expressions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.compiler.runtime.VariablePromise;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Function call that is invoked with the full R
 * flexibility, no assumptions are made...
 */
public class DynamicCall implements CallExpression {


  /**
   * The S expression from the original AST which we 
   * need to pass to closures at runtime. For example, given the AST
   * <code>
   * x$f(33)
   * </code>
   *
   * If x$f evaluates to a closure, a new Context needs to be created
   * that contains a copy of the original {@link FunctionCall}. So we 
   * need to retain the x$f literal.
   */
  private SEXP functionSexp;


  /**
   * The IR expression from which we will obtain the actual
   * function value at runtime, IF {@code functionName} is NOT a {@link Symbol}
   */
  private Expression functionExpr;

  /**
   * The original function call. We just need to pass this around.
   */
  private FunctionCall call;

  private final List<Expression> arguments;
  private final List<SEXP> argumentNames;
  private final String[] argumentNamesArray;
  private int elipsesIndex;

  public DynamicCall(FunctionCall call, Expression function,
                     List<SEXP> argumentNames, List<Expression> arguments) {
    this.call = call;
    this.functionSexp = call.getFunction();
    this.functionExpr = function;
    this.arguments = arguments;
    this.argumentNames = argumentNames;
    this.argumentNamesArray = new String[argumentNames.size()];
    for(int i=0;i!=argumentNames.size();++i) {
      if(argumentNames.get(i) instanceof Symbol) {
        argumentNamesArray[i] = ((Symbol) argumentNames.get(i)).getPrintName();
      }
    }
    this.elipsesIndex = -1;
    for(int i=0;i!=arguments.size();++i) {
      if(arguments.get(i) == Elipses.INSTANCE ) {
        elipsesIndex = i;
      }
    }
  }

  public FunctionCall getCall() {
    return call;
  }

  public Expression getFunction() {
    return functionExpr;
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  public Object retrieveValue(Context context, Object[] temps) {

    // locate function object
    Function functionValue = findFunction(context, temps);

    if(! hasElipses() && functionValue instanceof BuiltinFunction) {
      return ((BuiltinFunction)functionValue).apply(context, context.getEnvironment(), call, argumentNamesArray, evaluateArgs(context, temps));
    } else if(functionValue instanceof Closure) {

      PairList.Builder args = new PairList.Builder();
      int argNameIndex=0;
      for(Expression argument : arguments) {
        if(argument instanceof Elipses) {
          // splice existing promise list into
          SEXP elipses = context.getEnvironment().findVariable(Symbols.ELLIPSES);
          args.addAll((PairList)elipses);
        } else if(argument instanceof IRThunk) {
          if(argument.getSExpression() instanceof Symbol) {
            args.add(argumentNames.get(argNameIndex++),
                    new VariablePromise(context, ((Symbol) argument.getSExpression()).getPrintName()));
          } else {
            args.add(argumentNames.get(argNameIndex++), new IRPromise(context, temps, (IRThunk)argument));
          }
        } else {
          args.add(argumentNames.get(argNameIndex++), (SEXP)argument.retrieveValue(context, temps));
        }
      }
      // TODO: check calling environment
      return ((Closure) functionValue).matchAndApply(context, context.getEnvironment(), call, args.build());

    } else {
      return functionValue.apply(context, context.getEnvironment(), call, call.getArguments());
    }
  }

  private static class IRPromise extends Promise {
    private Object[] temps;
    private IRThunk thunk;

    public IRPromise(Context context, Object temps[], IRThunk thunk) {
      super(context.getEnvironment(), thunk.getSExpression());
      this.temps = temps;
      this.thunk = thunk;
    }

    @Override
    protected SEXP doEval(Context context) {
      return thunk.retrieveValue(context, temps);
    }
  }

  private SEXP[] evaluateArgs(Context context, Object[] temps) {
    SEXP[] evaluated = new SEXP[arguments.size()];
    for(int i=0;i!=evaluated.length;++i) {
      evaluated[i] = (SEXP) arguments.get(i).retrieveValue(context, temps);
    }
    return evaluated;
  }

  private Function findFunction(Context context, Object[] temps) {

    // we have to different cases here. 
    // if the function call is in the form 
    //
    // f(a,b,c)
    // 
    // then we do a special lookup to find the first 
    // _function_ with the name `f` in the enclosing environments.
    // bindings with non-function values are ignored.
    //
    // this is *different* then simply evaluating the symbol `f`.

    if(functionSexp instanceof Symbol) {
      return context.getEnvironment().findFunctionOrThrow(context, (Symbol)functionSexp);
    } else {

      // otherwise, we need to proceed to evaluate the expression 
      // as it's been translated into IR. It must evaluate to a
      // function value.

      Object value = functionExpr.retrieveValue(context, temps);
      if(!(value instanceof Function)) {
        throw new EvalException("attempt to apply non-function: " + value);
      }
      return (Function) value;
    }
  }

  public SEXP getFunctionSexp() {
    return functionSexp;
  }

  @Override
  public Set<Variable> variables() {
    Set<Variable> variables = Sets.newHashSet();
    variables.addAll( functionExpr.variables() );
    for(Expression operand : arguments) {
      variables.addAll( operand.variables() );
    }
    return Collections.unmodifiableSet(variables);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("\u0394 " + functionExpr + "(");
    for(int i=0;i!=argumentNames.size();++i) {
      if(i > 0) {
        sb.append(", ");
      }
      if(argumentNames.get(i) != Null.INSTANCE) {
        sb.append(argumentNames.get(i)).append(" = ");
      }
      sb.append(arguments.get(i));
    }
    sb.append(")");
    return sb.toString();
  }

  @Override
  public Expression replaceVariable(Variable name, Variable newName) {
    List<Expression> newOps = Lists.newArrayListWithCapacity(arguments.size());
    for(Expression argument : arguments) {
      newOps.add(argument.replaceVariable(name, newName));
    }
    return new DynamicCall(call,
            (Variable)this.functionExpr.replaceVariable(name, newName),
            argumentNames,
            newOps);
  }

  @Override
  public List<Expression> getChildren() {
    List<Expression> children = Lists.newArrayList();
    children.add(functionExpr);
    children.addAll(arguments);
    return children;
  }

  @Override
  public void setChild(int i, Expression expr) {
    if(i == 0) {
      functionExpr = (Variable)expr;
    } else {
      arguments.set(i-1, expr);
    }
  }

  @Override
  public void accept(ExpressionVisitor visitor) {
    visitor.visitDynamicCall(this);
  }

  @Override
  public FunctionCall getSExpression() {
    return call;
  }

  public List<String> getArgumentNames() {
    return Lists.transform(argumentNames, new com.google.common.base.Function<SEXP, String>() {

      @Override
      public String apply(SEXP input) {
        if(input instanceof Symbol) {
          return ((Symbol) input).getPrintName();
        } else {
          return null;
        }
      }
    });
  }

  @Override
  public int getElipsesIndex() {
    return elipsesIndex;
  }

  @Override
  public boolean hasElipses() {
    return elipsesIndex != -1;
  }
}
