package r.jvmi.r2j;

import r.lang.AbstractSEXP;
import r.lang.Context;
import r.lang.Environment;
import r.lang.EvalResult;
import r.lang.Function;
import r.lang.FunctionCall;
import r.lang.PairList;
import r.lang.SexpVisitor;

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
  public EvalResult apply(Context context, Environment rho, FunctionCall call,
      PairList args) {
    return new EvalResult(functionBinding.invoke(instance, context, rho, args));
  }
}
