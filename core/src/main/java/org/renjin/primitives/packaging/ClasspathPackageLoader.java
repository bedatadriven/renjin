/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.packaging;

import java.util.Optional;

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
      return Optional.of(pkg);
    } else {
      return Optional.empty();
    }
  }

}
