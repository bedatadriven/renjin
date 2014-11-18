package org.renjin.eval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.compiler.pipeline.SimpleVectorPipeliner;
import org.renjin.compiler.pipeline.VectorPipeliner;
import org.renjin.primitives.io.connections.ConnectionTable;
import org.renjin.primitives.packaging.NamespaceRegistry;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.sexp.*;
import org.renjin.stats.internals.distributions.RNG;
import org.renjin.util.FileSystemUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Outermost context for R evaluation.
 * 
 * <p>The Session corresponds to an R process
 * of the original interpreter, but multiple Renjin Sessions can be
 * live within a single JVM.
 */
public class Session {
  
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

  /**
   * Whether the result of the evaluation should be "invisible" in a
   * REPL
   */
  boolean invisible;

  Session(Map<Class, Object> bindings) {
    this.fileSystemManager = (FileSystemManager) bindings.get(FileSystemManager.class);
    this.homeDirectory = FileSystemUtils.homeDirectoryInCoreJar();
    this.workingDirectory = FileSystemUtils.workingDirectory(fileSystemManager);
  
    this.systemEnvironment = Maps.newHashMap(System.getenv()); //load system environment variables
    this.globalEnvironment = Environment.createGlobalEnvironment();
    this.baseEnvironment = globalEnvironment.getBaseEnvironment();
    this.baseNamespaceEnv = Environment.createBaseNamespaceEnvironment(globalEnvironment);
    this.baseNamespaceEnv.setVariable(Symbol.get(".BaseNamespaceEnv"), baseNamespaceEnv);
    this.topLevelContext = new Context(this);

    namespaceRegistry = new NamespaceRegistry((PackageLoader) bindings.get(PackageLoader.class),  topLevelContext, baseNamespaceEnv);
    securityManager = new SecurityManager(); 
    
    if(bindings.containsKey(VectorPipeliner.class)) {
      vectorPipeliner = (VectorPipeliner) bindings.get(VectorPipeliner.class);
    } else {
      vectorPipeliner = new SimpleVectorPipeliner();
    }

    // TODO(alex)
    // several packages rely on the presence of .Random.seed in the global
    // even though it's an implementation detail.
    globalEnvironment.setVariable(".Random.seed", IntVector.valueOf(1)); 
  }

  /** 
   * Sets the paths in which to search for libraries.
   *
   * @param paths a semi-colon delimited list of paths
   */
  public void setLibraryPaths(String paths) {
    systemEnvironment.put("R_LIBS", paths);
  }


  public void setStdOut(PrintWriter writer) {
    this.connectionTable.getStdout().setOutputStream(writer);
  }
  
  public void setStdErr(PrintWriter writer) {
    this.connectionTable.getStderr().setOutputStream(writer);
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
  
  public Environment getGlobalEnvironment() {
    return globalEnvironment;
  }

  public ConnectionTable getConnectionTable() {
    return connectionTable;
  }

  public void setWorkingDirectory(FileObject dir) {
    this.workingDirectory = dir;
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
  
  public StringVector getCommandLineArguments() {
    return commandLineArguments;
  }

  public boolean isInvisible() {
    return invisible;
  }

  public PrintWriter getStdOut() throws IOException {
    return connectionTable.getStdout().getPrintWriter();
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

  public void registerFinalizer(Environment environment, Closure function, boolean onExit) {
    if(finalizers == null) {
      finalizers = new FinalizerRegistry();
    }
    finalizers.register(environment, function, onExit);
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
}