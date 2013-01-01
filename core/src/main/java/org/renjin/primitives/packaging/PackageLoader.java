package org.renjin.primitives.packaging;

import org.renjin.eval.EvalException;

public class PackageLoader {

  public Package load(String name) {
    MavenPackage pkg =  new MavenPackage(name);
    if(!pkg.exists()) {
      throw new EvalException("Cannot find package " + name + " on the classpath");    
    }
    return pkg;
  }
}
