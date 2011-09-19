package r.jvmi.wrapper.generator.args;

import r.jvmi.binding.JvmMethod.Argument;
import r.jvmi.wrapper.generator.scalars.ScalarType;
import r.jvmi.wrapper.generator.scalars.ScalarTypes;

public class ToScalar extends ArgConverterStrategy {
  private ScalarType scalarType;
  
  public ToScalar(Argument formal) {
    super(formal);
    this.scalarType = ScalarTypes.get(formal.getClazz());
  }

  public static boolean accept(Argument formal) {
    return ScalarTypes.has(formal.getClazz());
  }

  @Override
  public String conversionExpression(String argumentExpression) {
    return scalarType.getConversionMethod() + "(" + argumentExpression + ")";
  }

  @Override
  public String getTestExpr(String argLocal) {
    return scalarType.testExpr(argLocal);
  }
}
