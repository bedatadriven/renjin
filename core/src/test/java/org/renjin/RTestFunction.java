package org.renjin;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import r.lang.Context;
import r.lang.FunctionCall;
import r.lang.Symbol;
import r.lang.exception.EvalException;

public class RTestFunction extends Runner {

  private Context testContext;
  private Symbol testFunction;
  
  public RTestFunction(Context testContext, Symbol testFunction) {
    super();
    this.testContext = testContext;
    this.testFunction = testFunction;
  }

  @Override
  public Description getDescription() {
    return Description.createSuiteDescription(testFunction.toString());
  }

  @Override
  public void run(RunNotifier notifier) {
    notifier.fireTestStarted(getDescription());
    try {
      FunctionCall testCall = FunctionCall.newCall(testFunction);
      testContext.evaluate(testCall);
    } catch(Exception e)  {
      // cheap hack, to fix:
      if(e instanceof EvalException && e.getMessage().startsWith("\nExpected:")) {
        notifier.fireTestFailure(new Failure(getDescription(), 
            new AssertionError(e.getMessage())));
        
      } else {
        notifier.fireTestFailure(new Failure(getDescription(), e));
      }
    }
    notifier.fireTestFinished(getDescription()); 
  }

}
