package r.jvmi.wrapper.generator.generic;

import r.jvmi.wrapper.WrapperSourceWriter;

public class OpsGroupGenericDispatchStrategy extends GenericDispatchStrategy {

  private final String name;

  public OpsGroupGenericDispatchStrategy(String name) {
    this.name = name;
  }

  @Override
  public void beforePrimitiveIsCalled(WrapperSourceWriter s, int arity) {

    if(arity == 1) {
      s.writeBeginIf("((AbstractSEXP)s0).isObject()");
    } else {
      s.writeBeginIf("((AbstractSEXP)s0).isObject() || ((AbstractSEXP)s1).isObject()");
    }
    
    StringBuilder argsList = new StringBuilder("context, rho, call, \"Ops\", \"" + name + "\", s0");
    if(arity == 2) {
      argsList.append(", s1");
    }

    s.writeStatement("EvalResult genericResult = tryDispatchGroupFromPrimitive(" + argsList.toString() + ")");
    s.writeBeginBlock("if(genericResult != null) {");
    s.writeStatement("return genericResult");
    s.writeCloseBlock();
    
    s.writeCloseBlock();
  }
}

