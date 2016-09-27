/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.renjin.eval.vfs.FastJarFileProvider;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;

import java.io.File;

public class FileSystemUtils {
  

  /**
   *
   * @return  the path to the R home directory as packaged within
   * the renjin-core.jar archive. This will be a layered URI in the form
   * jar:file:///path/to/renjin-core.jar!/org/renjin/library
   */
  public static String homeDirectoryInCoreJar() {
    // hardcode to the R home location to the classpath location
    // where this class is found.

    return embeddedRHomeFromSEXPClassURL(org.renjin.primitives.System.class.getResource("/org/renjin/sexp/SEXP.class").toString());
  }

  public static String homeDirectoryInLocalFs() {
    return localRHomeFromSEXPClassURL(org.renjin.primitives.System.class.getResource("/org/renjin/sexp/SEXP.class").toString());

  }

  @VisibleForTesting
  public static String embeddedRHomeFromSEXPClassURL(String url) {
    String homeUrl = url.substring(0, url.length() - "/org/renjin/sexp/SEXP.class".length()) + "/org/renjin";
    if(homeUrl.startsWith("file:/")) {
      homeUrl = homeUrl.substring("file:".length());
    }
    return homeUrl;
  }
  
  @VisibleForTesting
  public static String localRHomeFromSEXPClassURL(String url) {
    String homeUrl = url.substring(0, url.length() - "/org/renjin/sexp/SEXP.class".length());
    
    if(homeUrl.endsWith(".jar!") && homeUrl.startsWith("jar:file:")) {

      int depDir = homeUrl.lastIndexOf('/');
      int homeDir = homeUrl.lastIndexOf('/', depDir-1);
      if(depDir == -1 || homeDir == -1) {
        throw new IllegalStateException("Can't figure out the R_HOME from the jar location '" + homeUrl + "'");
      }
      return homeUrl.substring("jar:".length(), homeDir);
    } else {

      return url.substring(0, url.length() - "/sexp/SEXP.class".length());

//      // file:/home/alex/dev/renjin/core/target/classes/org/renjin/sexp/SEXP.class
//      throw new UnsupportedOperationException();
    }
  }

  /**
   *
   * @return a semi-colon delimited set of paths in which to search for libraries
   * that includes all jars and directories on the classpath ({@code System.getProperty(java.class.path)}
   */
  public static String libraryPathsFromClassPath() {
    return libraryPathsFromClassPath(System.getProperty("java.class.path"));
  }

  @VisibleForTesting
  public static String libraryPathsFromClassPath(String classPathString) {
    StringBuilder path = new StringBuilder();
    if(classPathString != null) {
      String classPaths[] = classPathString.split(File.pathSeparator);
      for(String classPath : classPaths) {
        String libraryPath = libraryPathFromClassPathEntry(classPath);
        if(libraryPath != null) {
          if(path.length() != 0) {
            path.append(";");
          }
          path.append(libraryPath);
        }
      }
    }
    return path.toString();
  }

  public static String libraryPathFromClassPathEntry(String classPath) {
    if(classPath.endsWith(".jar")) {
      return libraryPathFromJarFile(classPath);
    } else {
      return libraryPathFromFolder(classPath);
    }
  }

  public static String libraryPathFromFolder(String classPath) {
    try {
      return new File(classPath).getAbsolutePath();
    } catch(Exception e) {
      // we can get security exceptions for some jars
      return null;
    }
  }
  

  public static String libraryPathFromJarFile(String classPath)  {
    return "jar:file://" + absolutePath(classPath);  
  }
  
  private static String absolutePath(String path) {
    return new File(path).getAbsolutePath();
  }

  public static FileObject workingDirectory(FileSystemManager fileSystemManager) {
    try {
      return fileSystemManager.resolveFile(new File("").getAbsolutePath());
    } catch (FileSystemException e) {
      throw new RuntimeException("Could not resolve current working directory", e);
    }
  }

  public static FileSystemManager getMinimalFileSystemManager() throws FileSystemException {
    DefaultFileSystemManager fsm = new DefaultFileSystemManager();
    fsm.setDefaultProvider(new UrlFileProvider());
    fsm.addProvider("file", new DefaultLocalFileProvider());
    fsm.addProvider("jar", new FastJarFileProvider());
    fsm.addProvider("res", new ResourceFileProvider());
    fsm.init();
    return fsm;
  }
}
