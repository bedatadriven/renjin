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
package org.renjin.packaging;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.eval.Context;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Package;
import org.renjin.sexp.NamedValue;
import org.renjin.util.NamedByteSource;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class InitializingPackage extends Package {

  private final File packageRoot;
  private ClassLoader classLoader;

  protected InitializingPackage(FqPackageName name, File packageRoot, ClassLoader classLoader) {
    super(name);
    this.packageRoot = packageRoot;
    this.classLoader = classLoader;
  }

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
    return Collections.emptySet();
  }

  @Override
  public NamedByteSource getResource(String name) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class loadClass(String className) throws ClassNotFoundException {
    return classLoader.loadClass(className);
  }

  @Override
  public String getPackageRootUri(FileSystemManager fileSystemManager) throws FileSystemException {
    return fileSystemManager.toFileObject(packageRoot).getURL().toString();
  }
}
