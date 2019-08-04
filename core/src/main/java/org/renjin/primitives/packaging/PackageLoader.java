/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
 * Interface to a service which can load R Packages.
 *
 * <p>This is an extension point that allows users to customize the loading of packages
 * in a Renjin {@link org.renjin.eval.Session}. By default, packages are loaded from the classpath
 * using the {@link ClasspathPackageLoader}, but you can provide your own mechanism that loads
 * packages over the network, from a specific file location (see {@link FileBasedPackage}, or
 * something more complex.</p>
 */
public interface PackageLoader {

  /**
   * @param packageName the fully-qualified package name
   * @return a set of resources implementing the Package interface,
   * if a package matching the name could be located
   */
  Optional<Package> load(FqPackageName packageName);

  /**
   * @param packageName a local package name, not qualified with a group name
   */
  Optional<Package> load(String packageName);
}