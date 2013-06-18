package org.renjin.invoke.reflection.converters;

import org.renjin.sexp.SEXP;

/**
 * Interface to objects which convert between JVM objects and S-expressions
 *
 * @param <T> the Java-language class to be converted to/from an R SEXP
 */
public interface Converter<T>  {

  /**
   * Converts a JVM object instance to an R S-expression
   * @param value
   * @return
   */
  SEXP convertToR(T value);

  /**
   *
   * @param expression
   * @return true if this converter can handle the given R {@code expression}
   */
  boolean acceptsSEXP(SEXP expression);

  /**
   * Converts the provided R {@code expression} to a JVM class instance
   * @param expression
   * @return
   */
  Object convertToJava(SEXP expression);

  /**
   * @return  an number indicating the specificity of the
   */
  int getSpecificity();

}