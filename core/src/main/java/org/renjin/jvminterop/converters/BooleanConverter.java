package org.renjin.jvminterop.converters;

import org.renjin.sexp.*;

public class BooleanConverter extends PrimitiveScalarConverter<Boolean> {

  public static final Converter INSTANCE = new BooleanConverter();

  public static boolean accept(Class clazz) {
    return clazz == Boolean.TYPE || clazz == Boolean.class;
  }
  
  @Override
  public SEXP convertToR(Boolean value) {
    if(value == null) {
      return new LogicalArrayVector(LogicalVector.NA);
    } else {
      return new LogicalArrayVector(value);
    }
  }

  @Override
  protected Object getFirstElement(Vector vector) {
    return vector.getElementAsLogical(0) != Logical.FALSE;
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof LogicalVector;
  }

  @Override
  public int getSpecificity() {
    return Specificity.BOOLEAN;
  }
}
