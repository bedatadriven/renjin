package org.renjin.invoke.reflection.converters;

import org.renjin.sexp.*;

/**
 * Converts between scalar {@code boolean} values and {@code logical} vectors
 */
public class BooleanConverter extends PrimitiveScalarConverter<Boolean> {

  public static final BooleanConverter INSTANCE = new BooleanConverter();

  public static boolean accept(Class clazz) {
    return clazz == Boolean.TYPE || clazz == Boolean.class;
  }
  
  @Override
  public LogicalVector convertToR(Boolean value) {
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
