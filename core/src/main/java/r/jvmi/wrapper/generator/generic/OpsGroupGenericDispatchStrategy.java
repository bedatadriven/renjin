package r.jvmi.wrapper.generator.generic;

import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.WrapperSourceWriter;

public class OpsGroupGenericDispatchStrategy extends GenericDispatchStrategy {

  private final String name;

  public OpsGroupGenericDispatchStrategy(String name) {
    this.name = name;
  }

  @Override
  public void beforePrimitiveIsCalled(WrapperSourceWriter s, JvmMethod overload) {

    StringBuilder argsList = new StringBuilder("context, rho, call, \"Ops\", \"" + name + "\", s0");
    if(overload.getFormals().size() == 2) {
      argsList.append(", s1");
    }

    s.writeStatement("EvalResult genericResult = tryDispatchGroupFromPrimitive(" + argsList.toString() + ")");
    s.writeBeginBlock("if(genericResult != null) {");
    s.writeStatement("return genericResult");
    s.writeCloseBlock();
  }
}

