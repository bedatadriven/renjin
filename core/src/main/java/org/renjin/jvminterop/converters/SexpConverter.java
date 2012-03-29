package org.renjin.jvminterop.converters;

import org.renjin.sexp.SEXP;

public class SexpConverter implements Converter<SEXP> {

  private Class clazz;
  
  public SexpConverter(Class clazz) {
    super();
    this.clazz = clazz;
  }

  @Override
  public SEXP convertToR(SEXP value) {
    return value;
  }

  @Override
  public boolean acceptsSEXP(SEXP exp) {
    return clazz.isAssignableFrom(exp.getClass());
  }

  public static boolean acceptsJava(Class clazz) {
    return SEXP.class.isAssignableFrom(clazz);
  }
  
  @Override
  public Object convertToJava(SEXP value) {
    return value;
  }

  @Override
  public int getSpecificity() {
    return Specificity.SEXP;
  }

}
