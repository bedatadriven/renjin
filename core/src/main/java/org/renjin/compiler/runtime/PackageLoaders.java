package org.renjin.compiler.runtime;

import org.renjin.eval.Context;
import org.renjin.sexp.Environment;


public class PackageLoaders {
  
  public static void load(Context context, Environment rho, String packageName) {
    try {
      Class<PackageLoader> loaderClass = (Class<PackageLoader>) Class.forName(packageName + ".Loader");
      PackageLoader loader = loaderClass.newInstance();
      loader.load(context, rho);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
