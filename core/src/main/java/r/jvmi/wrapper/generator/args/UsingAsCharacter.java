package r.jvmi.wrapper.generator.args;

import r.jvmi.annotations.InvokeAsCharacter;
import r.jvmi.binding.JvmMethod.Argument;

public class UsingAsCharacter extends ArgConverterStrategy {

  public UsingAsCharacter(Argument formal) {
    super(formal);
  }

  public static boolean accept(Argument formal) {
    return formal.isAnnotatedWith(InvokeAsCharacter.class);
  }

  @Override
  public String conversionExpression(String argumentExpression) {
    return "invokeAsCharacter(context, rho, " + argumentExpression + ")";
  }

  @Override
  public String getTestExpr(String argLocal) {
    return "true";
  }
  
  

}
