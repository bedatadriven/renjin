package org.renjin.primitives.packaging;


import org.renjin.repackaged.guava.base.Optional;

public interface PackageLoader {

  /**
   * @param name the package name
   * @return a set of resources implementing the Package interface,
   * if a package matching the name could be located
   */
  Optional<Package> load(FqPackageName packageName);

}