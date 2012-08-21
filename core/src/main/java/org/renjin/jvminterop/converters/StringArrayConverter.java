package org.renjin.jvminterop.converters;

import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;

public class StringArrayConverter implements Converter<String[]>{

  public static final Converter INSTANCE = new StringArrayConverter();
  
  private StringArrayConverter() { }
  
  public static boolean accept(Class clazz) {
    return clazz.isArray() && clazz.getComponentType() == String.class;
  }
  
  @Override
  public SEXP convertToR(String[] value) {
    return new StringArrayVector(value);
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return exp instanceof StringVector;
  }

  @Override
  public Object convertToJava(SEXP value) {
    AtomicVector vector = (AtomicVector)value;
    String[] array = new String[value.length()];
    for(int i=0;i!=value.length();++i) {
      array[i] = vector.getElementAsString(i);
    }
    return array;
  }

  @Override
  public int getSpecificity() {
    return Specificity.SPECIFIC_OBJECT;
  }
}
