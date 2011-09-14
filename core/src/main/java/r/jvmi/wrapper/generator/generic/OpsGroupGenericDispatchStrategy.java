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

    s.writeBeginBlock("if(" + createFastTest(overload) + ") {");
    s.writeStatement("EvalResult genericResult = tryDispatchGroupFromPrimitive(" +
        createArgsList(overload) + ")");
    s.writeBeginBlock("if(genericResult != null) {");
    s.writeStatement("return genericResult");
    s.writeCloseBlock();
    s.writeCloseBlock();
  }

  private String createArgsList(JvmMethod overload) {
    StringBuilder argsList = new StringBuilder("context, rho, call, \"Ops\", \"" + name + "\", s0");
    if(overload.getFormals().size() == 2) {
      argsList.append(", s1");
    }
    return argsList.toString();
  }

  private String createFastTest(JvmMethod overload) {
    StringBuilder condition = new StringBuilder();
    condition.append("s0.isObject()");
    if(overload.getFormals().size() == 2) {
      condition.append(" || s1.isObject()");
    }
    return condition.toString();
  }
}

