package org.renjin.jvminterop.converters;

import org.renjin.sexp.*;

public class LongConverter extends PrimitiveScalarConverter<Number> {

  public static final Converter INSTANCE = new LongConverter();
  
  private LongConverter() {
  }

  @Override
  public SEXP convertToR(Number value) {
    if(value == null) {  
      return new DoubleArrayVector(DoubleVector.NA);
    } else {
      return new LongArrayVector(value.longValue());
    }
  }

  public static boolean accept(Class clazz) {
    return 
        clazz == Long.TYPE || clazz == Long.class;
  }

  @Override
  protected Object getFirstElement(Vector value) {
    if(value instanceof LongArrayVector) {
      return ((LongArrayVector) value).getElementAsLong(0);
    } else {
      return (long)value.getElementAsDouble(0);
    }
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
