package r.jvmi.wrapper.generator.args;

import r.jvmi.binding.JvmMethod.Argument;
import r.jvmi.wrapper.generator.scalars.ScalarType;
import r.jvmi.wrapper.generator.scalars.ScalarTypes;
import r.lang.Vector;

public class Recyclable extends ArgConverterStrategy {

  private ScalarType scalarType;
  
  public Recyclable(Argument formal) {
    super(formal);
    this.scalarType = ScalarTypes.get(formal.getClazz());
  }

  public static boolean accept(Argument formal) {
    return formal.isRecycle();
  }

  @Override
  public Class getTempLocalType() {
    return Vector.class;
  }

  @Override
  public String conversionExpression(String argumentExpression) {
    return "convertToVector(" + argumentExpression + ")";
  }

  @Override
  public String getTestExpr(String argLocal) {
    return argLocal + " instanceof Vector";
  }

}
