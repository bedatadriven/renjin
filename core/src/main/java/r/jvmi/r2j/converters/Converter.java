package r.jvmi.r2j.converters;

import r.lang.SEXP;

public interface Converter<T>  {
  
  SEXP convert(T value);
  
  boolean acceptsSEXP(SEXP exp);
  
  Object convertToJava(SEXP value);
  
  int getSpecificity();

}