package org.renjin.jvminterop.converters;

import org.renjin.sexp.SEXP;

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