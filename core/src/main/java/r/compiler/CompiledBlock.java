package r.compiler;

import r.lang.Context;
import r.lang.Environment;
import r.lang.SEXP;

public interface CompiledBlock {

  SEXP eval(Context context, Environment rho);
  
}
