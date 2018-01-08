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
package org.renjin.appengine;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.cache.NullFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.LocalFileProvider;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.eval.vfs.FastJarFileProvider;
import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.script.RenjinScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AppEngineContextFactory {

  private static final Logger LOG = Logger.getLogger(AppEngineContextFactory.class.getName() );

  public static ScriptEngine createScriptEngine(ServletContext servletContext) {
    RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
    return factory.getScriptEngine(createSession(servletContext));
  }

  public static Session createSession(ServletContext servletContext) {
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
      Session session = new SessionBuilder()
          .withFileSystemManager(fileSystemManager)
          .withDefaultPackages()
          .build();

      session.setWorkingDirectory(fileSystemManager.resolveFile("file:///"));

      return session;
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Failed to initialize master context", e);
      throw new RuntimeException(e);
    }
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
      FastJarFileProvider jarFileProvider = new FastJarFileProvider();

      // this provides a fake local file system rooted in the servlet context root.
      // this is necessary because on the actual appengine platform, any queries to the ancestors
      // of the servlet context (e.g. /base) will throw a security exception

      DefaultFileSystemManager dfsm = new DefaultFileSystemManager();
      dfsm.addProvider("jar", jarFileProvider);
      dfsm.addProvider("file", localFileProvider);
      dfsm.addProvider("res", new ResourceFileProvider());
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
    homePath.append('/').append(jarFile.getName()).append("!/org/renjin");

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
}
