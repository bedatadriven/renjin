package r.jvmi.wrapper.generator;

import r.jvmi.wrapper.WrapperSourceWriter;

public class GroupGenericDispatchStrategy extends GenericDispatchStrategy {
  private final String group;
  
  public GroupGenericDispatchStrategy(String group, String name) {
    super(name);
    this.group = group;
  }

  @Override
  public void writeMaybeDispatch(WrapperSourceWriter s, int argIndex) {
    if(argIndex == 0) {
      s.writeStatement("EvalResult genericResult = tryDispatchGroupFromPrimitive(context, rho, call, \"" + group + "\", \"" + 
              name + "\", s0, args);");
      s.writeBeginBlock("if(genericResult != null) {");
      s.writeStatement("return genericResult");
      s.writeCloseBlock();
    }
  }
  
  

}
