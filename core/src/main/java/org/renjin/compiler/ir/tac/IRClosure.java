package org.renjin.compiler.ir.tac;

import org.renjin.eval.Calls;
import org.renjin.eval.ClosureDispatcher;
import org.renjin.eval.Context;
import org.renjin.primitives.special.ReturnException;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;


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
    
    return matchAndApply(callingContext, callingEnvironment, call, promisedArgs);
  }
  
  
  
  @Override
  public SEXP doApply(Context functionContext) {
    return function.getBody().evaluate(functionContext);
  }
  
}
