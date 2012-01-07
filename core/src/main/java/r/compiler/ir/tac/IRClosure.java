package r.compiler.ir.tac;

import r.base.ClosureDispatcher;
import r.lang.Closure;
import r.lang.Context;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.SEXP;

public class IRClosure extends Closure {

  private IRFunction function;

  public IRClosure(Environment environment, IRFunction function) {
    super(environment, function.getFormals(), function.getBody());
    this.function = function;
  }

  @Override
  public SEXP apply(Context callingContext, Environment rho, FunctionCall call,
      PairList args) {

    Context functionContext = callingContext.beginFunction(call, this, args);
    Environment functionEnvironment = functionContext.getEnvironment();

    ClosureDispatcher.matchArgumentsInto(getFormals(), args, functionContext, functionEnvironment);

    if(Context.PRINT_IR) {
      System.out.println(function.getScope());
    }
    
    SEXP result = function.getScope().evaluate(functionContext);
    functionContext.exit();

    return result;
  }  
}
