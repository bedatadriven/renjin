package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

public class RecallFunction extends SpecialFunction {

  public RecallFunction() {
    super("Recall");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call,
      PairList args) {
    
    // this is an .Internal function, so we need to go up one context.
    Context originalContext = context.getParent();
    
    Closure closure = originalContext.getClosure();
    if(closure == null) {
      throw new EvalException("Recall() must be called from within a closure");
    }
    
    PairList newArguments = (PairList)rho.getVariable(Symbols.ELLIPSES);
    FunctionCall newCall = new FunctionCall(originalContext.getCall().getFunction(), newArguments);
    
    return closure.apply(originalContext, 
          originalContext.getEnvironment(), 
          newCall, 
          newArguments);
  }

  
  
}
