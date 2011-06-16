/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.util;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;

import java.io.File;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileSystemUtils {


  /**
   *
   * @return  the path to the R home directory as packaged within
   * the renjin-core.jar archive. This will be a layered URI in the form
   * jar:file:///path/to/renjin-core.jar!/r/library
   */
  public static String homeDirectoryInCoreJar() {
    // hardcode to the R home location to the classpath location
    // where this class is found.
    // R_LIBS will contain ALL the /r/library paths found on the classpath

    return RHomeFromSEXPClassURL(r.base.System.class.getResource("/r/lang/SEXP.class").toString());
  }


  @VisibleForTesting
  public static String RHomeFromSEXPClassURL(String url) {
    String homeUrl = url.substring(0, url.length() - "/r/lang/SEXP.class".length()) + "/r";
    if(homeUrl.startsWith("jar:file")) {

    }
    if(homeUrl.startsWith("file:/")) {
      homeUrl = homeUrl.substring("file:".length());
    }
    return homeUrl;
  }

  /**
   *
   * @return a default, semi-colon delimited, set of paths in which to search for libraries
   * that includes all jars and directories on the classpath ({@code System.getProperty(java.class.path)}
   * that contains an {@code r/library} subdirectory.
   */
  public static String defaultLibraryPaths() {
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
      File file = new File(new File(classPath, "r"), "library");
      if(file.exists() && file.isDirectory()) {
        return file.getAbsolutePath();
      } else {
        return null;
      }
    } catch(Exception e) {
      // we can get security exceptions for some jars
      return null;
    }
  }

  @VisibleForTesting
  public static String libraryPathFromJarFile(String classPath)  {
    try {
      JarFile jarFile = new JarFile(classPath);
      JarEntry entry = jarFile.getJarEntry("r/library");
      jarFile.close();
      if(entry != null) {
        return "jar:file://" + absolutePath(classPath) + "!/r/library";
      }
    } catch (Exception e) {
    } finally {
      
    }
    return null;
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
}
