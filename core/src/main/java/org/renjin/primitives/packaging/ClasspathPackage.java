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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.repackaged.guava.io.Resources;
import org.renjin.util.NamedByteSource;

import java.io.IOException;
import java.net.URL;

/**
 * Provides access to a Renjin package that is on the application's classpath.
 */
public class ClasspathPackage extends FileBasedPackage {

  private ClassLoader classLoader;

  public ClasspathPackage(ClassLoader classLoader, FqPackageName name) {
    super(name);
    this.classLoader = classLoader;
  }
  
  public ClasspathPackage(FqPackageName name) {
    this(ClasspathPackage.class.getClassLoader(), name);
  }
  
  public boolean exists() {
    return resourceExists("environment");
  }

  @Override
  public NamedByteSource getResource(String name) throws IOException {
    String qualifiedName = qualifyResourceName(name);
    URL url = classLoader.getResource(qualifiedName);
    if (url == null) {
      throw new IOException(String.format("Could not find %s (%s)", name, qualifiedName));
    }
    try {
      return new NamedByteSource(name, Resources.asByteSource(url));
    } catch(Exception e) {
      throw new IOException(String.format("Could not load %s (%s)", name, url.toString()), e);
    }
  }

  @Override
  public Class loadClass(String name) throws ClassNotFoundException {
    return classLoader.loadClass(name);
  }

  @Override
  public FileObject resolvePackageRoot(FileSystemManager fileSystemManager) throws FileSystemException {
    // Find the URL where the package is located
    String qualifiedName = qualifyResourceName("environment");
    String uri = "res:" + qualifiedName;


    FileObject environmentFileObject;
    try {
      environmentFileObject = fileSystemManager.resolveFile(uri);
    } catch (FileSystemException e) {
      throw new FileSystemException("Exception locating package resource '" + uri + "' using the provided VirtualFileSystem, " +
          "check your Renjin Session configuration.", e);
    }

    if(!environmentFileObject.exists()) {
      throw new FileSystemException("Could not locate resource '" + uri + "' using the provided VirtualFileSystem, " +
          "check your Renjin Session configuration.");
    }

    return environmentFileObject.getParent();
  }


  private String qualifyResourceName(String name) {
    return
        getName().getGroupId().replace('.', '/') +
        "/" +
        getName().getPackageName() +
        "/" +
        name;
  }

  @Override
  public boolean resourceExists(String name) {
    URL url = classLoader.getResource(qualifyResourceName(name));
    return url != null;
  }
}
