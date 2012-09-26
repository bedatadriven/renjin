package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.SpecialFunction;
import org.renjin.sexp.Symbols;

import polyglot.ast.Call;

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
