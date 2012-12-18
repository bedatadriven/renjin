package org.renjin.methods;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

/**
 * Handles the dispatching of methods from standardGeneric()
 * 
 */
public interface MethodDispatcher {

  public SEXP dispatch(Context context, Symbol fsym, Environment ev, SEXP fdef);
  
}
