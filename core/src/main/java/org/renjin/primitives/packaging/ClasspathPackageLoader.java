package org.renjin.primitives.packaging;

import java.util.Set;

import com.google.common.base.Optional;
import org.renjin.eval.EvalException;

import com.google.common.collect.Sets;

/**
 * Loads Packages from the class path
 */
public class ClasspathPackageLoader implements PackageLoader {

  @Override
  public Optional<Package> load(FqPackageName name) {
    ClasspathPackage pkg = new ClasspathPackage(name);
    if(pkg.resourceExists("environment")) {
      return Optional.<Package>of(pkg);
    } else {
      return Optional.absent();
    }
  }

}
