package org.renjin.compiler.runtime;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;


public interface PackageLoader {
  public void load(Context context, Environment rho);
  

}
