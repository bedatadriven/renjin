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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import r.parser.RParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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

    public PairList conditionHandlerStack = Null.INSTANCE;
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
      systemEnvironment.put("R_LIBS", "classpath:/r/library");
      globalEnvironment = Environment.createGlobalEnvironment();
      baseEnvironment = globalEnvironment.getBaseEnvironment();
      namespaceRegistry = new HashFrame();
      baseNamespaceEnv = Environment.createNamespaceEnvironment(globalEnvironment, "base");
      namespaceRegistry.setVariable(new Symbol("base"), baseNamespaceEnv);
      globalEnvironment.setVariable(new Symbol(".BaseNamespaceEnv"), baseNamespaceEnv);
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

  public void setOnExit(SEXP exp) {
    onExit = Lists.newArrayList(exp);
  }

  public void addOnExit(SEXP exp) {
    onExit.add(exp);
  }

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
