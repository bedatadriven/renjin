package org.renjin.eval;

import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.primitives.packaging.ClasspathPackageLoader;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.util.concurrent.MoreExecutors;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.Symbol;
import org.renjin.util.FileSystemUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class SessionBuilder {

  private boolean loadBasePackage = true;
  private List<String> packagesToLoad = Lists.newArrayList();
  
  private FileSystemManager fileSystemManager = null;
  private ExecutorService executorService = null;
  private PackageLoader packageLoader = null;
 
  public SessionBuilder() {
  }

  /**
   * Sets the {@link FileSystemManager} to use for this session. Base file functions like {@code file()},
   * {@code list.files()}, etc, are mediated through this instance.  
   * 
   * @deprecated Use {@link SessionBuilder#setFileSystemManager(FileSystemManager)}.
   */
  @Deprecated
  public SessionBuilder withFileSystemManager(FileSystemManager fsm) {
    setFileSystemManager(fsm);
    return this;
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
   * Loads the default packages for R 2.14.2 (stats, utils, graphics, grDevices, datasets, methods)
   */
  public SessionBuilder withDefaultPackages() {
    packagesToLoad = Session.DEFAULT_PACKAGES;
    return this;
  }

  /**
   * Sets the {@link FileSystemManager} to use for this session. Base file functions like {@code file()},
   * {@code list.files()}, etc, are mediated through this instance. 
   * 
   * <p>By default, a {@code FileSystemManager} is instantiated for the new {@code Session} via 
   * {@link FileSystemUtils#getMinimalFileSystemManager()}, which provides access to the local file system,  as 
   * well as the classpath via the {@code res:} scheme.
   * 
   * <p>Callers can, however, customize the execution environment of this {@code Session} by providing 
   * their own {@code FileSystemManager} instance, which might limit access to the local file system, or 
   * provide an entirely different virtual filesystem.</p>
   * 
   * @see <a href="https://commons.apache.org/proper/commons-vfs/">Apache Commons Virtual File System</a>
   */
  public SessionBuilder setFileSystemManager(FileSystemManager manager) {
    this.fileSystemManager = manager;
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
   * Sets the {@link PackageLoader} implementation to use for loading packages in this {@code Session}.
   * 
   * <p>Calls to {@code library()} and {@code require()} rely on the {@code PackageLoader} interface, which is 
   * responsible for locating packages and accessing their resources.</p>
   * 
   * <p>By default, the {@link ClasspathPackageLoader} is used, which loads packages using the {@link ClassLoader}
   * associated with this {@code SessionBuilder}. 
   * 
   * <p>Callers can change this behavior by providing their own {@code packageLoader}. Renjin's
   * interactive REPL module, for example, uses the {@code AetherPackageLoader} to load packages on demand
   * from remote Maven repositories.</p>
   */
  public SessionBuilder setPackageLoader(PackageLoader packageLoader) {
    this.packageLoader = packageLoader;
    return this;
  }
  
  /**
   * Binds a Renjin interface to its implementation
   * @deprecated Use one of the {@link SessionBuilder#setPackageLoader(PackageLoader)}, 
   * {@link SessionBuilder#setExecutorService(ExecutorService)} or 
   * {@link SessionBuilder#setFileSystemManager(FileSystemManager)}
   * methods
   */
  @Deprecated
  public <T> SessionBuilder bind(Class<T> clazz, T instance) {
    if(clazz.equals(PackageLoader.class)) {
      return setPackageLoader((PackageLoader) instance);
    } else if(clazz.equals(FileSystemManager.class)) {
      return setFileSystemManager((FileSystemManager) instance);
    } else {
      throw new UnsupportedOperationException("class: " + clazz.getName());
    }
  }
  
  public Session build() {
    try {
      if(fileSystemManager == null) {
        fileSystemManager = FileSystemUtils.getMinimalFileSystemManager();
      }
      if(packageLoader == null) {
        packageLoader = new ClasspathPackageLoader();
      }
      if(executorService == null) {
        executorService = MoreExecutors.sameThreadExecutor();
      }
         
      Session session = new Session(fileSystemManager, packageLoader, executorService);
      if(loadBasePackage) {
        session.getTopLevelContext().init();
      }
      for(String packageToLoad : packagesToLoad) {
        session.getTopLevelContext().evaluate(FunctionCall.newCall(Symbol.get("library"),
            Symbol.get(packageToLoad)));
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
