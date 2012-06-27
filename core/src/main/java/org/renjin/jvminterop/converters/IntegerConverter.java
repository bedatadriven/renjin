package org.renjin.jvminterop.converters;

import org.renjin.sexp.IntArrayVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

public class IntegerConverter extends PrimitiveScalarConverter<Number> {

  public static final IntegerConverter INSTANCE = new IntegerConverter();
  
  private IntegerConverter() {
  }

  @Override
  public SEXP convertToR(Number value) {
    if(value == null) {
      return new IntArrayVector(IntVector.NA);
    } else {
      return new IntArrayVector(value.intValue());
    }
  }

  public static boolean accept(Class clazz) {
    return 
        clazz == Integer.TYPE || clazz == Integer.class ||
        clazz == Short.TYPE || clazz == Short.class;
  }

  @Override
  protected Object getFirstElement(Vector value) {
    return value.getElementAsInt(0);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof IntVector;// ||exp instanceof DoubleVector ;
//           exp instanceof LogicalVector;
  }

  @Override
  public int getSpecificity() {
    return Specificity.INTEGER;
  }
}
