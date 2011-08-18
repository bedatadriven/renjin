package r.jvmi.wrapper.generator.args;

import r.jvmi.binding.JvmMethod.Argument;

public class UnwrapExternalObject extends ArgConverterStrategy {

  @Override
  public boolean accept(Argument formal) {
    return !formal.getClazz().isPrimitive();
  }

  @Override
  public String convert(Argument formal, String argumentExpression) {
    return "WrapperRuntime.<" + formal.getClazz().getName() + ">unwrapExternal(" + argumentExpression + ")";
  }

}
