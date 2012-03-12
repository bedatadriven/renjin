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

package com.bedatadriven.renjin.appengine;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.servlet.ServletContext;

import org.apache.commons.vfs.CacheStrategy;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.cache.NullFilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.LocalFileProvider;
import org.apache.commons.vfs.provider.jar.AppEngineJarFileProvider;
import org.apache.commons.vfs.provider.jar.JarFileProvider;
import org.apache.commons.vfs.provider.url.UrlFileProvider;

import r.lang.Context;
import r.lang.SEXP;
//import r.scripting.RenjinScriptEngineFactory;
import org.renjin.script.*;

import com.google.common.annotations.VisibleForTesting;

public class AppEngineContextFactory {

  private static final Logger LOG = Logger.getLogger(AppEngineContextFactory.class.getName() );

  public static ScriptEngine createScriptEngine(ServletContext servletContext) {
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    return factory.getScriptEngine(createTopLevelContext(servletContext));
  }

  public static Context createTopLevelContext(ServletContext servletContext) {
    FileSystemManager fileSystemManager;
    try {
      fileSystemManager = createFileSystemManager(servletContext);
    } catch (FileSystemException e) {
      LOG.log(Level.SEVERE, "Failed to initialize VFS file system manager", e);
      throw new RuntimeException(e);
    }
    try {

      // initialize our master context here; a fresh but shallow copy will
      // be forked on each incoming request
      Context context = Context.newTopLevelContext(fileSystemManager,
          findHomeDirectory(servletContext), fileSystemManager.resolveFile("file:///"));
      context.getGlobals().setLibraryPaths(computeLibraryPaths(fileSystemManager, servletContext));
      context.init();
      return context;
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Failed to initialize master context", e);
      throw new RuntimeException(e);
    }
  }

  private static String findHomeDirectory(ServletContext servletContext) throws IOException {
    return findHomeDirectory(contextRoot(servletContext),
        SEXP.class.getResource("/r/lang/SEXP.class").getFile());
  }

  public static FileSystemManager createFileSystemManager(ServletContext context) throws FileSystemException {
    final File contextRoot = contextRoot(context);
    return createFileSystemManager(new AppEngineLocalFilesSystemProvider(contextRoot));
  }

  private static File contextRoot(ServletContext context) {
    return new File(context.getRealPath(context.getContextPath()));
  }

  @VisibleForTesting
  static FileSystemManager createFileSystemManager(LocalFileProvider localFileProvider) throws FileSystemException {
    try {
      JarFileProvider jarFileProvider = new AppEngineJarFileProvider();

      // this provides a fake local file system rooted in the servlet context root.
      // this is necessary because on the actual appengine platform, any queries to the ancestors
      // of the servlet context (e.g. /base) will throw a security exception

      DefaultFileSystemManager dfsm = new DefaultFileSystemManager();
      dfsm.addProvider("jar", jarFileProvider);
      dfsm.addProvider("file", localFileProvider);
      dfsm.addExtensionMap("jar", "jar");
      dfsm.setDefaultProvider(new UrlFileProvider());
      dfsm.setFilesCache(new NullFilesCache());
      dfsm.setCacheStrategy(CacheStrategy.ON_RESOLVE);
      dfsm.setBaseFile(new File("/"));
      dfsm.init();

      return dfsm;
    } catch(FileSystemException e) {
      LOG.log(Level.SEVERE, "Failed to initialize file system for development server", e);
      throw new RuntimeException(e);
    }
  }

  @VisibleForTesting
  static String findHomeDirectory(File servletContextRoot, String sexpClassPath) throws IOException {

    LOG.fine("Found SEXP in '" + sexpClassPath);

    File jarFile = jarFileFromResource(sexpClassPath);
    StringBuilder homePath = new StringBuilder();
    homePath.append('/').append(jarFile.getName()).append("!/r");

    File parent = jarFile.getParentFile();
    while(!servletContextRoot.equals(parent)) {
      if(parent==null) {
        throw new IllegalStateException("Expected the renjin-core jar to be in the WEB-INF, bound found it in:\n" +
            jarFile.toString() + "\nAre you sure you are running in a servlet environment?");
      }
      homePath.insert(0, parent.getName());
      homePath.insert(0, '/');
      parent = parent.getParentFile();
    }
    homePath.insert(0, "jar:file://");

    return homePath.toString();
  }

  @VisibleForTesting
  static File jarFileFromResource(String path) {
    int sep = path.indexOf('!');
    if(sep == -1) {
      throw new IllegalStateException("Expected to find renjin-core classes in a jar in the WEB-INF/lib folder." +
          " This probably means that you are not running in a servlet environment and you do not need to use " +
          " AppEngineContextFactory; you may be able to just call Context.newTopLevelContext()");
    }
    String jarPath = path.substring(0,sep);
    if(jarPath.toLowerCase().startsWith("file:")) {
      jarPath = jarPath.substring(5);
    }
    return new File(jarPath);
  }

  /**
   *
   * @param fileSystemManager
   * @param servletContext 
   * @return
   */
  private static String computeLibraryPaths(FileSystemManager fileSystemManager, ServletContext servletContext) {
    File contextRoot = contextRoot(servletContext);
    File webInfFolder = new File(contextRoot, "WEB-INF");
    File libFolder = new File(webInfFolder, "lib");

    StringBuilder paths = new StringBuilder();
    
    for(File lib : libFolder.listFiles()) {
      if(lib.getName().endsWith(".jar")) {
        if(paths.length() > 0) {
          paths.append(";");
        }
        paths.append("jar:file:///WEB-INF/lib/").append(lib.getName()).append("!/");
      }
    }
    return paths.toString();
  }
}
