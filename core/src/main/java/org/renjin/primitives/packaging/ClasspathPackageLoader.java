package org.renjin.primitives.packaging;

import java.util.Set;

import org.renjin.eval.EvalException;

import com.google.common.collect.Sets;

/**
 * Loads Packages from the class path
 */
public class ClasspathPackageLoader implements PackageLoader {

  
  /**
   * These packages are part of the R distribution and carry the 
   * org.renjin groupId.
   */
  private static final Set<String> CORE_PACKAGES = Sets.newHashSet("datasets", "graphics", "grDevices", "hamcrest", 
      "methods", "splines", "stats", "stats4", "utils", "grid");
  
  /* (non-Javadoc)
   * @see org.renjin.primitives.packaging.IPackageLoader#load(java.lang.String)
   */
  @Override
  public ClasspathPackage load(String name) {
    ClasspathPackage pkg;
    if(CORE_PACKAGES.contains(name)) {
      pkg = new ClasspathPackage("org.renjin", name);
    } else {
      pkg = new ClasspathPackage("org.renjin.cran", name);
    }
    if(!pkg.exists()) {
      return null;   
    }
    return pkg;
  }
}
