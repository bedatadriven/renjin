package r.compiler.ir.tac;

import java.lang.reflect.Method;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class JvmMethodCall implements Expr {

  private final Method method;
  private final List<Expr> arguments;
  
  public JvmMethodCall(Method method, Expr... arguments) {
    super();
    this.method = method;
    this.arguments = Lists.newArrayList(arguments);
  }

  @Override
  public String toString() {
    return "<" + method.toString() + ">(" +
        Joiner.on(", ").join(arguments) + ")";
  }
}
