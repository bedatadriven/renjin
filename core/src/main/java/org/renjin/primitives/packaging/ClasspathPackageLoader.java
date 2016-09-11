package org.renjin.primitives.packaging;

import org.renjin.repackaged.guava.base.Optional;

/**
 * Loads Packages from the class path
 */
public class ClasspathPackageLoader implements PackageLoader {
  
  private ClassLoader classLoader;

  public ClasspathPackageLoader() {
    this.classLoader = getClass().getClassLoader();
  }
  
  public ClasspathPackageLoader(ClassLoader loader) {
    this.classLoader = loader;
  }

  @Override
  public Optional<Package> load(FqPackageName name) {
    ClasspathPackage pkg = new ClasspathPackage(classLoader, name);
    if(pkg.resourceExists("environment")) {
      return Optional.<Package>of(pkg);
    } else {
      return Optional.absent();
    }
  }

}
