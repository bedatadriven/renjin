package r.jvmi.wrapper.generator.args;

import r.jvmi.binding.JvmMethod.Argument;
import r.lang.Vector;

public class Recyclable extends ArgConverterStrategy {

  @Override
  public boolean accept(Argument formal) {
    return formal.isRecycle();
  }

  @Override
  public Class getTempLocalType(Argument formal) {
    return Vector.class;
  }

  @Override
  public String conversionExpression(Argument formal, String argumentExpression) {
    return "convertToVector(" + argumentExpression + ")";
  }

}
