package org.renjin.compiler;


import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

public interface CompiledLoopBody {
  
  void run(Context context, Environment rho, SEXP sequence, int iteration);
  
}
