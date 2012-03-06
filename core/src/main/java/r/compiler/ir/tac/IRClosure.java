package r.compiler.ir.tac;

import r.base.Calls;
import r.base.ClosureDispatcher;
import r.base.special.ReturnException;
import r.lang.Closure;
import r.lang.Context;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.SEXP;

public class IRClosure extends Closure {

  private IRFunction function;

  public IRClosure(Environment environment, IRFunction function) {
    super(environment, function.getFormals(), function.getBodyExpression());
    this.function = function;
  }

  // this is the old way of dispatching function calls
  @Override
  public SEXP apply(Context callingContext, Environment callingEnvironment, 
      FunctionCall call, PairList args) {

    PairList promisedArgs = Calls.promiseArgs(args, callingContext, callingEnvironment);
    
    return matchAndApply(callingContext, call, promisedArgs);
  }
  
  
  
  @Override
  public SEXP doApply(Context functionContext) {
    return function.getBody().evaluate(functionContext);
  }
  
}
