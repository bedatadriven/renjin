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

package r.lang;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import r.parser.RParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Contexts are the internal mechanism used to keep track of where a
 * computation has got to (and from where),
 * so that control-flow constructs can work and reasonable information
 * can be produced on error conditions,
 * (such as via traceback) and otherwise (the sys.xxx functions).
 */
public class Context {



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
    private Map<String, SEXP> map = Maps.newHashMap();

    public Options() {
      map.put("prompt", new StringVector("> "));
      map.put("continue", new StringVector("+ "));
      map.put("expressions" , new IntVector(5000));
      map.put("width", new IntVector(80));
      map.put("digits", new IntVector(7));
      map.put("echo", new LogicalVector(false));
      map.put("verbose", new LogicalVector(false));
      map.put("check.bounds", new LogicalVector(false));
      map.put("keep.source", new LogicalVector(true));
      map.put("warnings.length", new IntVector(1000));
      map.put("OutDec", new StringVector("."));
    }

    public SEXP get(String name) {
      SEXP value = map.get(name);
      return value == null ? Null.INSTANCE : value;
    }

    public SEXP set(String name, SEXP value) {
      SEXP old = map.put(name, value);
      return old == null ? Null.INSTANCE : value;
    }

    public Set<String> names() {
      return map.keySet();
    }

  }

  /**
   * Parking lot for random global variables from the C-implementation of R
   *  that we need to do something with.
   */
  public static class Globals {

    public final Options options = new Options();

    /**
     * This is the environment
     */
    public final Map<String, String> systemEnvironment = Maps.newHashMap();

    public final Frame namespaceRegistry;

    public final Environment baseEnvironment;
    public final Environment globalEnvironment;
    public final Environment baseNamespaceEnv;

    private Globals() {
      systemEnvironment.put("R_LIBS", getLibraryPaths());
      globalEnvironment = Environment.createGlobalEnvironment();
      baseEnvironment = globalEnvironment.getBaseEnvironment();
      namespaceRegistry = new HashFrame();
      baseNamespaceEnv = Environment.createNamespaceEnvironment(globalEnvironment, "base");
      namespaceRegistry.setVariable(new Symbol("base"), baseNamespaceEnv);
      globalEnvironment.setVariable(new Symbol(".BaseNamespaceEnv"), baseNamespaceEnv);
    }

    private String getLibraryPaths() {
      return libraryPathsFromClassPath(System.getProperty("java.class.path"));
    }

    @VisibleForTesting
    static String libraryPathsFromClassPath(String classPathString) {
      String classPaths[] = classPathString.split(";");
      StringBuilder path = new StringBuilder();
      for(String classPath : classPaths) {
        String libraryPath = libraryPathFromClassPathEntry(classPath);
        if(libraryPath != null) {
          if(path.length() != 0) {
            path.append(";");
          }
          path.append(libraryPath);
        }
      }
      return path.toString();
    }

    static String libraryPathFromClassPathEntry(String classPath) {
      if(classPath.endsWith(".jar")) {
        return libraryPathFromJarFile(classPath);
      } else {
        return libraryPathFromFolder(classPath);
      }
    }

    private static String libraryPathFromFolder(String classPath) {
      File file = new File(new File(classPath, "r"), "library");
      if(file.exists() && file.isDirectory()) {
        return file.getAbsolutePath();
      } else {
        return null;
      }
    }

    @VisibleForTesting
    static String libraryPathFromJarFile(String classPath)  {
      try {
        JarFile jarFile = new JarFile(classPath);
        JarEntry entry = jarFile.getJarEntry("r/library");
        if(entry != null) {
          return "jar:file:" + classPath + "!/r/library";
        }
      } catch (IOException e) {
      }
      return null;
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

  private Context() {
  }

  public static Context newTopLevelContext() {
    Globals globals = new Globals();

    Context context = new Context();
    context.globals = globals;
    context.type = Type.TOP_LEVEL;
    context.environment = globals.globalEnvironment;
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

  public FileSystemManager getFileSystemManager() throws FileSystemException {
    return VFS.getManager();
  }

  public Environment getEnvironment() {
    return environment;
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
      exp.evaluate(this, environment);
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
   * @see r.base.Conditions
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
   *  <li>Load the base package (/r/library/base/R/base)</li>
   *  <li>Execute the system profile (/r/library/base/R/Rprofile)</li>
   *  <li>Evaluate .OptRequireMethods()</li>
   *  <li>Evaluate .First.Sys()</li>
   * </ol>
   *
   */
  public void init() throws IOException {
    loadBasePackage();
    executeStartupProfile();

    // FunctionCall.newCall(new Symbol(".OptRequireMethods")).evaluate(this, environment);
    FunctionCall.newCall(new Symbol(".First.sys")).evaluate(this, environment);

  }

  public void loadBasePackage() throws IOException {
    Reader reader = new InputStreamReader(getClass().getResourceAsStream("/r/library/base/R/base"));
    SEXP loadingScript = RParser.parseSource(reader).evaluate(this, globals.baseNamespaceEnv).getExpression();
    loadingScript.evaluate(this, globals.baseNamespaceEnv);
  }

  public void executeStartupProfile() throws IOException {
    Reader reader = new InputStreamReader(getClass().getResourceAsStream("/r/library/base/R/Rprofile"));
    SEXP profileScript = RParser.parseSource(reader).evalToExp(this, globals.baseNamespaceEnv);
    profileScript.evaluate(this, globals.baseNamespaceEnv);
  }
}
