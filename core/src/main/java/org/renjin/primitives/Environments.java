package org.renjin.primitives;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.*;
import org.renjin.sexp.*;

import java.util.List;

/**
 * R primitive functions which operate on {@code Environment}s
 */
public final class Environments {

  private Environments() {}

  @Builtin
  public static Environment asEnvironment(Environment arg) {
    return arg;
  }

  @Internal
  public static ListVector env2list(Environment env, boolean allNames) {
    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    for(Symbol name : env.getSymbolNames()) {
      if(allNames || !name.getPrintName().startsWith(".")) {
        list.add(name, env.getVariable(name));
      }
    }
    return list.build();
  }

  @Internal
  public static Environment list2env(ListVector list, Environment env) {
    AtomicVector names = list.getNames();
    if(names.length() != list.length()) {
      throw new EvalException("names(x) must be a character vector of the same length as x");
    }
    for (NamedValue namedValue : list.namedValues()) {
      env.setVariable(namedValue.getName(), namedValue.getValue());
    }
    
    return env;
  }
  
  @Builtin("as.environment")
  public static Environment asEnvironment(@Current Context context, int pos) {
    Environment env;
    Context cptr;

    if (IntVector.isNA(pos) || pos < -1 || pos == 0) {
        throw new EvalException("invalid 'pos' argument");
    } else if (pos == -1) {
      /* make sure the context is a funcall */
      cptr = context;
      while( context.getType() != Context.Type.FUNCTION && !cptr.isTopLevel() ) {
        cptr = cptr.getParent();
      }
      if( cptr.getType() != Context.Type.FUNCTION) {
        throw new EvalException("no enclosing environment");
      }
      env = cptr.getCallingEnvironment();
      if (env == null) {
        throw new EvalException("invalid 'pos' argument");
      }
    } else {
      for (env = context.getGlobalEnvironment(); env != Environment.EMPTY && pos > 1;
          env = env.getParent()) {
        pos--;
      }
      if (pos != 1) {
        throw new EvalException("invalid 'pos' argument");
      }
    }
    return env;
  }

  @Builtin("as.environment")
  public static Environment asEnvironment(@Current Context context, String name) {

    if(name.equals(".GlobalEnv")) {
      return context.getGlobalEnvironment();
    }

    Environment result = context.getEnvironment();
    while(result != Environment.EMPTY) {
      if(Objects.equal(result.getName(), name)) {
        return result;
      }
      if(name.equals("package:base") && result == context.getBaseEnvironment()) {
        return result;
      }
      result = result.getParent();

    }
    throw new EvalException("no environment called '%s' on the search list", name);
  }

  @Builtin("as.environment")
  public static Environment asEnvironment(ListVector list) {
    Environment env = Environment.createChildEnvironment(Environment.EMPTY);
    for(NamedValue namedValue : list.namedValues()) {
      env.setVariable(namedValue.getName(), namedValue.getValue());
    }
    return env;
  }

  @Builtin("as.environment")
  public static Environment asEnvironment(S4Object obj) {
    SEXP env = obj.getAttribute(Symbol.get(".xData"));
    if(env instanceof Environment) {
      return (Environment)env;
    }
    throw new EvalException("object does not extend 'environment'");
  }

  @Internal
  public static String environmentName(Environment env) {
    return env.getName();
  }

  @Internal("parent.env")
  public static Environment getParentEnv(Environment environment) {
    return environment.getParent();
  }

  @Internal("parent.env<-")
  public static Environment setParentEnv(Environment environment,
      Environment newParent) {
    environment.setParent(newParent);
    return environment;
  }

  @Internal
  public static StringVector ls(Environment environment, boolean allNames) {
    StringVector.Builder names = new StringVector.Builder();

    for (Symbol name : environment.getSymbolNames()) {
      if (allNames || !name.getPrintName().startsWith(".")) {
        names.add(name.getPrintName());
      }
    }
    return names.build();
  }

  @Internal
  public static void lockEnvironment(Environment env, boolean bindings) {
    env.lock(bindings);
  }

  @Internal
  public static void lockBinding(Symbol name, Environment env) {
    env.lockBinding(name);
  }

  @Internal
  public static void unlockBinding(Symbol name, Environment env) {
    env.unlockBinding(name);
  }

  @Internal
  public static boolean bindingIsLocked(Symbol name, Environment env) {
    return env.bindingIsLocked(name);
  }

  @Internal
  public static boolean environmentIsLocked(Environment env) {
    return env.isLocked();
  }

  /*----------------------------------------------------------------------

    do_libfixup

    This function copies the bindings in the loading environment to the
    library environment frame (the one that gets put in the search path)
    and removes the bindings from the loading environment.  Values that
    contain promises (created by delayedAssign, for example) are not forced.
    Values that are closures with environments equal to the loading
    environment are reparented to .GlobalEnv.  Finally, all bindings are
    removed from the loading environment.

    This routine can die if we automatically create a name space when
    loading a package.
     */
  @Internal("lib.fixup")
  public static Environment libfixup(Environment loadEnv, Environment libEnv) {
    for (Symbol name : loadEnv.getSymbolNames()) {
      SEXP value = loadEnv.getVariable(name);
      if (value instanceof Closure) {
        Closure closure = (Closure) value;
        if (closure.getEnclosingEnvironment() == loadEnv) {
          value = closure.setEnclosingEnvironment(libEnv);
        }
      }
      loadEnv.setVariable(name, value);
    }
    return libEnv;
  }

  @Internal
  public static Environment environment(@Current Context context) {
    // since this primitive is internal, we will be called by a wrapping closure,
    // so grab the parent context
    return context.getParent().getEnvironment();
  }

  @Internal
  public static SEXP environment(@Current Context context, SEXP exp) {
    if (exp == Null.INSTANCE) {
      // if the user passes null, we return the current exp
      // but since this primitive is internal, we will be called by a wrapping closure,
      // so grab the parent context
      return context.getCallingEnvironment();
    } else if (exp instanceof Closure) {
      return ((Closure) exp).getEnclosingEnvironment();
    } else {
      return exp.getAttribute(Symbols.DOT_ENVIRONMENT);
    }
  }

  @Builtin("environment<-")
  public static SEXP setEnvironment(SEXP exp, Environment newRho) {
    if (exp instanceof Closure) {
      return ((Closure) exp).setEnclosingEnvironment(newRho);
    } else {
      return exp.setAttribute(Symbols.DOT_ENVIRONMENT.getPrintName(), newRho);
    }
  }

  @Internal("new.env")
  public static Environment newEnv(boolean hash, Environment parent, int size) {
    return Environment.createChildEnvironment(parent);
  }

  @Builtin
  public static Environment baseenv(@Current Context context) {
    return context.getBaseEnvironment();
  }

  @Builtin
  public static Environment emptyenv() {
    return Environment.EMPTY;
  }

  @Builtin
  public static Environment globalenv(@Current Context context) {
    return context.getGlobalEnvironment();
  }

  @Internal
  @DataParallel
  public static boolean exists(@Current Context context, @Recycle String x,
      Environment environment, String mode, boolean inherits) {
    return environment.findVariable(context, Symbol.get(x), Vectors.modePredicate(mode),
        inherits) != Symbol.UNBOUND_VALUE;
  }

  @Internal
  public static SEXP get(@Current Context context, String x,
      Environment environment, String mode, boolean inherits) {
    SEXP value = environment.findVariable(context, Symbol.get(x), Vectors.modePredicate(mode), inherits);
    if(value == Symbol.UNBOUND_VALUE) {
      throw new EvalException("Object '%s' not found", x);
    }
    return value;
  }

  @Internal
  public static SEXP mget(@Current Context context, StringVector x,
                         Environment environment, String mode, SEXP defaultValue, boolean inherits) {

    Predicate<SEXP> predicate = Vectors.modePredicate(mode);

    ListVector.NamedBuilder result = new ListVector.NamedBuilder();
    for (String name : x) {
      SEXP value = environment.findVariable(context, Symbol.get(name), predicate, inherits);
      if(value == Symbol.UNBOUND_VALUE) {
        result.add(name, defaultValue);
      } else {
        result.add(name, value);
      }
    }
    return result.build();
  }
  
  @Internal
  public static SEXP get0(@Current Context context, String x,
                         Environment environment, String mode, boolean inherits, SEXP ifnotfound) {
    SEXP value = environment.findVariable(context, Symbol.get(x), Vectors.modePredicate(mode), inherits);
    if(value == Symbol.UNBOUND_VALUE) {
      return ifnotfound;
    } else {
      return value;
    }
  }



  @Internal
  public static StringVector search(@Current Context context) {
    List<String> names = Lists.newArrayList();
    Environment env = context.getGlobalEnvironment();
    while (env != Environment.EMPTY) {
      if(context.getNamespaceRegistry().isNamespaceEnv(env)) {
        names.add("namespace:" + env.getName());
      } else {
        names.add(env.getName());
      }
      env = env.getParent();
    }
    // special cased:
    names.set(0, ".GlobalEnv");
    names.set(names.size() - 1, "package:base");

    return new StringArrayVector(names);
  }

  @Invisible
  @Internal
  public static Environment attach(@Current Context context, SEXP what,
      int pos, String name) {

    // By default the database is attached in position 2 in the search path,
    // immediately after the user's workspace and before all previously loaded
    // packages and
    // previously attached databases. This can be altered to attach later in the
    // search
    // path with the pos option, but you cannot attach at pos=1.

    if (pos < 2) {
      throw new EvalException("Attachment position must be 2 or greater");
    }

    Environment child = context.getGlobalEnvironment();
    for (int i = 2; i != pos; ++i) {
      child = child.getParent();
    }

    Environment newEnv = Environment.createChildEnvironment(child.getParent());
    child.setParent(newEnv);

    newEnv.setAttribute(Symbols.NAME.getPrintName(), StringVector.valueOf(name));

    // copy all values from the provided environment, list  into the
    // new environment
    if (what instanceof HasNamedValues) {
      for(NamedValue namedValue : ((HasNamedValues)what).namedValues()) {
        if(!namedValue.hasName()) {
          throw new UnsupportedOperationException("all elements of a list must be named");
        }
        newEnv.setVariable(namedValue.getName(), namedValue.getValue());
      }
    } else {
      throw new EvalException("object of type '%s' cannot be attached", what.getTypeName());
    }
    return newEnv;
  }


  /**
   * Registers an R function to be called upon garbage collection of
   * object or (optionally) at the end of an R session.
   *
   * @param environment the environment
   * @param function the function to call when the environment is garbage collected
   * @param onExit true if the function should be called on the session's exit
   */
  @Internal("reg.finalizer")
  public static void registerFinalizer(@Current org.renjin.eval.Session session,
                                       Environment environment, Closure function, boolean onExit) {

    session.registerFinalizer(environment, function, onExit);
  }
}
