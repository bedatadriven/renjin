package org.renjin.jvminterop.converters;

import org.renjin.sexp.*;

public class DoubleConverter extends PrimitiveScalarConverter<Number> {

  public static final Converter INSTANCE = new DoubleConverter();
  
  private DoubleConverter() {
  }

  @Override
  public SEXP convertToR(Number value) {
    if(value == null) {  
      return new DoubleArrayVector(DoubleVector.NA);
    } else {
      return new DoubleArrayVector(value.doubleValue());
    }
  }

  public static boolean accept(Class clazz) {
    return 
        clazz == Double.TYPE || clazz == Double.class ||
        clazz == Float.TYPE || clazz == Float.class ||
        clazz == Long.TYPE || clazz == Long.class;
  }

  @Override
  protected Object getFirstElement(Vector value) {
    return value.getElementAsDouble(0);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof DoubleVector || exp instanceof IntVector ||
           exp instanceof LogicalVector;
  }

  @Override
  public int getSpecificity() {
    return Specificity.DOUBLE;
  }
}
