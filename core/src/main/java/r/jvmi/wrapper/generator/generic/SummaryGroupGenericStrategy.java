package r.jvmi.wrapper.generator.generic;

import r.jvmi.wrapper.WrapperSourceWriter;

public class SummaryGroupGenericStrategy extends GenericDispatchStrategy {

  private final String name;

  public SummaryGroupGenericStrategy(String name) {
    super();
    this.name = name;
  }

  
  @Override
  public void beforePrimitiveCalled(WrapperSourceWriter s) {

    s.writeBeginIf("argList.length() > 0 && ((AbstractSEXP)argList.getElementAsSEXP(0)).isObject()");
    

    s.writeStatement("SEXP genericResult = tryDispatchSummaryFromPrimitive(context, rho, call, " +
    		"\"" + name + "\", argList, arg0)");
    s.writeBeginBlock("if(genericResult != null) {");
    s.writeStatement("return genericResult");
    s.writeCloseBlock();
    
    s.writeCloseBlock();
  }
}
