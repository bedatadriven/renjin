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
package org.renjin.packaging;

import org.renjin.primitives.packaging.PackageLoader;

import java.io.File;
import java.util.List;

/**
 * Provides general access to the build environment, whether
 * that is Maven, Gradle, etc.
 */
public interface BuildContext {
  
  BuildLogger getLogger();

  void setupNativeCompilation();


  /**
   * @return the path to the GCC Bridge plugin
   */
  File getGccBridgePlugin();

  /**
   * 
   * @return the path to the unpacked GNU R Home.
   */
  File getGnuRHomeDir();

  /**
   * 
   * @return the path to the unpacked include files of this package's dependencies.
   */
  File getUnpackedIncludesDir();

  /**
   * @return the output directory for Java class files.
   */
  File getOutputDir();

  /**
   *
   * @return the directory to which compile logs hsould be written
   */
  File getCompileLogDir();

  /**
   * Returns the directory where package output is to be written.
   * 
   * <p>This will be a sub directory of the java output directory that includes the groupId as the 
   * (java) package. For example, if the output directory is {@code target/classes}, and the groupId of the package
   * is {@code org.renjin.cran} and the package name {@code Matrix}, then this directory will be 
   * {@code target/classes/org/renjin/cran/Matrix}</p>
   */
  File getPackageOutputDir();


  PackageLoader getPackageLoader();

  /**
   * @return the class loader to use for evaluating R sources
   */
  ClassLoader getClassLoader();

  /**
   * 
   * @return the nmaes of the packages that should be on the search path when the 
   * 
   */
  List<String> getDefaultPackages();
}
