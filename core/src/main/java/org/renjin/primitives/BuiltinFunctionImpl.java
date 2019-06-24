package org.renjin.primitives;

import org.renjin.eval.ArgList;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.sexp.BuiltinFunction;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.SEXP;

import java.lang.invoke.MethodHandle;

public class BuiltinFunctionImpl extends BuiltinFunction {

  private final MethodHandle methodHandle;

  public BuiltinFunctionImpl(String name, MethodHandle methodHandle) {
    super(name);
    this.methodHandle = methodHandle;
  }

  @Override
  public SEXP applyEvaluated(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP[] evaluatedArguments) {
    try {
      return (SEXP)methodHandle.invokeExact(context, rho, new ArgList(argumentNames, evaluatedArguments), call);
    } catch (EvalException e) {
      throw e;
    } catch (Throwable throwable) {
      throw new EvalException(throwable);
    }
  }
}
