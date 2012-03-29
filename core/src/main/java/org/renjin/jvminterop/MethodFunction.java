package org.renjin.jvminterop;

import org.renjin.eval.Context;
import org.renjin.sexp.AbstractSEXP;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.SexpVisitor;


public class MethodFunction extends AbstractSEXP implements Function {

  private final Object instance;
  private final FunctionBinding functionBinding;
  
  public MethodFunction(Object instance, FunctionBinding functionBinding) {
    super();
    this.instance = instance;
    this.functionBinding = functionBinding;
  }

  @Override
  public String getTypeName() {
    return "method";
  }

  @Override
  public void accept(SexpVisitor visitor) {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call,
      PairList args) {
    return functionBinding.invoke(instance, context, rho, args);
  }
}
