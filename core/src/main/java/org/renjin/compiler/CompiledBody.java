package org.renjin.compiler;


import org.renjin.eval.Context;
import org.renjin.sexp.Environment;

public interface CompiledBody {

  void evaluate(Context context, Environment rho);
}
