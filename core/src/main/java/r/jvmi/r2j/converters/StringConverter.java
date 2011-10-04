package r.jvmi.r2j.converters;

import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Vector;

public class StringConverter extends BoxedScalarConverter<String> {

  public static final StringConverter INSTANCE = new StringConverter();
  
  private StringConverter() { }
 
  public static boolean accept(Class clazz) {
    return clazz == String.class;
  }
  
  @Override
  public SEXP convertToR(String value) {
    return new StringVector(value);
  }

  @Override
  protected Object getFirstElement(Vector value) {
    return value.getElementAsString(0);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof Vector;
  }

  @Override
  public int getSpecificity() {
    return Specificity.STRING;
  }
}
