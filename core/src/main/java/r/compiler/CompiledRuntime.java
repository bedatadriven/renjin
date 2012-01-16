package r.compiler;

import r.lang.Logical;
import r.lang.SEXP;
import r.lang.exception.EvalException;

public class CompiledRuntime {

  public static boolean evaluateCondition(SEXP s) {

    if (s.length() == 0) {
      throw new EvalException("argument is of length zero");
    }
//    if (s.length() > 1) {
//      Warning.invokeWarning(context, call, "the condition has length > 1 and only the first element will be used");
//    }

    Logical logical = s.asLogical();
    if (logical == Logical.NA) {
      throw new EvalException("missing value where TRUE/FALSE needed");
    }

    return logical == Logical.TRUE;
  }
  
}
