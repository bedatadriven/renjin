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
package org.renjin.util;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.renjin.eval.vfs.FastJarFileProvider;

import java.io.File;

public class FileSystemUtils {
  

  /**
   *
   * @return  the path to the R home directory as packaged within
   * the renjin-core.jar archive. This can be a layered URI in the form
   * jar:file:///path/to/renjin-core.jar!/org/renjin/library
   */
  public static String homeDirectoryInCoreJar(FileSystemManager fileSystemManager) {
    // hardcode to the R home location to the classpath location
    // where this class is found.

    try {
      String resourceName = "res:org/renjin/sexp/SEXP.class";
      FileObject fileObject = fileSystemManager.resolveFile(resourceName);
      if(!fileObject.exists()) {
        throw new IllegalStateException("Cannot locate resource '" + resourceName + "' in provided virtual file system," +
            " make sure that you are including a ResourceFileProvider with scheme res:");
      }

      return fileObject.getParent().getParent().getURL().toString();

    } catch (FileSystemException e) {
      throw new IllegalStateException("Failed to locate R.home: ", e);
    }
  }

  public static FileObject workingDirectory(FileSystemManager fileSystemManager) {
    try {
      return fileSystemManager.resolveFile(new File("").getAbsolutePath());
    } catch (FileSystemException e) {
      throw new RuntimeException("Could not resolve current working directory", e);
    }
  }

  public static FileSystemManager getMinimalFileSystemManager(ClassLoader classLoader) throws FileSystemException {
    DefaultFileSystemManager fsm = new DefaultFileSystemManager();
    fsm.setReplicator(new DefaultFileReplicator());
    fsm.setDefaultProvider(new UrlFileProvider());
    fsm.addProvider("file", new DefaultLocalFileProvider());
    fsm.addProvider("jar", new FastJarFileProvider());
    fsm.addProvider("res", new ClasspathFileProvider(classLoader));
    fsm.init();
    return fsm;
  }
}
