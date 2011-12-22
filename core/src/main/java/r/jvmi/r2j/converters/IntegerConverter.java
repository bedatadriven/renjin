package r.jvmi.r2j.converters;

import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.LogicalVector;
import r.lang.SEXP;
import r.lang.Vector;

public class IntegerConverter extends PrimitiveScalarConverter<Number> {

  public static final IntegerConverter INSTANCE = new IntegerConverter();
  
  private IntegerConverter() {
  }

  @Override
  public SEXP convertToR(Number value) {
    if(value == null) {
      return new IntVector(IntVector.NA);
    } else {
      return new IntVector(value.intValue());
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
