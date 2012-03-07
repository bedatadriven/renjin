package r.compiler.runtime;

import r.lang.Context;
import r.lang.Environment;

public interface PackageLoader {
  public void load(Context context, Environment rho);
  

}
