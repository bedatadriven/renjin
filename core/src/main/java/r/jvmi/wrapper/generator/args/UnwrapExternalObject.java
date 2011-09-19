package r.jvmi.wrapper.generator.args;

import r.jvmi.binding.JvmMethod.Argument;
import r.lang.ExternalExp;

public class UnwrapExternalObject extends ArgConverterStrategy {

  public UnwrapExternalObject(Argument formal) {
    super(formal);
  }

  public static boolean accept(Argument formal) {
    return !formal.getClazz().isPrimitive();
  }

  @Override
  public String conversionExpression(String argumentExpression) {
    return "WrapperRuntime.<" + formal.getClazz().getName() + ">unwrapExternal(" + argumentExpression + ")";
  }

  @Override
  public String getTestExpr(String argLocal) {
    return argLocal + " instanceof " + ExternalExp.class.getSimpleName() + " && " +
        "((" + ExternalExp.class.getSimpleName() + ")" + argLocal + ").getValue() instanceof " + formal.getClazz().getName();
  }

}
