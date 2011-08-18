package r.jvmi.wrapper.generator.args;

import r.jvmi.annotations.InvokeAsCharacter;
import r.jvmi.binding.JvmMethod.Argument;

public class UsingAsCharacter extends ArgConverterStrategy {

  @Override
  public boolean accept(Argument formal) {
    return formal.isAnnotatedWith(InvokeAsCharacter.class);
  }

  @Override
  public String conversionExpression(Argument formal, String argumentExpression) {
    return "invokeAsCharacter(context, rho, " + argumentExpression + ")";
  }

}
