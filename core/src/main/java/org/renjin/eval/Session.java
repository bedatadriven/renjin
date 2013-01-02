package org.renjin.eval;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;


import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.graphics.ColorPalette;
import org.renjin.primitives.io.connections.ConnectionTable;
import org.renjin.primitives.io.serialization.RDatabase;
import org.renjin.primitives.packaging.NamespaceRegistry;
import org.renjin.primitives.packaging.PackageLoader;
import org.renjin.primitives.random.RNG;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Frame;
import org.renjin.sexp.HashFrame;
import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Outermost context for R evaluation.
 * 
 * <p>The Session corresponds to an R process
 * of the original interpreter, but multiple Renjin Sessions can be
 * live within a single JVM.
 */
public class Session {
  
  private final Context topLevelContext;
  
  /**
   * This is the environment
   */
  public final Map<String, String> systemEnvironment;

  public final NamespaceRegistry namespaceRegistry;

  /**
   * The R_HOME path. This is the path from which the base package is loaded.
   */
  public final String homeDirectory;

  public final Environment baseEnvironment;
  public final Environment globalEnvironment;
  public final Environment baseNamespaceEnv;

  public final FileSystemManager fileSystemManager;
  
  public SecurityManager securityManager;
  
  private Map<Class, Object> singletons = Maps.newHashMap();
  
  private ColorPalette colorPalette = new ColorPalette();

  private final ConnectionTable connectionTable = new ConnectionTable();

  /**
   * Package database cache to speed up lazy loading of package
   * members
   */
  private final Cache<String, RDatabase> packageDatabaseCache = CacheBuilder.newBuilder()
        .weakValues()
        .build();

  // can this be moved down to context so it's not global?
  public FileObject workingDirectory;
  
  private StringVector commandLineArguments = StringVector.valueOf("renjin");
  
  public RNG rng = new RNG(this);
   
  private SessionController sessionController = new SessionController();
  
  /**
   * Whether the result of the evaluation should be "invisible" in a
   * REPL
   */
  boolean invisible;

  Session(Context topLevelContext, FileSystemManager fileSystemManager, String homeDirectory,
                  FileObject workingDirectory) {
    this.topLevelContext = topLevelContext;
    this.fileSystemManager = fileSystemManager;
    this.homeDirectory = homeDirectory;
    this.workingDirectory = workingDirectory;

    systemEnvironment = Maps.newHashMap(System.getenv()); //load system environment variables
    globalEnvironment = Environment.createGlobalEnvironment();
    baseEnvironment = globalEnvironment.getBaseEnvironment();
    baseNamespaceEnv = Environment.createBaseNamespaceEnvironment(globalEnvironment);
    baseNamespaceEnv.setVariable(Symbol.get(".BaseNamespaceEnv"), baseNamespaceEnv);
    namespaceRegistry = new NamespaceRegistry(new PackageLoader(),  topLevelContext, baseNamespaceEnv);
    securityManager = new SecurityManager(); 
    
    // quick fix: more work needs to be done to figure out where 
    // to put this, but in the meantime some packages require its presence
    globalEnvironment.setVariable(".Random.seed", new IntArrayVector(1));
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
  
  public ConnectionTable getConnectionTable() {
    return connectionTable;
  }

  public ColorPalette getColorPalette() {
    return colorPalette;
  }

  public Cache<String, RDatabase> getPackageDatabaseCache() {
    return packageDatabaseCache;
  }
  
  public void setCommandLineArguments(String executableName, String... arguments) {
    commandLineArguments = new StringArrayVector(Lists.asList(executableName, arguments));
  }
  
  public StringVector getCommandLineArguments() {
    return commandLineArguments;
  }

  public void setColorPalette(ColorPalette colorPalette) {
    this.colorPalette = colorPalette;
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
}