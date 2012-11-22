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

package org.renjin.eval;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.compiler.ir.tac.IRFunctionTable;
import org.renjin.graphics.ColorPalette;
import org.renjin.graphics.GraphicsDevices;
import org.renjin.parser.RParser;
import org.renjin.primitives.io.connections.ConnectionTable;
import org.renjin.primitives.io.serialization.RDatabase;
import org.renjin.primitives.random.RNG;
import org.renjin.sexp.*;
import org.renjin.util.FileSystemUtils;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contexts are the internal mechanism used to keep track of where a
 * computation has got to (and from where),
 * so that control-flow constructs can work and reasonable information
 * can be produced on error conditions,
 * (such as via traceback) and otherwise (the sys.xxx functions).
 */
public class Context {

  public final static boolean USE_IR = false;

  public final static boolean PRINT_IR = false;



  public enum Type {
    /** toplevel context */
    TOP_LEVEL,

    /** target for next */
    NEXT,

    /** target for break */
    BREAK,

    /** break or next target */
    LOOP,

    /** function closure */
    FUNCTION,

    /** other functions that need error cleanup */
    CCODE,

    /** return() from a closure */
    RETURN,

    /** return target on exit from browser */
    BROWSER,

    /** rather, running an S3 method */
    GENERIC,

    /** a call to restart was made from a closure */
    RESTART,

    /** builtin internal function */
    BUILTIN
  }

  private List<SEXP> onExit = Lists.newArrayList();

    
  public static class Options {
    private Map<String, SEXP> map;

    public Options() {
      map = Maps.newHashMap();
      map.put("prompt", new StringArrayVector("> "));
      map.put("continue", new StringArrayVector("+ "));
      map.put("expressions" , new IntArrayVector(5000));
      map.put("width", new IntArrayVector(80));
      map.put("digits", new IntArrayVector(7));
      map.put("echo", new LogicalArrayVector(false));
      map.put("verbose", new LogicalArrayVector(false));
      map.put("check.bounds", new LogicalArrayVector(false));
      map.put("keep.source", new LogicalArrayVector(true));
      map.put("keep.source.pkgs", new LogicalArrayVector(false));
      map.put("warnings.length", new IntArrayVector(1000));
      map.put("OutDec", new StringArrayVector("."));
    }

    private Options(Options toCopy) {
      map = Maps.newHashMap(toCopy.map);
    }

    public SEXP get(String name) {
      SEXP value = map.get(name);
      return value == null ? Null.INSTANCE : value;
    }
    
    public int getInt(String name, int defaultValue) {
      SEXP value = get(name);
      if(value instanceof AtomicVector && value.length() >= 1) {
        return ((AtomicVector)value).getElementAsInt(0);
      }
      return defaultValue;
    }

    public SEXP set(String name, SEXP value) {
      SEXP old = map.put(name, value);
      return old == null ? Null.INSTANCE : value;
    }

    public Set<String> names() {
      return map.keySet();
    }

    public Options clone() {
      return new Options(this);
    }
  }

  /**
   * Parking lot for random global variables from the C-implementation of R
   *  that we need to do something with.
   */
  public static class Globals {

    public final Options options;

    public IRFunctionTable functionTable = new IRFunctionTable();
    
    /**
     * This is the environment
     */
    public final Map<String, String> systemEnvironment;

    public final Frame namespaceRegistry;

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
    
    private GraphicsDevices graphicsDevices = new GraphicsDevices();
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
    private boolean invisible;

    private Globals(FileSystemManager fileSystemManager, String homeDirectory,
                    FileObject workingDirectory) {
      this.fileSystemManager = fileSystemManager;
      this.homeDirectory = homeDirectory;
      this.workingDirectory = workingDirectory;

      systemEnvironment = Maps.newHashMap(System.getenv()); //load system environment variables
      options = new Options();
      globalEnvironment = Environment.createGlobalEnvironment();
      baseEnvironment = globalEnvironment.getBaseEnvironment();
      namespaceRegistry = new HashFrame();
      baseNamespaceEnv = Environment.createBaseNamespaceEnvironment(globalEnvironment);
      baseNamespaceEnv.setVariable(Symbol.get(".BaseNamespaceEnv"), baseNamespaceEnv);
      namespaceRegistry.setVariable(Symbol.get("base"), baseNamespaceEnv);
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

    private Globals(Globals toShare) {
      this.homeDirectory = toShare.homeDirectory;
      this.fileSystemManager = toShare.fileSystemManager;
      this.systemEnvironment = Maps.newHashMap(toShare.systemEnvironment);
      this.globalEnvironment = Environment.forkGlobalEnvironment(toShare.globalEnvironment);
      this.baseEnvironment = toShare.baseEnvironment;
      this.namespaceRegistry = toShare.namespaceRegistry;
      this.baseNamespaceEnv = toShare.baseNamespaceEnv;
      namespaceRegistry.setVariable(Symbol.get("base"), baseNamespaceEnv);
      globalEnvironment.setVariable(Symbol.get(".BaseNamespaceEnv"), baseNamespaceEnv);
      workingDirectory = toShare.workingDirectory;
      options = toShare.options.clone();
    }

    public void setStdOut(PrintWriter writer) {
      this.connectionTable.getStdout().setOutputStream(writer);
    }
    
    public GraphicsDevices getGraphicsDevices() {
      return graphicsDevices;
    }

    public SEXP getOption(String name) {
      return options.get(name);
    }
    
    public SessionController getSessionController() {
      return sessionController;
    }
    
    /**
     * Retrieves the singleton associated with this apartment.
     * @param clazz
     * @return
     */
    public <X> X getSingleton(Class<X> clazz) {
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
  }


  private Context parent;
  private int evaluationDepth;
  private Type type;
  private Environment environment;
  private Globals globals;
  private FunctionCall call;
  private Closure closure;
  private PairList arguments = Null.INSTANCE;

  /**
   * Conditions are analogous to Exceptions.
   * Handlers are R functions that are called immediately when
   * conditions are signaled.
   */
  private Map<String, SEXP> conditionHandlers = Maps.newHashMap();

  private Map<Class, Object> stateMap = null;

  private Context() {
  }

  /**
   *
   * @return a new top level context using the default VFS FileSystemManager and the
   * renjin-core jar as the R_HOME directory.
   *
   * @see org.apache.commons.vfs2.VFS#getManager()
   * @see org.renjin.util.FileSystemUtils#homeDirectoryInCoreJar()
   */
  public static Context newTopLevelContext() {
    try {
      FileSystemManager fsm = FileSystemUtils.getMinimalFileSystemManager();
      return newTopLevelContext(fsm,
            FileSystemUtils.homeDirectoryInCoreJar(),
            FileSystemUtils.workingDirectory(fsm));
    } catch (FileSystemException e) {
      throw new RuntimeException("Could not init FileSystemManger", e);
    }
  }

  /**
   *
   * @param fileSystemManager the VFS file system manager which regulates access to the underlying
   * filesystem
   * @param homeDirectory  the R_HOME directory
   * @return a new top level context that can be used to evaluate R expressions
   */
  public static Context newTopLevelContext(FileSystemManager fileSystemManager, String homeDirectory,
                                           FileObject workingDirectory) {
    Globals globals = new Globals(fileSystemManager, homeDirectory, workingDirectory);
    Context context = new Context();
    context.globals = globals;
    context.type = Type.TOP_LEVEL;
    context.environment = globals.globalEnvironment;
    return context;
  }

  /**
   *
   * @return a new Context that can be used independently of the current context,
   * but shares everything except the
   */
  public Context fork() {
    // TODO: extract TopLevelContext class
    if(this.type != Context.Type.TOP_LEVEL) {
      throw new UnsupportedOperationException("fork() can only be called on a top level context");
    }
    Context context = new Context();
    context.globals = new Globals(this.globals);
    context.type = Context.Type.TOP_LEVEL;
    context.environment = context.globals.globalEnvironment;
    return context;

  }

  public Context beginFunction(FunctionCall call, Closure closure, PairList arguments) {
    Context context = new Context();
    context.type = Type.FUNCTION;
    context.parent = this;
    context.evaluationDepth = evaluationDepth+1;
    context.closure = closure;
    context.environment = Environment.createChildEnvironment(closure.getEnclosingEnvironment());
    context.globals = globals;
    context.arguments = arguments;
    context.call= call;
    return context;
  }
  
  public Context beginEvalContext(Environment environment) {
    Context context = new Context();
    context.type = Type.RETURN;
    context.parent = this;
    context.evaluationDepth = evaluationDepth+1;
    context.environment = environment;
    context.globals = globals;
    return context;
  }
  
  public SEXP evaluate(SEXP expression) {
    return evaluate(expression, environment);
  }
  
  public SEXP evaluate(SEXP expression, Environment rho) {
    if(expression instanceof Symbol) {
      return evaluateSymbol((Symbol)expression, rho);
    } else if(expression instanceof ExpressionVector) {
      return evaluateExpressionVector((ExpressionVector) expression, rho);
    } else if(expression instanceof FunctionCall) {
      return evaluateCall((FunctionCall) expression, rho);
    } else if(expression instanceof Promise) {
      return expression.force(this);
    } else if(expression != Null.INSTANCE && expression instanceof PromisePairList) {
      throw new EvalException("'...' used in an incorrect context");
    } else {
      clearInvisibleFlag();
      return expression;
    }
  }

  public <T> T getState(Class<T> clazz) {
    if(stateMap != null) {
      return (T)stateMap.get(clazz);
    } else {
      return null;
    }
  }


  public <T> void setState(T instance) {
    this.<T>setState((Class<T>) instance.getClass(), instance);
  }

  public <T> void setState(Class<T> clazz, T instance) {
    if(stateMap == null) {
      stateMap = Maps.newHashMap();
    }
    stateMap.put(clazz, instance);
  }

  private SEXP evaluateSymbol(Symbol symbol, Environment rho) {
    clearInvisibleFlag();

    if(symbol == Symbol.MISSING_ARG) {
      return symbol;
    }
    SEXP value = rho.findVariable(symbol);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException(String.format("object '%s' not found", symbol.getPrintName()));
    } 
    
    if(value instanceof Promise) {
      return evaluate(value, rho);
    } else {
      return value;
    }
  }
  
  private SEXP evaluateExpressionVector(ExpressionVector expressionVector, Environment rho) {
    if(expressionVector.length() == 0) {
      setInvisibleFlag();
      return Null.INSTANCE;
    } else {
      SEXP result = Null.INSTANCE;
      for(SEXP sexp : expressionVector) {
        result = evaluate(sexp, rho);
      }
      return result;
    }
  }
  

  private SEXP evaluateCall(FunctionCall call, Environment rho) {
    clearInvisibleFlag();
    Function functionExpr = evaluateFunction(call.getFunction(), rho);
    return functionExpr.apply(this, rho, call, call.getArguments());
  }

  private Function evaluateFunction(SEXP functionExp, Environment rho) {
    if(functionExp instanceof Symbol) {
      Symbol symbol = (Symbol) functionExp;
      Function fn = rho.findFunction(this, symbol);
      if(fn == null) {
        throw new EvalException("could not find function '%s'", symbol.getPrintName());      
      }
      return fn;
    } else {
      SEXP evaluated = evaluate(functionExp, rho).force(this);
      if(!(evaluated instanceof Function)) {
        throw new EvalException("'function' of lang expression is of unsupported type '%s'", evaluated.getTypeName());
      }
      return (Function)evaluated;
    }
  }

  /**
   * This is the new interpreter routine. It is not yet passing all 
   * tests.
   */
  public SEXP evaluateIR(SEXP expression, Environment rho) {
    
    IRBodyBuilder builder = new IRBodyBuilder(globals.functionTable);
    if(expression instanceof Promise) {
      return expression.force(parent);
    } else {
      IRBody body = builder.build(expression);
      
      if(PRINT_IR) {
        System.out.println(body);
      }
      return body.evaluate(this);
    }
  }

  /**
   *
   * @return the {@link FileSystemManager} associated with this Context. All R primitives that
   * interact with the file system defer to this manager.
   */
  public FileSystemManager getFileSystemManager() {
    return globals.fileSystemManager;
  }

  /**
   * Translates a uri/path into a VFS {@code FileObject}.
   *
   * @param uri uniform resource indicator. This could be, for example:
   * <ul>
   * <li>jar:file:///path/to/my/libray.jar!/mylib/R/mylib.R</li>
   * <li>/usr/lib</li>
   * <li>c:&#92;users&#92;owner&#92;data.txt</li>
   * </ul>
   *
   * @return
   * @throws FileSystemException
   */
  public FileObject resolveFile(String uri) throws FileSystemException {
    return getFileSystemManager().resolveFile(globals.workingDirectory, uri);
  }

  /**
   * @return the environment associated with this {@code Context}. This will be
   * either the global environment for top-level contexts
   */
  public Environment getEnvironment() {
    return environment;
  }

  public Environment getGlobalEnvironment() {
    return globals.globalEnvironment;
  }

  public Closure getClosure() {
    return closure;
  }

  public PairList getArguments() {
    if(type != Type.FUNCTION) {
      throw new IllegalStateException("Only Contexts of type FUNCTION contain a FunctionCall");
    }
    return arguments;
  }


  public SEXP getFunctionName() {
    return call.getFunction();
  }

  public FunctionCall getCall() {
    return call;
  }

  public int getEvaluationDepth() {
    return evaluationDepth;
  }

  public Context getParent() {
    return parent;
  }

  public Globals getGlobals() {
    return globals;
  }

  public Type getType() {
    return type;
  }

  public boolean isTopLevel() {
    return parent == null;
  }

  /**
   * Sets an expression to evaluate upon exiting this context,
   * removing any previously added exit expressions.
   * "on.exit" expressions generally do things like close file connections
   * or delete temporary files, often what might be found in a Java {@code finally} clause.
   *
   * @param exp the expression to evaluate upon exiting this context.
   */
  public void setOnExit(SEXP exp) {
    onExit = Lists.newArrayList(exp);
  }

  /**
   * Adds an expression to evaluate upon exiting this context.
   *  "on.exit" expressions generally do things like close file connections
   * or delete temporary files, often what might be found in a Java {@code finally} clause.
   *
   * @param exp the expression to evaluate upon exiting this context.
   */
  public void addOnExit(SEXP exp) {
    onExit.add(exp);
  }

  /**
   * Removes all previously added expressions to evaluate upon exiting this context.
   */
  public void clearOnExits() {
    onExit = Lists.newArrayList();
  }

  /**
   * Invokes any on.exit expressions that have been set.
   */
  public void exit() {
    for(SEXP exp : onExit) {
      evaluate(exp, environment);
    }
  }

  public SEXP findNamespace(Symbol name) {
    SEXP value =  globals.namespaceRegistry.getVariable(name);
    if(value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    } else {
      return value;
    }
  }

  /**
   * Sets a function to handle a specific class of condition.
   * @see org.renjin.primitives.Conditions
   *
   * @param conditionClass  the (S3) condition class to be handled by this handler.
   * @param function an expression that evaluates to a function that accepts a signaled
   * condition as an argument.
   */
  public void setConditionHandler(String conditionClass, SEXP function) {
    conditionHandlers.put(conditionClass, function);
  }

  public SEXP getConditionHandler(String conditionClass) {
    return conditionHandlers.get(conditionClass);
  }


  /**
   * Executes the default the standard R initialization sequence:
   * <ol>
   *  <li>Load the base package (/org/renjin/library/base/R/base)</li>
   *  <li>Execute the system profile (/org/renjin/library/base/R/Rprofile)</li>
   *  <li>Evaluate .OptRequireMethods()</li>
   *  <li>Evaluate .First.Sys()</li>
   * </ol>
   *
   */
  public void init() throws IOException {
    evalBaseResource("/org/renjin/library/base/R/base");
    evalBaseResource("/org/renjin/library/base/R/Rprofile");
    
    // FunctionCall.newCall(new Symbol(".OptRequireMethods")).evaluate(this, environment);
    evaluate( FunctionCall.newCall(Symbol.get(".First.sys")), environment);
  }

  protected void evalBaseResource(String resourceName) throws IOException {
    Context evalContext = this.beginEvalContext(globals.baseNamespaceEnv);
    InputStream in = getClass().getResourceAsStream(resourceName);
    if(in == null) {
      throw new IOException("Could not load resource '" + resourceName + "'");
    }
    Reader reader = new InputStreamReader(in);
    try {
      evalContext.evaluate(RParser.parseSource(reader));
    } finally {
      reader.close();
    }
  }
  
  public void setInvisibleFlag() {
    globals.invisible = true;
  }
  
  public void clearInvisibleFlag() {
    globals.invisible = false;
  }
}
