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

    SEXP result;

    PairList promisedArgs = Calls.promiseArgs(args, callingContext, callingEnvironment);
    
    Context functionContext = callingContext.beginFunction(call, this, promisedArgs);
    Environment functionEnvironment = functionContext.getEnvironment();    
  
    ClosureDispatcher.matchArgumentsInto(getFormals(), promisedArgs, functionContext, functionEnvironment);

    if(Context.PRINT_IR) {
      System.out.println("=== " + function.toString() + ", function context = " + Integer.toHexString(System.identityHashCode(functionContext)));
      System.out.println(function.getBody());
    }
    
    try {

    
      result = function.getBody().evaluate(functionContext);

    } catch(ReturnException e) {
      if(functionEnvironment != e.getEnvironment()) {
        throw e;
      }
      result = e.getValue();
    } finally {
      functionContext.exit();
    }
    return result;

  }  
  
}
