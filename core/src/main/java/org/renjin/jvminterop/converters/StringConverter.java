package org.renjin.jvminterop.converters;

import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

public class StringConverter extends BoxedScalarConverter<String> {

  public static final StringConverter INSTANCE = new StringConverter();
  
  private StringConverter() { }
 
  public static boolean accept(Class clazz) {
    if(clazz.isArray()) return false;
    return clazz == String.class;
  }
  
  @Override
  public SEXP convertToR(String value) {
    return StringVector.valueOf(value);
  }

  @Override
  protected Object getFirstElement(Vector value) {
    return value.getElementAsString(0);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return (exp instanceof Vector)&&(((Vector)exp).length()==1);
  }

  @Override
  public int getSpecificity() {
    return Specificity.STRING;
  }
}
