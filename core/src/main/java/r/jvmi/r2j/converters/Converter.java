package r.jvmi.r2j.converters;

import r.lang.SEXP;

/**
 * 
 * @param <T> the Java-language class to be converted to/from an R SEXP
 */
public interface Converter<T>  {
  
  SEXP convertToR(T value);
  
  boolean acceptsSEXP(SEXP exp);
  
  Object convertToJava(SEXP value);
  
  int getSpecificity();

}