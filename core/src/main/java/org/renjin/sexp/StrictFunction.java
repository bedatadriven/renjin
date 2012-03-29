package org.renjin.sexp;

import org.renjin.eval.Context;

/**
 * Interface to a {@link PrimitiveFunction} which is strict; that is 
 * all of its arguments will be evaluated.
 */
public interface StrictFunction extends Function {


  /**
   * Applies this {@code BuiltinFunction} to the given the 
   * @param context the runtime context in which to evaluate this function
   * @param call the original function call
   * @param argumentNames the names of the arguments 
   * @param arguments the <b><i>evaluated</i></b> arguments
   * @return the result of the function
   */
  SEXP applyStrict(Context context, Environment rho, FunctionCall call, 
      String argumentNames[], SEXP arguments[]);
  
}
