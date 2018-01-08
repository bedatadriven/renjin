/**
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
package org.renjin.eval;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.pipeliner.VectorPipeliner;
import org.renjin.primitives.Warning;
import org.renjin.primitives.io.connections.ConnectionTable;
import org.renjin.primitives.packaging.DllInfo;
import org.renjin.primitives.packaging.NamespaceRegistry;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.repackaged.guava.collect.ImmutableList;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.*;
import org.renjin.stats.internals.distributions.RNG;
import org.renjin.util.FileSystemUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Outermost context for R evaluation.
 * 
 * <p>The Session corresponds to an R process
 * of the original interpreter, but multiple Renjin Sessions can be
 * live within a single JVM.
 */
public class Session {

  public static final List<String> DEFAULT_PACKAGES = ImmutableList.of(
      "stats",  "graphics", "grDevices", "utils",  "datasets", "methods");
  
  private final Context topLevelContext;

  private FinalizerRegistry finalizers = null;

  /**
   * The map of environment variables exposed to 
   * the R code. Initialized to System.getenv() but
   * can be modified.
   */
  private final Map<String, String> systemEnvironment;

  /***
   * Registry containing all namespaces that have been loaded
   * into this session
   */
  private final NamespaceRegistry namespaceRegistry;

  /**
   * The R_HOME path. This is the path from which the base package is loaded.
   */
  private final String homeDirectory;

  /**
   * The base package environment
   */
  private final Environment baseEnvironment;
  
  /**
   * This session's global environment
   */
  private final Environment globalEnvironment;
  
  /**
   * This session's base namespace environment.
   */
  private final Environment baseNamespaceEnv;

  private final FileSystemManager fileSystemManager;
  
  private SecurityManager securityManager;
  
  private Map<Class, Object> singletons = Maps.newHashMap();
  
  private final ConnectionTable connectionTable = new ConnectionTable();

  private FileObject workingDirectory;
  
  private StringVector commandLineArguments = StringVector.valueOf("renjin");
  
  public RNG rng = new RNG(this);
   
  private SessionController sessionController = new SessionController();
  
  private VectorPipeliner vectorPipeliner;

  private ClassLoader classLoader;

  /**
   * Dynamic libraries that have been loaded for this session.
   */
  private List<DllInfo> loadedLibraries = new ArrayList<>();


  /**
   * Whether the result of the evaluation should be "invisible" in a
   * REPL
   */
  boolean invisible;


  /**
   * When this is set, we are evaluating a calling handler and should only match handlers at this
   * level or higher.
   */
  Context conditionStack = null;


  Session(FileSystemManager fileSystemManager,
          ClassLoader classLoader,
          PackageLoader packageLoader,
          ExecutorService executorService, Frame globalFrame) {
    this.fileSystemManager = fileSystemManager;
    this.classLoader = classLoader;
    this.homeDirectory = FileSystemUtils.homeDirectoryInCoreJar();
    this.workingDirectory = FileSystemUtils.workingDirectory(fileSystemManager);
    this.systemEnvironment = Maps.newHashMap(System.getenv()); //load system environment variables
    this.baseEnvironment = Environment.createBaseEnvironment();
    this.globalEnvironment = Environment.createGlobalEnvironment(baseEnvironment, globalFrame);
    this.baseNamespaceEnv = Environment.createBaseNamespaceEnvironment(globalEnvironment, baseEnvironment).build();
    this.topLevelContext = new Context(this);
    this.baseNamespaceEnv.setVariableUnsafe(Symbol.get(".BaseNamespaceEnv"), baseNamespaceEnv);

    namespaceRegistry = new NamespaceRegistry(packageLoader, topLevelContext, baseNamespaceEnv);
    securityManager = new SecurityManager();

    this.vectorPipeliner = new VectorPipeliner(executorService);


    // TODO(alex)
    // several packages rely on the presence of .Random.seed in the global
    // even though it's an implementation detail.
    globalEnvironment.setVariable(topLevelContext, ".Random.seed", IntVector.valueOf(1));
  }


  public void setStdOut(PrintWriter writer) {
    this.connectionTable.getStdout().setStream(writer);
  }

  public void setStdIn(Reader reader) {
    this.connectionTable.getStdin().setReader(reader);
  }
  
  public void setStdErr(PrintWriter writer) {
    this.connectionTable.getStderr().setStream(writer);
  }
  
  public SessionController getSessionController() {
    return sessionController;
  }
  
  /**
   * Retrieves the singleton associated with this session.
   * @param clazz
   * @return
   */
  public <X> X getSingleton(Class<X> clazz) {
    if(clazz == NamespaceRegistry.class) {
      return (X)namespaceRegistry;
    }
    X instance = (X) singletons.get(clazz);
    if(instance == null) {
      try {
        instance = clazz.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Can instantiate singleton " + clazz.getName() + 
            ": the class must have a public default constructor", e);
      }
      singletons.put(clazz, instance);
    }
    return instance;
  }

  public void setSessionController(SessionController sessionController) {
    this.sessionController = sessionController;
  }
  
  public RNG getRNG() {
    return rng;
  }

  public Environment getGlobalEnvironment() {
    return globalEnvironment;
  }

  public ConnectionTable getConnectionTable() {
    return connectionTable;
  }

  public void setWorkingDirectory(FileObject dir) {
    this.workingDirectory = dir;
  }


  public void setWorkingDirectory(File dir) throws FileSystemException {
    this.workingDirectory = fileSystemManager.resolveFile(dir.getAbsolutePath());
  }
  
  public FileObject getWorkingDirectory() {
    return workingDirectory;
  }
  
  public VectorPipeliner getVectorEngine() {
    return vectorPipeliner;
  }
  
  public void setCommandLineArguments(String executableName, String... arguments) {
    commandLineArguments = new StringArrayVector(Lists.asList(executableName, arguments));
  }
  
  public void setCommandLineArguments(String executableName, List<String> arguments) {
    List<String> commandLine = Lists.newArrayList();
    commandLine.add(executableName);
    commandLine.addAll(arguments);
    commandLineArguments = new StringArrayVector(commandLine);
  }
  
  public StringVector getCommandLineArguments() {
    return commandLineArguments;
  }

  public boolean isInvisible() {
    return invisible;
  }

  /**
   *
   * Returns the standard output stream associated with this Session. This is always the original
   * stream provided via {@link #setStdOut(PrintWriter)} even if there is an active sink.
   */
  public PrintWriter getStdOut() {
    return connectionTable.getStdout().getStream();
  }

  /**
   * Returns the standard output stream associated with this Stream or the sink writer if a sink
   * is active.
   */
  public PrintWriter getEffectiveStdOut() {
    return connectionTable.getStdout().getOpenPrintWriter();
  }

  /**
   *
   * Returns the standard error stream associated with this Session. This is always the original
   * stream provided via {@link #setStdErr(PrintWriter)} even if there is an active sink.
   */
  public PrintWriter getStdErr() {
    return connectionTable.getStderr().getStream();
  }

  /**
   * Returns the standard error stream associated with this Session or the sink writer if one is active.
   */
  public PrintWriter getEffectiveStdErr() {
    return connectionTable.getStderr().getOpenPrintWriter();
  }

  public Reader getStdIn() {
    return connectionTable.getStdin().getReader();
  }

  public NamespaceRegistry getNamespaceRegistry() {
    return namespaceRegistry;
  }

  public Context getTopLevelContext() {
    return topLevelContext;
  }

  public FileSystemManager getFileSystemManager() {
    return fileSystemManager;
  }

  public Environment getBaseEnvironment() {
    return baseEnvironment;
  }

  public Environment getBaseNamespaceEnv() {
    return baseNamespaceEnv;
  }

  public String getHomeDirectory() {
    return homeDirectory;
  }

  public Map<String, String> getSystemEnvironment() {
    return systemEnvironment;
  }

  public SecurityManager getSecurityManager() {
    return securityManager;
  }

  public void setSecurityManager(SecurityManager securityManager) {
    this.securityManager = securityManager;
  }
  
  public ClassLoader getClassLoader() {
    return classLoader;
  }



  public void registerFinalizer(SEXP sexp, FinalizationHandler handler, boolean onExit) {
    if(finalizers == null) {
      finalizers = new FinalizerRegistry();
    }
    finalizers.register(sexp, handler, onExit);
  }

  /**
   * Invokes any registered finalizers for Environments that have been queued
   * for garbage collection. This method, if invoked, must be called from this session's
   * thread to avoid undefined effects resulting from executing the finalizers concurrently
   * with other session evaluation.
   */
  public void runFinalizers() {
    if(finalizers != null) {
      finalizers.finalizeDisposedEnvironments(topLevelContext);
    }
  }


  /**
   * Invokes any on.exit() methods registered with the top level context and
   * any finalizers registered with reg.finalizer(on.exit = TRUE)
   */
  public void close() {
    topLevelContext.exit();
    if(finalizers != null) {
      finalizers.finalizeOnExit(topLevelContext);
    }
  }

  public MethodHandle getRngMethod() {
    return rng.getMethodHandle();
  }

  public void loadLibrary(DllInfo library) {
    loadedLibraries.add(library);
  }

  public Iterable<DllInfo> getLoadedLibraries() {
    return loadedLibraries;
  }


  public void printWarnings() {
    SEXP warnings = baseEnvironment.getVariable(topLevelContext, Warning.LAST_WARNING);
    if(warnings != Symbol.UNBOUND_VALUE) {
      topLevelContext.evaluate( FunctionCall.newCall(Symbol.get("print.warnings"), warnings),
          topLevelContext.getBaseEnvironment());

    }
  }

  public void clearWarnings() {
    baseEnvironment.remove(Warning.LAST_WARNING);
  }
}