package org.renjin.jvminterop.converters;

import org.renjin.sexp.SEXP;

public class RuntimeConverter implements Converter<Object> {

  public static final RuntimeConverter INSTANCE = new RuntimeConverter();
  
  private RuntimeConverter() {
 
  }
  
  @Override
  public SEXP convertToR(Object value) {
    Converter converter = Converters.get(value.getClass());
    return converter.convertToR(value);
  }

  @Override
  public Object convertToJava(SEXP value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getSpecificity() {
    return Integer.MAX_VALUE;
  }

}
