package org.renjin.jvminterop;

import org.renjin.eval.Context;
import org.renjin.sexp.*;

/**
 * An R Function object that wraps a JVM method. The object stores both the reference
 * to the method and an instance on which the function is to be applied.
 */
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
    return functionBinding.evaluateArgsAndInvoke(instance, context, rho, args);
  }

  /**
   *
   * @return  the JVM class instance to which the function is bound, or {@code null} if the
   * method is static
   */
  public Object getInstance() {
    return instance;
  }

  /**
   *
   * @return  true if this method is static and has no instance binding
   */
  public boolean isStatic() {
    return instance == null;
  }

  /*
   * @return
   */
  public FunctionBinding getFunctionBinding() {
    return functionBinding;
  }
  
  @Override
  public String toString() {
    return instance.getClass().getName() + ":" + functionBinding.toString();
  }
}
