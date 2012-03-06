package org.renjin.primitives.annotations.processor.args;

import org.renjin.primitives.annotations.processor.scalars.ScalarType;
import org.renjin.primitives.annotations.processor.scalars.ScalarTypes;

import r.jvmi.binding.JvmMethod.Argument;
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
    //return argLocal + " instanceof Vector";
    return "(" + argLocal + ".length()==0 || (" + scalarType.testExpr(argLocal) + "))";
  }

}
