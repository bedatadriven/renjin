package r.jvmi.wrapper.generator;

import r.jvmi.wrapper.WrapperSourceWriter;

public class GenericDispatchStrategy {

  protected final String name;
  
  public GenericDispatchStrategy(String name) {
    this.name = name;
  }

  public void writeMaybeDispatch(WrapperSourceWriter s, int argIndex) {
    if(argIndex == 0) {
      s.writeStatement("EvalResult genericResult = tryDispatchFromPrimitive(context, rho, call, \"" + name + "\", s0, args);");
      s.writeBeginBlock("if(genericResult != null) {");
      s.writeStatement("return genericResult");
      s.writeCloseBlock();
    }
  }

}
