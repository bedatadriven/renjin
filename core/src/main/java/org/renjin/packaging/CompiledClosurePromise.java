package org.renjin.packaging;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

public class CompiledClosurePromise extends Promise {
  private static final MethodType METHOD_TYPE = MethodType.methodType(SEXP.class, Context.class, FunctionEnvironment.class);

  private final Environment enclosingEnvironment;
  private final PairList formals;
  private final PairList attributes;
  private final ListVector frameVars;
  private final String compiledClassName;
  private final String compiledMethodName;
  private final Supplier<SEXP> body;

  public CompiledClosurePromise(Environment enclosingEnvironment, PairList formals, PairList attributes, ListVector frameVars, Supplier<SEXP> body, String compiledClassName, String compiledMethodName) {
    super(Environment.EMPTY, Null.INSTANCE);
    this.enclosingEnvironment = enclosingEnvironment;
    this.formals = formals;
    this.attributes = attributes;
    this.frameVars = frameVars;
    this.body = body;
    this.compiledClassName = compiledClassName;
    this.compiledMethodName = compiledMethodName;
  }

  @Override
  protected SEXP doEval(Context context, boolean allowMissing) {

    MethodHandle methodHandle;
    try {
      methodHandle = MethodHandles.publicLookup().findStatic(Class.forName(compiledClassName), compiledMethodName, METHOD_TYPE);
    } catch (Exception e) {
      throw new EvalException("Could not load compiled closure", e);
    }

    return new Closure(enclosingEnvironment, formals, body, AttributeMap.fromPairList(attributes), methodHandle,
        frameVars.toArrayUnsafe());
  }

}
