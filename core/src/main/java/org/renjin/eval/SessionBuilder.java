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
package org.renjin.eval;

import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.util.concurrent.MoreExecutors;
import org.renjin.sexp.Frame;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.PairList;
import org.renjin.sexp.Symbol;
import org.renjin.util.FileSystemUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class SessionBuilder {

  private boolean loadBasePackage = true;
  private List<String> packagesToLoad = Lists.newArrayList();

  private FileSystemManager fileSystemManager;
  private PackageLoader packageLoader;
  private ClassLoader classLoader;
  private ExecutorService executorService = null;

  private Frame globalFrame = new HashFrame();

  public SessionBuilder() {

  }

  /**
   *
   * @param fsm
   * @return
   * @deprecated see {@link #setFileSystemManager(FileSystemManager)}
   */
  @Deprecated
  public SessionBuilder withFileSystemManager(FileSystemManager fsm) {
    return setFileSystemManager(fsm);
  }
  
  /**
   * Disables loading of the R-Language portions of the base package:
   * primitives will still be available but none of the functions in the base 
   * package will be loaded. 
   */
  public SessionBuilder withoutBasePackage() {
    this.loadBasePackage = false;
    return this;
  }
  
  /**
   * Loads the default packages for R 3.3.2 (stats, graphics, grDevices, utils, datasets, methods)
   */
  public SessionBuilder withDefaultPackages() {
    packagesToLoad = Session.DEFAULT_PACKAGES;
    return this;
  }

  /**
   * Sets the {@link FileSystemManager} used to implement calls to R's builtin functions.
   *
   * <p>By default, the new {@code Session} will use {@link FileSystemUtils#getMinimalFileSystemManager()},
   * but a custom {@code FileSystemManager} can be provided to limit or customize the access of R scripts
   * to the filesystem.</p>
   *
   * @param fileSystemManager
   */
  public SessionBuilder setFileSystemManager(FileSystemManager fileSystemManager) {
    this.fileSystemManager = fileSystemManager;
    return this;
  }

  /**
   * Sets the {@link PackageLoader} implementation to be used for loading R packages by the new {@code Session}.
   *
   * <p>By default, the new {@code Session} will use a {@link ClasspathPackageLoader} with the provided
   * {@code ClassPathLoader}, or this class' {@code ClassPathLoader} if none is provided.
   *
   * <p>If new {@code Session} should load packages from remote repositories on demand, you can use the
   * {@code AetherPackageLoader} from the {@code renjin-aether-package-loader} module.
   *
   */
  public SessionBuilder setPackageLoader(PackageLoader packageLoader) {
    this.packageLoader = packageLoader;
    return this;
  }

  /**
   * Sets the {@link ExecutorService} to use for parallelizing work for this {@code Session}.
   *
   * <p>By default, {@link MoreExecutors#sameThreadExecutor()} is called to obtain an {@code ExecutorService}
   * instance for the new {@code Session}, but callers can provider their own {@code ExecutorService} to enable
   * multi-threading.</p>
   *
   * @see java.util.concurrent.Executors
   * @see MoreExecutors
   */
  public SessionBuilder setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }


  /**
   * Sets the {@link ClassLoader} to use to resolve JVM classes by the {@code import()} builtin.
   */
  public SessionBuilder setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  /**
   * Sets the {@link Frame} that backs the Global Environment.
   */
  public SessionBuilder setGlobalFrame(Frame globalFrame) {
    this.globalFrame = globalFrame;
    return this;
  }

  /**
   * Binds a Renjin interface to its implementation
   * @deprecated Use {@link #setFileSystemManager(FileSystemManager)}
   */
  @Deprecated
  public <T> SessionBuilder bind(Class<T> clazz, T instance) {
    if(clazz.equals(FileSystemManager.class)) {
      setFileSystemManager((FileSystemManager) instance);
    } else if(clazz.equals(PackageLoader.class)) {
      setPackageLoader((PackageLoader) instance);
    } else if(clazz.equals(ClassLoader.class)) {
      setClassLoader((ClassLoader) instance);
    } else {
      // Do nothing: this was the behavior of the previous
      // implementation.
    }
    return this;
  }

  public Session build() {
    try {

      if(fileSystemManager == null) {
        fileSystemManager = FileSystemUtils.getMinimalFileSystemManager();
      }

      if(classLoader == null) {
        classLoader = getClass().getClassLoader();
      }

      if(packageLoader == null) {
        packageLoader = new ClasspathPackageLoader(classLoader);
      }

      if(executorService == null) {
        executorService = MoreExecutors.sameThreadExecutor();
      }

      Session session = new Session(fileSystemManager, classLoader, packageLoader, executorService,
          globalFrame);
      if(loadBasePackage) {
        session.getTopLevelContext().init();
      }

      for (int i = packagesToLoad.size()-1; i >= 0; i--) {
        session.getTopLevelContext().evaluate(PairList.Node.newCall(Symbol.get("library"),
            Symbol.get(packagesToLoad.get(i))));
      }
      return session;
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public static Session buildDefault() {
    return new SessionBuilder().build();
  }
}
