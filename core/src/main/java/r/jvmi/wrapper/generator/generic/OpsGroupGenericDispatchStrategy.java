package r.jvmi.wrapper.generator.generic;

import r.jvmi.wrapper.WrapperSourceWriter;

/**
 * The 'Ops' group requires special treatment because they are always unary or binary,
 * and dispatch on either the first or second argument, which are always evaluated.
 */
public class OpsGroupGenericDispatchStrategy extends GenericDispatchStrategy {

  private final String name;

  public OpsGroupGenericDispatchStrategy(String name) {
    this.name = name;
  }

  @Override
  public void beforeTypeMatching(WrapperSourceWriter s, int arity) {

    if(arity == 1) {
      s.writeBeginIf("((AbstractSEXP)s0).isObject()");
    } else {
      s.writeBeginIf("((AbstractSEXP)s0).isObject() || ((AbstractSEXP)s1).isObject()");
    }
    
    StringBuilder argsList = new StringBuilder("context, rho, call, \"Ops\", \"" + name + "\", s0");
    if(arity == 2) {
      argsList.append(", s1");
    }

    s.writeStatement("SEXP genericResult = tryDispatchGroupFromPrimitive(" + argsList.toString() + ")");
    s.writeBeginBlock("if(genericResult != null) {");
    s.writeStatement("return genericResult");
    s.writeCloseBlock();
    
    s.writeCloseBlock();
  }
}

